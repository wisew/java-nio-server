package com.wisew.nioserver;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class MessageWriter {

    private List<Message> writeQueue = new ArrayList<>();
    private Message messageInProgress = null;
    private int bytesWritten = 0;

    public MessageWriter() {
    }

    public void enqueue(Message message) {
        if (this.messageInProgress == null) {
            this.messageInProgress = message;
        } else {
            this.writeQueue.add(message);
        }
    }

    // 写入的长度 > 该条message的长度如何处理
    public void write(Socket socket, ByteBuffer byteBuffer) throws IOException {
        byteBuffer.put(this.messageInProgress.sharedArray, this.messageInProgress.offset + this.bytesWritten, this.messageInProgress.length - this.bytesWritten);
        byteBuffer.flip();

        this.bytesWritten += socket.write(byteBuffer);
        byteBuffer.clear();

        if (bytesWritten >= this.messageInProgress.length) {
            if (this.writeQueue.size() > 0) {
                this.messageInProgress = this.writeQueue.remove(0);
            } else {
                this.messageInProgress = null;
                //todo unregister from selector
            }
        }
        this.bytesWritten = 0;
    }

    public boolean isEmpty() {
        return this.writeQueue.isEmpty() && this.messageInProgress == null;
    }

}
