package com.banco.usecasetest;

import com.banco.exception.AccountNotFoundException;
import com.banco.exception.TransferException;
import com.banco.model.Account;
import com.banco.repository.AccountRepository;
import com.banco.usecase.ProcessTransferUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProcessTransferUseCaseTest {

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private ProcessTransferUseCase processTransferUseCase;

    private Account sourceAccount;
    private Account destAccount;

    @BeforeEach
    void setUp() {
        sourceAccount = new Account("ACC-100", "Ana Silva", new BigDecimal("1000.00"));
        destAccount = new Account("ACC-200", "Carlos Souza", new BigDecimal("500.00"));
    }

    @Test
    @DisplayName("Deve realizar a transferência com sucesso e atualizar saldos e persistência")
    void execute_Success() {
        String key = "KEY-123";
        BigDecimal amount = new BigDecimal("200.00");

        when(accountRepository.isIdempotencyKeyProcessed(key)).thenReturn(false);
        when(accountRepository.findById("ACC-100")).thenReturn(Optional.of(sourceAccount));
        when(accountRepository.findById("ACC-200")).thenReturn(Optional.of(destAccount));

        processTransferUseCase.execute("ACC-100", "ACC-200", amount, key);

        assertEquals(new BigDecimal("800.00"), sourceAccount.getBalance());
        assertEquals(new BigDecimal("700.00"), destAccount.getBalance());

        verify(accountRepository, times(1)).save(sourceAccount);
        verify(accountRepository, times(1)).save(destAccount);
        verify(accountRepository, times(1)).registerIdempotencyKey(key);
    }

    @Test
    @DisplayName("Deve lançar TransferException quando a chave de idempotência já tiver sido processada")
    void execute_WhenIdempotencyKeyAlreadyProcessed_ShouldThrowException() {
        String key = "KEY-PROCESSED";
        when(accountRepository.isIdempotencyKeyProcessed(key)).thenReturn(true);

        TransferException exception = assertThrows(
                TransferException.class,
                () -> processTransferUseCase.execute("ACC-100", "ACC-200", new BigDecimal("100.00"), key)
        );

        assertEquals("Esta transferência já foi processada anteriormente.", exception.getMessage());
        verify(accountRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar TransferException quando as contas de origem e destino forem iguais")
    void execute_WhenSourceAndDestAreSame_ShouldThrowException() {
        String key = "KEY-123";
        when(accountRepository.isIdempotencyKeyProcessed(key)).thenReturn(false);

        TransferException exception = assertThrows(
                TransferException.class,
                () -> processTransferUseCase.execute("ACC-100", "ACC-100", new BigDecimal("100.00"), key)
        );

        assertEquals("Conta de origem e destino devem ser diferentes.", exception.getMessage());
        verify(accountRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar AccountNotFoundException quando a conta de origem não for encontrada")
    void execute_WhenSourceAccountNotFound_ShouldThrowException() {
        String key = "KEY-123";
        when(accountRepository.isIdempotencyKeyProcessed(key)).thenReturn(false);
        when(accountRepository.findById("ACC-100")).thenReturn(Optional.empty());

        AccountNotFoundException exception = assertThrows(
                AccountNotFoundException.class,
                () -> processTransferUseCase.execute("ACC-100", "ACC-200", new BigDecimal("100.00"), key)
        );

        assertTrue(exception.getMessage().contains("Conta de origem não encontrada"));
    }

    @Test
    @DisplayName("Deve lançar AccountNotFoundException quando a conta de destino não for encontrada")
    void execute_WhenDestAccountNotFound_ShouldThrowException() {
        String key = "KEY-123";
        when(accountRepository.isIdempotencyKeyProcessed(key)).thenReturn(false);
        when(accountRepository.findById("ACC-100")).thenReturn(Optional.of(sourceAccount));
        when(accountRepository.findById("ACC-200")).thenReturn(Optional.empty());

        AccountNotFoundException exception = assertThrows(
                AccountNotFoundException.class,
                () -> processTransferUseCase.execute("ACC-100", "ACC-200", new BigDecimal("100.00"), key)
        );

        assertTrue(exception.getMessage().contains("Conta de destino não encontrada"));
    }
}
