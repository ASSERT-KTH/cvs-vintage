/*
 * This upgrade script moves the DEDUPE column and its contents from
 * the SCARAB_MODULE table to the SCARAB_R_MODULE_ISSUE_TYPE table.
 *
 * Created By: Elicia David
 * $Id: mysql-upgrade-1.0b1-1.0b2-1.sql,v 1.3 2002/01/31 01:33:49 dlr Exp $
 */

alter table SCARAB_R_MODULE_ISSUE_TYPE add column DEDUPE int(1) not null default 1;

insert into SCARAB_R_MODULE_ISSUE_TYPE (dedupe) select DEDUPE from SCARAB_MODULE;

alter table SCARAB_MODULE drop column DEDUPE;
