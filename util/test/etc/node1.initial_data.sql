/*
 * initial data for integration testing
 */
\set INTEGRATION_PROJECT_NAME '\'integration\''
\set INTEGRATION_PROJECT_TYPE 5
\set INTEGRATION_OWNER 5
\set INTEGRATION_ACL 5

INSERT INTO projects (name, projecttypeid, owner_id, aclist_id, description) 
  VALUES (:INTEGRATION_PROJECT_NAME, :INTEGRATION_PROJECT_TYPE, :INTEGRATION_OWNER,  
  :INTEGRATION_ACL, 'test project for integration testing');
INSERT INTO projecttemplates (materialdetailtypeid, aclistid, projectid) 
  SELECT mt.id AS materialdetailtypeid, :INTEGRATION_ACL AS aclistid, p.id AS projectid FROM materialdetailtypes AS mt 
  CROSS JOIN projects AS p WHERE p.name= :INTEGRATION_PROJECT_NAME ;
