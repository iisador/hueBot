package ru.isador.telega.huebot;

import ru.isador.converters.yt2mp3.Extraction;
import ru.isador.converters.yt2mp3.StatusUpdateListener;
import ru.isador.converters.yt2mp3.YoutubeLinkVideoConverter;

public class YoutubeVideoConverterStub extends YoutubeLinkVideoConverter {

    private final Extraction result;

    public YoutubeVideoConverterStub(Extraction result) {
        this.result = result;
    }

    @Override
    public Extraction download(String id, StatusUpdateListener listener) {
        return result;
    }
}
