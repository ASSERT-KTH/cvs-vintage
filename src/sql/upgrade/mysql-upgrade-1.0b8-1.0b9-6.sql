/*
 * standard x-module search lists
 * These should have been added as part of mysql-upgrade-1.0b8-1.0b9-2.sql
 * but they were left out.  Since some people have already run the #2 
 * script, this is being added as a separate file.
 *
 * Created By: John McNally 
 */
insert into SCARAB_MIT_LIST (LIST_ID, NAME, ACTIVE, MODIFIABLE)
    values (1, 'All modules and issue types', 1, 0);
insert into SCARAB_MIT_LISTITEM (ITEM_ID, LIST_ID, MODULE_ID, ISSUE_TYPE_ID)
    values (1, 1, 0, 0);
insert into SCARAB_MIT_LIST (LIST_ID, NAME, ACTIVE, MODIFIABLE)
    values (2, 'All issue types in the current module', 1, 0);
insert into SCARAB_MIT_LISTITEM (ITEM_ID, LIST_ID, MODULE_ID, ISSUE_TYPE_ID)
    values (2, 2, NULL, 0);
insert into SCARAB_MIT_LIST (LIST_ID, NAME, ACTIVE, MODIFIABLE)
    values (3, 'Current issue type in all modules', 1, 0);
insert into SCARAB_MIT_LISTITEM (ITEM_ID, LIST_ID, MODULE_ID, ISSUE_TYPE_ID)
    values (3, 3, 0, NULL);
insert into SCARAB_MIT_LIST (LIST_ID, NAME, ACTIVE, MODIFIABLE)
    values (4, 'Current issue type in the current module', 0, 0);
insert into SCARAB_MIT_LISTITEM (ITEM_ID, LIST_ID, MODULE_ID, ISSUE_TYPE_ID)
    values (4, 4, NULL, NULL);
