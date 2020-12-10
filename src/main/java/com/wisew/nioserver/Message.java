package com.wisew.nioserver;

import java.nio.ByteBuffer;

public class Message {

    private MessageBuffer messageBuffer = null;

    public long socketId = 0; // the id of source socket or destination socket, depending on whether is going in or out.

    public byte[] sharedArray = null;
    public int offset = 0; //offset into sharedArray where this message data starts.
    public int capacity = 0; //the size of the section in the sharedArray allocated to this message.
    public int length = 0; //the number of bytes used of the allocated section.

    public Object metaData = null;

    public Message(MessageBuffer messageBuffer) {
        this.messageBuffer = messageBuffer;
    }

    // 把ByteBuffer中的数据写到MessageBuffer
    public int writeToMessage(ByteBuffer byteBuffer) {
        int remaining = byteBuffer.remaining();

        while (this.length + remaining > capacity) {
            // 如果扩展了，之前在小block中的数据已经复制到了扩容后的block中
            if (!this.messageBuffer.expandMessage(this)) {
                return -1;
            }
        }

        int bytesToCopy = Math.min(remaining, this.capacity - this.length);
        byteBuffer.get(this.sharedArray, this.offset + this.length, bytesToCopy);
        this.length += bytesToCopy;

        return bytesToCopy;
    }


    public int writeToMessage(byte[] byteArray) {
        return writeToMessage(byteArray, 0, byteArray.length);
    }


    public int writeToMessage(byte[] byteArray, int offset, int length) {
        int remaining = length;

        while (this.length + remaining > capacity) {
            if (!this.messageBuffer.expandMessage(this)) {
                return -1;
            }
        }

        int bytesToCopy = Math.min(remaining, this.capacity - this.length);
        System.arraycopy(byteArray, offset, this.sharedArray, this.offset + this.length, bytesToCopy);
        this.length += bytesToCopy;
        return bytesToCopy;
    }


    public void writePartialMessageToMessage(Message message, int endIndex) {
        int startIndexOfPartialMessage = message.offset + endIndex;
        int lengthOfPartialMessage = (message.offset + message.length) - endIndex;

        System.arraycopy(message.sharedArray, startIndexOfPartialMessage, this.sharedArray, this.offset, lengthOfPartialMessage);
    }

    public int writeToByteBuffer(ByteBuffer byteBuffer) {
        return 0;
    }

    @Override
    public String toString() {
        byte[] content = new byte[length];
        System.arraycopy(sharedArray,offset,content,0,length);

        String describe = String.format(
                "==============================\ntotal=%dKB\nblock=%dKB\noffset=%dKB\nlength=%d\ncontent=%s\nsocketId=%d\n==============================",
                sharedArray.length / 1024,
                capacity / 1024,
                offset / 1024,
                length,
                new String(content),
                this.socketId
        );
        return describe;
    }
}
