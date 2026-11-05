-- Dados iniciais: perfis de acesso, usuários de demonstração e pacientes de exemplo.
-- Rode depois do 01_schema.sql. Pode ser executado mais de uma vez (ignora o que já existe).
--
-- O administrador (admin@progweb.com) não está aqui: o backend o cria sozinho na primeira
-- execução, com o e-mail e a senha do arquivo .env. Por isso o perfil "Admin" precisa existir.
--
-- Usuários de demonstração: a senha de todos é "senha123". Troque ou apague antes de usar de verdade.

USE progweb;

INSERT IGNORE INTO tipo_user (nome,
    cadastrar_user, editar_user, excluir_user, visualizar_user,
    cadastrar_paciente, editar_paciente, excluir_paciente, visualizar_paciente,
    cadastrar_classificacao, editar_classificacao, excluir_classificacao, visualizar_classificacao,
    atender_classificacao)
VALUES
    ('Admin',                1, 1, 1, 1,  1, 1, 1, 1,  1, 1, 1, 1,  1),
    ('Recepcionista',        0, 0, 0, 0,  1, 1, 0, 1,  0, 0, 0, 0,  0),
    ('Profissional técnico', 0, 0, 0, 0,  0, 0, 0, 1,  1, 1, 1, 1,  0),
    ('Médico',               0, 0, 0, 0,  0, 0, 0, 1,  0, 0, 0, 1,  1),
    ('Usuário comum',        0, 0, 0, 0,  0, 0, 0, 1,  0, 0, 0, 1,  0);

INSERT IGNORE INTO users (fk_tp_user, nome, telefone, email, cpf, data_nasc, cep, endereco, senha)
VALUES
    ((SELECT id_tp_user FROM tipo_user WHERE nome = 'Recepcionista'),
     'Recepcionista Demo', '67999990001', 'recep@progweb.com', '52998224725', '1995-03-12', '79002241',
     'Rua General Mello, Centro, Campo Grande/MS',
     '$2a$10$RrTTm.RCSFY49TG9aiTRaO2BGOg3ht3cr7y4MAH/FogptEOCMpXye'),
    ((SELECT id_tp_user FROM tipo_user WHERE nome = 'Profissional técnico'),
     'Profissional Demo', '67999990002', 'profissional@progweb.com', '11144477735', '1990-07-25', '79002241',
     'Rua General Mello, Centro, Campo Grande/MS',
     '$2a$10$8Kuuqtmsf5XiGMlO8ALdoOfNNQoCWKF1iVMEiuPQn/fQacjHjT8Ju'),
    ((SELECT id_tp_user FROM tipo_user WHERE nome = 'Médico'),
     'Médico Demo', '67999990003', 'medico@progweb.com', '39053344705', '1985-11-02', '79002241',
     'Rua General Mello, Centro, Campo Grande/MS',
     '$2a$10$hv0IESp.e0MC57Qw.E1vqeLcjL6EUeQL5Xqi/hNZqY8xWjbB/eV22'),
    ((SELECT id_tp_user FROM tipo_user WHERE nome = 'Usuário comum'),
     'Usuário Comum Demo', '67999990004', 'comum@progweb.com', '86288366757', '2000-01-20', '79002241',
     'Rua General Mello, Centro, Campo Grande/MS',
     '$2a$10$guJqdvqlkDMC5bsplz8cxeqkVvzEY32gXm7WxSaw9NkZaCq0h6T0q');

INSERT IGNORE INTO pacientes (nome, telefone, email, cpf, data_nasc, cep, endereco, idade, sexo)
VALUES
    ('Maria Souza Lima', '(67) 99111-0001', 'maria.lima@exemplo.com', '123.456.789-09', '1988-05-14', '79002-241',
     'Rua General Mello, Centro, Campo Grande/MS', TIMESTAMPDIFF(YEAR, '1988-05-14', CURDATE()), 'F'),
    ('João Pereira Santos', '(67) 99111-0002', 'joao.santos@exemplo.com', '453.178.287-91', '1975-09-30', '79002-241',
     'Rua General Mello, Centro, Campo Grande/MS', TIMESTAMPDIFF(YEAR, '1975-09-30', CURDATE()), 'M'),
    ('Ana Clara Oliveira', '(67) 99111-0003', 'ana.oliveira@exemplo.com', '935.411.347-80', '2001-12-08', '79002-241',
     'Rua General Mello, Centro, Campo Grande/MS', TIMESTAMPDIFF(YEAR, '2001-12-08', CURDATE()), 'F');
