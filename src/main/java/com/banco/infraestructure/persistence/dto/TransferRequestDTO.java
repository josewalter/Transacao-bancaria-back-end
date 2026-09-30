package com.banco.infraestructure.persistence.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record TransferRequestDTO(
        @NotBlank(message = "A conta de origem é obrigatória.")
        String sourceAccountId,

        @NotBlank(message = "A conta de destino é obrigatória.")
        String destinationAccountId,

        @NotNull(message = "O valor é obrigatório.")
        @DecimalMin(value = "0.01", message = "O valor mínimo de transferência é R$ 0,01.")
        BigDecimal amount,

        @NotBlank(message = "A chave de idempotência é obrigatória.")
        String idempotencyKey
) {}
