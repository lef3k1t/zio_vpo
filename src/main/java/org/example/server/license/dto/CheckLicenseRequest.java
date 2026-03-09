package org.example.server.license.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CheckLicenseRequest {
    @NotBlank private String deviceMac;
    @NotNull private Long productId;
}