package org.example.server.license.repo;

import org.example.server.license.entity.License;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.OffsetDateTime;
import java.util.Optional;

public interface LicenseRepository extends JpaRepository<License, Long> {

    Optional<License> findByCode(String code);

    @Query("""
        select l from License l
          join DeviceLicense dl on dl.license = l
          join Device d on dl.device = d
        where d.macAddress = :deviceMac
          and l.user.id = :userId
          and l.product.id = :productId
          and l.blocked = false
          and l.endingDate is not null
          and l.endingDate >= :now
        """)
    Optional<License> findActiveByDeviceUserAndProduct(String deviceMac, Long userId, Long productId, OffsetDateTime now);
}