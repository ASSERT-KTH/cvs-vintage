/*
 * b19-Workflow Migration
 *
 * Add the new field for the conditionally required attributes.
 *
 */


alter table SCARAB_R_MODULE_ATTRIBUTE ADD (REQUIRED_OPTION_ID NUMBER NULL);
