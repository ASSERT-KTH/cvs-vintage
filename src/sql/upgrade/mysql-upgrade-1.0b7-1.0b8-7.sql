/*
 * This upgrade script adds an ATTACHMENT_ID column to the SCARAB_ACTIVITY table.
 *
 * Created By: Jon Scott Stevens
 * $Id: mysql-upgrade-1.0b7-1.0b8-7.sql,v 1.4 2002/06/24 15:38:20 jon Exp $
 */

ALTER TABLE SCARAB_ACTIVITY ADD column ATTACHMENT_ID integer null;

update SCARAB_ACTIVITY set ATTACHMENT_ID=null where ATTACHMENT_ID=0;
