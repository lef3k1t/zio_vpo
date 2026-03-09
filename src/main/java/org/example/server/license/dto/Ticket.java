package org.example.server.license.dto;

import lombok.Builder;
import lombok.Value;

import java.time.OffsetDateTime;

@Value
@Builder
public class Ticket {
    OffsetDateTime serverNow;
    long ticketTtlSeconds;

    OffsetDateTime licenseActivationDate;
    OffsetDateTime licenseEndingDate;

    Long userId;
    Long deviceId;

    boolean licenseBlocked;
}