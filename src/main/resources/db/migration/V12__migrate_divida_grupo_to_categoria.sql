ALTER TABLE divida ADD COLUMN categoria_divida_id BIGINT;

INSERT INTO categoria_divida (nome, user_email)
SELECT DISTINCT d.grupo, d.user_email
FROM divida d
WHERE d.grupo IS NOT NULL
  AND TRIM(d.grupo) <> ''
  AND NOT EXISTS (
      SELECT 1
      FROM categoria_divida c
      WHERE c.nome = d.grupo
        AND (
            c.user_email = d.user_email
            OR (c.user_email IS NULL AND d.user_email IS NULL)
        )
  );

UPDATE divida d
SET categoria_divida_id = c.id
FROM categoria_divida c
WHERE c.nome = d.grupo
  AND (
      c.user_email = d.user_email
      OR (c.user_email IS NULL AND d.user_email IS NULL)
  );

ALTER TABLE divida
    ADD CONSTRAINT fk_divida_categoria_divida
    FOREIGN KEY (categoria_divida_id) REFERENCES categoria_divida(id);

CREATE INDEX idx_divida_categoria_divida_id ON divida(categoria_divida_id);

ALTER TABLE divida ALTER COLUMN categoria_divida_id SET NOT NULL;
ALTER TABLE divida DROP COLUMN grupo;
