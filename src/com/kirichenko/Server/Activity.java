package com.kirichenko.Server;

import com.kirichenko.Server.entity.Message;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class Activity {
    private static final AtomicInteger index = new AtomicInteger(0);
    private final String name = "activity-" + index.getAndIncrement();
    private final Socket socket;
    private final ThreadGroup threadGroup;
    private final BlockingQueue<Message> writeQueue = new LinkedBlockingQueue<Message>();
    private final BlockingQueue<Message> readQueue = new LinkedBlockingQueue<Message>();
    private final BlockingQueue<String> deathQueue;
    private final AtomicBoolean killState = new AtomicBoolean(false);

    public Activity(final Socket socket, BlockingQueue<String> deathQueue) {
        this.socket = socket;
        this.deathQueue = deathQueue;
        this.threadGroup = new ThreadGroup("ThreadGroup/" + name);
        addChatHistory(Server.getChatHistory().getHistory());

        startSocketWriter(socket);
        startSocketReader(socket);
        startWriteTimer();
        startReaderTimer();
    }

    private void startReaderTimer() {
        new Thread(threadGroup, new Runnable() {
            @Override
            public void run() {

                try {
                    while (true) {
                        Message msg = readQueue.poll(4 * 3, TimeUnit.SECONDS);
                        if (msg == null) {
                            kill();
                            break;
                        }
                    }
                } catch (InterruptedException e) {
                    kill();
                }
            }
        }).start();
    }

    private void startWriteTimer() {
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
                    if (killState.get()) {
                    } else {
                        writeQueue.put(new Message("PING/SERVER"));
                        scheduledExecutorService.schedule(this, 3, TimeUnit.SECONDS);
                    }
                } catch (InterruptedException e) {
                    kill();
                }
            }
        };
        scheduledExecutorService.schedule(pingRunnable, 4500, TimeUnit.MILLISECONDS);
    }

    private void startSocketReader(final Socket socket) {
        new Thread(threadGroup, new Runnable() {
            @Override
            public void run() {
                try {
                    ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
                    while (true) {
                        Message msg = (Message) in.readObject();
                        readQueue.put(msg);
                    }
                } catch (IOException | InterruptedException | ClassNotFoundException e) {
                    kill();
                }
            }
        }).start();
    }

    private void startSocketWriter(final Socket socket) {
        new Thread(threadGroup, new Runnable() {
            @Override
            public void run() {
                try {
                    ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                    while (true) {
                        Message msg = writeQueue.take();
                        out.writeObject(msg);
                        out.flush();
                    }
                } catch (IOException | InterruptedException e) {
                    kill();
                }
            }
        }).start();
    }

    public void sendMessage(Message msg) throws InterruptedException {
        writeQueue.put(msg);
    }

    public void kill() {
        if (killState.compareAndSet(false, true)) {
            try {
                socket.close();
            } catch (IOException e) {/*NOP*/}

            threadGroup.interrupt();

            try {
                deathQueue.put(name);
            } catch (InterruptedException e) {/*NOP*/}
        }
    }

    public String getName() {
        return name;
    }

    public void addChatHistory (List<Message> list) {
        for (Message message: list) {
            try {
                sendMessage(message);
            } catch (InterruptedException e) {
                kill();
            }
        }
    }
}
