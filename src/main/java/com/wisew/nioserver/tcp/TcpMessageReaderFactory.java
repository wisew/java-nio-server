package com.wisew.nioserver.tcp;

import com.wisew.nioserver.IMessageReader;
import com.wisew.nioserver.IMessageReaderFactory;

public class TcpMessageReaderFactory implements IMessageReaderFactory {
    @Override
    public IMessageReader createMessageReader() {
        return new TcpMessageReader();
    }
}
