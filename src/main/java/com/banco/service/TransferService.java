package com.banco.service;

import com.banco.model.Account;
import com.banco.model.TransferRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TransferService {

    // Simulação de banco de dados em memória thread-safe
    private final Map<String, Account> accounts = new ConcurrentHashMap<>();

    // Controle de idempotência para evitar cobranças duplicadas (Retry/Concorrência)
    private final Set<String> processedKeys = ConcurrentHashMap.newKeySet();

    public TransferService() {
        accounts.put("ACC-100", new Account("ACC-100", "Ana Silva", new BigDecimal("1000.00")));
        accounts.put("ACC-200", new Account("ACC-200", "Carlos Souza", new BigDecimal("500.00")));
    }

    public Account getAccount(String id) {
        return accounts.get(id);
    }

    public synchronized Map<String, Account> processTransfer(TransferRequest request) {
        // Validation 1: Anti-duplicação (Idempotência)
        if (processedKeys.contains(request.getIdempotencyKey())) {
            throw new IllegalArgumentException("Esta transferência já foi processada.");
        }

        Account source = accounts.get(request.getSourceAccountId());
        Account destination = accounts.get(request.getDestinationAccountId());

        // Validation 2: Contas existentes
        if (source == null || destination == null) {
            throw new IllegalArgumentException("Conta de origem ou destino inválida.");
        }

        // Validation 3: Mesma conta
        if (source.getId().equals(destination.getId())) {
            throw new IllegalArgumentException("A conta de origem e destino devem ser diferentes.");
        }

        // Validation 4: Saldo suficiente
        if (source.getBalance().compareTo(request.getAmount()) < 0) {
            throw new IllegalArgumentException("Saldo insuficiente para realizar a transferência.");
        }

        // Operação Atômica de Atualização de Saldo usando os métodos da Entidade
        source.debit(request.getAmount());
        destination.credit(request.getAmount());

        // Registra a chave processada
        processedKeys.add(request.getIdempotencyKey());

        return accounts;
    }
}