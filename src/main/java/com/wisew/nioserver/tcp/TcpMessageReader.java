package com.wisew.nioserver.tcp;

import com.wisew.nioserver.IMessageReader;
import com.wisew.nioserver.Message;
import com.wisew.nioserver.MessageBuffer;
import com.wisew.nioserver.Socket;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class TcpMessageReader implements IMessageReader {

    private MessageBuffer messageBuffer = null;

    private List<Message> completeMessages = new ArrayList<Message>();
    private Message nextMessage = null;

    @Override
    public void init(MessageBuffer readMessageBuffer) {
        this.messageBuffer = readMessageBuffer;
        this.nextMessage = messageBuffer.getMessage();
    }

    @Override
    public void read(Socket socket, ByteBuffer byteBuffer) throws IOException {
        int totalReads = socket.read(byteBuffer);
        System.out.printf("总共读了%d个字节\n",totalReads);
        byteBuffer.flip();

        if (byteBuffer.remaining() == 0) {
            byteBuffer.clear();
            return;
        }
        // ByteBuffer写入到MessageBuffer缓存
        this.nextMessage.writeToMessage(byteBuffer);
        System.out.println(nextMessage);
        int endIndex = endIndexOfMessage(this.nextMessage.sharedArray, this.nextMessage.offset, this.nextMessage.length);
        // 当前读的部分是完整的一条消息
        if (endIndex != -1) {
            this.completeMessages.add(nextMessage);
            this.nextMessage = messageBuffer.getMessage();
        }
        byteBuffer.clear();
    }


    @Override
    public List<Message> getMessages() {
        return this.completeMessages;
    }

    // 检测结束符
    private int endIndexOfMessage(byte[] src, int startIndex, int length) {
        if (src[startIndex + length - 1] != '$') {
            return -1;
        }
        return startIndex + length - 1;
    }
}
