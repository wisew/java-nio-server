package com.wisew.nioserver.http;

import com.wisew.nioserver.IMessageReader;
import com.wisew.nioserver.IMessageReaderFactory;

public class HttpMessageReaderFactory implements IMessageReaderFactory {

    public HttpMessageReaderFactory() {
    }

    @Override
    public IMessageReader createMessageReader() {
        return new HttpMessageReader();
    }
}
