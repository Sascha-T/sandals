package de.saschat.sandals.server.address;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class DefaultAddressResolver implements AddressResolver {
    @Override
    public InetAddress resolve(String hostname) throws UnknownHostException {
        return InetAddress.getByName(hostname);
    }

    @Override
    public InetAddress resolveIPv4(byte[] data) throws UnknownHostException {
        return Inet4Address.getByAddress(data);
    }

    @Override
    public InetAddress resolveIPv6(byte[] data) throws UnknownHostException {
        return Inet6Address.getByAddress(data);
    }
}
