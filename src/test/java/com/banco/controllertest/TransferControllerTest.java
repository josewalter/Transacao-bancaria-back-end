package com.banco.controllertest;

import com.banco.exception.AccountNotFoundException;
import com.banco.exception.TransferException;
import com.banco.infraestructure.controller.TransferController;
import com.banco.infraestructure.persistence.dto.TransferRequestDTO;
import com.banco.model.Account;
import com.banco.usecase.GetAccountUseCase;
import com.banco.usecase.ProcessTransferUseCase;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TransferController.class)
public class TransferControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProcessTransferUseCase processTransferUseCase;

    @MockBean
    private GetAccountUseCase getAccountUseCase;

    @Test
    @DisplayName("GET /api/accounts/{id} - Deve retornar 200 OK com os dados da conta")
    void getAccount_Success() throws Exception {
        Account account = new Account("ACC-100", "Ana Silva", new BigDecimal("1000.00"));
        when(getAccountUseCase.execute("ACC-100")).thenReturn(account);

        mockMvc.perform(get("/api/accounts/ACC-100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("ACC-100"))
                .andExpect(jsonPath("$.ownerName").value("Ana Silva")) // <--- Altere de $.owner para $.ownerName
                .andExpect(jsonPath("$.balance").value(1000.00));
    }

    @Test
    @DisplayName("GET /api/accounts/{id} - Deve retornar 404 NOT FOUND quando conta não existir")
    void getAccount_NotFound() throws Exception {
        when(getAccountUseCase.execute("ACC-999"))
                .thenThrow(new AccountNotFoundException("Conta com ID ACC-999 não encontrada."));

        mockMvc.perform(get("/api/accounts/ACC-999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Conta com ID ACC-999 não encontrada."));
    }

    @Test
    @DisplayName("POST /api/transfer - Deve executar transferência e retornar 200 OK")
    void transfer_Success() throws Exception {
        TransferRequestDTO requestDTO = new TransferRequestDTO(
                "ACC-100",
                "ACC-200",
                new BigDecimal("200.00"),
                "KEY-123"
        );

        Account source = new Account("ACC-100", "Ana Silva", new BigDecimal("800.00"));
        Account dest = new Account("ACC-200", "Carlos Souza", new BigDecimal("700.00"));

        doNothing().when(processTransferUseCase).execute(any(), any(), any(), any());
        when(getAccountUseCase.execute("ACC-100")).thenReturn(source);
        when(getAccountUseCase.execute("ACC-200")).thenReturn(dest);

        mockMvc.perform(post("/api/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Transferência realizada com sucesso em tempo real!"))
                .andExpect(jsonPath("$.sourceAccountId").value("ACC-100"))
                .andExpect(jsonPath("$.sourceBalance").value(800.00))
                .andExpect(jsonPath("$.destinationAccountId").value("ACC-200"))
                .andExpect(jsonPath("$.destinationBalance").value(700.00));
    }

    @Test
    @DisplayName("POST /api/transfer - Deve retornar 400 BAD REQUEST quando o UseCase lançar TransferException")
    void transfer_TransferException() throws Exception {
        TransferRequestDTO requestDTO = new TransferRequestDTO(
                "ACC-100",
                "ACC-100",
                new BigDecimal("200.00"),
                "KEY-123"
        );

        doThrow(new TransferException("Conta de origem e destino devem ser diferentes."))
                .when(processTransferUseCase).execute(any(), any(), any(), any());

        mockMvc.perform(post("/api/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Conta de origem e destino devem ser diferentes."));
    }
}
