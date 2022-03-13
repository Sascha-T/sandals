package de.saschat.sandals.server.auth;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class NoAuthenticationHandler implements AuthHandler  {
    @Override
    public void data(ByteBuffer data, ByteBuffer reply) {
        throw new RuntimeException();
    }

    @Override
    public boolean done() {
        return true;
    }

    @Override
    public boolean authorized() {
        return true;
    }
}
