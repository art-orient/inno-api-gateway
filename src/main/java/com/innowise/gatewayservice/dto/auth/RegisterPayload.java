package com.innowise.gatewayservice.dto.auth;

public record RegisterPayload(
        String username,
        String password
) {}
