package org.example.server.license.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "product", uniqueConstraints = @UniqueConstraint(columnNames = "name"))
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "is_blocked", nullable = false)
    private boolean blocked;
}