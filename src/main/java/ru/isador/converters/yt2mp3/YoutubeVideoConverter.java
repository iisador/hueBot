package ru.isador.converters.yt2mp3;

/**
 * Основной интерфейс для преобразования видео с youtube в mp3.
 *
 * @since 1.0.0
 */
public interface YoutubeVideoConverter {

    /**
     * Основной метод обработки видео с ютуба.
     *
     * @param link ссылка на видео.
     *
     * @return информация о преобразованном файле.
     *
     * @since 1.0.0
     */
    Extraction download(String link, StatusUpdate statusUpdate) throws VideoConversionException;
}
