package ru.isador.converters.yt2mp3;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class YoutubeLinkVideoConverterTest {

    private YoutubeLinkVideoConverter converter;

    @BeforeEach
    void setUp() {
        converter = new YoutubeVideoConverterStub();
    }

    @Test
    void testLinkIsNull() {
        NullPointerException e = assertThrows(NullPointerException.class, () -> {
            try (Extraction extraction = converter.downloadFromLink(null)) {
                // ignore
            }
        });

        assertNotNull(e, "Exception should be present");
    }

    @Test
    void testLinkInvalid() {
        VideoConversionException e = assertThrows(VideoConversionException.class, () -> {
            try (Extraction extraction = converter.downloadFromLink("123")) {
                // ignore
            }
        });

        assertNotNull(e, "Exception should be present");
        assertEquals("Not a youtube link", e.getMessage());
    }

    @Test
    void testLinkValid() throws VideoConversionException {
        Extraction e = converter.downloadFromLink("https://www.youtube.com/watch?v=oHg5SJYRHA0");

        assertNull(e, "Лень проверять");
    }
}
