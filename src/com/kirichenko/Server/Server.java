package com.kirichenko.Server;

import com.kirichenko.Server.entity.ChatHistory;
import com.kirichenko.Server.entity.Config;
import com.kirichenko.Server.entity.Message;
import com.kirichenko.Server.tmp.DbFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.sql.SQLException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.LinkedBlockingQueue;


public class Server {
    public static final String COMMAND_EXIT = "exit";

    public static ServerSocket serverSocket;

    private static final ConcurrentMap<String, Activity> activityMap = new ConcurrentHashMap<>();
    private static final ChatHistory chatHistory = new ChatHistory();
    private static final BlockingQueue<String> deathQueue = new LinkedBlockingQueue<>();


    public static void main(String[] args) throws IOException, InterruptedException {

        System.out.println("Server started. For exit write " + '\"' + COMMAND_EXIT + '\"');
        startServerSocketListener();
        startActivityDeathListener();

        try {
            chatHistory.addMessageList(DbFactory.getMessages());
            BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
            while (true) {
                String line = in.readLine();
                if (COMMAND_EXIT.equalsIgnoreCase(line)) {
                    for (Activity activity : activityMap.values()) {
                        activity.kill();
                    }
                    break;
                } else if (line != null && !line.equals("")) {
                    Message message = new Message(line);
                    DbFactory.saveMessage(message);
                    chatHistory.addMessage(message);
                    for (Activity activity : activityMap.values()) {
                        activity.sendMessage(new Message(line));
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DbFactory.close();
            if (serverSocket != null) serverSocket.close();
        }
    }

    private static void startActivityDeathListener() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        String deadActivityName = deathQueue.take();
                        activityMap.remove(deadActivityName);
                    } catch (InterruptedException e) { /*NOP*/ }
                }
            }
        }, "MAIN/DeathListener").start();
    }

    private static void startServerSocketListener() throws IOException {
        serverSocket = new ServerSocket(Config.PORT);
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Activity activity = new Activity(serverSocket.accept(), deathQueue);
                        activityMap.put(activity.getName(), activity);
                    } catch (IOException e) {
                        throw new RuntimeException();
                    }
                }
            }
        }, "MAIN/ServerSocketListener").start();
    }

    public static ChatHistory getChatHistory() {
        return chatHistory;
    }
}

