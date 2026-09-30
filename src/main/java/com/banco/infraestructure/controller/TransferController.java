package com.banco.infraestructure.controller;

import com.banco.infraestructure.persistence.dto.TransferRequestDTO;
import com.banco.infraestructure.persistence.dto.TransferResponseDTO;
import com.banco.model.Account;
import com.banco.usecase.GetAccountUseCase;
import com.banco.usecase.ProcessTransferUseCase;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class TransferController {

    private final ProcessTransferUseCase processTransferUseCase;
    private final GetAccountUseCase getAccountUseCase;

    // Injeção direta dos UseCases via construtor
    public TransferController(ProcessTransferUseCase processTransferUseCase, GetAccountUseCase getAccountUseCase) {
        this.processTransferUseCase = processTransferUseCase;
        this.getAccountUseCase = getAccountUseCase;
    }

    @GetMapping("/accounts/{id}")
    public ResponseEntity<Account> getAccount(@PathVariable String id) {
        Account account = getAccountUseCase.execute(id);
        return ResponseEntity.ok(account);
    }

    @PostMapping("/transfer")
    public ResponseEntity<TransferResponseDTO> transfer(@Valid @RequestBody TransferRequestDTO dto) {
        processTransferUseCase.execute(
                dto.sourceAccountId(),
                dto.destinationAccountId(),
                dto.amount(),
                dto.idempotencyKey()
        );

        Account source = getAccountUseCase.execute(dto.sourceAccountId());
        Account dest = getAccountUseCase.execute(dto.destinationAccountId());

        TransferResponseDTO response = new TransferResponseDTO(
                "Transferência realizada com sucesso em tempo real!",
                source.getId(), source.getBalance(),
                dest.getId(), dest.getBalance()
        );

        return ResponseEntity.ok(response);
    }
}