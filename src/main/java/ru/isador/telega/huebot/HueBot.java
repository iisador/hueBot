package ru.isador.telega.huebot;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendAudio;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import ru.isador.converters.yt2mp3.Extraction;
import ru.isador.converters.yt2mp3.ExtractionStatus;
import ru.isador.converters.yt2mp3.StatusUpdate;
import ru.isador.converters.yt2mp3.VideoConversionException;
import ru.isador.converters.yt2mp3.YoutubeVideoConverter;
import ru.isador.converters.yt2mp3.apiyoutubecc.ApiYoutubeMp3Extractor;

public class HueBot extends TelegramLongPollingBot {

    private static final Logger logger = LoggerFactory.getLogger(HueBot.class);

    private final YoutubeVideoConverter mp3Extractor;
    private final ExecutorService executor;

    public HueBot() {
        super(System.getenv("BOT_TOKEN"));
        mp3Extractor = new ApiYoutubeMp3Extractor();
        executor = Executors.newFixedThreadPool(5);
    }

    @Override
    public void onUpdateReceived(Update update) {
        executor.execute(new MessageProcessor(update));
    }

    @Override
    public void onClosing() {
        logger.debug("Shutting down");
        executor.shutdownNow();
        try {
            logger.debug("Waiting executor");
            if (!executor.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS)) {
                logger.error("Да нифига такого не случится");
            }
        } catch (InterruptedException e) {
            logger.error("", e);
        }
        logger.debug("Bye!");
    }

    @Override
    public String getBotUsername() {
        return System.getenv("BOT_USERNAME");
    }

    public static void main(String[] args) throws TelegramApiException {
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
        telegramBotsApi.registerBot(new HueBot());
        logger.debug("Bot started");
    }

    private class MessageProcessor implements Runnable {

        private final Update update;

        public MessageProcessor(Update update) {
            this.update = update;
        }

        @Override
        public void run() {
            logger.debug("{}: {}", update.getMessage().getFrom().getUserName(), update.getMessage().getText());
            if ("/start".equalsIgnoreCase(update.getMessage().getText())) {
                sendText(update, """
                    Дороу. Я простой бот который преобразует outube видео в mp3.
                    Просто скинь мне ссылку на видео, и в ответ получишь mp3 максимум 320 кбит/c.
                    """);
            } else {
                try (Extraction ex = mp3Extractor.download(update.getMessage().getText(), new TelegaStatusUpdate())) {
                    try {
                        execute(SendAudio.builder().audio(new InputFile(ex.stream(), ex.fileName())).chatId(update.getMessage().getChat().getId()).build());
                    } catch (TelegramApiException e) {
                        logger.error("", e);
                    }
                } catch (VideoConversionException | IOException e) {
                    sendText(update, e.getMessage());
                }
            }
        }

        private void sendText(Update update, String text) {
            try {
                execute(SendMessage.builder().text(text).chatId(update.getMessage().getChat().getId()).build());
            } catch (TelegramApiException ex) {
                throw new RuntimeException(ex);
            }
        }

        private class TelegaStatusUpdate implements StatusUpdate {

            @Override
            public void onStatusUpdated(ExtractionStatus status) {
                switch (status) {
                    case DRAFT -> sendText(update, "Запрос принят");
                    case PROCESS -> sendText(update, "Обработка запущена");
                    case PROCESSING -> sendText(update, "Всё ещё обрабатывается");
                    case DONE -> sendText(update, "Обработано успешно, отправка...");
                    case FAILED -> sendText(update, "Ошибка обработки...");
                }
            }
        }
    }
}
