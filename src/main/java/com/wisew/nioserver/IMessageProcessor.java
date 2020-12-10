package com.wisew.nioserver;

public interface IMessageProcessor {

    void process(Message message, WriteProxy writeProxy);

}
