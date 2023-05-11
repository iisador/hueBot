package ru.isador.converters.yt2mp3;

/**
 * Подслушивалка процесса обработки.
 *
 * @since 1.0.0
 */
@FunctionalInterface
public interface StatusUpdate {

    /**
     * Вызывается при изменении статуса обработки.
     *
     * @param status новый статус обработки.
     *
     * @since 1.0.0
     */
    void onStatusUpdated(ExtractionStatus status);
}
