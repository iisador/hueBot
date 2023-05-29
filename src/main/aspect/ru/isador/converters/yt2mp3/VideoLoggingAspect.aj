package ru.isador.converters.yt2mp3;

import org.slf4j.MDC;

public aspect VideoLoggingAspect {

    pointcut videoIdResolved(String videoId, StatusUpdateListener stat): call (Extraction *.download(String, StatusUpdateListener)) && args(videoId, stat);

    before(String videoId, StatusUpdateListener stat): videoIdResolved(videoId, stat) {
        MDC.put("videoId", videoId);
    }

    after(String videoId, StatusUpdateListener stat): videoIdResolved(videoId, stat) {
        MDC.remove("videoId");
    }
}
