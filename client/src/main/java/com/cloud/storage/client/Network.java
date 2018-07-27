package com.cloud.storage.client;

import com.cloud.storage.common.AbstractMessage;
import com.cloud.storage.common.CmdMessage;
import com.cloud.storage.common.Ping;
import com.cloud.storage.common.ServerCallbackMessage;
import io.netty.handler.codec.serialization.ObjectDecoderInputStream;
import io.netty.handler.codec.serialization.ObjectEncoderOutputStream;

import java.io.IOException;
import java.io.Serializable;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Queue;
import java.util.concurrent.*;

public  class  Network {

    private static volatile Network instance;

    public enum RC {
        OK,
        ERROR
    }

    private class InputRunnable implements Runnable {

        @Override
        public void run() {
            System.out.println("New Input thread");
            try {
                while(true) {
                    System.out.println("Tick In " + rc);
                    if(Thread.interrupted()) break;
                    try {
                        AbstractMessage mes = (AbstractMessage) odis.readObject();
                        inQueue.add(mes);
                        System.out.println("New message!");
                        if (inQueue.size() > 0) {
                            fireListeners(inQueue.poll());
                        }
                    }catch (IOException | NullPointerException e) {
                        System.err.println(e.getMessage());
                        fireListenersRC(RC.ERROR);
                    } catch (ClassNotFoundException e) {
                        System.err.println("Неопознанный тип сообщения");
                        System.err.println(e.getMessage());
                        fireListenersRC(RC.ERROR);
                    }
                    Thread.sleep(1000);
                }
            } catch (InterruptedException e) {
                System.err.println(e.getMessage());
                fireListenersRC(RC.ERROR);
            } finally {
                System.out.println("Del Input thread");
            }
        }
    }

    private class OutputRunnable implements Runnable {

        @Override
        public void run() {
            System.out.println("New Output thread");
            try {
                while(true) {
                    if(Thread.interrupted()) break;
                    try {
                        System.out.println("Tick Out");
                        if (outQueue.size() > 0) {
                            oeos.writeObject(outQueue.poll());
                            oeos.flush();
                        }
                        Thread.sleep(1000);
                    } catch (IOException e) {
                        System.err.println(e.getMessage());
                        fireListenersRC(RC.ERROR);
                    }
                }
            } catch (InterruptedException | NullPointerException e) {
                e.printStackTrace();
                fireListenersRC(RC.ERROR);
            } finally {
                System.out.println("Del Output thread");
            }
        }
    }

    private volatile int counter = 0;
    private Socket sock = null;
    private Queue<Serializable> outQueue;
    private Queue<AbstractMessage> inQueue;
    private ObjectEncoderOutputStream oeos = null;
    private ObjectDecoderInputStream odis = null;
    private ExecutorService service;
    private volatile RC rc = RC.ERROR;
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

    public <T extends AbstractMessage> void addToQueue  (T msg) {
        this.outQueue.add(msg);
        System.err.println("New Output message");
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
        listeners.removeAll(listeners); //FIXME
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
        return rc == RC.OK;
    }

    public void connect() {
        try {
            String host;
            int port;
            if(PropertiesLoader.getInstance().getStatus()) {
                host = PropertiesLoader.getInstance().getProperty("host");
                port = Integer.parseInt(PropertiesLoader.getInstance().getProperty("port"));
                System.err.println("Loaded from Properties");
            } else {
                host = "localhost";
                port = 8189;
            }
            try {
                if (!sock.isClosed())
                    sock.close();
            } catch (NullPointerException e) {}
            sock = new Socket(host, port);
            //sock.setReceiveBufferSize(sock.getReceiveBufferSize() * 20);
            oeos = new ObjectEncoderOutputStream(sock.getOutputStream());
            odis = new ObjectDecoderInputStream(sock.getInputStream());
            rc = RC.OK;
            counter = 0;
            System.out.println("Connected. Receive/Send Buffer size: " + sock.getReceiveBufferSize() + ":"+sock.getSendBufferSize());
        } catch (UnknownHostException e) {
            System.out.println(e.getMessage());
            rc = RC.ERROR;
        } catch (IOException e) {
            System.err.println(e.getMessage());
            rc = RC.ERROR;
        }
    }

    public void disconnect() {
        System.err.println("Try to close connection");
        try {
            oeos.close();
            odis.close();
            sock.close();
            rc = RC.ERROR;
            System.err.println("Disconnected");
        } catch (IOException | NullPointerException e) {
            System.err.println(e.getMessage());
        }
    }

    // Метод при запуске приложения. Запускает потоки (пусть работают в холостую). Потоки стоит сделать deamon
    public void start() {
        service = Executors.newFixedThreadPool(2);
        service.submit(new OutputRunnable());
        service.submit(new InputRunnable());
    }

    // Метод при выключении приложения. Выключает потоки
    public void stop() {
        service.shutdown();
        try {
            service.awaitTermination(5, TimeUnit.SECONDS);
            if(!service.isTerminated())
                System.err.println(service.shutdownNow().size());
            System.err.println("service is " + service.isShutdown());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void fireListenersRC(RC code) {
        if(rc != code) {
            rc = code;
            switch(rc){
                case OK:
                    //fireListeners(new ServerCallbackMessage(ServerCallbackMessage.Answer.CONNECTED)); //Не нужно
                    break;
                case ERROR:
                    fireListeners(new ServerCallbackMessage(ServerCallbackMessage.Answer.DISCONNECTED));
                    break;
            }

        }

    }

}
