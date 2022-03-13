package de.saschat.sandals.server.auth;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class PasswordAuthenticationHandler implements AuthHandler {
    boolean authorized = false;
    boolean done = false;

    PasswordAuthenticationHandlerFactory.PasswordChecker checker;
    public PasswordAuthenticationHandler(PasswordAuthenticationHandlerFactory.PasswordChecker checker) {
        this.checker = checker;
    }

    @Override
    public void data(ByteBuffer data, ByteBuffer reply) {
        byte version = data.get();
        if(version != 0x01) {
            done = true;
            return;
        }

        byte nameLen = data.get();
        byte[] nameDat = new byte[nameLen];
        data.get(nameDat);
        String name = new String(nameDat);

        byte passLen = data.get();
        byte[] passDat = new byte[passLen];
        data.get(passDat);
        String pass = new String(passDat);

        done = true;
        authorized = checker.check(name, pass);

        reply.put((byte) 0x1);
        reply.put(authorized ? 0x00 : (byte) 0xFF);
    }

    @Override
    public boolean done() {
        return done;
    }

    @Override
    public boolean authorized() {
        return authorized;
    }
}
