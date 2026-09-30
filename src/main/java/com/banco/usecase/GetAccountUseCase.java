package com.banco.usecase;

import com.banco.exception.AccountNotFoundException;
import com.banco.model.Account;
import com.banco.repository.AccountRepository;
import org.springframework.stereotype.Service;

@Service
public class GetAccountUseCase {

    private final AccountRepository accountRepository;

    public GetAccountUseCase(AccountRepository accountRepository) {

        this.accountRepository = accountRepository;
    }

    public Account execute(String accountId) {
        return accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException("Conta com ID " + accountId + " não encontrada."));
    }
}
