/*
 * This upgrade script shortens the name of a long index name, so that
 * it will work for oracle as well as mysql.
 *
 * Created By: John McNally (originally by Matthew Stahl)
 * $Id: mysql-upgrade-1.0b6-1.0b7-2.sql,v 1.1 2002/04/12 00:13:21 jmcnally Exp $
 */

DROP INDEX IX_ISSUE_MODULE_PREFERRED_ORDER ON SCARAB_ATTRIBUTE_GROUP;
CREATE INDEX IX_ISSUE_MODULE_PREF_ORDER ON SCARAB_ATTRIBUTE_GROUP 
       (ISSUE_TYPE_ID, MODULE_ID, PREFERRED_ORDER);

