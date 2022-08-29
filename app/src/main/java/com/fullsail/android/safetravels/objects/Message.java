package com.fullsail.android.safetravels.objects;

public class Message {
    String toUID;
    String fromUID;
    String message;
    String timeStamp;
    boolean readStatus;

    public Message(String toUID, String fromUID, String message, String timeStamp, boolean readStatus) {
        this.toUID = toUID;
        this.fromUID = fromUID;
        this.message = message;
        this.timeStamp = timeStamp;
        this.readStatus = readStatus;
    }

    public String getToUID() {
        return toUID;
    }

    public String getFromUID() {
        return fromUID;
    }

    public String getMessage() {
        return message;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public boolean isReadStatus() {
        return readStatus;
    }
}
