/*
 * This upgrade script renames the combo-box attribute type name to be
 * Dropdown list.
 *
 * Created By: Jon Scott Stevens
 * $Id: mysql-upgrade-1.0b6-1.0b7-1.sql,v 1.1 2002/04/09 23:42:19 jon Exp $
 */

update SCARAB_ATTRIBUTE_TYPE 
    set ATTRIBUTE_TYPE_NAME='Dropdown list'
    where ATTRIBUTE_TYPE_NAME='combo-box';
