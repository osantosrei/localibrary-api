## ✅ Pré-requisitos

- Docker 20+ e Docker Compose 2+
- Java 17+ (apenas para desenvolvimento local sem Docker)
- Maven 3.9+ (apenas para desenvolvimento local sem Docker)
- Chave da API do Google Maps (para geocodificação)

## 🚀 Instalação

### Com Docker (Recomendado)
```bash
  # 1. Clonar repositório
  git clone https://github.com/osantosrei/localibrary-api.git
  cd localibrary-api
  
  # 2. Copiar arquivo de ambiente
  cp .env.example .env
  
  # 3. Editar .env com suas credenciais
  nano .env
  
  # 4. Subir containers
  docker-compose up --build
```

A API estará disponível em `http://localhost:8080`

### Sem Docker (Desenvolvimento Local)
```bash
  # 1. Instalar MySQL 8.0 e criar database
  mysql -u root -p
  CREATE DATABASE db_localibrary CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
  
  # 2. Configurar variáveis de ambiente
  export DB_HOST=localhost
  export DB_PORT=3306
  export DB_NAME=db_localibrary
  export DB_USER=root
  export DB_PASS=sua_senha
  export JWT_SECRET=seu_secret_jwt_minimo_32_caracteres
  export GOOGLE_API_KEY=sua_chave_google
  
  # 3. Executar aplicação
  ./mvnw spring-boot:run
```

## ⚙️ Configuração

### Variáveis de Ambiente Obrigatórias

| Variável | Descrição | Exemplo |
  |----------|-----------|---------|
| `MYSQL_ROOT_PASSWORD` | Senha do root MySQL | `root123` |
| `MYSQL_DATABASE` | Nome do banco | `db_localibrary` |
| `MYSQL_USER` | Usuário do banco | `app_user` |
| `MYSQL_PASSWORD` | Senha do usuário | `app_pass` |
| `JWT_SECRET` | Secret para assinatura JWT | `min_32_chars...` |
| `GOOGLE_API_KEY` | Chave Google Maps API | `AIza...` |
| `ALLOWED_ORIGINS` | Origens CORS (produção) | `https://site.com` |

## 🏃 Executando

### Comandos Docker
```bash
  # Subir em background
  docker-compose up -d
  
  # Ver logs
  docker-compose logs -f localibrary-api
  
  # Parar containers
  docker-compose down
  
  # Rebuild após mudanças
  docker-compose up --build
  
  # Limpar volumes (⚠️ apaga dados)
  docker-compose down -v
```

## 📄 API Documentation

Após subir a aplicação, acesse:

- **Swagger UI:** `http://localhost:8080/swagger-ui/index.html`
- **OpenAPI JSON:** `http://localhost:8080/v3/api-docs`

### Autenticação

1. Faça login em `/auth/login`
2. Copie o `accessToken` da resposta
3. No Swagger, clique em "Authorize" e cole: `Bearer {seu_token}`

### Endpoints Principais

| Método | Endpoint | Descrição | Auth |
  |--------|----------|-----------|------|
| POST | `/auth/cadastro` | Cadastrar biblioteca | Não |
| POST | `/auth/login` | Fazer login | Não |
| POST | `/auth/refresh` | Renovar token | Não |
| GET | `/bibliotecas` | Listar bibliotecas ativas | Não |
| GET | `/bibliotecas/{id}` | Detalhes da biblioteca | Não |
| GET | `/livros?titulo=X` | Buscar livros | Não |
| GET | `/livros/{id}/bibliotecas` | Onde encontrar livro | Não |
| GET | `/bibliotecas/{id}/livros` | Acervo da biblioteca | Sim |
| POST | `/bibliotecas/{id}/livros` | Adicionar livro | Sim |
| GET | `/admin/dashboard` | Painel administrativo | Admin |

## 🏗️ Arquitetura
```
  src/main/java/com/localibrary/
  ├── config/              # Configurações Spring
  │   ├── SecurityConfig   # CORS, JWT, autenticação
  │   └── OpenApiConfig    # Swagger
  ├── controller/          # Endpoints REST
  ├── dto/                 # Data Transfer Objects
  ├── entity/              # Entidades JPA
  ├── enums/               # Enumerações
  ├── exception/           # Exceções customizadas
  ├── repository/          # Repositories Spring Data
  ├── security/            # JWT, UserDetails, Filters
  ├── service/             # Lógica de negócio
  └── util/                # Classes utilitárias
```
