/*
 * This upgrade script adds a column to the user preferences table.
 *
 * Created By: John McNally
 */

ALTER TABLE SCARAB_USER_PREFERENCE ADD HOME_PAGE varchar(32) null;
