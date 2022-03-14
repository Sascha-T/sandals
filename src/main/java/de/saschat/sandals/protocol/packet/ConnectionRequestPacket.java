package de.saschat.sandals.protocol.packet;

import de.saschat.sandals.protocol.AddressType;
import de.saschat.sandals.protocol.InvalidSOCKSVersionException;
import de.saschat.sandals.protocol.Packet;

import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class ConnectionRequestPacket extends Packet {
    public Command COMMAND;
    public AddressType ADDRESS_TYPE;
    public byte[] ADDRESS;
    public short PORT;

    public ConnectionRequestPacket(ByteBuffer buffer) throws UnknownHostException {
        super(buffer);
        byte v = buffer.get();
        if (v != 0x05)
            throw new InvalidSOCKSVersionException(v, 5);
        COMMAND = Command.from(buffer.get());
        buffer.get(); // Reserved.
        ADDRESS_TYPE = AddressType.from(buffer.get());
        switch (ADDRESS_TYPE) {
            case IPV4: {
                ADDRESS = new byte[]{buffer.get(), buffer.get(), buffer.get(), buffer.get()};
                break;
            }
            case IPV6: {
                ADDRESS = new byte[]{
                    buffer.get(), buffer.get(), buffer.get(), buffer.get(),
                    buffer.get(), buffer.get(), buffer.get(), buffer.get(),
                    buffer.get(), buffer.get(), buffer.get(), buffer.get(),
                    buffer.get(), buffer.get(), buffer.get(), buffer.get()
                };
                break;
            }
            case DOMAIN: {
                byte len = buffer.get();
                byte[] domainData = new byte[len];
                buffer.get(domainData);
                ADDRESS = domainData;
                break;
            }
        }
        PORT = buffer.getShort();
    }

    @Override
    public void write(ByteBuffer buffer) {
        buffer.put((byte) 0x05);
        buffer.put(COMMAND.cmd);
        buffer.put(ADDRESS_TYPE.cmd);
        buffer.put(ADDRESS);
        buffer.putShort(PORT);
    }

    public enum Command {
        CONNECT_TCP((byte) 0x01),
        BIND_TCP((byte) 0x02),
        FORWARD_UDP((byte) 0x03);

        public byte cmd;

        Command(byte cmd) {
            this.cmd = cmd;
        }

        public static Command from(byte name) {
            return Arrays.stream(Command.values()).filter(a -> a.cmd == name).findFirst().get();
        }
    }

    @Override
    public String toString() {
        return "ConnectionRequestPacket{" +
            "COMMAND=" + COMMAND +
            ", ADDRESS_TYPE=" + ADDRESS_TYPE +
            ", ADDRESS=" + Arrays.toString(ADDRESS) +
            ", PORT=" + PORT +
            '}';
    }
}
