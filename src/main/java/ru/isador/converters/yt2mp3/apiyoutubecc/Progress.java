package ru.isador.converters.yt2mp3.apiyoutubecc;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Объект, получаемый в результате вызова progress.php.
 * <p>
 * Поля объекта используются в дальнейшем при скачивании аудио.
 *
 * @since 1.0.0
 */
public class Progress {

    /** Статус экспорта аудио. 0, 1, 2 - в процессе. 3 - завершено. */
    private Integer progress;

    /** Название аудио (название youtube ролика). */
    private String title;

    /** Флаг наличия ошибки. */
    private Integer error;

    /** Сообщение об ошибке. */
    private String errormsg;

    /** Список доступных битрейтов для скачивания. */
    private List<QualityInfo> mp3;

    @Override
    public String toString() {
        return "Progress{" +
                   "progress=" + progress +
                   ", title='" + title + '\'' +
                   ", error=" + error +
                   ", errormsg='" + errormsg + '\'' +
                   ", mp3=" + mp3 +
                   '}';
    }

    /**
     * Возвращает максимально доступное для скачивания качество аудио.
     * <p>
     * При вызове сортирует список mp3 и выбирает первое значение.
     *
     * @return максимальное качество.
     */
    public Integer getBestQuality() {
        Collections.sort(mp3);
        return mp3.get(0).getAq();
    }

    public Integer getError() {
        return error;
    }

    public String getErrormsg() {
        return errormsg;
    }

    public void setErrormsg(String errormsg) {
        this.errormsg = errormsg;
    }

    public List<QualityInfo> getMp3() {
        return mp3;
    }

    public void setMp3(List<QualityInfo> mp3) {
        this.mp3 = mp3;
    }

    public Integer getProgress() {
        return progress;
    }

    public void setProgress(Integer progress) {
        this.progress = progress;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isDone() {
        return progress == 3;
    }

    public boolean isError() {
        return error == 1;
    }

    public void setError(Integer error) {
        this.error = error;
    }

    public boolean isInProcess() {
        return progress == 0
                   || progress == 1
                   || progress == 2;
    }

    /**
     * Информация о доступном для скачивания качестве.
     *
     * @since 1.0.0
     */
    private static class QualityInfo implements Comparable<QualityInfo> {

        private Integer aq;

        @Override
        public int hashCode() {
            return Objects.hash(aq);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            QualityInfo that = (QualityInfo) o;
            return aq.equals(that.aq);
        }

        @Override
        public String toString() {
            return "QualityInfo{" +
                       "aq='" + aq + '\'' +
                       '}';
        }

        @Override
        public int compareTo(QualityInfo o) {
            return o.aq - aq;
        }

        public Integer getAq() {
            return aq;
        }

        public void setAq(Integer aq) {
            this.aq = aq;
        }
    }
}
