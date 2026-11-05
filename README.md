# Triagem - Trabalho de Programação Web (UFMS)

Sistema de triagem: cadastro de pacientes, fila de classificação de risco, aferição dos sinais vitais,
atendimento médico e controle de usuários por perfil de acesso.

- **Backend:** Java 21, Spring Boot, Spring Security (JWT), Spring Data JPA e MySQL 8
- **Frontend:** Vue 3, Vite, Pinia, Vue Router, Axios e Bootstrap

```
backend/    API REST (Spring Boot)
frontend/   Interface (Vue)
database/   Scripts SQL: criação do banco e dados iniciais
postman/    Coleção para importar no Postman
docs/       Diagrama do banco e slides da apresentação
.env.example  Modelo das variáveis de ambiente
```

## Requisitos

- Java 21 ou superior (o Maven já vem junto, pelo `mvnw`)
- Node.js 20.19 ou superior (ou 22.12+)
- MySQL 8 ou superior

## Como rodar

**1. Banco de dados**

```bash
mysql -u root -p < database/01_schema.sql
mysql -u root -p < database/02_dados_iniciais.sql
```

Sem MySQL instalado, dá para subir um com Docker:

```bash
docker run -d --name progweb-mysql -e MYSQL_ROOT_PASSWORD=senha -p 3306:3306 mysql:8.4
docker exec -i progweb-mysql mysql -uroot -psenha < database/01_schema.sql
docker exec -i progweb-mysql mysql -uroot -psenha < database/02_dados_iniciais.sql
```

**2. Variáveis de ambiente**

```bash
cp .env.example .env
```

Abra o `.env` e preencha ao menos `DB_PASSWORD` (a senha do MySQL) e `JWT_SECRET`
(por exemplo, a saída de `openssl rand -hex 32`). A aferição só funciona com o `INTEGRADOR_TOKEN` preenchido.

**3. Backend** (porta 8080)

```bash
cd backend
./mvnw spring-boot:run
```

No Windows: `mvnw.cmd spring-boot:run`.

**4. Frontend** (porta 5173), em outro terminal

```bash
cd frontend
npm install
npm run dev
```

Abra http://localhost:5173. O frontend repassa as chamadas `/api` para o backend em `localhost:8080`.

## Rodar tudo com Docker

Sobe o banco, o backend e o frontend de uma vez, sem instalar Java, Node nem MySQL.
Os scripts da pasta `database/` são importados automaticamente na primeira vez.

```bash
cp .env.example .env
docker compose up -d --build
```

Antes, preencha no `.env` o `DB_PASSWORD` (a senha que o MySQL do contêiner vai usar) e o `JWT_SECRET`.
Depois abra http://localhost:8080 (para outra porta, defina `WEB_PORT` no `.env`).

## Usuários para testar

| Perfil | E-mail | Senha |
|---|---|---|
| Admin (acesso total) | admin@progweb.com | `admin123` (ou o `ADMIN_PASSWORD` do `.env`) |
| Recepcionista | recep@progweb.com | `senha123` |
| Profissional técnico | profissional@progweb.com | `senha123` |
| Médico | medico@progweb.com | `senha123` |
| Usuário comum (só consulta) | comum@progweb.com | `senha123` |

O Admin é criado pelo backend na primeira execução. Os demais vêm do `02_dados_iniciais.sql`.
Troque ou apague essas contas antes de usar o sistema de verdade.

## Postman

Importe `postman/progweb.postman_collection.json`. Rode primeiro a requisição **Login**: ela guarda o token
nas variáveis da coleção, e as demais usam esse token. A URL da API fica na variável `base_url`
(`http://localhost:8080/api`).
