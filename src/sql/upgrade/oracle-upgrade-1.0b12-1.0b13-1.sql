/*
 * changes the DATA column of SCARAB_ATTACHMENT to be a text type
 * so that non ascii Strings will be encoded properly when stored.
 * This column was originally binary with the presumption that binary
 * files might be stored here.  Storage of uploaded files is not done
 * in the database, however.
 *
 * Oracle cannot change a BLOB to a VARCHAR2.  To not lose
 * information we have to do this in multiple steps, by adding
 * a temporary column, copying the data (with conversion) to it,
 * removing the old column, creating the new column, copying the
 * data back, then removing the temporary column.
 *
 * Note that this procedure only works for Oracle 8i and above.
 * Oracle 8 does not allow deletion of columns.
 *
 * Created By: John McNally
 * Modified for Oracle By: Thierry Lach
 */

ALTER TABLE SCARAB_ATTACHMENT ADD TEMP_ATTACHMENT_DATA VARCHAR2(2000);

update SCARAB_ATTACHMENT
set TEMP_ATTACHMENT_DATA = DBMS_LOB.SUBSTR(ATTACHMENT_DATA,2000,1)
where ATTACHMENT_DATA IS NOT NULL;

ALTER TABLE SCARAB_ATTACHMENT DROP COLUMN ATTACHMENT_DATA;

ALTER TABLE SCARAB_ATTACHMENT DROP UNUSED COLUMNS;

ALTER TABLE SCARAB_ATTACHMENT ADD ATTACHMENT_DATA VARCHAR2(2000);

UPDATE SCARAB_ATTACHMENT SET ATTACHMENT_DATA = TEMP_ATTACHMENT_DATA;

ALTER TABLE SCARAB_ATTACHMENT DROP COLUMN TEMP_ATTACHMENT_DATA;

ALTER TABLE SCARAB_ATTACHMENT DROP UNUSED COLUMNS;

