package org.example.server.binary;

import org.example.server.malware.entity.MalwareSignature;
import org.example.server.malware.entity.SignatureStatus;
import org.example.server.malware.repo.MalwareSignatureRepository;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class BinarySignatureExportService {

    private final MalwareSignatureRepository signatureRepository;
    private final BinaryDataSerializer binaryDataSerializer;
    private final BinaryManifestSerializer binaryManifestSerializer;

    public BinarySignatureExportService(MalwareSignatureRepository signatureRepository,
                                        BinaryDataSerializer binaryDataSerializer,
                                        BinaryManifestSerializer binaryManifestSerializer) {
        this.signatureRepository = signatureRepository;
        this.binaryDataSerializer = binaryDataSerializer;
        this.binaryManifestSerializer = binaryManifestSerializer;
    }

    public BinaryExportBundle exportFull() {
        List<MalwareSignature> signatures = signatureRepository.findAllByStatusOrderByUpdatedAtAsc(SignatureStatus.ACTUAL);
        return buildBundle(signatures, BinaryExportType.FULL, BinaryFormatConstants.NO_SINCE);
    }

    public BinaryExportBundle exportIncrement(Instant since) {
        List<MalwareSignature> signatures = signatureRepository.findAllByUpdatedAtAfterOrderByUpdatedAtAsc(since);
        return buildBundle(signatures, BinaryExportType.INCREMENT, since.toEpochMilli());
    }

    public BinaryExportBundle exportByIds(List<UUID> ids) {
        if (ids == null || ids.isEmpty()) {
            return buildBundle(List.of(), BinaryExportType.BY_IDS, BinaryFormatConstants.NO_SINCE);
        }

        Map<UUID, MalwareSignature> foundById = new LinkedHashMap<>();
        for (MalwareSignature signature : signatureRepository.findAllByIdIn(ids)) {
            foundById.put(signature.getId(), signature);
        }

        List<MalwareSignature> ordered = ids.stream()
                .map(foundById::get)
                .filter(signature -> signature != null)
                .toList();

        return buildBundle(ordered, BinaryExportType.BY_IDS, BinaryFormatConstants.NO_SINCE);
    }

    private BinaryExportBundle buildBundle(List<MalwareSignature> signatures,
                                           BinaryExportType exportType,
                                           long sinceEpochMillis) {
        List<byte[]> serializedDataRecords = new ArrayList<>(signatures.size());
        List<BinaryManifestEntry> manifestEntries = new ArrayList<>(signatures.size());

        long currentOffset = 0L;
        for (MalwareSignature signature : signatures) {
            BinaryDataRecord dataRecord = toDataRecord(signature);
            byte[] serializedRecord = binaryDataSerializer.serializeRecord(dataRecord);
            serializedDataRecords.add(serializedRecord);

            manifestEntries.add(new BinaryManifestEntry(
                    signature.getId(),
                    toStatusCode(signature.getStatus()),
                    signature.getUpdatedAt().toEpochMilli(),
                    currentOffset,
                    serializedRecord.length,
                    Base64.getDecoder().decode(signature.getDigitalSignatureBase64())
            ));

            currentOffset += serializedRecord.length;
        }

        byte[] dataBytes = binaryDataSerializer.serialize(serializedDataRecords);
        byte[] dataSha256 = sha256(dataBytes);
        byte[] manifestBytes = binaryManifestSerializer.serialize(
                exportType,
                Instant.now().toEpochMilli(),
                sinceEpochMillis,
                dataSha256,
                manifestEntries
        );

        return new BinaryExportBundle(manifestBytes, dataBytes);
    }

    private BinaryDataRecord toDataRecord(MalwareSignature signature) {
        return new BinaryDataRecord(
                signature.getThreatName(),
                decodeHex(signature.getFirstBytesHex()),
                decodeHex(signature.getRemainderHashHex()),
                signature.getRemainderLength(),
                signature.getFileType(),
                signature.getOffsetStart(),
                signature.getOffsetEnd()
        );
    }

    private int toStatusCode(SignatureStatus status) {
        return switch (status) {
            case ACTUAL -> 1;
            case DELETED -> 2;
        };
    }

    private byte[] decodeHex(String value) {
        return HexFormat.of().parseHex(value);
    }

    private byte[] sha256(byte[] bytes) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(bytes);
        } catch (Exception e) {
            throw new IllegalStateException("failed to calculate data SHA-256", e);
        }
    }
}
