package ru.isador.telega.huebot;

import ru.isador.converters.yt2mp3.Extraction;

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

    /**
     * Отправка аудио-файла.
     *
     * @param extraction преобразованный аудио-файл.
     * @param chatId     айдишник чата из телеги.
     *
     * @since 2.0.2
     */
    void sendAudio(Extraction extraction, Long chatId);
}
