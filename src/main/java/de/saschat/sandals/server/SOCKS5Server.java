package de.saschat.sandals.server;

import de.saschat.sandals.protocol.AddressType;
import de.saschat.sandals.protocol.packet.AuthReplyPacket;
import de.saschat.sandals.protocol.packet.AuthRequestPacket;
import de.saschat.sandals.protocol.packet.ConnectionRequestPacket;
import de.saschat.sandals.protocol.packet.ConnectionResponsePacket;
import de.saschat.sandals.server.address.AddressResolver;
import de.saschat.sandals.server.auth.AuthHandlerFactory;
import de.saschat.sandals.server.auth.NoAuthenticationHandlerFactory;

import java.io.IOException;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.time.Instant;
import java.util.*;

public class SOCKS5Server implements Runnable {
    public ServerSocketChannel mainServer;
    public List<AuthHandlerFactory> authMethods = List.of(new NoAuthenticationHandlerFactory());
    public AddressResolver resolver;
    public long timeout;
    protected SOCKS5Server(int port, List<AuthHandlerFactory> factories, AddressResolver resolver, long timeout) throws IOException {
        this.mainServer = ServerSocketChannel.open().bind(new InetSocketAddress(port));
        mainServer.configureBlocking(false);
        this.authMethods = factories;
        this.resolver = resolver;
        this.timeout = timeout;
    }
    public void run() {
        try {
            Selector selector = Selector.open();
            mainServer.register(selector, SelectionKey.OP_ACCEPT);

            ByteBuffer mainBuffer = ByteBuffer.allocate(65535);
            ByteBuffer secondaryBuffer = ByteBuffer.allocate(65535);

            while(true) {
                if(selector.selectNow() == 0) continue;
                Iterator<SelectionKey> keys = selector.selectedKeys().iterator();
                while(keys.hasNext()) {
                    SelectionKey key = keys.next();
                    keys.remove();
                    mainBuffer.clear();
                    secondaryBuffer.clear();

                    try {
                        if(key.isReadable()) {
                            SocketChannel channel = (SocketChannel) key.channel();
                            SessionData data = (SessionData) key.attachment();
                            if(!channel.isOpen())
                                continue;

                            if(data.status == SessionData.Status.CONNECTING) {
                                // Receive methods.
                                channel.read(mainBuffer);
                                mainBuffer.flip();
                                AuthRequestPacket packet = new AuthRequestPacket(mainBuffer);

                                // Choose method.
                                AuthHandlerFactory use = null;
                                for (AuthHandlerFactory factory: authMethods) {
                                    if(packet.METHODS.contains(factory.id())) {
                                        use = factory;
                                        break;
                                    }
                                }

                                byte method = (byte) 0xFF;
                                if(use != null)
                                    method = use.id();

                                AuthReplyPacket reply = new AuthReplyPacket(method);
                                reply.write(secondaryBuffer);
                                secondaryBuffer.flip();
                                channel.write(secondaryBuffer);

                                if(method == (byte) 0xFF) {
                                    // Close connection.
                                    channel.close();
                                    continue;
                                }

                                data.status = SessionData.Status.AUTHENTICATING;
                                data.handler = use.create();
                                if(data.handler.done())
                                    if(data.handler.authorized())
                                        data.status = SessionData.Status.AUTHENTICATED;
                            } else if(data.status == SessionData.Status.AUTHENTICATING) {
                                channel.read(mainBuffer);
                                mainBuffer.flip();
                                data.handler.data(mainBuffer, secondaryBuffer);
                                secondaryBuffer.flip();
                                if(secondaryBuffer.limit() > 0) // Potentially unnecessary comparison.
                                    channel.write(secondaryBuffer);

                                if(data.handler.done()) {
                                    if (data.handler.authorized())
                                        data.status = SessionData.Status.AUTHENTICATED;
                                    else {
                                        channel.close();
                                        continue;
                                    }
                                }
                            } else if(data.status == SessionData.Status.AUTHENTICATED) {
                                channel.read(mainBuffer);
                                mainBuffer.flip();
                                ConnectionRequestPacket request = new ConnectionRequestPacket(mainBuffer);
                                InetAddress address;
                                try {
                                    address = switch (request.ADDRESS_TYPE) {
                                        case IPV4 -> resolver.resolveIPv4(request.ADDRESS);
                                        case IPV6 -> resolver.resolveIPv6(request.ADDRESS);
                                        case DOMAIN -> resolver.resolve(new String(request.ADDRESS));
                                    };
                                } catch (Exception ex) {
                                    ConnectionResponsePacket packet = new ConnectionResponsePacket(
                                        ConnectionResponsePacket.ResponseCode.HOST_UNREACHABLE,
                                        AddressType.IPV4,
                                        new byte[] {0, 0, 0, 0},
                                        (short) 0
                                    );
                                    packet.write(secondaryBuffer);
                                    secondaryBuffer.flip();
                                    channel.write(secondaryBuffer);
                                    channel.close();
                                    continue;
                                }
                                InetSocketAddress socket = new InetSocketAddress(address, request.PORT);
                                data.address = socket;
                                switch (request.COMMAND) {
                                    case BIND_TCP -> {
                                        data.listening = true;
                                        data.type = SessionData.Type.TCP;
                                        // @TODO: Add support
                                        channel.close();
                                        continue;
                                    }
                                    case CONNECT_TCP -> {
                                        data.listening = false;
                                        data.type = SessionData.Type.TCP;
                                        try {
                                            data.tcpChannel = SocketChannel.open();
                                            data.tcpChannel.configureBlocking(false);
                                            data.tcpChannel.connect(socket);
                                            long time = Instant.now().toEpochMilli();
                                            while(!data.tcpChannel.finishConnect()) {
                                                if(Instant.now().toEpochMilli() > time + timeout)
                                                    throw new ConnectException("timeout");
                                            }
                                            ConnectionResponsePacket packet = new ConnectionResponsePacket(
                                                ConnectionResponsePacket.ResponseCode.SUCCEEDED,
                                                AddressType.IPV4,
                                                new byte[] {0, 0, 0, 0},
                                                (short) 0
                                            );
                                            packet.write(secondaryBuffer);
                                            secondaryBuffer.flip();
                                            channel.write(secondaryBuffer);
                                        } catch (SecurityException ex) {
                                            ex.printStackTrace();
                                            ConnectionResponsePacket packet = new ConnectionResponsePacket(
                                                ConnectionResponsePacket.ResponseCode.CONNECTION_NOT_ALLOWED,
                                                AddressType.IPV4,
                                                new byte[] {0, 0, 0, 0},
                                                (short) 0
                                            );
                                            packet.write(secondaryBuffer);
                                            secondaryBuffer.flip();
                                            channel.write(secondaryBuffer);
                                            channel.close();
                                            continue;
                                        } catch (ConnectException ex) {
                                            ex.printStackTrace();
                                            ConnectionResponsePacket packet = new ConnectionResponsePacket(
                                                ConnectionResponsePacket.ResponseCode.HOST_UNREACHABLE,
                                                AddressType.IPV4,
                                                new byte[] {0, 0, 0, 0},
                                                (short) 0
                                            );
                                            packet.write(secondaryBuffer);
                                            secondaryBuffer.flip();
                                            channel.write(secondaryBuffer);
                                            channel.close();
                                            continue;
                                        } catch (Exception ex) {
                                            ex.printStackTrace();
                                            ConnectionResponsePacket packet = new ConnectionResponsePacket(
                                                ConnectionResponsePacket.ResponseCode.SERVER_ERROR,
                                                AddressType.IPV4,
                                                new byte[] {0, 0, 0, 0},
                                                (short) 0
                                            );
                                            packet.write(secondaryBuffer);
                                            secondaryBuffer.flip();
                                            channel.write(secondaryBuffer);
                                            channel.close();
                                            continue;
                                        }
                                    }
                                    case FORWARD_UDP -> {
                                        data.type = SessionData.Type.UDP;
                                        // @TODO: Add support
                                        channel.close();
                                        continue;
                                    }
                                }
                                data.status = SessionData.Status.ROUTED;
                            } else if (data.status == SessionData.Status.ROUTED) {
                                if(data.type == SessionData.Type.TCP)
                                    if(data.listening) {
                                        // @TODO: Add support
                                        channel.close();
                                    } else {
                                        channel.read(mainBuffer);
                                        if(!data.tcpChannel.isOpen())
                                            channel.close();
                                        mainBuffer.flip();
                                        data.tcpChannel.write(mainBuffer);
                                    }
                            }
                        }
                        if (key.isWritable()) {
                            SocketChannel channel = (SocketChannel) key.channel();
                            SessionData data = (SessionData) key.attachment();
                            if(!channel.isOpen())
                                continue;
                            if(data.status == SessionData.Status.ROUTED) {
                                if(data.type == SessionData.Type.TCP)
                                    if(data.listening) {
                                        // @TODO: Add support
                                        channel.close();
                                    } else {
                                        if(!data.tcpChannel.isOpen())
                                            channel.close();
                                        int a = data.tcpChannel.read(mainBuffer);
                                        if(a > 0) {
                                            mainBuffer.flip();
                                            channel.write(mainBuffer);
                                        }
                                    }
                            }
                        }
                        if (key.isAcceptable()) {
                            ServerSocketChannel server = (ServerSocketChannel) key.channel();
                            SocketChannel channel = server.accept();

                            channel.configureBlocking(false);
                            channel.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE, new SessionData());
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
