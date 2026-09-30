package com.banco.repository;

import com.banco.model.Account;

import java.util.Optional;

public interface AccountRepository {

    Optional<Account> findById(String id);
    void save(Account account);
    boolean isIdempotencyKeyProcessed(String key);
    void registerIdempotencyKey(String key);
}
