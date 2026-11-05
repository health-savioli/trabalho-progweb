-- Criação do banco e de todas as tabelas (MySQL 8 ou superior).
-- Pode ser executado mais de uma vez: não apaga nada que já exista.

CREATE DATABASE IF NOT EXISTS progweb
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE progweb;

CREATE TABLE IF NOT EXISTS tipo_user (
    id_tp_user                BIGINT       NOT NULL AUTO_INCREMENT,
    nome                      VARCHAR(255) NOT NULL,
    cadastrar_user            TINYINT(1)   NOT NULL DEFAULT 0,
    editar_user               TINYINT(1)   NOT NULL DEFAULT 0,
    excluir_user              TINYINT(1)   NOT NULL DEFAULT 0,
    visualizar_user           TINYINT(1)   NOT NULL DEFAULT 0,
    cadastrar_paciente        TINYINT(1)   NOT NULL DEFAULT 0,
    editar_paciente           TINYINT(1)   NOT NULL DEFAULT 0,
    excluir_paciente          TINYINT(1)   NOT NULL DEFAULT 0,
    visualizar_paciente       TINYINT(1)   NOT NULL DEFAULT 0,
    cadastrar_classificacao   TINYINT(1)   NOT NULL DEFAULT 0,
    editar_classificacao      TINYINT(1)   NOT NULL DEFAULT 0,
    excluir_classificacao     TINYINT(1)   NOT NULL DEFAULT 0,
    visualizar_classificacao  TINYINT(1)   NOT NULL DEFAULT 0,
    atender_classificacao     TINYINT(1)   NOT NULL DEFAULT 0,
    PRIMARY KEY (id_tp_user),
    UNIQUE KEY uq_tipo_user_nome (nome)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS users (
    id_user     BIGINT       NOT NULL AUTO_INCREMENT,
    fk_tp_user  BIGINT       NOT NULL,
    nome        VARCHAR(255) NOT NULL,
    telefone    VARCHAR(13)  NOT NULL,
    email       VARCHAR(255) NOT NULL,
    cpf         VARCHAR(16)  NOT NULL,
    data_nasc   DATE         NOT NULL,
    cep         VARCHAR(20)  NOT NULL,
    endereco    VARCHAR(255) NOT NULL,
    senha       VARCHAR(255) NOT NULL,
    PRIMARY KEY (id_user),
    UNIQUE KEY uq_users_email (email),
    UNIQUE KEY uq_users_cpf (cpf),
    KEY fk_users_tipo_user (fk_tp_user),
    CONSTRAINT fk_users_tipo_user FOREIGN KEY (fk_tp_user) REFERENCES tipo_user (id_tp_user)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS pacientes (
    id_paciente  BIGINT       NOT NULL AUTO_INCREMENT,
    nome         VARCHAR(255) NOT NULL,
    telefone     VARCHAR(20)  NULL,
    email        VARCHAR(255) NULL,
    cpf          VARCHAR(14)  NOT NULL,
    data_nasc    DATE         NULL,
    cep          VARCHAR(9)   NULL,
    endereco     VARCHAR(255) NULL,
    idade        INT          NULL,
    sexo         VARCHAR(1)   NULL,
    PRIMARY KEY (id_paciente),
    UNIQUE KEY uq_pacientes_cpf (cpf),
    UNIQUE KEY uq_pacientes_email (email),
    CONSTRAINT chk_pacientes_sexo CHECK (sexo IN ('M', 'F'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS afericao (
    id_afericao          BIGINT        NOT NULL AUTO_INCREMENT,
    fk_paciente          BIGINT        NOT NULL,
    fk_user              BIGINT        NOT NULL,
    sessao_id            BIGINT        NULL COMMENT 'id da sessão devolvido pelo integrador',
    atendimento_id       VARCHAR(100)  NOT NULL COMMENT 'atendimento-<guid> enviado ao abrir a sessão',
    status               VARCHAR(30)   NOT NULL,
    temperatura          DECIMAL(4,1)  NULL,
    saturacao_o2         INT           NULL,
    frequencia_cardiaca  INT           NULL,
    criado_em            DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id_afericao),
    UNIQUE KEY uq_afericao_atendimento (atendimento_id),
    UNIQUE KEY uq_afericao_sessao (sessao_id),
    KEY idx_afericao_paciente (fk_paciente),
    KEY idx_afericao_user (fk_user),
    CONSTRAINT fk_afericao_paciente FOREIGN KEY (fk_paciente) REFERENCES pacientes (id_paciente),
    CONSTRAINT fk_afericao_user FOREIGN KEY (fk_user) REFERENCES users (id_user)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS classificacao (
    id_classificacao  BIGINT        NOT NULL AUTO_INCREMENT,
    fk_paciente       BIGINT        NOT NULL,
    fk_user           BIGINT        NULL,
    fk_afericao       BIGINT        NULL,
    risco             VARCHAR(255)  NULL,
    status            VARCHAR(255)  NOT NULL,
    queixa            VARCHAR(500)  NULL,
    criado_em         DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id_classificacao),
    UNIQUE KEY uq_classificacao_afericao (fk_afericao),
    KEY idx_classificacao_paciente (fk_paciente),
    KEY idx_classificacao_user (fk_user),
    CONSTRAINT fk_classificacao_paciente FOREIGN KEY (fk_paciente) REFERENCES pacientes (id_paciente),
    CONSTRAINT fk_classificacao_user FOREIGN KEY (fk_user) REFERENCES users (id_user) ON DELETE SET NULL,
    CONSTRAINT fk_classificacao_afericao FOREIGN KEY (fk_afericao) REFERENCES afericao (id_afericao) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
