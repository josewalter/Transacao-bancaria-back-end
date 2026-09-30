package com.banco.infraestructure.config;

import com.banco.repository.AccountRepository;
import com.banco.usecase.GetAccountUseCase;
import com.banco.usecase.ProcessTransferUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UseCaseConfig {

    @Bean
    public ProcessTransferUseCase processTransferUseCase(AccountRepository accountRepository) {
        return new ProcessTransferUseCase(accountRepository);
    }
}
