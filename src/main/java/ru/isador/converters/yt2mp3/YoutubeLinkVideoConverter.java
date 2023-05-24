package ru.isador.converters.yt2mp3;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Конвертер видео из HTTP\HTTPS ссылки.
 *
 * @since 2.0.1
 */
public abstract class YoutubeLinkVideoConverter implements YoutubeVideoConverter {

    // Спижжено отсюда: https://stackoverflow.com/a/6904504
    private static final Pattern YT_PATTERN = Pattern.compile("(?:youtube\\.com/(?:[^/]+/.+/|(?:v|e(?:mbed)|shorts?)/|.*[?&]v=)|youtu\\.be/)([^\"&?/\\s]{11})");

    /**
     * Преобразование youtube видео в mp3.
     *
     * @param link                 ссылка на видео.
     * @param statusUpdateListener прослушивалка процесса обработки.
     *
     * @return результат преобразования.
     *
     * @since 2.0.0
     */
    public Extraction downloadFromLink(String link, StatusUpdateListener statusUpdateListener) throws VideoConversionException {
        String videoId = getVideoId(link);
        return download(videoId, statusUpdateListener);
    }

    /**
     * Метод извлечения кода видео из ссылки.
     *
     * @param link ссылка на видео.
     *
     * @throws VideoConversionException если не удалось определить код видео.
     * @throws NullPointerException     если {@code link} == null.
     */
    private String getVideoId(String link) throws VideoConversionException {
        Matcher m = YT_PATTERN.matcher(link);
        if (m.find()) {
            return m.group(1);
        }
        throw new VideoConversionException("Not a youtube link");
    }

    /**
     * То же, только без прослушки.
     *
     * @param link ссылка на видео.
     *
     * @return результат преобразования.
     *
     * @since 2.0.1
     */
    public Extraction downloadFromLink(String link) throws VideoConversionException {
        String videoId = getVideoId(link);
        return download(videoId);
    }
}
