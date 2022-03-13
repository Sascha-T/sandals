package de.saschat.sandals.protocol.packet;

import de.saschat.sandals.protocol.AddressType;
import de.saschat.sandals.protocol.InvalidSOCKSVersionException;
import de.saschat.sandals.protocol.Packet;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class ConnectionResponsePacket extends Packet {
    public ResponseCode RESPONSE_CODE;
    public AddressType BIND_ADDRESS_TYPE;
    public byte[] BIND_ADDRESS;
    public short PORT;

    public ConnectionResponsePacket(ByteBuffer buffer) {
        super(buffer);
        byte v = buffer.get();
        if(v != 0x05)
            throw new InvalidSOCKSVersionException(v, 5);
        RESPONSE_CODE = ResponseCode.from(buffer.get());
        buffer.get();
        BIND_ADDRESS_TYPE = AddressType.from(buffer.get());
        switch (BIND_ADDRESS_TYPE) {
            case IPV4 -> BIND_ADDRESS = new byte[] {buffer.get(), buffer.get(), buffer.get(), buffer.get()};
            case IPV6 -> BIND_ADDRESS = new byte[] {
                buffer.get(), buffer.get(), buffer.get(), buffer.get(),
                buffer.get(), buffer.get(), buffer.get(), buffer.get(),
                buffer.get(), buffer.get(), buffer.get(), buffer.get(),
                buffer.get(), buffer.get(), buffer.get(), buffer.get()
            };
            case DOMAIN -> {
                byte len = buffer.get();
                byte[] domainData = new byte[len];
                buffer.get(domainData);
                BIND_ADDRESS = domainData;
            }
        }
        PORT = buffer.getShort();
    }
    public ConnectionResponsePacket(ResponseCode code, AddressType type, byte[] address, short port) {
        super(null);
        this.RESPONSE_CODE = code;
        this.BIND_ADDRESS_TYPE = type;
        this.BIND_ADDRESS = address;
        this.PORT = port;
    }

    @Override
    public void write(ByteBuffer buffer) {
        buffer.put((byte) 0x05);
        buffer.put(RESPONSE_CODE.data);
        buffer.put((byte) 0x00);
        buffer.put(BIND_ADDRESS_TYPE.cmd);
        buffer.put(BIND_ADDRESS);
        buffer.putShort(PORT);
    }

    public enum ResponseCode {
        SUCCEEDED((byte) 0x00),
        SERVER_ERROR((byte) 0x01),
        CONNECTION_NOT_ALLOWED((byte) 0x02),
        NETWORK_UNREACHABLE((byte) 0x03),
        HOST_UNREACHABLE((byte) 0x04),
        CONNECTION_REFUSED((byte) 0x05),
        TTL_EXPIRED((byte) 0x06),
        COMMAND_NOT_SUPPORTED((byte) 0x07),
        ADDRESS_TYPE_NOT_SUPPORTED((byte) 0x08);

        byte data;
        ResponseCode(byte data) {
            this.data = data;
        }

        public static ResponseCode from(byte data) {
            return Arrays.stream(ResponseCode.values()).filter(a -> a.data == data).findFirst().get();
        }
    }
}
