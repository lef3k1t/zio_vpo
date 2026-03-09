package org.example.server.license;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.server.license.dto.*;
import org.example.server.license.entity.*;
import org.example.server.license.repo.*;
import org.example.server.license.security.TicketSigner;
import org.example.server.user.ApplicationUser;
import org.example.server.user.ApplicationUserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class LicenseService {

    private final ProductRepository productRepository;
    private final LicenseTypeRepository licenseTypeRepository;
    private final LicenseRepository licenseRepository;

    private final DeviceRepository deviceRepository;
    private final DeviceLicenseRepository deviceLicenseRepository;

    private final LicenseHistoryRepository historyRepository;
    private final ApplicationUserRepository userRepository;

    private final TicketSigner ticketSigner;

    @Value("${app.ticket.ttl-seconds:300}")
    private long ticketTtlSeconds;

    private static final SecureRandom RND = new SecureRandom();


    @Transactional
    public CreateLicenseResponse createLicense(CreateLicenseRequest req, Long adminId) {
        Product product = productRepository.findById(req.getProductId())
                .orElseThrow(() -> new IllegalArgumentException("product not found"));
        if (product.isBlocked()) throw new IllegalArgumentException("product is blocked");

        LicenseType type = licenseTypeRepository.findById(req.getTypeId())
                .orElseThrow(() -> new IllegalArgumentException("type not found"));

        ApplicationUser owner = userRepository.findById(req.getOwnerId())
                .orElseThrow(() -> new IllegalArgumentException("owner not found"));
        if (!owner.isEnabled()) throw new IllegalArgumentException("owner is not active");

        ApplicationUser admin = userRepository.findById(adminId)
                .orElseThrow(() -> new IllegalArgumentException("admin not found"));

        License license = License.builder()
                .code(generateCode())
                .product(product)
                .type(type)
                .owner(owner)
                .user(null)
                .firstActivationDate(null)
                .endingDate(null)
                .blocked(false)
                .deviceCount(req.getDeviceCount())
                .description(req.getDescription())
                .build();

        license = licenseRepository.save(license);

        historyRepository.save(LicenseHistory.builder()
                .license(license)
                .user(admin)
                .status(LicenseHistoryStatus.CREATED)
                .description("License created")
                .build());

        return new CreateLicenseResponse(license.getId(), license.getCode());
    }


    @Transactional
    public TicketResponse activateLicense(ActivateLicenseRequest req, Long userId) {
        License license = licenseRepository.findByCode(req.getActivationKey())
                .orElseThrow(() -> new IllegalArgumentException("license not found"));
        if (license.isBlocked()) throw new IllegalArgumentException("license is blocked");

        ApplicationUser user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("user not found"));

        if (license.getUser() != null && !license.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("license owned by another user");
        }

        Device device = deviceRepository.findByMacAddress(req.getDeviceMac())
                .orElseGet(() -> deviceRepository.save(Device.builder()
                        .macAddress(req.getDeviceMac())
                        .name(req.getDeviceName())
                        .user(user)
                        .build()));

        if (!device.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("device owned by another user");
        }

        boolean firstActivation = (license.getUser() == null);

        if (firstActivation) {
            OffsetDateTime now = OffsetDateTime.now();
            license.setUser(user);
            license.setFirstActivationDate(now);
            license.setEndingDate(now.plusDays(license.getType().getDefaultDurationInDays()));
            licenseRepository.save(license);
        }

        boolean alreadyLinked = deviceLicenseRepository.existsByLicenseIdAndDeviceId(license.getId(), device.getId());
        if (!alreadyLinked) {
            long used = deviceLicenseRepository.countByLicenseId(license.getId());
            if (used >= license.getDeviceCount()) {
                throw new IllegalStateException("device limit reached");
            }
            deviceLicenseRepository.save(DeviceLicense.builder()
                    .license(license)
                    .device(device)
                    .build());
        }

        historyRepository.save(LicenseHistory.builder()
                .license(license)
                .user(user)
                .status(LicenseHistoryStatus.ACTIVATED)
                .description(firstActivation ? "First activation" : "Activation on additional device")
                .build());

        return buildTicketResponse(license, device);
    }


    public TicketResponse checkLicense(CheckLicenseRequest req, Long userId) {
        Device device = deviceRepository.findByMacAddress(req.getDeviceMac())
                .orElseThrow(() -> new IllegalArgumentException("device not found"));

        if (!device.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("device owned by another user");
        }

        License license = licenseRepository.findActiveByDeviceUserAndProduct(
                req.getDeviceMac(), userId, req.getProductId(), OffsetDateTime.now()
        ).orElseThrow(() -> new IllegalArgumentException("license not found"));

        return buildTicketResponse(license, device);
    }


    @Transactional
    public TicketResponse renewLicense(RenewLicenseRequest req, Long userId) {
        License license = licenseRepository.findByCode(req.getActivationKey())
                .orElseThrow(() -> new IllegalArgumentException("license not found"));

        if (license.getUser() == null || !license.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("license not owned by user");
        }
        if (license.isBlocked()) throw new IllegalArgumentException("license is blocked");
        if (license.getEndingDate() == null) throw new IllegalStateException("license not activated");

        OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime end = license.getEndingDate();

        boolean renewable = end.isBefore(now) || !end.isAfter(now.plusDays(7));
        if (!renewable) throw new IllegalStateException("renew not allowed");

        license.setEndingDate(end.plusDays(license.getType().getDefaultDurationInDays()));
        licenseRepository.save(license);

        ApplicationUser user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("user not found"));

        historyRepository.save(LicenseHistory.builder()
                .license(license)
                .user(user)
                .status(LicenseHistoryStatus.RENEWED)
                .description("License renewed")
                .build());

        Device device = deviceRepository.findByMacAddress(req.getDeviceMac())
                .orElseThrow(() -> new IllegalArgumentException("device not found"));

        if (!device.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("device owned by another user");
        }

        return buildTicketResponse(license, device);
    }



    private TicketResponse buildTicketResponse(License license, Device device) {
        Ticket ticket = Ticket.builder()
                .serverNow(OffsetDateTime.now())
                .ticketTtlSeconds(ticketTtlSeconds)
                .licenseActivationDate(license.getFirstActivationDate())
                .licenseEndingDate(license.getEndingDate())
                .userId(license.getUser() == null ? null : license.getUser().getId())
                .deviceId(device.getId())
                .licenseBlocked(license.isBlocked())
                .build();

        String sig = ticketSigner.sign(ticket);
        return new TicketResponse(ticket, sig);
    }

    private String generateCode() {
        byte[] bytes = new byte[24];
        RND.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}