package com.banco.usecase;


import com.banco.exception.AccountNotFoundException;
import com.banco.exception.TransferException;
import com.banco.model.Account;
import com.banco.repository.AccountRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

public class ProcessTransferUseCase {

    private final AccountRepository accountRepository;

    public ProcessTransferUseCase(AccountRepository accountRepository) {

        this.accountRepository = accountRepository;
    }

    public synchronized void execute(String sourceId, String destId, BigDecimal amount, String idempotencyKey) {
        // Validação de Idempotência
        if (accountRepository.isIdempotencyKeyProcessed(idempotencyKey)) {
            throw new TransferException("Esta transferência já foi processada anteriormente.");
        }

        // Validação de Conta Única
        if (sourceId.equals(destId)) {
            throw new TransferException("Conta de origem e destino devem ser diferentes.");
        }

        Account sourceAccount = accountRepository.findById(sourceId)
                .orElseThrow(() -> new AccountNotFoundException("Conta de origem não encontrada: " + sourceId));

        Account destAccount = accountRepository.findById(destId)
                .orElseThrow(() -> new AccountNotFoundException("Conta de destino não encontrada: " + destId));

        // Execução Atômica no Domínio
        sourceAccount.debit(amount);
        destAccount.credit(amount);

        // Persistência e Idempotência
        accountRepository.save(sourceAccount);
        accountRepository.save(destAccount);
        accountRepository.registerIdempotencyKey(idempotencyKey);
    }
}
