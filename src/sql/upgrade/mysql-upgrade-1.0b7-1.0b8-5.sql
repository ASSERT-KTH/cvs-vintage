/*
 * Adds a column to store a module specific archive email address.
 * 
 * Created By: John McNally 
 * $Id: mysql-upgrade-1.0b7-1.0b8-5.sql,v 1.1 2002/05/18 16:50:48 jmcnally Exp $
 */

alter table SCARAB_MODULE add ARCHIVE_EMAIL VARCHAR(99);