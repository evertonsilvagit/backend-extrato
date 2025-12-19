CREATE TABLE IF NOT EXISTS conta (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    descricao VARCHAR(255) NOT NULL,
    valor DECIMAL(19,2) NOT NULL,
    dia_pagamento INT NOT NULL,
    meses_vigencia VARCHAR(255)
);
