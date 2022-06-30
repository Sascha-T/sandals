package de.saschat.sandals.server.connection;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketAddress;
import java.nio.channels.ByteChannel;
import java.nio.channels.SocketChannel;
import java.time.Instant;

public class DefaultConnectionHandler implements ConnectionHandler {
    @Override
    public ByteChannel connect(ConnectionInformation information) throws IOException {
        if(information.protocol != ConnectionProtocolType.TCP)
            throw new RuntimeException("Only TCP is supported.");
        SocketChannel channel = SocketChannel.open();
        channel.configureBlocking(false);
        channel.connect(information.address);
        long time = Instant.now().toEpochMilli();
        while (!channel.finishConnect()) {
            if (Instant.now().toEpochMilli() > time + information.timeout)
                throw new ConnectException("timeout");
        }
        return channel;
    }
}
