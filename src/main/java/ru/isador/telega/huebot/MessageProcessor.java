package ru.isador.telega.huebot;

import java.io.IOException;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.isador.converters.yt2mp3.Extraction;
import ru.isador.converters.yt2mp3.ExtractionStatus;
import ru.isador.converters.yt2mp3.StatusUpdateListener;
import ru.isador.converters.yt2mp3.VideoConversionException;
import ru.isador.converters.yt2mp3.YoutubeLinkVideoConverter;
import ru.isador.telega.huebot.version.VersionProvider;

/**
 * Обработчик входящих сообщений.
 *
 * @since 2.0.1
 */
public class MessageProcessor implements Runnable {

    static final String VERSION_TEMPLATE = "Версия %s, от %s";

    private static final Logger logger = LoggerFactory.getLogger(MessageProcessor.class);

    private final Update update;
    private final MessageSender messageSender;
    private final UUID queryId;

    private YoutubeLinkVideoConverter mp3Extractor;
    private VersionProvider versionProvider;

    /**
     * Создание обработчика.
     *
     * @param messageSender интерфайс для исходящих сообщений.
     * @param update        входящее сообщение.
     *
     * @since 2.0.1
     */
    public MessageProcessor(Update update, MessageSender messageSender) {
        this.update = update;
        this.messageSender = messageSender;
        queryId = UUID.randomUUID();
    }

    @Override
    public void run() {
        MDC.put("queryId", queryId.toString());
        logger.info("{}: {}", update.getMessage().getFrom().getUserName(), update.getMessage().getText());

        Long chatId = update.getMessage().getChat().getId();

        switch (update.getMessage().getText()) {
            case "/start" -> messageSender.sendText("""
                Дороу. Я простой бот который преобразует youtube видео в mp3.
                Просто скинь мне ссылку на видео, и в ответ получишь mp3 максимум 320 кбит/c.
                """, chatId);
            case "/version" -> {
                if (versionProvider == null) {
                    logger.warn("No VersionProvider implementation found");
                    messageSender.sendText("UNKNOWN", chatId);
                } else {
                    messageSender.sendText(String.format(VERSION_TEMPLATE, versionProvider.getVersion(), versionProvider.getBuildTimestamp()), chatId);
                }
            }
            default -> {
                if (mp3Extractor == null) {
                    logger.warn("No YoutubeLinkVideoConverter implementation found");
                } else {
                    try (Extraction extraction = mp3Extractor.downloadFromLink(update.getMessage().getText(), new TelegaStatusUpdateListener(messageSender, chatId))) {
                        messageSender.sendAudio(extraction, chatId);
                    } catch (VideoConversionException | IOException e) {
                        logger.error("", e);
                        String errorMsg = String.format("ID запроса: %s; %s", queryId, e.getMessage());
                        messageSender.sendText(errorMsg, chatId);
                    }
                }
            }
        }
        MDC.remove("queryId");
    }

    /**
     * Используется в аспекте {@link QueryTimingAspect}.
     */
    UUID getQueryId() {
        return queryId;
    }

    public void setMp3Extractor(YoutubeLinkVideoConverter mp3Extractor) {
        this.mp3Extractor = mp3Extractor;
    }

    public void setVersionProvider(VersionProvider versionProvider) {
        this.versionProvider = versionProvider;
    }

    private record TelegaStatusUpdateListener(MessageSender messageSender, Long chatId) implements StatusUpdateListener {

        @Override
        public void onStatusUpdated(ExtractionStatus status) {
            logger.trace("Status updated: {}", status);
            switch (status) {
                case DRAFT -> messageSender.sendText("Запрос принят", chatId);
                case PROCESS -> messageSender.sendText("Обработка запущена", chatId);
                case PROCESSING -> messageSender.sendText("Всё ещё обрабатывается", chatId);
                case DONE -> messageSender.sendText("Обработано успешно, отправка...", chatId);
                case FAILED -> messageSender.sendText("Ошибка обработки...", chatId);
            }
        }
    }
}
