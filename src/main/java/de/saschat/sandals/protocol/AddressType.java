package de.saschat.sandals.protocol;

import de.saschat.sandals.protocol.packet.ConnectionRequestPacket;

import java.util.Arrays;

public enum AddressType {
    IPV4((byte) 0x01),
    DOMAIN((byte) 0x03),
    IPV6((byte) 0x04);

    public byte cmd;

    AddressType(byte cmd) {
        this.cmd = cmd;
    }

    public static AddressType from(byte name) {
        return Arrays.stream(AddressType.values()).filter(a -> a.cmd == name).findFirst().get();
    }
}
