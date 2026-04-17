package org.example.server.binary;

import java.util.UUID;

public record BinaryManifestEntry(
        UUID id,
        int statusCode,
        long updatedAtEpochMillis,
        long dataOffset,
        long dataLength,
        byte[] recordSignatureBytes
) {
}
