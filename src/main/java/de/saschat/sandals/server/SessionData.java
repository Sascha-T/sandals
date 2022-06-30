package de.saschat.sandals.server;

import de.saschat.sandals.server.auth.AuthHandler;

import java.net.InetSocketAddress;
import java.nio.channels.ByteChannel;
import java.nio.channels.SocketChannel;

public class SessionData {
    public Status status;

    // AUTHENTICATING
    public AuthHandler handler;

    // ROUTED
    public boolean listening;
    public Type type;
    public InetSocketAddress address;

    public ByteChannel readChannel; // type == TCP && !listening

    public enum Type {
        TCP, UDP
    }
    public enum Status {
        CONNECTING,
        AUTHENTICATING,
        AUTHENTICATED,
        ROUTED
    }
    public SessionData() {
        this.status = Status.CONNECTING;
    }

    @Override
    public String toString() {
        return "SessionData{" +
            "status=" + status +
            ", handler=" + handler +
            ", listening=" + listening +
            ", type=" + type +
            ", address=" + address +
            ", tcpChannel=" + readChannel +
            '}';
    }
}
