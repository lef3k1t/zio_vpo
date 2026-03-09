package org.example.server.license.repo;

import org.example.server.license.entity.DeviceLicense;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeviceLicenseRepository extends JpaRepository<DeviceLicense, Long> {
    long countByLicenseId(Long licenseId);
    boolean existsByLicenseIdAndDeviceId(Long licenseId, Long deviceId);
}