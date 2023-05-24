package ru.isador.converters.yt2mp3;

public class YoutubeVideoConverterStub extends YoutubeLinkVideoConverter {

    private final Extraction result;

    public YoutubeVideoConverterStub() {
        result = null;
    }

    @Override
    public Extraction download(String link, StatusUpdateListener listener) {
        return result;
    }
}
