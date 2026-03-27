CREATE TABLE divida (
                        id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                        descricao VARCHAR(255) NOT NULL,
                        valor DECIMAL(19,2) NOT NULL,
                        grupo VARCHAR(255) NOT NULL
);