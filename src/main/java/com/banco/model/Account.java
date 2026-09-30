package com.banco.model;

import com.banco.exception.InsufficientBalanceException;

import java.math.BigDecimal;
import java.util.Objects;

public class Account {

    private final String id;

    private final String ownerName;

    private BigDecimal balance;

    public Account(String id, String ownerName, BigDecimal balance) {
        if (id == null || id.isBlank()) throw new IllegalArgumentException("ID de conta inválido.");
        if (balance == null || balance.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Saldo inicial não pode ser negativo.");
        }
        this.id = id;
        this.ownerName = ownerName;
        this.balance = balance;
    }

    // Encapsulamento da Regra de Negócio: Débito
    public synchronized void debit(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("O valor do débito deve ser maior que zero.");
        }
        if (this.balance.compareTo(amount) < 0) {
            throw new InsufficientBalanceException("Saldo insuficiente na conta " + id);
        }
        this.balance = this.balance.subtract(amount);
    }

    // Encapsulamento da Regra de Negócio: Crédito
    public synchronized void credit(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("O valor do crédito deve ser maior que zero.");
        }
        this.balance = this.balance.add(amount);
    }

    public String getId() { return id; }
    public String getOwnerName() { return ownerName; }
    public BigDecimal getBalance() { return balance; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Account account = (Account) o;
        return Objects.equals(id, account.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}

