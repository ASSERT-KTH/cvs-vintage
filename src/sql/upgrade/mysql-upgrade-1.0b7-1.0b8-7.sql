/*
 * Create a mapping for the global module and root issue type.
 *
 * Created By: Jon Scott Stevens
 * $Id: mysql-upgrade-1.0b7-1.0b8-7.sql,v 1.1 2002/06/04 20:46:26 jon Exp $
 */

insert into SCARAB_R_MODULE_ISSUE_TYPE (MODULE_ID, ISSUE_TYPE_ID, ACTIVE, DISPLAY) 
    VALUES (0, 0, 1, 1);
