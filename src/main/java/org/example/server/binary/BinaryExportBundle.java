package org.example.server.binary;

public record BinaryExportBundle(byte[] manifestBytes, byte[] dataBytes) {
}
