package com.cloud.storage.client;

import com.cloud.storage.common.AbstractMessage;
import io.netty.handler.codec.serialization.ObjectDecoderInputStream;
import io.netty.handler.codec.serialization.ObjectEncoderOutputStream;

import java.io.IOException;
import java.io.Serializable;
import java.net.Socket;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

public  class  Network {
    private Socket sock;
    Queue<Serializable> outQueue;
    Queue<Serializable> inQueue;
    Thread input;
    Thread output;

    public Network() {
        outQueue = new LinkedBlockingQueue<>();
        inQueue = new LinkedBlockingQueue<>();
    }

    public void connect(String host, int port) throws IOException {
        sock = new Socket(host, port);
        output = new Thread(() -> {
            ObjectEncoderOutputStream oeos = null;
            try {
                oeos = new ObjectEncoderOutputStream(sock.getOutputStream());
                while(true) {
                    if(!Thread.currentThread().isInterrupted()) {
                        if(!outQueue.isEmpty()) {
                            oeos.writeObject(outQueue.poll());
                            oeos.flush();
                        }
                    } else {
                        throw new InterruptedException();
                    }
                }
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
            ObjectDecoderInputStream odis = null;
            try {
                odis = new ObjectDecoderInputStream(sock.getInputStream());
                while(true) {
                    if(!Thread.currentThread().isInterrupted()) {
                        inQueue.add((AbstractMessage)odis.readObject());
                    } else {
                        throw new InterruptedException();
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
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
        output.setDaemon(false);
        input.setDaemon(false);
        output.run();
        input.run();
    }

    public void disconnect() throws IOException {
        input.interrupt();
        output.interrupt();
        try {
            input.join(10000);
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
