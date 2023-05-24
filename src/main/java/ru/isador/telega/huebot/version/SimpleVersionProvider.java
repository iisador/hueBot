package ru.isador.telega.huebot.version;

/**
 * Простая реализация провайдера версии приложения.
 *
 * @since 2.0.1
 */
public class SimpleVersionProvider implements VersionProvider {

    private final String version;
    private final String buildTimestamp;

    /**
     * Простой конструктор принимающий на вход версию и время сборки приложения.
     */
    public SimpleVersionProvider(String version, String buildTimestamp) {
        this.version = version;
        this.buildTimestamp = buildTimestamp;
    }

    @Override
    public String getBuildTimestamp() {
        return buildTimestamp;
    }

    @Override
    public String getVersion() {
        return version;
    }
}
