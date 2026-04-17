package org.example.server.binary;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

@Component
public class MultipartMixedResponseFactory {

    public MultiValueMap<String, Object> create(BinaryExportBundle exportBundle) {
        LinkedMultiValueMap<String, Object> parts = new LinkedMultiValueMap<>();
        parts.add("manifest", createPart("manifest.bin", exportBundle.manifestBytes()));
        parts.add("data", createPart("data.bin", exportBundle.dataBytes()));
        return parts;
    }

    private HttpEntity<ByteArrayResource> createPart(String filename, byte[] bytes) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentLength(bytes.length);
        headers.setContentDisposition(ContentDisposition.attachment().filename(filename).build());

        ByteArrayResource resource = new NamedByteArrayResource(filename, bytes);
        return new HttpEntity<>(resource, headers);
    }

    private static final class NamedByteArrayResource extends ByteArrayResource {

        private final String filename;

        private NamedByteArrayResource(String filename, byte[] byteArray) {
            super(byteArray);
            this.filename = filename;
        }

        @Override
        public String getFilename() {
            return filename;
        }
    }
}
