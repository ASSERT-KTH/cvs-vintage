/*
 * This upgrade script adds a column to the user preferences table.
 *
 * Created By: Elicia David 
 */

ALTER TABLE SCARAB_USER_PREFERENCE ADD ENTER_ISSUE_REDIRECT int(11) null;
