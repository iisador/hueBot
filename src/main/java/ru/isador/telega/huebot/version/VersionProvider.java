package ru.isador.telega.huebot.version;

/**
 * Интерфейс для вывода вeрсии приложения.
 *
 * @since 2.0.1
 */
public interface VersionProvider {

    /** Дата/Время сборки. */
    String getBuildTimestamp();

    /** Версия сборки. */
    String getVersion();
}
