package com.banco.servicetest;

import com.banco.model.Account;
import com.banco.model.TransferRequest;
import com.banco.service.TransferService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class TransferServiceTest {

    private TransferService transferService;

    @BeforeEach
    void setUp() {
        transferService = new TransferService();
    }

    @Test
    @DisplayName("GetAccount - Deve retornar a conta inicial cadastrada")
    void getAccount_Success() {
        Account account = transferService.getAccount("ACC-100");
        assertNotNull(account);
        assertEquals("Ana Silva", account.getOwnerName()); // Corrigido para getOwnerName()
    }

    @Test
    @DisplayName("ProcessTransfer - Deve atualizar saldos corretamente")
    void processTransfer_Success() {
        TransferRequest request = createTransferRequest("ACC-100", "ACC-200", new BigDecimal("300.00"), "KEY-1");

        Map<String, Account> updatedAccounts = transferService.processTransfer(request);

        assertEquals(new BigDecimal("700.00"), updatedAccounts.get("ACC-100").getBalance());
        assertEquals(new BigDecimal("800.00"), updatedAccounts.get("ACC-200").getBalance());
    }

    @Test
    @DisplayName("ProcessTransfer - Deve falhar por saldo insuficiente")
    void processTransfer_InsufficientBalance() {
        TransferRequest request = createTransferRequest("ACC-100", "ACC-200", new BigDecimal("5000.00"), "KEY-2");

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> transferService.processTransfer(request)
        );

        assertEquals("Saldo insuficiente para realizar a transferência.", ex.getMessage());
    }

    @Test
    @DisplayName("ProcessTransfer - Deve falhar por chave de idempotência repetida")
    void processTransfer_DuplicateKey() {
        TransferRequest request1 = createTransferRequest("ACC-100", "ACC-200", new BigDecimal("100.00"), "KEY-DUP");
        TransferRequest request2 = createTransferRequest("ACC-100", "ACC-200", new BigDecimal("100.00"), "KEY-DUP");

        transferService.processTransfer(request1);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> transferService.processTransfer(request2)
        );

        assertEquals("Esta transferência já foi processada.", ex.getMessage());
    }

    // Método auxiliar para construir o TransferRequest via setters
    private TransferRequest createTransferRequest(String sourceId, String destId, BigDecimal amount, String idempotencyKey) {
        TransferRequest request = new TransferRequest();
        request.setSourceAccountId(sourceId);
        request.setDestinationAccountId(destId);
        request.setAmount(amount);
        request.setIdempotencyKey(idempotencyKey);
        return request;
    }
}