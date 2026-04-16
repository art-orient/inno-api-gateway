package com.innowise.gatewayservice.dto;

import java.time.LocalDate;

public record RegistrationRequest(
        String username,
        String password,
        String name,
        String surname,
        String email,
        LocalDate birthDate
) {}
