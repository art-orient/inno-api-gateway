package com.innowise.gatewayservice.dto;

import java.time.LocalDate;

public record UserCreatePayload(
        Long id,
        String name,
        String surname,
        String email,
        Boolean active,
        LocalDate birthDate
) {}
