/*
 * Drops the unique constraint on the NAME column.  
 * Mysql treats this as an index as far as removing it goes.
 */

alter table SCARAB_GLOBAL_PARAMETER drop index NAME;
