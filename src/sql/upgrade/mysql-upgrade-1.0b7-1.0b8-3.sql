/*
 * This upgrade script increases the length of 
 * SCARAB_ATTACHMENT.ATTACHMENT_MIME_TYPE column.  
 * 25 is not long enough, don't know what is maximum, so go with 255.
 *
 * Created By: John McNally
 * $Id: mysql-upgrade-1.0b7-1.0b8-3.sql,v 1.1 2002/05/09 02:16:14 jmcnally Exp $ */

alter table SCARAB_ATTACHMENT change ATTACHMENT_MIME_TYPE ATTACHMENT_MIME_TYPE varchar(255) NOT NULL; 
