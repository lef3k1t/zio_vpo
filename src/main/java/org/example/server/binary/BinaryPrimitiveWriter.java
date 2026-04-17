package org.example.server.binary;

import org.example.server.signature.SignatureException;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public final class BinaryPrimitiveWriter {

    private BinaryPrimitiveWriter() {
    }

    public static byte[] toByteArray(ThrowingConsumer<DataOutputStream> consumer) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            DataOutputStream dataOutputStream = new DataOutputStream(outputStream);
            consumer.accept(dataOutputStream);
            dataOutputStream.flush();
            return outputStream.toByteArray();
        } catch (IOException e) {
            throw new SignatureException("Failed to write binary payload", e);
        }
    }

    public static void writeMagic(DataOutputStream outputStream, byte[] magic) throws IOException {
        outputStream.write(magic);
    }

    public static void writeU8(DataOutputStream outputStream, int value) throws IOException {
        if (value < 0 || value > 0xFF) {
            throw new IllegalArgumentException("uint8 value is out of range: " + value);
        }
        outputStream.writeByte(value);
    }

    public static void writeU16(DataOutputStream outputStream, int value) throws IOException {
        if (value < 0 || value > 0xFFFF) {
            throw new IllegalArgumentException("uint16 value is out of range: " + value);
        }
        outputStream.writeShort(value);
    }

    public static void writeU32(DataOutputStream outputStream, long value) throws IOException {
        if (value < 0 || value > 0xFFFF_FFFFL) {
            throw new IllegalArgumentException("uint32 value is out of range: " + value);
        }
        outputStream.writeInt((int) value);
    }

    public static void writeU64(DataOutputStream outputStream, long value) throws IOException {
        if (value < 0) {
            throw new IllegalArgumentException("uint64 value must be >= 0: " + value);
        }
        outputStream.writeLong(value);
    }

    public static void writeI64(DataOutputStream outputStream, long value) throws IOException {
        outputStream.writeLong(value);
    }

    public static void writeUuid(DataOutputStream outputStream, UUID value) throws IOException {
        outputStream.writeLong(value.getMostSignificantBits());
        outputStream.writeLong(value.getLeastSignificantBits());
    }

    public static void writeUtf8(DataOutputStream outputStream, String value) throws IOException {
        byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
        writeU32(outputStream, bytes.length);
        outputStream.write(bytes);
    }

    public static void writeByteArray(DataOutputStream outputStream, byte[] value) throws IOException {
        writeU32(outputStream, value.length);
        outputStream.write(value);
    }

    @FunctionalInterface
    public interface ThrowingConsumer<T> {
        void accept(T value) throws IOException;
    }
}
