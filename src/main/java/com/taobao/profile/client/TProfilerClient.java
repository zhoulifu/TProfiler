/**
 * (C) 2011-2012 Alibaba Group Holding Limited.
 * <p>
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * version 2 as published by the Free Software Foundation.
 */
package com.taobao.profile.client;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

/**
 * Client for remote control of the TProfiler
 */
public class TProfilerClient {
    public static void main(String[] args) {
        if (args.length != 3) {
            System.err.println(
                    "Usage: <server ip> <server port> " +
                    "<command[start/stop/status/flush_method]>");
            return;
        }

        String host = args[0];
        int port = Integer.valueOf(args[1]);

        try (Socket socket = new Socket(host, port);
             BufferedOutputStream out = new BufferedOutputStream(
                     socket.getOutputStream());
             BufferedReader reader = new BufferedReader(
                     new InputStreamReader(socket.getInputStream()))
        ) {
            out.write(args[2].getBytes());
            out.write('\r');
            out.flush();
            String rtn;
            if ((rtn = reader.readLine()) != null) {
                System.out.println(rtn);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
