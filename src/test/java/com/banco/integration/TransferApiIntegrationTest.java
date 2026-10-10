package com.banco.integration;

import com.banco.infraestructure.persistence.dto.TransferRequestDTO;
import com.banco.model.Account;
import com.banco.repository.AccountRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class TransferApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AccountRepository accountRepository;

    private Account sourceAccount;
    private Account destinationAccount;

    @BeforeEach
    void setUp() {
        sourceAccount = new Account("ACC-100", "Ana Silva", new BigDecimal("1000.00"));
        destinationAccount = new Account("ACC-200", "Carlos Souza", new BigDecimal("500.00"));
    }

    @Test
    @DisplayName("GET /api/accounts/{id} - Sucesso: Deve retornar 200 OK e a conta correspondente")
    void getAccount_Integration_Success() throws Exception {
        when(accountRepository.findById("ACC-100")).thenReturn(Optional.of(sourceAccount));

        mockMvc.perform(get("/api/accounts/ACC-100"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON)) // <--- Corrigido para content().contentType(...)
                .andExpect(jsonPath("$.id").value("ACC-100"))
                .andExpect(jsonPath("$.ownerName").value("Ana Silva"))
                .andExpect(jsonPath("$.balance").value(1000.00));

        verify(accountRepository, times(1)).findById("ACC-100");
    }

    @Test
    @DisplayName("GET /api/accounts/{id} - Erro: Deve retornar 404 NOT FOUND quando a conta não existir")
    void getAccount_Integration_NotFound() throws Exception {
        when(accountRepository.findById("ACC-999")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/accounts/ACC-999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Conta com ID ACC-999 não encontrada."));
    }

    @Test
    @DisplayName("POST /api/transfer - Sucesso: Deve executar transferência e retornar TransferResponseDTO")
    void transfer_Integration_Success() throws Exception {
        TransferRequestDTO requestDTO = new TransferRequestDTO(
                "ACC-100",
                "ACC-200",
                new BigDecimal("200.00"),
                "KEY-INTEGRATION-001"
        );

        when(accountRepository.isIdempotencyKeyProcessed("KEY-INTEGRATION-001")).thenReturn(false);
        when(accountRepository.findById("ACC-100")).thenReturn(Optional.of(sourceAccount));
        when(accountRepository.findById("ACC-200")).thenReturn(Optional.of(destinationAccount));

        mockMvc.perform(post("/api/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Transferência realizada com sucesso em tempo real!"))
                .andExpect(jsonPath("$.sourceAccountId").value("ACC-100"))
                .andExpect(jsonPath("$.sourceBalance").value(800.00))
                .andExpect(jsonPath("$.destinationAccountId").value("ACC-200"))
                .andExpect(jsonPath("$.destinationBalance").value(700.00));

        verify(accountRepository, times(1)).save(sourceAccount);
        verify(accountRepository, times(1)).save(destinationAccount);
        verify(accountRepository, times(1)).registerIdempotencyKey("KEY-INTEGRATION-001");
    }

    @Test
    @DisplayName("POST /api/transfer - Erro: Deve retornar 400 BAD REQUEST quando os DTOs falharem na validação")
    void transfer_Integration_ValidationError() throws Exception {
        TransferRequestDTO invalidDTO = new TransferRequestDTO(
                "ACC-100",
                "ACC-200",
                new BigDecimal("0.00"),
                ""
        );

        mockMvc.perform(post("/api/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());

        verify(accountRepository, never()).save(any());
    }

    @Test
    @DisplayName("POST /api/transfer - Erro: Deve retornar 400 BAD REQUEST para chave de idempotência repetida")
    void transfer_Integration_DuplicateIdempotencyKey() throws Exception {
        TransferRequestDTO requestDTO = new TransferRequestDTO(
                "ACC-100",
                "ACC-200",
                new BigDecimal("100.00"),
                "KEY-PROCESSED"
        );

        when(accountRepository.isIdempotencyKeyProcessed("KEY-PROCESSED")).thenReturn(true);

        mockMvc.perform(post("/api/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Esta transferência já foi processada anteriormente."));

        verify(accountRepository, never()).save(any());
    }
}
