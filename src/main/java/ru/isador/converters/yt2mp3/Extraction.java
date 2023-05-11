package ru.isador.converters.yt2mp3;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

/**
 * Информация о скачанном аудио.
 *
 * @param fileName имя файла.
 * @param stream   mp3 поток.
 *
 * @since 1.0.0
 */
public record Extraction(String fileName, InputStream stream) implements Closeable {

    @Override
    public void close() throws IOException {
        if (stream != null) {
            stream.close();
        }
    }
}
