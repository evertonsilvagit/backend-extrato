ALTER TABLE entrada
    ADD COLUMN IF NOT EXISTS dias_recebimento VARCHAR(255);

UPDATE entrada
SET dias_recebimento = CAST(dia_recebimento AS VARCHAR)
WHERE dia_recebimento IS NOT NULL
  AND (dias_recebimento IS NULL OR dias_recebimento = '');
