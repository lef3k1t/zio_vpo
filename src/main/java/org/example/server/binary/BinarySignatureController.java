package org.example.server.binary;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.server.malware.dto.SignatureIdsRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/binary/signatures")
public class BinarySignatureController {

    private final BinarySignatureExportService exportService;
    private final MultipartMixedResponseFactory multipartMixedResponseFactory;

    @GetMapping("/full")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<MultiValueMap<String, Object>> getFull() {
        return multipartResponse(exportService.exportFull());
    }

    @GetMapping("/increment")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<MultiValueMap<String, Object>> getIncrement(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            Instant since
    ) {
        return multipartResponse(exportService.exportIncrement(since));
    }

    @PostMapping("/by-ids")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<MultiValueMap<String, Object>> getByIds(@Valid @RequestBody SignatureIdsRequest request) {
        return multipartResponse(exportService.exportByIds(request.ids()));
    }

    private ResponseEntity<MultiValueMap<String, Object>> multipartResponse(BinaryExportBundle exportBundle) {
        return ResponseEntity.ok()
                .contentType(MediaType.MULTIPART_MIXED)
                .body(multipartMixedResponseFactory.create(exportBundle));
    }
}
