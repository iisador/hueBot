package ru.isador.converters.yt2mp3;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class YoutubeLinkVideoConverter implements YoutubeVideoConverter {

    private static final Pattern HTTP_PARAM_TEMPL = Pattern.compile("(\\w+)=([\\w-]+)");

    /**
     * Преобразование youtube видео в mp3.
     *
     * @param link         ссылка на видео.
     * @param statusUpdate прослушивалка процесса обработки.
     *
     * @return результат преобразования.
     *
     * @since 2.0.0
     */
    public Extraction downloadFromLink(String link, StatusUpdate statusUpdate) throws VideoConversionException {
        String videoId = getVideoId(link);
        return download(videoId, statusUpdate);
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
}
