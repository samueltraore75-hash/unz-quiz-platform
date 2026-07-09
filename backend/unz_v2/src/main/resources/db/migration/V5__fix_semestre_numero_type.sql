-- ============================================================================
-- v3.3 — correction de type de colonne : le champ Java Semestre.numero est un
-- Integer, qu'Hibernate valide contre un INT SQL. La V1 avait utilisé TINYINT
-- (optimisation inutile vu qu'il n'y a que 2 valeurs possibles) — corrigé ici.
-- ============================================================================

ALTER TABLE semestres
    MODIFY COLUMN numero INT NOT NULL;
