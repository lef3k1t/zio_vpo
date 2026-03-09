package org.example.server.license.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.server.user.ApplicationUser;

import java.time.OffsetDateTime;

@Entity
@Table(
        name = "license",
        indexes = {
                @Index(name="idx_license_user", columnList = "user_id"),
                @Index(name="idx_license_owner", columnList = "owner_id")
        },
        uniqueConstraints = @UniqueConstraint(name="idx_license_code", columnNames = "code")
)
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class License {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 64, nullable = false)
    private String code;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name="product_id", nullable = false)
    private Product product;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name="type_id", nullable = false)
    private LicenseType type;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name="owner_id", nullable = false)
    private ApplicationUser owner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="user_id")
    private ApplicationUser user;

    @Column(name="first_activation_date")
    private OffsetDateTime firstActivationDate;

    @Column(name="ending_date")
    private OffsetDateTime endingDate;

    @Column(nullable = false)
    private boolean blocked;

    @Column(name="device_count", nullable = false)
    private int deviceCount;

    private String description;
}