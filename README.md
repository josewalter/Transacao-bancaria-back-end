# Bank Transfer.

## Project Structure

```text
banco/
├── Dockerfile                              <- Dockerfile do Back-end Spring Boot
├── docker-compose.yml                      <- Docker Compose principal da aplicação
├── mysql-docker-compose.yml                <- Docker Compose para o container MySQL 8.0.39
├── pom.xml
├── src/main/java/com/banco/
│   ├── exception/
│   │   ├── AccountNotFoundException.java
│   │   ├── InsufficientBalanceException.java
│   │   └── TransferException.java
│   ├── model/
│   │   ├── Account.java
│   │   └── TransferRequest.java
│   ├── repository/
│   │   ├── AccountRepository.java          <- Interface do repositório
│   │   └── InMemoryAccountRepository.java  <- Implementação com Lock Ordering
│   ├── service/
│   │   └── RealtimeNotificationService.java<- Notificação em tempo real via SSE
│   ├── usecase/
│   │   ├── GetAccountUseCase.java
│   │   └── ProcessTransferUseCase.java    <- Anotado com @Service + SSE
│   └── infrastructure/
│       ├── controller/
│       │   ├── GlobalExceptionHandler.java
│       │   └── TransferController.java     <- Inclui SSE /events/{accountId}
│       └── persistence/dto/
│           ├── TransferRequestDTO.java
│           └── TransferResponseDTO.java
└── frontend/                               <- Interface React completa
    ├── Dockerfile                          <- Dockerfile do Front-end React
    ├── docker-compose.yml                  <- Docker Compose isolado do Front-end
    ├── nginx.conf                          <- Configuração de proxy/servidor Nginx
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

## 🚀 Step-by-Step Installation and Execution Guide

### Step 1: Local Prerequisites
To run the API locally, clone the repository. You will need **Java 17**, **Maven 3.8+**, and **MySQL** installed locally or running inside a Docker container.
- *Note on MySQL:* Install version **8.0.39** on your machine following the official Oracle documentation or YouTube tutorials, or use the container configured in Step 7.

---

### Step 2: MySQL Database Setup
Create the MySQL database using your preferred client (MySQL Workbench, DBeaver, phpMyAdmin):
```sql
CREATE DATABASE IF NOT EXISTS banco_db;
```
*Validated MySQL version:* `8.0.39`.

---

### Step 3: Execution via IDE
Launch the Spring Boot project using your preferred IDE: **IntelliJ IDEA**, **Eclipse** ou **VS Code**.
1. Open the project root directory `banco/`.
2. Wait for Maven files to synchronize.
3. Run the Spring Boot main class (`com.banco.BancoApplication`).

---

### Step 4: Framework and Entity Mapping
The project uses **Spring Boot 3.4.0**, which automatically generates the database schema/tables and populates the initial account data for integration testing.

---

### Step 5: Maven Project Build
If your IDE does not download dependencies automatically:
- Right-click the root `pom.xml` file.
- Navigate to the **Maven** menu and click **Reload Project** (or run `mvn clean install` in your terminal).

---

### Step 6: API Testing Endpoints
Below are the available endpoints for manual testing via Postman, Insomnia, or cURL:

* **GET** `http://localhost:8080/api/accounts` — List all accounts
* **GET** `http://localhost:8080/api/accounts/{id}` — Fetch account by ID
* **POST** `http://localhost:8080/api/transfer` — Process idempotent transfer
    * *Body JSON:*
      ```json
      {
        "sourceAccountId": "ACC-100",
        "destinationAccountId": "ACC-200",
        "amount": 150.00,
        "idempotencyKey": "TX-998231-UUID"
      }
      ```
* **GET** `http://localhost:8080/api/events/{accountId}` — SSE endpoint for real-time balance updates

---

### Step 7: Execution via Docker & Containers

#### 7.1 Compose File and Database Port
Run the following command in the directory containing `mysql-docker-compose.yml`:
```bash
docker-compose -f mysql-docker-compose.yml up -d
```
> **Port Notice:** The MySQL container is mapped to port `3307` to avoid conflicts with local MySQL instances running on default port `3306`. If you do not have a local MySQL instance running, you can map it back to `3306`.

#### 7.2 Configuring `application.properties`
Adjust the connection string in `src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:mysql://localhost:3307/banco_db?useSSL=false&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=12345
spring.jpa.hibernate.ddl-auto=update
```

#### 7.3 Building the API Docker Image
To build the image and spin up containers manually on the same Docker network:

1. **Build the Backend Image:**
   ```bash
   docker image build -t transferenciabancaria .
   ```

2. **Start MySQL Container on the Network:**
   ```bash
   docker container run -d -p 3307:3307 -e MYSQL_ROOT_PASSWORD=12345 --network transferenciabancaria-network --name transferenciabancaria-mysql mysql:8.0.39
   ```

3. **Start Backend API Container:**
   ```bash
   docker container run -d -p 8080:8080 -e DB_HOST=transferenciabancaria-mysql --network transferenciabancaria-network transferenciabancaria
   ```

4. **Start Frontend Container on the Network:**
   ```bash
   docker container run -d -p 4200:4200 --network transferenciabancaria-network angular-transferenciabancaria
   ```

---

## 🔒 Concurrency and Deadlock Prevention

To ensure financial safety without the risk of **Double Spending** or **Deadlocks** under high concurrent load, `ProcessTransferUseCase` enforces **Lock Ordering**:

```java
// Atomic deadlock prevention via deterministic ID ordering
Account firstLock = sourceId.compareTo(destId) < 0 ? sourceAccount : destAccount;
Account secondLock = sourceId.compareTo(destId) < 0 ? destAccount : sourceAccount;

synchronized (firstLock) {
    synchronized (secondLock) {
        sourceAccount.debit(amount);
        destAccount.credit(amount);
        accountRepository.save(sourceAccount);
        accountRepository.save(destAccount);
    }
}
```

