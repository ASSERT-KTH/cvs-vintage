/*
 * changes the DATA column of SCARAB_ATTACHMENT to be a text type
 * so that non ascii Strings will be encoded properly when stored.
 * This column was originally binary with the presumption that binary
 * files might be stored here.  Storage of uploaded files is not done
 * in the database, however.
 *
 * Created By: John McNally
 */

ALTER TABLE SCARAB_ATTACHMENT MODIFY ATTACHMENT_DATA MEDIUMTEXT;
