/*
 * ACCEPT_LANGUAGE was not an acceptable name so we are
 * changing it to just LANGUAGE to be more generic.
 * 
 * Created By: Jon Scott Stevens
 */

ALTER TABLE SCARAB_USER_PREFERENCE CHANGE ACCEPT_LANGUAGE LANGUAGE VARCHAR(255) NULL;
