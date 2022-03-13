package de.saschat.sandals.server.auth;

public interface AuthHandlerFactory<T extends AuthHandler> {
    T create();
    byte id();
}
