package de.saschat.sandals.server.address;

import java.net.InetAddress;
import java.net.UnknownHostException;

public interface AddressResolver {
    public InetAddress resolve(String hostname) throws UnknownHostException;
    public InetAddress resolveIPv4(byte[] data) throws UnknownHostException;
    public InetAddress resolveIPv6(byte[] data) throws UnknownHostException;
}
