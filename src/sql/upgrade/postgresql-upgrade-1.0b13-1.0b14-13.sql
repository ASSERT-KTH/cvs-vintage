/*
 * Adds a column to the SCARAB_USER_PREFERENCE which stores
 * the users last Accept-Language: HTTP header so that emails
 * and other data can be localized to the users stored 
 * language preference.
 * 
 * Created By: Jon Scott Stevens
 */

ALTER TABLE SCARAB_USER_PREFERENCE ADD COLUMN ACCEPT_LANGUAGE VARCHAR(255) NULL;