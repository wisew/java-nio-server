package com.wisew.nioserver;

/**
 * 类似循环队列功能，尾部写满后继续从头部写入，flipped进行翻转判断
 * flipped == false代表write指针在read指针之后
 * 每个方法需要注意及时修改flipped
 */
public class QueueIntFlip {

    public int[] elements = null;

    public int capacity = 0;
    public int writePos = 0;
    public int readPos  = 0;
    public boolean flipped = false;

    public QueueIntFlip(int capacity) {
        this.capacity = capacity;
        //todo 考虑添加TypeAllocator
        this.elements = new int[capacity];
    }

    public void reset() {
        this.writePos = 0;
        this.readPos  = 0;
        this.flipped  = false;
    }

    // 记录还有多少个元素可读
    public int available() {
        if(!flipped){
            return writePos - readPos;
        }
        return capacity - readPos + writePos;
    }

    // 剩余的写入空间大小
    public int remainingCapacity() {
        if(!flipped){
            return capacity - writePos;
        }
        return readPos - writePos;
    }

    // 能否写入一条数据
    public boolean put(int element){
        if(!flipped){
            if(writePos == capacity){
                writePos = 0;
                flipped = true;

                if(writePos < readPos){
                    elements[writePos++] = element;
                    return true;
                } else {
                    return false;
                }
            } else {
                elements[writePos++] = element;
                return true;
            }
        } else {
            if(writePos < readPos ){
                elements[writePos++] = element;
                return true;
            } else {
                return false;
            }
        }
    }

    // 写入新的数据，返回写入长度
    public int put(int[] newElements, int length){
        int newElementsReadPos = 0;
        if(!flipped){

            // 尾部足够写入所有的数据
            if(length <= capacity - writePos){
                for(; newElementsReadPos < length; newElementsReadPos++){
                    this.elements[this.writePos++] = newElements[newElementsReadPos];
                }

                return newElementsReadPos;
            } else {
                for(;this.writePos < capacity; this.writePos++){
                    this.elements[this.writePos] = newElements[newElementsReadPos++];
                }

                //writing to bottom
                this.writePos = 0;
                this.flipped  = true;
                // 可能出现不够写入的情况
                int endPos = Math.min(this.readPos, length - newElementsReadPos);
                for(; this.writePos < endPos; this.writePos++){
                    this.elements[writePos] = newElements[newElementsReadPos++];
                }


                return newElementsReadPos;
            }

        } else {
            int endPos = Math.min(this.readPos, this.writePos + length);

            for(; this.writePos < endPos; this.writePos++){
                this.elements[this.writePos] = newElements[newElementsReadPos++];
            }

            return newElementsReadPos;
        }
    }

    // 获取当前正在读取的值 or -1
    public int take() {
        if(!flipped){
            if(readPos < writePos){
                return elements[readPos++];
            } else {
                // 还未有任何写入或者写入的都被读过了
                return -1;
            }
        } else {
            if(readPos == capacity){
                readPos = 0;
                flipped = false;

                if(readPos < writePos){
                    return elements[readPos++];
                } else {
                    return -1;
                }
            } else {
                return elements[readPos++];
            }
        }
    }

    // 返回实际读取的元素个数
    public int take(int[] into, int length){
        int intoWritePos = 0;
        if(!flipped){
            int endPos = Math.min(this.writePos, this.readPos + length);
            for(; this.readPos < endPos; this.readPos++){
                into[intoWritePos++] = this.elements[this.readPos];
            }
            // intoWritePos < length代表into[]未读满
            return intoWritePos;
        } else {

            if(length <= capacity - readPos){
                for(; intoWritePos < length; intoWritePos++){
                    into[intoWritePos] = this.elements[this.readPos++];
                }

                return intoWritePos;
            } else {
                for(; this.readPos < capacity; this.readPos++){
                    into[intoWritePos++] = this.elements[this.readPos];
                }

                this.readPos = 0;
                this.flipped = false;
                // 比较剩余需要读的个数和剩余可读的个数
                int endPos = Math.min(this.writePos, length - intoWritePos);
                for(; this.readPos < endPos; this.readPos++){
                    into[intoWritePos++] = this.elements[this.readPos];
                }

                return intoWritePos;
            }
        }
    }

}
