# Bank Transfer.

## Project Structure

```text
banco/
├── src/main/java/com/banco/
│   ├── exception/
│   │   ├── AccountNotFoundException.java
│   │   ├── InsufficientBalanceException.java
│   │   └── TransferException.java
│   ├── model/
│   │   ├── Account.java
│   │   └── TransferRequest.java
│   ├── repository/
│   │   ├── AccountRepository.java          <- [NOVO] Interface do repositório
│   │   └── InMemoryAccountRepository.java  <- [NOVO] Implementação com Lock Ordering
│   ├── service/
│   │   └── RealtimeNotificationService.java<- [NOVO] Notificação em tempo real via SSE
│   ├── usecase/
│   │   ├── GetAccountUseCase.java
│   │   └── ProcessTransferUseCase.java    <- [AJUSTADO] Anotado com @Service + SSE
│   └── infrastructure/
│       ├── controller/
│       │   ├── GlobalExceptionHandler.java
│       │   └── TransferController.java     <- [EXPANDIDO] Inclui SSE /events/{accountId}
│       └── persistence/dto/
│           ├── TransferRequestDTO.java
│           └── TransferResponseDTO.java
└── frontend/                               <- [NOVO] Interface React completa
    ├── src/
    │   ├── components/
    │   │   ├── AccountCard.tsx
    │   │   ├── TransferForm.tsx
    │   │   └── StressTestPanel.tsx
    │   ├── hooks/
    │   │   └── useRealtimeAccount.ts
    │   └── App.tsx
    └── package.json
```
