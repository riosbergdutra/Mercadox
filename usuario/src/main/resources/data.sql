-- Criação da tabela 'usuarios'
CREATE TABLE usuarios (
    id_usuario UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nome VARCHAR(255) NOT NULL,
    imagem VARCHAR(2048),
    email VARCHAR(255) NOT NULL UNIQUE,
    senha VARCHAR(255) NOT NULL,
    roles VARCHAR(255)[] NOT NULL,
    data_conta DATE,
);