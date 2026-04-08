ALTER TABLE entrada
    ADD COLUMN IF NOT EXISTS categoria_recebimento VARCHAR(64) NOT NULL DEFAULT 'STANDARD';

UPDATE entrada
SET categoria_recebimento = CASE
    WHEN nome ILIKE '% - 15' THEN 'SALARY_ADVANCE'
    WHEN nome ILIKE '% - 30' THEN 'SALARY_SETTLEMENT'
    ELSE COALESCE(NULLIF(categoria_recebimento, ''), 'STANDARD')
END;
