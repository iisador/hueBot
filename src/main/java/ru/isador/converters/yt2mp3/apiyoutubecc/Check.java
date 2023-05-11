package ru.isador.converters.yt2mp3.apiyoutubecc;

/**
 * Объект, получаемый в результате вызова check.php.
 * <p>
 * Поля объекта используются в дальнейшем при скачивании аудио.
 *
 * @since 1.0.0
 */
public class Check {

    private String user;
    private String hash;

    @Override
    public String toString() {
        return "Check{" +
               "user='" + user + '\'' +
               ", hash='" + hash + '\'' +
               '}';
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }
}
