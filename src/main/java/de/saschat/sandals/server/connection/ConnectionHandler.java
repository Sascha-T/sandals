package de.saschat.sandals.server.connection;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.channels.ByteChannel;
import java.nio.channels.SelectableChannel;

public interface ConnectionHandler {
    public ByteChannel connect(ConnectionInformation information) throws IOException;

    class ConnectionInformation {
        public InetSocketAddress address;
        public ConnectionProtocolType protocol;
        public long timeout;

        public ConnectionInformation(InetSocketAddress address, ConnectionProtocolType protocol, long timeout) {
            this.address = address;
            this.protocol = protocol;
            this.timeout = timeout;
        }
    }

    enum ConnectionProtocolType {
        UDP,
        TCP
    }
}
