/*
 * This upgrade script removes a few tables that were not used by the 
 * application, so there should be no data related changes.
 *
 * Created By: John McNally
 * $Id: mysql-upgrade-1.0b3-1.0b4-1.sql,v 1.2 2002/02/14 00:36:03 jmcnally Exp $
 */

drop table if exists SCARAB_ISSUE_ATTRIBUTE_VOTE;
drop table if exists SCARAB_R_ATTRIBUTE_VALUE_WORD;
drop table if exists SCARAB_WORD;