package ru.isador.telega.huebot;

import java.time.LocalDateTime;

/**
 * Информация о версии бота.
 *
 * @param version        версия
 * @param buildTimestamp время сборки.
 *
 * @since 2.0.0
 */
public record Version(String version, LocalDateTime buildTimestamp) {

}
