package ru.isador.telega.huebot;

/**
 * Интерфейс для отправки исходящих сообщений.
 *
 * @since 2.0.1
 */
public interface MessageSender {

    /**
     * Отправить сообщение.
     *
     * @param text   текст сообщения.
     * @param chatId айдишник чата из телеги.
     */
    void sendText(String text, Long chatId);
}
