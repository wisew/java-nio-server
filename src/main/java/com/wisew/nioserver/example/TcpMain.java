package com.wisew.nioserver.example;

import com.wisew.nioserver.IMessageProcessor;
import com.wisew.nioserver.Server;
import com.wisew.nioserver.tcp.TcpMessageReaderFactory;

import java.io.IOException;

public class TcpMain {
    public static void main(String[] args) throws IOException {
        IMessageProcessor messageProcessor = (request, writeProxy) -> {
            System.out.println("Message Received from socket: " + request.socketId);
        };

        Server server = new Server(9999, new TcpMessageReaderFactory(), messageProcessor);

        server.start();
    }
}
