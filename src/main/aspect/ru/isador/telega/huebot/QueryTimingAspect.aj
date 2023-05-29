package ru.isador.telega.huebot;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public aspect QueryTimingAspect {

    private static final Logger logger = LoggerFactory.getLogger(MessageProcessor.class);
    private long dtmStart;

    pointcut newRequest(MessageProcessor mp):
        execution(void MessageProcessor.run()) && target(mp);

    before(MessageProcessor mp): newRequest(mp) {
        dtmStart = System.currentTimeMillis();
    }

    after(MessageProcessor mp): newRequest(mp) {
        long time = System.currentTimeMillis() - dtmStart;
        logger.debug("[{}] executed in [{} s]", mp.getQueryId(), TimeUnit.MILLISECONDS.toSeconds(time));
    }
}
