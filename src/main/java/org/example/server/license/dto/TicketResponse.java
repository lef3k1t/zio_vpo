package org.example.server.license.dto;

public record TicketResponse(Ticket ticket, String signature) {}