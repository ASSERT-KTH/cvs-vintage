/*
 * This upgrade script removes the SCARAB_ISSUE_ATTRIBUTE_VOTE table.
 * The table was not used by the application, so there should be no
 * data related changes.
 *
 * Created By: John McNally
 * $Id: mysql-upgrade-1.0b3-1.0b4-1.sql,v 1.1 2002/02/13 23:27:01 jmcnally Exp $
 */

drop table if exists SCARAB_ISSUE_ATTRIBUTE_VOTE;

