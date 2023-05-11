package ru.isador.converters.yt2mp3;

/**
 * Исключительная ситуация возникшая в процессе обработки. Обычно в него оборачиваются внутренние ошибки.
 * <p>
 * Не собирает трейс и не является подавляемым.
 *
 * @since 1.0.0
 */
public class VideoConversionException extends Exception {

    public VideoConversionException(String message) {
        super(message, null, false, false);
    }

    public VideoConversionException(Throwable cause) {
        super(cause.getMessage(), cause, false, false);
    }
}
