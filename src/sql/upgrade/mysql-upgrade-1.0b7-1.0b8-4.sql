/*
 * This script adds a scope_id foreign key to the saved records table.  It also
 * renames the global scope to module.
 *
 * Created By: John McNally 
 * $Id: mysql-upgrade-1.0b7-1.0b8-4.sql,v 1.1 2002/05/18 16:47:49 jmcnally Exp $
 */

alter table SCARAB_REPORT add SCOPE_ID INTEGER(11); 

alter table SCARAB_REPORT add FOREIGN KEY (SCOPE_ID) REFERENCES SCARAB_SCOPE (SCOPE_ID); 

update SCARAB_SCOPE set SCOPE_NAME='module' where SCOPE_NAME='global';