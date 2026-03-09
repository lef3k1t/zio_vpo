package org.example.server.license.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.server.user.ApplicationUser;

@Entity
@Table(name = "device", uniqueConstraints = @UniqueConstraint(columnNames = "mac_address"))
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Device {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="name", nullable = false)
    private String name;

    @Column(name="mac_address", nullable = false)
    private String macAddress;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name="user_id", nullable = false)
    private ApplicationUser user;
}