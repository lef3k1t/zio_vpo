package org.example.server.binary;

public record BinaryDataRecord(
        String threatName,
        byte[] firstBytes,
        byte[] remainderHash,
        long remainderLength,
        String fileType,
        long offsetStart,
        long offsetEnd
) {
}
