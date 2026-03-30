CREATE TABLE categoria_conta (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nome VARCHAR(255) NOT NULL,
    user_email VARCHAR(255)
);

CREATE INDEX idx_categoria_conta_user_email ON categoria_conta(user_email);
CREATE UNIQUE INDEX uk_categoria_conta_user_email_nome ON categoria_conta(user_email, nome);
