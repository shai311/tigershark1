package com.tigershark.http;

import okhttp3.*;
import java.io.*;
import com.google.gson.*;
import java.util.concurrent.*;
import java.util.*;
import java.net.URI;
import java.nio.file.Paths;
import java.net.*;
import okio.*;

public class progress extends RequestBody {

    protected RequestBody mDelegate;
    protected Listener mListener;
    protected CountingSink mCountingSink;

    public progress(RequestBody delegate, Listener listener) {
        mDelegate = delegate;
        mListener = listener;
    }

    @Override
    public MediaType contentType() {
        return mDelegate.contentType();
    }

    @Override
    public long contentLength() {
        try {
            return mDelegate.contentLength();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return -1;
    }

    @Override
    public void writeTo(BufferedSink sink) throws IOException {
        mCountingSink = new CountingSink(sink);
        BufferedSink bufferedSink = Okio.buffer(mCountingSink);
        mDelegate.writeTo(bufferedSink);
        bufferedSink.flush();
    }

    protected final class CountingSink extends ForwardingSink {
        private long bytesWritten = 0;
        public CountingSink(Sink delegate) {
            super(delegate);
        }
        @Override
        public void write(Buffer source, long byteCount) throws IOException {
            super.write(source, byteCount);
            bytesWritten += byteCount;
            mListener.onProgress((int) bytesWritten);
        }
    }

    public interface Listener {
        void onProgress(int progress);
    }
}