/*
 * This script does the following:
 * 
 *    1) Gets a list of template issue types (i.e., issue types
 *       where the parent id is not 0).
 *    2) Deletes the entries for these issue types from the
 *       SCARAB_R_MODULE_ISSUE_TYPE table, as these mappings 
 *       are unnecessary.
 */  

delete from SCARAB_R_MODULE_ISSUE_TYPE
 where ISSUE_TYPE_ID in (select ISSUE_TYPE_ID
                           from SCARAB_ISSUE_TYPE
                          where PARENT_ID > 0);

