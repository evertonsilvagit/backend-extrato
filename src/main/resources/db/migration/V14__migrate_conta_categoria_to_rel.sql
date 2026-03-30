ALTER TABLE conta ADD COLUMN categoria_conta_id BIGINT;

INSERT INTO categoria_conta (nome, user_email)
SELECT DISTINCT c.categoria, c.user_email
FROM conta c
WHERE c.categoria IS NOT NULL
  AND TRIM(c.categoria) <> ''
  AND NOT EXISTS (
      SELECT 1
      FROM categoria_conta cc
      WHERE cc.nome = c.categoria
        AND (
            cc.user_email = c.user_email
            OR (cc.user_email IS NULL AND c.user_email IS NULL)
        )
  );

UPDATE conta c
SET categoria_conta_id = cc.id
FROM categoria_conta cc
WHERE cc.nome = c.categoria
  AND (
      cc.user_email = c.user_email
      OR (cc.user_email IS NULL AND c.user_email IS NULL)
  );

ALTER TABLE conta
    ADD CONSTRAINT fk_conta_categoria_conta
    FOREIGN KEY (categoria_conta_id) REFERENCES categoria_conta(id);

CREATE INDEX idx_conta_categoria_conta_id ON conta(categoria_conta_id);

ALTER TABLE conta ALTER COLUMN categoria_conta_id SET NOT NULL;
ALTER TABLE conta DROP COLUMN categoria;
