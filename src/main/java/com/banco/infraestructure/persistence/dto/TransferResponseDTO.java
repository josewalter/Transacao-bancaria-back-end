package com.banco.infraestructure.persistence.dto;

import java.math.BigDecimal;

public record TransferResponseDTO(
        String message,
        String sourceAccountId,
        BigDecimal sourceBalance,
        String destinationAccountId,
        BigDecimal destinationBalance
) {}
