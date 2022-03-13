package de.saschat.sandals.protocol.packet;

import de.saschat.sandals.protocol.InvalidSOCKSVersionException;
import de.saschat.sandals.protocol.Packet;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class AuthRequestPacket extends Packet {
    public Set<Byte> METHODS = new HashSet<>();

    public AuthRequestPacket(ByteBuffer buffer) {
        super(buffer);
        byte v = buffer.get();
        if(v != 0x05)
            throw new InvalidSOCKSVersionException(v, 5);
        byte l = buffer.get();
        for (int i = 0; i < l; i++)
            METHODS.add(buffer.get());
    }

    @Override
    public void write(ByteBuffer buffer) {
        buffer.put((byte) 0x05);
        buffer.put((byte) METHODS.size());
        METHODS.forEach(a -> buffer.put(a));
    }

    @Override
    public String toString() {
        return "AuthRequestPacket{" +
            "METHODS=" + METHODS +
            '}';
    }
}
