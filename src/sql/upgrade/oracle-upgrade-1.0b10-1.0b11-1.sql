/*
 * This only needs to be run for Oracle.
 * The two columns modified cause problems on Oracle because of the
 * differences between CHAR and VARCHAR2 handling.
 *
 * Created By: Thierry Lach 
 */
alter table SCARAB_ISSUE
    modify ID_PREFIX VARCHAR2(4);

update SCARAB_ISSUE
    set ID_PREFIX = rtrim(ID_PREFIX)
    where ID_PREFIX != rtrim(ID_PREFIX);

alter table SCARAB_MODULE
    modify MODULE_CODE VARCHAR2(4);

update SCARAB_MODULE
    set MODULE_CODE = rtrim(MODULE_CODE)
    where MODULE_CODE != rtrim(MODULE_CODE);
