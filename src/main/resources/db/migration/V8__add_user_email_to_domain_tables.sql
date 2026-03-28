ALTER TABLE conta ADD COLUMN user_email VARCHAR(255);
ALTER TABLE entrada ADD COLUMN user_email VARCHAR(255);
ALTER TABLE divida ADD COLUMN user_email VARCHAR(255);
ALTER TABLE lancamento ADD COLUMN user_email VARCHAR(255);

CREATE INDEX idx_conta_user_email ON conta(user_email);
CREATE INDEX idx_entrada_user_email ON entrada(user_email);
CREATE INDEX idx_divida_user_email ON divida(user_email);
CREATE INDEX idx_lancamento_user_email ON lancamento(user_email);
