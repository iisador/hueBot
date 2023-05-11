package ru.isador.converters.yt2mp3;

/**
 * Возможные статусы процесса обработки. Статусная модель проста:
 * DRAFT -> PROCESS -> PROCESSING -> DONE | FAILED
 *
 * @since 1.0.0
 */
public enum ExtractionStatus {
    DRAFT,
    PROCESS,
    PROCESSING,
    DONE,
    FAILED
}
