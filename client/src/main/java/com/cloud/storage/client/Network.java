package com.cloud.storage.client;

import com.cloud.storage.common.AbstractMessage;
import io.netty.handler.codec.serialization.ObjectDecoderInputStream;
import io.netty.handler.codec.serialization.ObjectEncoderOutputStream;

import java.io.IOException;
import java.io.Serializable;
import java.net.Socket;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public  class  Network {
    private Socket sock;
    private Queue<Serializable> outQueue;
    private Queue<Serializable> inQueue;
    private ObjectEncoderOutputStream oeos;
    private ObjectDecoderInputStream odis;
    private Thread input;
    private Thread output;

    public Network() {
        outQueue = new ConcurrentLinkedQueue<>();
        inQueue = new ConcurrentLinkedQueue<>();
    }

    public void connect(String host, int port) throws IOException {
        sock = new Socket(host, port);
        oeos = new ObjectEncoderOutputStream(sock.getOutputStream());
        odis = new ObjectDecoderInputStream(sock.getInputStream());
        output = new Thread(() -> {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            //ObjectEncoderOutputStream oeos = null;
            try {
                //oeos = new ObjectEncoderOutputStream(sock.getOutputStream());
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
            //ObjectDecoderInputStream odis = null;
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            try {
                //odis = new ObjectDecoderInputStream(sock.getInputStream());
                while(!Thread.currentThread().isInterrupted()) {
                    if(odis.available() > 0) {
                        inQueue.add((AbstractMessage) odis.readObject());
                    }
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
    }

    public <T extends AbstractMessage> void addToQueue  (T msg) {
        this.outQueue.add(msg);
    }

    public AbstractMessage getAnswer () {
        return (AbstractMessage) inQueue.poll();
    }
}
