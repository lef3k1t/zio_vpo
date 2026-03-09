package org.example.server.license.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.server.user.ApplicationUser;

import java.time.OffsetDateTime;

@Entity
@Table(
        name="license_history",
        indexes = {
                @Index(name="idx_lh_license", columnList = "license_id"),
                @Index(name="idx_lh_user", columnList = "user_id")
        }
)
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class LicenseHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name="license_id", nullable = false)
    private License license;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name="user_id", nullable = false)
    private ApplicationUser user;

    @Enumerated(EnumType.STRING)
    @Column(length = 32, nullable = false)
    private LicenseHistoryStatus status;

    @Column(name="change_date", nullable = false)
    @Builder.Default
    private OffsetDateTime changeDate = OffsetDateTime.now();

    private String description;
}