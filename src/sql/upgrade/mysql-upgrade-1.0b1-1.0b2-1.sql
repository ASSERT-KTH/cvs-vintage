/*
 * This upgrade script moves the DEDUPE column and its contents from
 * the SCARAB_MODULE table to the SCARAB_R_MODULE_ISSUE_TYPE table.
 *
 * Created By: Elicia David
 * $Id: mysql-upgrade-1.0b1-1.0b2-1.sql,v 1.2 2002/01/31 01:08:24 jon Exp $
 */

ALTER TABLE SCARAB_R_MODULE_ISSUE_TYPE add column DEDUPE int(1) not null default 1;

INSERT INTO SCARAB_R_MODULE_ISSUE_TYPE (dedupe) SELECT DEDUPE from SCARAB_MODULE;

ALTER TABLE SCARAB_MODULE drop DEDUPE;
