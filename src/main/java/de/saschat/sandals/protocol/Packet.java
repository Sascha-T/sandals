package de.saschat.sandals.protocol;

import java.nio.ByteBuffer;

public abstract class Packet {
    public Packet(ByteBuffer buffer) {}
    public abstract void write(ByteBuffer buffer);
}
