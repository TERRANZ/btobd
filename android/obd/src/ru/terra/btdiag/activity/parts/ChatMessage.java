package ru.terra.btdiag.activity.parts;

/**
 * Date: 13.12.14
 * Time: 14:38
 */
public class ChatMessage {
    private String date, message;
    private boolean isMy;

    public ChatMessage(String date, String message, boolean isMy) {
        this.date = date;
        this.message = message;
        this.isMy = isMy;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isMy() {
        return isMy;
    }

    public void setMy(boolean isMy) {
        this.isMy = isMy;
    }
}
