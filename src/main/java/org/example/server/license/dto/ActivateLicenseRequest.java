package org.example.server.license.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ActivateLicenseRequest {
    @NotBlank private String activationKey;
    @NotBlank private String deviceMac;
    @NotBlank private String deviceName;
}