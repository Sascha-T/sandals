package de.saschat.sandals.protocol.packet;

import de.saschat.sandals.protocol.InvalidSOCKSVersionException;
import de.saschat.sandals.protocol.Packet;

import java.nio.ByteBuffer;

public class AuthReplyPacket extends Packet {
    public AuthReplyPacket(ByteBuffer buffer) {
        super(buffer);
        byte v = buffer.get();
        if(v != 0x05)
            throw new InvalidSOCKSVersionException(v, 5);
        METHOD = buffer.get();
    }

    public byte METHOD;

    public AuthReplyPacket(byte method) {
        super(null);
        METHOD = method;
    }

    @Override
    public void write(ByteBuffer buffer) {
        buffer.put((byte) 0x05);
        buffer.put(METHOD);
    }
}
