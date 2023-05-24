package ru.isador.telega.huebot;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import ru.isador.converters.yt2mp3.Extraction;
import ru.isador.telega.huebot.version.SimpleVersionProvider;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MessageProcessorTest {

    private MessageSenderStub messageSender;
    private Long chatId;

    @BeforeEach
    void setUp() {
        messageSender = new MessageSenderStub();
        chatId = 1L;
    }

    @Test
    void testStartCommand() {
        MessageProcessor messageProcessor = new MessageProcessor(getCommand("/start"), messageSender);
        messageProcessor.run();

        Extraction result = messageSender.getExtraction();
        assertNull(result, "Conversion result should be empty");

        String expected = """
            Дороу. Я простой бот который преобразует youtube видео в mp3.
            Просто скинь мне ссылку на видео, и в ответ получишь mp3 максимум 320 кбит/c.
            """;
        String actual = messageSender.getMessage(chatId);

        assertEquals(expected, actual, "Result message doesn't match");
    }

    @Test
    void testVersionNoProvider() {
        MessageProcessor messageProcessor = new MessageProcessor(getCommand("/version"), messageSender);
        messageProcessor.run();

        Extraction result = messageSender.getExtraction();
        assertNull(result, "Conversion result should be empty");

        String expected = "UNKNOWN";
        String actual = messageSender.getMessage(chatId);

        assertEquals(expected, actual, "Result message doesn't match");
    }

    @Test
    void testVersion() {
        MessageProcessor messageProcessor = new MessageProcessor(getCommand("/version"), messageSender);
        messageProcessor.setVersionProvider(new SimpleVersionProvider("1.0.0", "01.01.1970 00:00:00"));
        messageProcessor.run();

        Extraction result = messageSender.getExtraction();
        assertNull(result, "Conversion result should be empty");

        String expected = String.format(String.format(MessageProcessor.VERSION_TEMPLATE, "1.0.0", "01.01.1970 00:00:00"));
        String actual = messageSender.getMessage(chatId);

        assertEquals(expected, actual, "Result message doesn't match");
    }

    @Test
    void testConversionNoConverter() {
        MessageProcessor messageProcessor = new MessageProcessor(getCommand("https://www.youtube.com/watch?v=oHg5SJYRHA0"), messageSender);
        messageProcessor.run();

        Extraction result = messageSender.getExtraction();
        assertNull(result, "Conversion result should be empty");
        assertTrue(messageSender.getMessages().isEmpty(), "Messages should be empty");
    }

    @Test
    void testConversion() {
        Extraction expected = new Extraction("test.mp3", null);
        MessageProcessor messageProcessor = new MessageProcessor(getCommand("https://www.youtube.com/watch?v=oHg5SJYRHA0"), messageSender);
        messageProcessor.setMp3Extractor(new YoutubeVideoConverterStub(expected));
        messageProcessor.run();

        Extraction result = messageSender.getExtraction();
        assertNotNull(result, "Conversion result should be present");
        assertEquals(expected.fileName(), result.fileName(), "Result filename doesn't match");
    }

    private Update getCommand(String command) {
        User user = new User();
        user.setUserName("test");

        Chat chat = new Chat();
        chat.setId(chatId);

        Message message = new Message();
        message.setFrom(user);
        message.setText(command);
        message.setChat(chat);

        Update update = new Update();
        update.setMessage(message);
        return update;
    }
}
