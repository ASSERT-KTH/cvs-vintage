/*
 * This upgrade script adds two columns to the SCARAB_QUERY table.
 *
 * Created By: Jon Scott Stevens
 * $Id: mysql-upgrade-1.0b7-1.0b8-6.sql,v 1.1 2002/05/21 22:12:22 jon Exp $
 */

ALTER TABLE SCARAB_QUERY ADD column HOME_PAGE int(1) null default 0;
ALTER TABLE SCARAB_QUERY ADD column PREFERRED_ORDER integer null default 0;
