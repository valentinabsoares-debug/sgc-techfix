# SGC TechFix — Sistema de Gestão Comercial

> Sistema de gestão comercial para assistência técnica, desenvolvido como projeto acadêmico na disciplina de Engenharia de Software — UniCEUB.

---

## Sobre o sistema

O SGC TechFix é uma aplicação desktop com API REST para gerenciar as operações de uma assistência técnica. O sistema permite o cadastro e controle de clientes, equipamentos, produtos/peças, vendas e usuários, com autenticação segura via JWT e interface gráfica em Java Swing.

---

## Tecnologias utilizadas

| Camada | Tecnologia |
|---|---|
| Linguagem | Java 21 |
| Framework backend | Spring Boot 3 |
| Banco de dados | MySQL 8 |
| ORM | Spring Data JPA / Hibernate |
| Autenticação | JWT (JSON Web Token) |
| Interface gráfica | Java Swing |
| Gerenciador de dependências | Maven |

---

## Arquitetura

O projeto segue arquitetura em camadas:


Controller → Service → Repository → Banco de Dados



src/main/java/com/sgctechfix/
├── config/          # Configurações de segurança (Spring Security)
├── controller/      # Endpoints REST (AuthController, ClienteController, ProdutoController)
├── dto/             # Objetos de transferência de dados (Request/Response)
├── exception/       # Exceções personalizadas e handler global
├── model/           # Entidades JPA (Cliente, Produto, Usuario)
├── repository/      # Interfaces Spring Data JPA
├── security/        # Filtro JWT e UserDetailsService
├── service/         # Regras de negócio
└── swing/           # Interface gráfica desktop


---

## Pré-requisitos

- Java 21+
- Maven 3.8+
- MySQL 8+

---

## Como executar

### 1. Clone o repositório

bash
git clone https://github.com/valentinabsoares-debug/sgc-techfix.git
cd sgc-techfix


### 2. Configure o banco de dados

Crie o banco e execute o script de criação:

bash
mysql -u root -p < sql/create_database.sql


### 3. Configure as credenciais

Edite o arquivo src/main/resources/application.properties:

properties
spring.datasource.url=jdbc:mysql://localhost:3306/sgc_techfix
spring.datasource.username=SEU_USUARIO
spring.datasource.password=SUA_SENHA


### 4. Execute o backend

bash
mvn spring-boot:run


A API ficará disponível em: http://localhost:8080

### 5. Execute a interface Swing

Após o backend estar rodando, execute a interface gráfica pela classe principal ou via Maven.

---

## Endpoints da API

### Autenticação

| Método | Endpoint | Descrição |
|---|---|---|
| POST | /auth/login | Login e geração de token JWT |
| POST | /auth/register | Cadastro de novo usuário |

### Clientes

| Método | Endpoint | Descrição |
|---|---|---|
| GET | /clientes | Listar todos os clientes |
| GET | /clientes/{id} | Buscar cliente por ID |
| POST | /clientes | Cadastrar novo cliente |
| PUT | /clientes/{id} | Atualizar cliente |
| DELETE | /clientes/{id} | Remover cliente |

### Produtos

| Método | Endpoint | Descrição |
|---|---|---|
| GET | /produtos | Listar todos os produtos |
| GET | /produtos/{id} | Buscar produto por ID |
| POST | /produtos | Cadastrar novo produto |
| PUT | /produtos/{id} | Atualizar produto |
| DELETE | /produtos/{id} | Remover produto |

> Todos os endpoints (exceto /auth/**) exigem o header: Authorization: Bearer <token>

---

## Banco de dados

O banco possui as seguintes tabelas:

- usuarios — controle de acesso com perfis ADMIN e FUNCIONARIO
- clientes — cadastro de clientes com CPF e e-mail únicos
- equipamentos — equipamentos vinculados a clientes
- produtos — peças e serviços com controle de estoque
- vendas — registro de vendas vinculadas a cliente e usuário
- itens_venda — produtos de cada venda com preço no momento da venda

---

## Equipe

| Nome | GitHub |
|---|---|
| Valentina B. Soares | [@valentinabsoares-debug](https://github.com/valentinabsoares-debug) |
| Giovanna Hamú | [@giovannahamu](https://github.com/giovannahamu) |
| Gabriel Takeuchi | [@Gabrielstak](https://github.com/Gabrielstak) |

---

## Entregas

| Entrega | Descrição | Data |
|---|---|---|
| Entrega 1 | Modelagem e Arquitetura | 02/04 |
| Entrega 2 | Backend e API REST | 07/05 |
| Entrega 3 | Sistema Completo com Swing | 25/06 |
