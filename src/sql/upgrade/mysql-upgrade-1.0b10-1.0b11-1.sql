/*
 * This script fixes a problem introduced via the 
 * mysql-upgrade-1.0b8-1.0b9-2.sql script.  That script has been 
 * fixed so this is an optional script for users that upgraded from a
 * pre-b9 version to b9 or b10, which contain a bad version of the script.
 * If the alteration is not needed and the script is run it will not
 * harm existing data.
 *
 * Created By: John McNally 
 */

ALTER TABLE SCARAB_MIT_LIST CHANGE NAME NAME VARCHAR(100) NULL;

