/*
 * This upgrade script adds an ATTACHMENT_ID column to the SCARAB_ACTIVITY table.
 *
 * Created By: Jon Scott Stevens
 * $Id: mysql-upgrade-1.0b7-1.0b8-7.sql,v 1.3 2002/06/13 22:08:43 jon Exp $
 */

ALTER TABLE SCARAB_ACTIVITY ADD column ATTACHMENT_ID integer null default 0;
