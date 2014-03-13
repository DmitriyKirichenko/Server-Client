package com.kirichenko.Server;

import com.kirichenko.Server.entity.Config;
import com.kirichenko.Server.entity.Message;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class Client {
    public static void main(String[] args) throws IOException {
        final Socket socket = new Socket(Config.HOST, Config.PORT);
        final ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
        final ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
        final BlockingQueue<Message> readQueue = new LinkedBlockingQueue<Message>();
        final String PING_SERVER_SIGNAL = "PING/SERVER";

        final AtomicBoolean killState = new AtomicBoolean(false);
        final ThreadGroup threadGroup = new ThreadGroup("ClientThreadGroup");
        threadGroup.setDaemon(true);
        final ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
            @Override
            public Thread newThread(Runnable runnable) {
                return new Thread(threadGroup, runnable);
            }
        });
        Runnable pingRunnable = new Runnable() {
            @Override
            public void run() {
                try {
                    out.writeObject(new Message("PING/CLIENT"));
                    out.flush();
                    scheduledExecutorService.schedule(this, 3, TimeUnit.SECONDS);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        scheduledExecutorService.schedule(pingRunnable, 3, TimeUnit.SECONDS);
        try {
        while (true) {
            try {
                readQueue.put((Message)in.readObject());
                Message msg = readQueue.poll(4 * 3, TimeUnit.SECONDS);
                if (msg == null) {
                  killState.set(true);
                  socket.close();
                  break;
                } else if (!PING_SERVER_SIGNAL.equals(msg.getMessage())) {
                  System.out.println(msg);
                }
            } catch (ClassNotFoundException | InterruptedException e) {
                e.printStackTrace();
            }
        }
        } finally {
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null) socket.close();
        }
    }
}
