package com.kirichenko.Server.entity;

import java.io.Serializable;
import java.util.Date;
import java.text.SimpleDateFormat;

public class Message implements Serializable {
    private  final String message;
    private  final Date date;

    public Message(String message) {
        this.message = message;
        this.date = new Date(System.currentTimeMillis());
    }

    public Message(String message, Date date) {
        this.message = message;
        this.date = date;
    }

    public String getMessage() {
        return message;
    }

    public Date getDate() {
        return date;
    }

    @Override
    public String toString() {
        return new SimpleDateFormat().format(date) + " " + message;
    }
}
