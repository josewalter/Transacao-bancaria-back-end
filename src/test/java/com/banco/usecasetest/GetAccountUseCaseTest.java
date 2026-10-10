package com.banco.usecasetest;

import com.banco.exception.AccountNotFoundException;
import com.banco.model.Account;
import com.banco.repository.AccountRepository;
import com.banco.usecase.GetAccountUseCase;
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
public class GetAccountUseCaseTest {

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private GetAccountUseCase getAccountUseCase;

    private Account mockAccount;

    @BeforeEach
    void setUp() {
        mockAccount = new Account("ACC-100", "Ana Silva", new BigDecimal("1000.00"));
    }

    @Test
    @DisplayName("Deve retornar a conta quando um ID válido for informado")
    void execute_WhenAccountExists_ShouldReturnAccount() {
        when(accountRepository.findById("ACC-100")).thenReturn(Optional.of(mockAccount));

        Account result = getAccountUseCase.execute("ACC-100");

        assertNotNull(result);
        assertEquals("ACC-100", result.getId());
        assertEquals("Ana Silva", result.getOwnerName());
        verify(accountRepository, times(1)).findById("ACC-100");
    }

    @Test
    @DisplayName("Deve lançar AccountNotFoundException quando a conta não existir")
    void execute_WhenAccountDoesNotExist_ShouldThrowAccountNotFoundException() {
        when(accountRepository.findById("ACC-999")).thenReturn(Optional.empty());

        AccountNotFoundException exception = assertThrows(
                AccountNotFoundException.class,
                () -> getAccountUseCase.execute("ACC-999")
        );

        assertTrue(exception.getMessage().contains("Conta com ID ACC-999 não encontrada"));
        verify(accountRepository, times(1)).findById("ACC-999");
    }
}
