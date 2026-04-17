package org.example.server.binary;

import java.nio.charset.StandardCharsets;

public final class BinaryFormatConstants {

    public static final byte[] MANIFEST_MAGIC = "MF-Kosolapov".getBytes(StandardCharsets.US_ASCII);
    public static final byte[] DATA_MAGIC = "DB-Kosolapov".getBytes(StandardCharsets.US_ASCII);

    public static final int VERSION = 1;
    public static final long NO_SINCE = -1L;
    public static final int SHA_256_LENGTH = 32;

    private BinaryFormatConstants() {
    }
}
