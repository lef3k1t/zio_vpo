package org.example.server.license.security;

import lombok.RequiredArgsConstructor;
import org.example.server.license.dto.Ticket;
import org.example.server.signature.SigningService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TicketSigner {

    private final SigningService signingService;

    public String sign(Ticket ticket) {
        return signingService.sign(ticket);
    }

    public boolean verify(Ticket ticket, String signature) {
        return signingService.verify(ticket, signature);
    }
}