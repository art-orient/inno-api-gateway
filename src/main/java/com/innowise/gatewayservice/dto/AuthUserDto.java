package com.innowise.gatewayservice.dto;

public record AuthUserDto(
        Long id,
        String username,
        String role,
        boolean active
) {}
