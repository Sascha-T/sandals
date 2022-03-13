package de.saschat.sandals.server.auth;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public interface AuthHandler {
    public void data(ByteBuffer data, ByteBuffer reply);
    public boolean done();
    public boolean authorized();
}
