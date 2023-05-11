package ru.isador.converters.yt2mp3.apiyoutubecc;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.glassfish.jersey.internal.inject.ExtractorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import ru.isador.converters.yt2mp3.Extraction;
import ru.isador.converters.yt2mp3.ExtractionStatus;
import ru.isador.converters.yt2mp3.StatusUpdate;
import ru.isador.converters.yt2mp3.VideoConversionException;
import ru.isador.converters.yt2mp3.YoutubeVideoConverter;

/**
 * Реализация, основанная на апи <a href="https://apiyoutube.cc">apiyoutube.cc</a>.
 * <p>
 * Скачивает сконвертированное аудио во временный файл. Процесс конвертирования состоит из тех шагов:
 * <p>
 * 1. Запускаем процесс конвертации вызвав check.php. Вызов вернет нам хэш и юзера.
 * <p>
 * 2. Проверяем статус процесса вызовом progress.php с полученным хэшом.
 * <p>
 * 3. Если процесс завершился успешно - скачиваем mp3.
 *
 * @since 1.0.0
 */
public class ApiYoutubeMp3Extractor implements YoutubeVideoConverter {

    private static final Logger logger = LoggerFactory.getLogger(ApiYoutubeMp3Extractor.class);

    private static final String BASE_URL = "https://apiyoutube.cc";
    private static final String CHECK_TEMPLATE = BASE_URL + "/check.php?v=%s";
    private static final String PROGRESS_TEMPLATE = BASE_URL + "/progress.php?id=%s";
    private static final String DOWNLOAD_TEMPLATE = BASE_URL + "/%s/%s";

    private static final Pattern HTTP_PARAM_TEMPL = Pattern.compile("(\\w+)=([\\w-]+)");

    private final HttpClient client;

    public ApiYoutubeMp3Extractor() {
        client = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NORMAL).build();
    }

    @Override
    public Extraction download(String link, StatusUpdate statusUpdate) throws VideoConversionException {
        logger.trace("Download started: {}", link);
        Optional<StatusUpdate> su = Optional.ofNullable(statusUpdate);

        String videoId = getVideoId(link);
        MDC.put("videoId", videoId);

        logger.trace("Video id resolved");
        su.ifPresent(l -> l.onStatusUpdated(ExtractionStatus.DRAFT));

        logger.debug("Checking video");
        Check check = checkVideo(videoId);

        logger.debug("Check complete: {}", check);
        su.ifPresent(l -> l.onStatusUpdated(ExtractionStatus.PROCESS));

        logger.debug("Checking progress");
        Progress progress = getProgress(check.getHash());
        logger.debug("Progress: {}", progress);

        while (progress.isInProcess() && !progress.isError()) {
            su.ifPresent(l -> l.onStatusUpdated(ExtractionStatus.PROCESSING));
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                throw new VideoConversionException("Extraction interrupted");
            }
            progress = getProgress(check.getHash());
            logger.debug("Progress: {}", progress);
        }

        if (progress.isDone()) {
            su.ifPresent(l -> l.onStatusUpdated(ExtractionStatus.DONE));
            Integer quality = progress.getBestQuality();
            logger.debug("Best quality resolved: {}", quality);
            return new Extraction(progress.getTitle(), downloadMp3(getDownloadUri(quality, check.getHash(), check.getUser())));
        }

        su.ifPresent(l -> l.onStatusUpdated(ExtractionStatus.FAILED));
        throw new ExtractorException(progress.getErrormsg());
    }

    private String getVideoId(String link) throws VideoConversionException {
        URI uri;
        try {
            uri = new URI(link);
        } catch (URISyntaxException e) {
            throw new VideoConversionException(e);
        }

        if (uri.getAuthority().equalsIgnoreCase("youtu.be")) {
            return uri.getRawPath().substring(1);
        }

        if (uri.getAuthority().equalsIgnoreCase("www.youtube.com")) {
            Matcher m = HTTP_PARAM_TEMPL.matcher(uri.getQuery());
            Map<String, String> parameters = new HashMap<>();
            while (m.find()) {
                parameters.put(m.group(1), m.group(2));
            }

            if (!parameters.containsKey("v")) {
                throw new VideoConversionException("No video ID in link");
            }

            return parameters.get("v");
        }

        throw new VideoConversionException("Not a youtube link");
    }

    private Check checkVideo(String videoId) throws VideoConversionException {
        return send(getCheckUri(videoId), s -> {
            try {
                return new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false).readValue(s, Check.class);
            } catch (JsonProcessingException e) {
                throw new VideoConversionException(e);
            }
        });
    }

    private <T> T send(URI uri, JsonHandler<T> handler) throws VideoConversionException {
        try {
            return handler.parse(client.send(HttpRequest.newBuilder(uri).build(), HttpResponse.BodyHandlers.ofString()).body());
        } catch (IOException e) {
            throw new VideoConversionException(e);
        } catch (InterruptedException e) {
            throw new VideoConversionException("Extraction interrupted");
        }
    }

    private URI getCheckUri(String videoId) {
        return URI.create(String.format(CHECK_TEMPLATE, videoId));
    }

    private Progress getProgress(String hash) throws VideoConversionException {
        return send(getProgressUri(hash), s -> {
            try {
                return new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false).readValue(s, Progress.class);
            } catch (JsonProcessingException e) {
                throw new VideoConversionException(e);
            }
        });
    }

    private URI getProgressUri(String hash) {
        return URI.create(String.format(PROGRESS_TEMPLATE, hash));
    }

    private InputStream downloadMp3(URI link) throws VideoConversionException {
        logger.debug("Requesting: {}", link);
        try {
            return client.send(HttpRequest.newBuilder(link).GET().build(), HttpResponse.BodyHandlers.ofInputStream()).body();
        } catch (IOException e) {
            throw new VideoConversionException(e);
        } catch (InterruptedException e) {
            throw new VideoConversionException("Extraction interrupted");
        }
    }

    private URI getDownloadUri(Integer quality, String hash, String user) {
        String id = hash + "::" + user;
        return URI.create(String.format(DOWNLOAD_TEMPLATE, quality, id));
    }

    private interface JsonHandler<T> {

        T parse(String s) throws VideoConversionException;
    }
}
