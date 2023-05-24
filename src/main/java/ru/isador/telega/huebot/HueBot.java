package ru.isador.telega.huebot;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
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
import ru.isador.converters.yt2mp3.YoutubeLinkVideoConverter;
import ru.isador.converters.yt2mp3.apiyoutubecc.ApiYoutubeMp3Extractor;
import ru.isador.telega.huebot.version.ManifestVersionProvider;
import ru.isador.telega.huebot.version.VersionProvider;

public class HueBot extends TelegramLongPollingBot implements MessageSender {

    private static final Logger logger = LoggerFactory.getLogger(HueBot.class);

    private final ExecutorService executorService;
    private YoutubeLinkVideoConverter mp3Extractor;
    private VersionProvider versionProvider;

    public HueBot(ExecutorService executorService) {
        super(System.getenv("BOT_TOKEN"));
        this.executorService = executorService;
    }

    @Override
    public void onUpdateReceived(Update update) {
        MessageProcessor ms = new MessageProcessor(update, this);
        ms.setVersionProvider(versionProvider);
        ms.setMp3Extractor(mp3Extractor);

        try (Extraction extraction = executorService.submit(ms).get()) {
            if (extraction != null) {
                execute(SendAudio.builder().audio(new InputFile(extraction.stream(), extraction.fileName())).chatId(update.getMessage().getChat().getId()).build());
            }
        } catch (TelegramApiException | InterruptedException | ExecutionException | IOException e) {
            logger.error("", e);
            sendText(e.getMessage(), update.getMessage().getChat().getId());
        }
    }

    @Override
    public void sendText(String text, Long chatId) {
        try {
            execute(SendMessage.builder().text(text).chatId(chatId).build());
        } catch (TelegramApiException e) {
            logger.error("", e);
        }
    }

    @Override
    public void onClosing() {
        logger.debug("Shutting down");
        executorService.shutdownNow();
        try {
            logger.debug("Waiting executor");
            if (!executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS)) {
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

    public static void main(String[] args) throws TelegramApiException, IOException {
        HueBot hueBot = new HueBot(Executors.newFixedThreadPool(5));
                hueBot.setVersionProvider(new ManifestVersionProvider());
        hueBot.setMp3Extractor(new ApiYoutubeMp3Extractor());

        TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
        telegramBotsApi.registerBot(hueBot);
        logger.debug("Bot started");
    }

    public void setMp3Extractor(YoutubeLinkVideoConverter mp3Extractor) {
        this.mp3Extractor = mp3Extractor;
    }

    public void setVersionProvider(VersionProvider versionProvider) {
        this.versionProvider = versionProvider;
    }
}
