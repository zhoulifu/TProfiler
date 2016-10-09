/**
 * (C) 2011-2012 Alibaba Group Holding Limited.
 * <p>
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * version 2 as published by the Free Software Foundation.
 */
package com.taobao.profile.thread;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import com.taobao.profile.Manager;
import com.taobao.profile.runtime.MethodCache;

/**
 * A thread for handling remote control commands
 */
public class InnerSocketThread implements Runnable {
    private enum Command {
        NONE,
        START, STOP, STATUS, FLUSH_METHOD
    }

    private static final int DEFAULT_READ_TIMEOUT = 5000;
    private boolean debug;

    public InnerSocketThread() {
        this(false);
    }

    public InnerSocketThread(boolean debug) {
        this.debug = debug;
    }

    public void run() {
        int port = debug ? 8080 : Manager.PORT;
        try (ServerSocket socket = new ServerSocket(port)) {
            while (true) {
                try (Socket client = socket.accept();
                     BufferedReader reader = new BufferedReader(
                             new InputStreamReader(client.getInputStream()))
                ) {
                    client.setSoTimeout(DEFAULT_READ_TIMEOUT);
                    String command = reader.readLine();
                    Command cmd;
                    try {
                        cmd = Command.valueOf(command.toUpperCase());
                    } catch (IllegalArgumentException e) {
                        cmd = Command.NONE;
                    }

                    switch (cmd) {
                        case START:
                            Manager.instance().setSwitchFlag(true);
                            break;
                        case STOP:
                            Manager.instance().setSwitchFlag(false);
                            break;
                        case STATUS:
                            flushRunningStatus(client.getOutputStream());
                            break;
                        case FLUSH_METHOD:
                            MethodCache.flushMethodData();
                            break;
                        default:
                            break;//ignore
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Write current TProfiler status to the {@code OutputStream}
     * @param os the specified outputStream
     * @throws IOException
     */
    private void flushRunningStatus(OutputStream os) throws IOException {
        BufferedOutputStream out = new BufferedOutputStream(os);
        out.write(
                (Manager.instance().getSwitchFlag() ? "running" : "stopped").getBytes());
        out.write('\r');
        out.flush();
    }

    /**
     * Main for debugging
     */
    public static void main(String[] args) {
        Thread thread = new Thread(new InnerSocketThread(true), "TProfiler-InnerSocket-Debug");
        thread.start();
    }
}
