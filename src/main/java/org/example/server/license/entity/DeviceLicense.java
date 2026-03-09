package org.example.server.license.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(
        name = "device_license",
        indexes = {
                @Index(name="idx_dl_license", columnList = "license_id"),
                @Index(name="idx_dl_device", columnList = "device_id")
        },
        uniqueConstraints = @UniqueConstraint(columnNames = {"license_id", "device_id"})
)
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class DeviceLicense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name="license_id", nullable = false)
    private License license;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name="device_id", nullable = false)
    private Device device;

    @Column(name="activation_date", nullable = false)
    @Builder.Default
    private OffsetDateTime activationDate = OffsetDateTime.now();
}