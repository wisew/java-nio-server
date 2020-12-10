package com.wisew.nioserver;

/**
 * 动态扩展，每个MessageReader独有
 */
public class MessageBuffer {

    public static int KB = 1024;
    public static int MB = 1024 * KB;

    private static final int CAPACITY_SMALL = 4 * KB;
    private static final int CAPACITY_MEDIUM = 128 * KB;
    private static final int CAPACITY_LARGE = 1024 * KB;

    /**
     * 这些buffer是所有的message共享的，每一个message都会记录着在其中的偏移量
     * 实际读取只需要单独读取一个块即可
     * 因为如果一条消息 > 当前块，则会自动将当前消息写入到更大的块中
     */
    private byte[] smallMessageBuffer = new byte[1024 * 4 * KB];   //1024 x   4KB messages =  4MB.
    private byte[] mediumMessageBuffer = new byte[128 * 128 * KB];   // 128 x 128KB messages = 16MB.
    private byte[] largeMessageBuffer = new byte[16 * 1 * MB];   //  16 *   1MB messages = 16MB.

    QueueIntFlip smallMessageBufferFreeBlocks = new QueueIntFlip(smallMessageBuffer.length / CAPACITY_SMALL);
    QueueIntFlip mediumMessageBufferFreeBlocks = new QueueIntFlip(mediumMessageBuffer.length / CAPACITY_MEDIUM);
    QueueIntFlip largeMessageBufferFreeBlocks = new QueueIntFlip(largeMessageBuffer.length / CAPACITY_LARGE);

    public MessageBuffer() {
        // 下标的位置值代表当前块的起始偏移量,例如[0,4KB,8KB...,1023*4KB]
        for (int i = 0; i < smallMessageBuffer.length; i += CAPACITY_SMALL) {
            this.smallMessageBufferFreeBlocks.put(i);
        }
        // [0,128KB,128*2KB...,127*128KB]
        for (int i = 0; i < mediumMessageBuffer.length; i += CAPACITY_MEDIUM) {
            this.mediumMessageBufferFreeBlocks.put(i);
        }
        // [0,1024KB,2*1024KB...,15*1024KB]
        for (int i = 0; i < largeMessageBuffer.length; i += CAPACITY_LARGE) {
            this.largeMessageBufferFreeBlocks.put(i);
        }
    }

    // 刚开始的buffer缓冲区选择smallMessageBuffer，即单个block=4KB
    public Message getMessage() {
        int nextFreeSmallBlock = this.smallMessageBufferFreeBlocks.take();
        // 代表当前buffer没有可读数据
        if (nextFreeSmallBlock == -1) return null;

        Message message = new Message(this);

        message.sharedArray = this.smallMessageBuffer;
        message.capacity = CAPACITY_SMALL;
        message.offset = nextFreeSmallBlock;
        message.length = 0;

        return message;
    }

    public boolean expandMessage(Message message) {
        if (message.capacity == CAPACITY_SMALL) {
            return moveMessage(message, this.smallMessageBufferFreeBlocks, this.mediumMessageBufferFreeBlocks, this.mediumMessageBuffer, CAPACITY_MEDIUM);
        } else if (message.capacity == CAPACITY_MEDIUM) {
            return moveMessage(message, this.mediumMessageBufferFreeBlocks, this.largeMessageBufferFreeBlocks, this.largeMessageBuffer, CAPACITY_LARGE);
        } else {
            return false;
        }
    }

    private boolean moveMessage(Message message, QueueIntFlip srcBlockQueue, QueueIntFlip destBlockQueue, byte[] dest, int newCapacity) {
        int nextFreeBlock = destBlockQueue.take();
        if (nextFreeBlock == -1) return false;

        // 将原来写入到小的block buffer的数据复制到更大的block buffer
        System.arraycopy(message.sharedArray, message.offset, dest, nextFreeBlock, message.length);

        // 这一步什么意思
        srcBlockQueue.put(message.offset);

        message.sharedArray = dest;
        message.offset = nextFreeBlock;
        message.capacity = newCapacity;
        return true;
    }


}
