package org.example.server.binary;

import org.example.server.signature.SigningService;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class BinaryManifestSerializer {

    private final SigningService signingService;

    public BinaryManifestSerializer(SigningService signingService) {
        this.signingService = signingService;
    }

    public byte[] serialize(BinaryExportType exportType,
                            long generatedAtEpochMillis,
                            long sinceEpochMillis,
                            byte[] dataSha256,
                            List<BinaryManifestEntry> entries) {
        if (dataSha256.length != BinaryFormatConstants.SHA_256_LENGTH) {
            throw new IllegalArgumentException("dataSha256 must contain exactly 32 bytes");
        }

        byte[] unsignedManifest = BinaryPrimitiveWriter.toByteArray(outputStream -> {
            BinaryPrimitiveWriter.writeMagic(outputStream, BinaryFormatConstants.MANIFEST_MAGIC);
            BinaryPrimitiveWriter.writeU16(outputStream, BinaryFormatConstants.VERSION);
            BinaryPrimitiveWriter.writeU8(outputStream, exportType.getCode());
            BinaryPrimitiveWriter.writeI64(outputStream, generatedAtEpochMillis);
            BinaryPrimitiveWriter.writeI64(outputStream, sinceEpochMillis);
            BinaryPrimitiveWriter.writeU32(outputStream, entries.size());
            outputStream.write(dataSha256);

            for (BinaryManifestEntry entry : entries) {
                BinaryPrimitiveWriter.writeUuid(outputStream, entry.id());
                BinaryPrimitiveWriter.writeU8(outputStream, entry.statusCode());
                BinaryPrimitiveWriter.writeI64(outputStream, entry.updatedAtEpochMillis());
                BinaryPrimitiveWriter.writeU64(outputStream, entry.dataOffset());
                BinaryPrimitiveWriter.writeU32(outputStream, entry.dataLength());
                BinaryPrimitiveWriter.writeByteArray(outputStream, entry.recordSignatureBytes());
            }
        });

        byte[] manifestSignatureBytes = signingService.sign(unsignedManifest);
        return BinaryPrimitiveWriter.toByteArray(outputStream -> {
            outputStream.write(unsignedManifest);
            BinaryPrimitiveWriter.writeByteArray(outputStream, manifestSignatureBytes);
        });
    }
}
