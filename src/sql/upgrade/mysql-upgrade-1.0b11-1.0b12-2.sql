/*
 * Add columns to ISSUE_TYPE
 *
 * $Id: mysql-upgrade-1.0b11-1.0b12-2.sql,v 1.2 2002/09/25 23:57:17 jon Exp $
 *
 * Created By: Elicia David
 */

ALTER TABLE SCARAB_ISSUE_TYPE ADD ISDEFAULT INTEGER (1) default 0;
