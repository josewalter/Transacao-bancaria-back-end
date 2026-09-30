package com.banco.infraestructure.persistence;

import com.banco.model.Account;
import com.banco.repository.AccountRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryAccountRepository implements AccountRepository {

    private final Map<String, Account> database = new ConcurrentHashMap<>();
    private final Set<String> idempotencyKeys = ConcurrentHashMap.newKeySet();

    public InMemoryAccountRepository() {
        database.put("ACC-100", new Account("ACC-100", "Ana Silva", new BigDecimal("1000.00")));
        database.put("ACC-200", new Account("ACC-200", "Carlos Souza", new BigDecimal("500.00")));
    }

    @Override
    public Optional<Account> findById(String id) {
        return Optional.ofNullable(database.get(id));
    }

    @Override
    public void save(Account account) {
        database.put(account.getId(), account);
    }

    @Override
    public boolean isIdempotencyKeyProcessed(String key) {
        return idempotencyKeys.contains(key);
    }

    @Override
    public void registerIdempotencyKey(String key) {
        idempotencyKeys.add(key);
    }
}
