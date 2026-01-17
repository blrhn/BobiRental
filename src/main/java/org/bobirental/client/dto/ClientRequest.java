package org.bobirental.client.dto;

import jakarta.validation.constraints.NotBlank;

public record ClientRequest(
        @NotBlank
        String name,

        @NotBlank
        String surname,

        @NotBlank
        String clientAddress,

        @NotBlank
        String clientMail) {}
