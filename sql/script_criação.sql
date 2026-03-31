-- ============================================================
--  SGC TechFix — Script de Criação do Banco de Dados
--  Sistema de Gestão Comercial para Assistência Técnica
--  Entrega 1 — Modelagem e Arquitetura
-- ============================================================

CREATE DATABASE IF NOT EXISTS sgc_techfix
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE sgc_techfix;

-- ============================================================
--  TABELA: usuarios
-- ============================================================
CREATE TABLE IF NOT EXISTS usuarios (
    id       BIGINT          NOT NULL AUTO_INCREMENT,
    username VARCHAR(100)    NOT NULL,
    senha    VARCHAR(255)    NOT NULL COMMENT 'Hash BCrypt',
    perfil   ENUM('ADMIN', 'FUNCIONARIO') NOT NULL DEFAULT 'FUNCIONARIO',
    ativo    TINYINT(1)      NOT NULL DEFAULT 1,
    CONSTRAINT pk_usuarios PRIMARY KEY (id),
    CONSTRAINT uq_usuarios_username UNIQUE (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
--  TABELA: clientes
-- ============================================================
CREATE TABLE IF NOT EXISTS clientes (
    id        BIGINT       NOT NULL AUTO_INCREMENT,
    nome      VARCHAR(150) NOT NULL,
    cpf       VARCHAR(14)  NOT NULL COMMENT 'Formato: 000.000.000-00',
    email     VARCHAR(150) NOT NULL,
    telefone  VARCHAR(20),
    endereco  VARCHAR(255),
    CONSTRAINT pk_clientes PRIMARY KEY (id),
    CONSTRAINT uq_clientes_cpf   UNIQUE (cpf),
    CONSTRAINT uq_clientes_email UNIQUE (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
--  TABELA: equipamentos
--  (entidade adicional do contexto de assistência técnica)
-- ============================================================
CREATE TABLE IF NOT EXISTS equipamentos (
    id         BIGINT       NOT NULL AUTO_INCREMENT,
    cliente_id BIGINT       NOT NULL,
    tipo       VARCHAR(80)  NOT NULL COMMENT 'Ex: Notebook, Celular, Desktop',
    marca      VARCHAR(80),
    modelo     VARCHAR(100),
    num_serie  VARCHAR(100),
    observacao TEXT,
    CONSTRAINT pk_equipamentos PRIMARY KEY (id),
    CONSTRAINT fk_equipamentos_cliente
        FOREIGN KEY (cliente_id) REFERENCES clientes(id)
        ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
--  TABELA: produtos
-- ============================================================
CREATE TABLE IF NOT EXISTS produtos (
    id           BIGINT         NOT NULL AUTO_INCREMENT,
    nome         VARCHAR(150)   NOT NULL,
    descricao    TEXT,
    preco        DECIMAL(10, 2) NOT NULL,
    qtd_estoque  INT            NOT NULL DEFAULT 0,
    estoque_min  INT            NOT NULL DEFAULT 5  COMMENT 'Alerta de estoque mínimo',
    ativo        TINYINT(1)     NOT NULL DEFAULT 1,
    CONSTRAINT pk_produtos PRIMARY KEY (id),
    CONSTRAINT chk_produtos_preco   CHECK (preco >= 0),
    CONSTRAINT chk_produtos_estoque CHECK (qtd_estoque >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
--  TABELA: vendas
-- ============================================================
CREATE TABLE IF NOT EXISTS vendas (
    id          BIGINT         NOT NULL AUTO_INCREMENT,
    data        DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    valor_total DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    cliente_id  BIGINT         NOT NULL,
    usuario_id  BIGINT         NOT NULL COMMENT 'Funcionário responsável',
    observacao  TEXT,
    CONSTRAINT pk_vendas PRIMARY KEY (id),
    CONSTRAINT fk_vendas_cliente
        FOREIGN KEY (cliente_id) REFERENCES clientes(id)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_vendas_usuario
        FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
        ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
--  TABELA: itens_venda
-- ============================================================
CREATE TABLE IF NOT EXISTS itens_venda (
    id              BIGINT         NOT NULL AUTO_INCREMENT,
    venda_id        BIGINT         NOT NULL,
    produto_id      BIGINT         NOT NULL,
    quantidade      INT            NOT NULL,
    preco_unitario  DECIMAL(10, 2) NOT NULL COMMENT 'Preço no momento da venda',
    CONSTRAINT pk_itens_venda PRIMARY KEY (id),
    CONSTRAINT fk_itens_venda_venda
        FOREIGN KEY (venda_id) REFERENCES vendas(id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_itens_venda_produto
        FOREIGN KEY (produto_id) REFERENCES produtos(id)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT chk_itens_quantidade CHECK (quantidade > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
--  ÍNDICES DE DESEMPENHO
-- ============================================================
CREATE INDEX idx_clientes_cpf         ON clientes(cpf);
CREATE INDEX idx_clientes_email       ON clientes(email);
CREATE INDEX idx_vendas_data          ON vendas(data);
CREATE INDEX idx_vendas_cliente       ON vendas(cliente_id);
CREATE INDEX idx_vendas_usuario       ON vendas(usuario_id);
CREATE INDEX idx_itens_venda_venda    ON itens_venda(venda_id);
CREATE INDEX idx_itens_venda_produto  ON itens_venda(produto_id);
CREATE INDEX idx_equipamentos_cliente ON equipamentos(cliente_id);

-- ============================================================
--  DADOS INICIAIS (seed)
-- ============================================================

-- Usuário ADMIN padrão
-- Senha: admin123  →  hash BCrypt gerado pela aplicação no primeiro start
INSERT INTO usuarios (username, senha, perfil) VALUES
    ('admin',      '$2a$12$placeholder_admin_hash_aqui',      'ADMIN'),
    ('funcionario','$2a$12$placeholder_func_hash_aqui', 'FUNCIONARIO');

-- Clientes de exemplo
INSERT INTO clientes (nome, cpf, email, telefone, endereco) VALUES
    ('João da Silva',     '123.456.789-00', 'joao@email.com',    '(61) 99999-0001', 'Rua A, 10 - Brasília/DF'),
    ('Maria Oliveira',    '987.654.321-00', 'maria@email.com',   '(61) 99999-0002', 'Av. B, 20 - Brasília/DF'),
    ('Carlos Souza',      '111.222.333-44', 'carlos@email.com',  '(61) 99999-0003', 'Rua C, 30 - Brasília/DF');

-- Produtos/Peças de exemplo
INSERT INTO produtos (nome, descricao, preco, qtd_estoque, estoque_min) VALUES
    ('Pasta Térmica Arctic MX-4', 'Pasta térmica de alta performance para CPUs',  25.00, 20, 5),
    ('SSD 240GB SATA',            'Unidade SSD 240GB para notebooks e desktops',  150.00, 15, 3),
    ('Memória RAM 8GB DDR4',      'Módulo de memória DDR4 2666MHz',               120.00, 10, 3),
    ('Fonte ATX 500W',            'Fonte de alimentação ATX 500W bivolt',         180.00, 8,  2),
    ('Teclado USB ABNT2',         'Teclado USB padrão ABNT2 para desktops',        45.00, 12, 3),
    ('Serviço de Formatação',     'Reinstalação do sistema operacional e drivers',  80.00, 999, 0),
    ('Serviço de Manutenção',     'Limpeza interna, troca de pasta térmica',        60.00, 999, 0);

-- Equipamento de exemplo
INSERT INTO equipamentos (cliente_id, tipo, marca, modelo, num_serie) VALUES
    (1, 'Notebook', 'Dell',    'Inspiron 15',  'DL123456'),
    (2, 'Desktop',  'Positivo','Master D570',  'POS789012'),
    (3, 'Celular',  'Samsung', 'Galaxy A52',   'SM-A52XXXX');

-- ============================================================
--  FIM DO SCRIPT
-- ============================================================
