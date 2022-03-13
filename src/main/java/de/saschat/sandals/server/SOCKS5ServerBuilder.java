package de.saschat.sandals.server;

import de.saschat.sandals.server.address.AddressResolver;
import de.saschat.sandals.server.address.DefaultAddressResolver;
import de.saschat.sandals.server.auth.AuthHandlerFactory;
import de.saschat.sandals.server.auth.NoAuthenticationHandlerFactory;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class SOCKS5ServerBuilder {
    List<AuthHandlerFactory> factories = new LinkedList<>();
    AddressResolver resolver = null;
    int port = -1;
    long timeout = -1;
    public SOCKS5ServerBuilder(boolean useDefault) {
        if(useDefault) {
            factories.add(new NoAuthenticationHandlerFactory());
            resolver = new DefaultAddressResolver();
            port = 1080;
            timeout = 5000;
        }
    }
    public SOCKS5ServerBuilder() {
        this(false);
    }
    public SOCKS5ServerBuilder clearAuthHandlerFactories() {
        factories.clear();
        return this;
    }

    public SOCKS5ServerBuilder addAuthHandlerFactory(AuthHandlerFactory factory) {
        factories.add(factory);
        return this;
    }

    public SOCKS5ServerBuilder setResolver(AddressResolver resolver) {
        this.resolver = resolver;
        return this;
    }

    public SOCKS5ServerBuilder setPort(short port) {
        this.port = port;
        return this;
    }

    public SOCKS5ServerBuilder setTimeout(long timeout) {
        this.timeout = timeout;
        return this;
    }

    public SOCKS5Server build() throws IOException {
        if(port == -1)
            throw new RuntimeException("Port not specified in SOCKS5ServerBuilder.");
        if(timeout == -1)
            throw new RuntimeException("Timeout not specified in SOCKS5ServerBuilder.");
        if(factories.size() == 0)
            throw new RuntimeException("No authentication methods specified in SOCKS5ServerBuilder.");
        if(resolver == null)
            throw new RuntimeException("Resolver not specified in SOCKS5ServerBuilder.");
        return new SOCKS5Server(port, factories, resolver, timeout);
    }
}
