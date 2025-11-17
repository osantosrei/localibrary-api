# Estágio 1: Build (Compilação com Maven)
# Usamos a imagem oficial do Maven com JDK 17
FROM maven:3.9-eclipse-temurin-17 AS builder

# Define o diretório de trabalho dentro do contêiner
WORKDIR /app

# Copia os arquivos de definição do Maven
COPY pom.xml .
COPY .mvn .mvn

# Baixa as dependências (para cachear)
RUN mvn dependency:go-offline

# Copia o código-fonte
COPY src src

# Compila o projeto e gera o .jar
RUN mvn package -DskipTests

# Estágio 2: Run (Execução)
# Usamos uma imagem JRE (Java Runtime) otimizada e menor
FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

# Copia o .jar gerado no Estágio 1
COPY --from=builder /app/target/*.jar app.jar

# Expõe a porta que o Spring Boot usa
EXPOSE 8080

# Comando para executar a aplicação
ENTRYPOINT ["java", "-jar", "app.jar"]