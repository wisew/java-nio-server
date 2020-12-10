package com.wisew.nioserver.example;

import com.wisew.nioserver.IMessageProcessor;
import com.wisew.nioserver.Message;
import com.wisew.nioserver.Server;
import com.wisew.nioserver.tcp.TcpMessageReaderFactory;

import java.io.IOException;

public class TcpMain {
    public static void main(String[] args) throws IOException {
        // 读出来的消息写入到写入队列
        IMessageProcessor messageProcessor = (request, writeProxy) -> {
            System.out.println("Message Received from socket: " + request.socketId);
            Message response = writeProxy.getMessage();
            response.socketId = request.socketId;
            response.writeToMessage(request.toString().getBytes());
            writeProxy.enqueue(response);
        };

        Server server = new Server(9999, new TcpMessageReaderFactory(), messageProcessor);

        server.start();
    }
}
