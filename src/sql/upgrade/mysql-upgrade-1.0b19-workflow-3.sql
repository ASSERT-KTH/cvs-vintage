/*
 * b19-Workflow Migration
 *
 * Add the new field for the conditionally required attributes.
 *
 */

alter table SCARAB_R_MODULE_ATTRIBUTE ADD COLUMN REQUIRED_OPTION_ID INTEGER;
