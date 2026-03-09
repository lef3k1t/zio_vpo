package org.example.server.license;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.server.license.dto.*;
import org.example.server.user.ApplicationUser;
import org.example.server.user.ApplicationUserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/license")
public class LicenseController {

    private final LicenseService licenseService;
    private final ApplicationUserRepository userRepository;

    private Long currentUserIdOrThrow() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getPrincipal() == null) {
            throw new IllegalStateException("unauthorized");
        }

        String email;
        Object principal = auth.getPrincipal();
        if (principal instanceof org.springframework.security.core.userdetails.UserDetails ud) {
            email = ud.getUsername();
        } else {
            email = String.valueOf(principal);
        }

        ApplicationUser user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("user not found"));
        return user.getId();
    }

    @PostMapping("/admin/create")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CreateLicenseResponse> create(@Valid @RequestBody CreateLicenseRequest req) {
        Long adminId = currentUserIdOrThrow();
        CreateLicenseResponse resp = licenseService.createLicense(req, adminId);
        return ResponseEntity.status(HttpStatus.CREATED).body(resp);
    }


    @PostMapping("/activate")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<TicketResponse> activate(@Valid @RequestBody ActivateLicenseRequest req) {
        Long userId = currentUserIdOrThrow();
        return ResponseEntity.ok(licenseService.activateLicense(req, userId));
    }


    @PostMapping("/check")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<TicketResponse> check(@Valid @RequestBody CheckLicenseRequest req) {
        Long userId = currentUserIdOrThrow();
        return ResponseEntity.ok(licenseService.checkLicense(req, userId));
    }

    @PostMapping("/renew")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<TicketResponse> renew(@Valid @RequestBody RenewLicenseRequest req) {
        Long userId = currentUserIdOrThrow();
        return ResponseEntity.ok(licenseService.renewLicense(req, userId));
    }
}