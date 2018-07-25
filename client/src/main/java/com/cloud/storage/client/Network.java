package com.cloud.storage.client;

import com.cloud.storage.common.AbstractMessage;
import io.netty.handler.codec.serialization.ObjectDecoderInputStream;
import io.netty.handler.codec.serialization.ObjectEncoderOutputStream;

import java.io.IOException;
import java.io.Serializable;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Queue;
import java.util.concurrent.*;

public  class  Network {

    public static volatile Network instance;

    private Socket sock;
    private Queue<Serializable> outQueue;//Целесообразность? Пока оставлю
    private Queue<AbstractMessage> inQueue;
    private ObjectEncoderOutputStream oeos;
    private ObjectDecoderInputStream odis;
    private Thread input;
    private Thread output;
    private volatile ArrayList<InputListener> listeners;
    private Semaphore smp = new Semaphore(1);

    private Network() {

        outQueue = new ConcurrentLinkedQueue<>();
        inQueue = new ConcurrentLinkedQueue<>();
        listeners = new ArrayList<>();
    }

    public static Network getInstance() {
        if (instance == null) {
            instance = new Network();
        }
        return instance;
    }

    public void connect(String host, int port) throws IOException {
        sock = new Socket(host, port);
        oeos = new ObjectEncoderOutputStream(sock.getOutputStream());
        odis = new ObjectDecoderInputStream(sock.getInputStream());
        output = new Thread(() -> {
            try {
                while(!Thread.currentThread().isInterrupted()) {
                    if (outQueue.size() > 0) {
                        oeos.writeObject(outQueue.poll());
                        oeos.flush();
                    }
                }
                throw new InterruptedException();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                try {
                    oeos.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
            }
        });
        input = new Thread(() -> {
            try {
                while(!Thread.currentThread().isInterrupted()) {
                    //System.out.println("Available " + odis.available());
                    if(odis.available() > 0) {
                        inQueue.add((AbstractMessage) odis.readObject());
                    }
                    if(inQueue.size() > 0) {  // Оставить здесь? Продумать механизм защиты в случае прерывания, чтобы не было потери сообщений
                        fireListeners(inQueue.poll());
                    }
                    Thread.sleep(500);
                }
                throw new InterruptedException();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                try {
                    odis.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
            }
        });
        output.setDaemon(true);
        input.setDaemon(true);
        output.start();
        input.start();
        System.out.println("Connected");
    }

    public void disconnect() throws IOException {
        input.interrupt();
        output.interrupt();
        try {
            input.join(10000);
            output.join(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        sock.close();
        System.out.println("Disconnected");
    }

    public <T extends AbstractMessage> void addToQueue  (T msg) {
        this.outQueue.add(msg);
    }

    public AbstractMessage getAnswer () { // TODO: Стоит убрать
        return (AbstractMessage) inQueue.poll();
    }

    public void addListener(InputListener listener) {
        listeners.add(listener);
        System.out.println("New Listener");
    }

    public void removeListener(InputListener listener) {
        listeners.remove(listener);
        listeners.trimToSize();
        System.out.println("Listener removed");
    }

    public void removeAll() {
        listeners.removeAll(listeners);
        System.out.println(listeners.size());
    }

    private <T extends AbstractMessage> void fireListeners(T msg) {
        try {
            smp.acquire();
            for(InputListener listener : listeners) {
                listener.onMsgReceived(msg);
            }
            smp.release();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public boolean getStatus() {
        return sock != null;
    }
}
