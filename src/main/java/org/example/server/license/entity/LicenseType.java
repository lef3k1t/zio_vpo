package org.example.server.license.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "license_type", uniqueConstraints = @UniqueConstraint(columnNames = "name"))
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class LicenseType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "default_duration_in_days", nullable = false)
    private int defaultDurationInDays;

    private String description;
}