package org.example.server.binary;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class BinaryDataSerializer {

    public byte[] serialize(List<byte[]> serializedRecords) {
        return BinaryPrimitiveWriter.toByteArray(outputStream -> {
            BinaryPrimitiveWriter.writeMagic(outputStream, BinaryFormatConstants.DATA_MAGIC);
            BinaryPrimitiveWriter.writeU16(outputStream, BinaryFormatConstants.VERSION);
            BinaryPrimitiveWriter.writeU32(outputStream, serializedRecords.size());

            for (byte[] serializedRecord : serializedRecords) {
                outputStream.write(serializedRecord);
            }
        });
    }

    public byte[] serializeRecord(BinaryDataRecord record) {
        return BinaryPrimitiveWriter.toByteArray(outputStream -> {
            BinaryPrimitiveWriter.writeUtf8(outputStream, record.threatName());
            BinaryPrimitiveWriter.writeByteArray(outputStream, record.firstBytes());
            BinaryPrimitiveWriter.writeByteArray(outputStream, record.remainderHash());
            BinaryPrimitiveWriter.writeI64(outputStream, record.remainderLength());
            BinaryPrimitiveWriter.writeUtf8(outputStream, record.fileType());
            BinaryPrimitiveWriter.writeI64(outputStream, record.offsetStart());
            BinaryPrimitiveWriter.writeI64(outputStream, record.offsetEnd());
        });
    }
}
