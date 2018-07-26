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
                    if(++counter > 10) {
                        if(request) {
                            addToQueue(new Ping());
                            request = false;
                            counter = 0;
                        } else {
                            rc = RC.ERROR;
                        }
                    }
                    System.out.println("Tick In " + rc);
                    if(Thread.interrupted()) break;
                    try {
                        if (odis.available() > 0) {
                            AbstractMessage mes = (AbstractMessage) odis.readObject();
                            if(mes instanceof Ping) {
                                request = true;
                                System.out.println("Ping");
                            } else {
                                inQueue.add((AbstractMessage) odis.readObject());
                                System.out.println("New message!");
                            }
                        }
                    } catch (IOException | NullPointerException e) {
                        System.err.println(e.getMessage());
                        rc = RC.ERROR;
                    } catch (ClassNotFoundException e) {
                        System.err.println("Неопознанный тип сообщения");
                        System.err.println(e.getMessage());
                        rc = RC.ERROR;
                    }
                    if (inQueue.size() > 0) {
                        fireListeners(inQueue.poll());
                    }
                    Thread.sleep(500);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
                rc = RC.ERROR;
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
                        Thread.sleep(500);
                    } catch (IOException e) {
                        System.err.println(e.getMessage());
                        rc = RC.ERROR;
                    }
                }
            } catch (InterruptedException | NullPointerException e) {
                e.printStackTrace();
                rc = RC.ERROR;
            } finally {
                System.out.println("Del Output thread");
            }
        }
    }

    private boolean request = true;
    private volatile int counter = 0;
    private Socket sock = null;
    private Queue<Serializable> outQueue;
    private Queue<AbstractMessage> inQueue;
    private ObjectEncoderOutputStream oeos = null;
    private ObjectDecoderInputStream odis = null;
    private ExecutorService service;
    private volatile RC rc = RC.ERROR;
//    private Runnable in1 = new Runnable() {
//        @Override
//        public void run() {
//            try {
//                while(true) {
//                    if (odis.available() > 0) {
//                        inQueue.add((AbstractMessage) odis.readObject());
//                    }
//                    if (inQueue.size() > 0) {
//                        fireListeners(inQueue.poll());
//                    }
//                    Thread.sleep(500);
//                }
//            } catch (IOException | InterruptedException e) {
//                e.printStackTrace();
//                rc = RC.ERROR;
//            } catch (ClassNotFoundException e) {
//                System.err.println("Неопознанный тип сообщения");
//                e.printStackTrace();
//                rc = RC.ERROR;
//            } finally {
//                try {
//                    odis.close();
//                } catch (IOException | NullPointerException e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//    };
//    private Runnable out1 = new Runnable() {
//        @Override
//        public void run() {
//            System.out.println("New Output thread");
//            try {
//                while(true) {
//                    if (outQueue.size() > 0) {
//                        oeos.writeObject(outQueue.poll());
//                        oeos.flush();
//                    }
//                    Thread.sleep(500);
//                }
//            } catch (InterruptedException | IOException e) {
//                e.printStackTrace();
//                rc = RC.ERROR;
//            } finally {
//                try {
//                    oeos.close();
//                } catch (IOException | NullPointerException e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//    };
//    private Runnable input = () -> {
//        System.out.println("New Input thread");
//        try {
//            while (!Thread.currentThread().isInterrupted()) {
//                //System.err.println(sock.isConnected() + " " + sock.isBound() + " " + sock.isClosed());;
//                try {
//                    if (odis.available() > 0) {
//                        inQueue.add((AbstractMessage) odis.readObject());
//                    }
//                    if (inQueue.size() > 0) {
//                        fireListeners(inQueue.poll());
//                    }
//                    Thread.sleep(500);
//                }catch (IOException e) {
//                    e.printStackTrace();
//                    try {
//                        disconnect();
//                    } catch (IOException e1) {
//                        e1.printStackTrace();
//                    }
//                }
//            }
//            throw new InterruptedException();
//        } catch (ClassNotFoundException | InterruptedException e) {
//            e.printStackTrace();
//        } finally {
//            try {
//                odis.close();
//            } catch (IOException | NullPointerException e1) {
//                e1.printStackTrace();
//            }
//            System.out.println("Input thread closed");
//        }
//    };
//    private Runnable output = () -> {
//        System.out.println("New Output thread");
//        try {
//            while (!Thread.currentThread().isInterrupted()) {
//                try {
//                    if (outQueue.size() > 0) {
//                        oeos.writeObject(outQueue.poll());
//                        oeos.flush();
//                    }
//                    Thread.sleep(500);
//                } catch (IOException e) {
//                    try {
//                        disconnect();
//                        Thread.currentThread().interrupt();
//                    } catch (IOException e1) {
//                        e1.printStackTrace();
//                    }
//                }
//            }
//            throw new InterruptedException();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        } finally {
//            try {
//                oeos.close();
//            } catch (IOException | NullPointerException e1) {
//                e1.printStackTrace();
//            }
//            System.out.println("Output thread closed");
//        }
//    };
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

//    public synchronized void connect() throws IOException {
//        String host = "localhost";
//        int port = 8189;
////        try {
////            PropertiesLoader.getInstance().load(PropertiesLoader.getInstance().PATH);
////            host = PropertiesLoader.getInstance().getProperty("host");
////            port = Integer.parseInt(PropertiesLoader.getInstance().getProperty("port"));
////            if(host == null) throw new NullPointerException();
////        } catch (NullPointerException e) {
////            host = "localhost";
////        } catch (IOException e) {
////            e.printStackTrace();
////            System.err.println("Не найден файл настроек/файл настроек неполный");
////            host = "localhost";
////            port = 8189;
////        } catch (NumberFormatException e) {
////            port = 8189;
////        }
//        sock = new Socket(host, port);
//        oeos = new ObjectEncoderOutputStream(sock.getOutputStream());
//        odis = new ObjectDecoderInputStream(sock.getInputStream());
//        System.out.println("Connection ok?");
//        rc = RC.OK;
//        if(getStatus()) {
//            service = Executors.newFixedThreadPool(2);
//            service.submit(new InputThread());
//            service.submit(new OutputThread());
//            System.out.println("Connected");
//        } else {
//            System.err.println("Неудачная попытка подключения");
//        }
//    }

//    public void disconnect() throws IOException {
//        try {
//            //fireListeners(new ServerCallbackMessage(ServerCallbackMessage.Answer.DISCONNECTED));
//            service.shutdown();
//            service.awaitTermination(10, TimeUnit.SECONDS);
//            if(!service.isShutdown())
//                service.shutdownNow();
//            sock.close();
//            System.out.println("Disconnected");
//        }catch (NullPointerException | InterruptedException e) {
//            e.printStackTrace();
//        }
//
//    }

    public <T extends AbstractMessage> void addToQueue  (T msg) {
        this.outQueue.add(msg);
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

    public void connectt() {
        try {
            String host = "localhost";
            int port = 8189;
            sock = new Socket(host, port);
            oeos = new ObjectEncoderOutputStream(sock.getOutputStream());
            odis = new ObjectDecoderInputStream(sock.getInputStream());
            System.out.println("Connection ok?");
            rc = RC.OK;
            counter = 0;
            System.out.println("Connected");
        } catch (UnknownHostException e) {
            System.out.println(e.getMessage());
            rc = RC.ERROR;
        } catch (IOException e) {
            System.err.println(e.getMessage());
            rc = RC.ERROR;
        }
    }

    public void disconnectt() {
        try {
            oeos.close();
            odis.close();
            sock.close();
            rc = RC.ERROR;
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
                service.shutdownNow();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
