/*
 * This upgrade script adds a column to the SCARAB_QUERY table.
 * and modifies the ISSUE_TYPE_ID column to allow nulls.
 *
 * Created By: John McNally
 */

ALTER TABLE SCARAB_QUERY ADD LIST_ID INTEGER null;

ALTER TABLE SCARAB_QUERY CHANGE ISSUE_TYPE_ID ISSUE_TYPE_ID INTEGER NULL;
