ALTER TABLE entrada ADD COLUMN ordem INT;

UPDATE entrada
SET ordem = id
WHERE ordem IS NULL;

ALTER TABLE entrada ALTER COLUMN ordem SET NOT NULL;
