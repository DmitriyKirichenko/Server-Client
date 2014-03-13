package com.kirichenko.Server.entity;

import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class ChatHistory implements Serializable {
    private List<Message> history = new LinkedList<Message>();

    public void addMessage(Message message) {
        this.history.add(message);
        while (history.size() > 10) {
            history.remove(0);
        }
    }

    public void addMessageList(List<Message> list) {
        for (Message message : list) {
            this.history.add(message);
            while (history.size() > 10) {
                history.remove(0);
            }
        }
    }

    public List<Message> getHistory() {
        return Collections.unmodifiableList(this.history);
    }

}

