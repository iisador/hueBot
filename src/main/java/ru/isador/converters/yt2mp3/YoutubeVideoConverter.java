package ru.isador.converters.yt2mp3;

/**
 * Основной интерфейс для преобразования видео с youtube в mp3.
 *
 * @since 1.0.0
 */
public interface YoutubeVideoConverter {

    /**
     * То же что и {@code download(String id, StatusUpdateListener listener)}, только без прослушки.
     *
     * @since 2.0.1
     */
    default Extraction download(String id) throws VideoConversionException {
        return download(id, null);
    }

    /**
     * Основной метод обработки видео с ютуба.
     *
     * @param id код видео с youtube.
     *
     * @return информация о преобразованном файле.
     *
     * @since 1.0.0
     */
    Extraction download(String id, StatusUpdateListener listener) throws VideoConversionException;
}
