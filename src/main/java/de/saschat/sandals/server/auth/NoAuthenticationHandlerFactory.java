package de.saschat.sandals.server.auth;

public class NoAuthenticationHandlerFactory implements AuthHandlerFactory<NoAuthenticationHandler> {
    @Override
    public NoAuthenticationHandler create() {
        return new NoAuthenticationHandler();
    }

    @Override
    public byte id() {
        return 0x00;
    }
}
