# Estágio 1: Build da aplicação Java com Maven
FROM maven:3.9.6-eclipse-temurin-17 AS builder
WORKDIR /app

# Copia os arquivos do projeto para cache de dependências
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copia os códigos fontes e realiza a compilação
COPY src ./src
RUN mvn package -DskipTests

# Estágio 2: Imagem final leve para execução
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copia o JAR gerado no estágio anterior
COPY --from=builder /app/target/*.jar app.jar

# Expõe a porta do Spring Boot
EXPOSE 8080

# Variáveis de ambiente padrão
ENV JAVA_OPTS="-Xms256m -Xmx512m"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]