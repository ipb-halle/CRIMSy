

\connect lbac 
\connect - lbac
\set LBAC_SCHEMA_VERSION '\'00004\''

BEGIN TRANSACTION;

UPDATE lbac.info SET value=:LBAC_SCHEMA_VERSION WHERE key='DBSchema Version';

CREATE VIEW taxonomy_direct_children AS select taxoid,max(parentid) as parentid from effective_taxonomy group by taxoid;

COMMIT;


