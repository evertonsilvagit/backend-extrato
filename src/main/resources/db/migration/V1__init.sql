-- Tabelas iniciais

CREATE TABLE IF NOT EXISTS entrada (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    nome VARCHAR(255) NOT NULL,
    tipo VARCHAR(50) NOT NULL,
    valor DECIMAL(19,2) NOT NULL,
    taxa_imposto DECIMAL(5,2) NOT NULL
);

CREATE TABLE IF NOT EXISTS entrada_mes (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    entrada_id BIGINT NOT NULL,
    mes INT NOT NULL,
    CONSTRAINT fk_entrada_mes__entrada FOREIGN KEY (entrada_id) REFERENCES entrada(id)
);

CREATE INDEX IF NOT EXISTS idx_entrada_mes__entrada ON entrada_mes(entrada_id);

CREATE TABLE IF NOT EXISTS lancamento (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    data DATE NOT NULL,
    tipo VARCHAR(20) NOT NULL,
    valor DECIMAL(19,2) NOT NULL,
    descricao VARCHAR(500),
    categoria VARCHAR(100),
    conta_id BIGINT NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_lancamento__conta ON lancamento(conta_id);
CREATE INDEX IF NOT EXISTS idx_lancamento__data ON lancamento(data);
