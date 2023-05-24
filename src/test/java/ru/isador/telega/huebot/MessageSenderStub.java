package ru.isador.telega.huebot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MessageSenderStub implements MessageSender {

    private final Map<Long, List<String>> messages;

    public MessageSenderStub() {
        messages = new HashMap<>();
    }

    @Override
    public void sendText(String text, Long chatId) {
        messages.computeIfAbsent(chatId, aLong -> new ArrayList<>()).add(text);
    }

    public Map<Long, List<String>> getMessages() {
        return messages;
    }

    public String getMessage(Long chatId) {
        return messages.get(chatId).get(0);
    }
}
