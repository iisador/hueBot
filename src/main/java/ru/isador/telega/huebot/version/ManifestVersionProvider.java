package ru.isador.telega.huebot.version;

import java.io.IOException;
import java.util.jar.Manifest;

/**
 * Получение версии приложения на основе манифеста.
 *
 * @since 2.0.1
 */
public class ManifestVersionProvider extends SimpleVersionProvider {

    public ManifestVersionProvider() throws IOException {
        this(new Manifest(ManifestVersionProvider.class.getClassLoader().getResourceAsStream("META-INF/MANIFEST.MF")));
    }

    public ManifestVersionProvider(Manifest mf) {
        super(mf.getMainAttributes().getValue("Implementation-Version"), mf.getMainAttributes().getValue("Build-Time"));
    }
}
