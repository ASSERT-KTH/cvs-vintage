/*
 * Sample user
 */
insert into TURBINE_USER (USER_ID, LOGIN_NAME, PASSWORD_VALUE, FIRST_NAME, LAST_NAME, EMAIL, CONFIRM_VALUE ) 
    values (2, 'jon@latchkey.com', 'NWoZK3kTsExUV00Ywo1G5jlU', 'Jon', 'Stevens', 'jon@latchkey.com', 'CONFIRMED' );
insert into TURBINE_USER (USER_ID, LOGIN_NAME, PASSWORD_VALUE, FIRST_NAME, LAST_NAME, EMAIL, CONFIRM_VALUE ) 
    values (3, 'jss@latchkey.com', 'NWoZK3kTsExUV00Ywo1G5jlU', 'Jon', 'Stevens', 'jon@latchkey.com', 'abcdef' );
insert into TURBINE_USER (USER_ID, LOGIN_NAME, PASSWORD_VALUE, FIRST_NAME, LAST_NAME, EMAIL, CONFIRM_VALUE ) 
    values (4, 'jmcnally@collab.net', 'NWoZK3kTsExUV00Ywo1G5jlU', 'John', 'McNally', 'jmcnally@collab.net', 'CONFIRMED' );

/*
 * Sample Project
 */

insert into SCARAB_MODULE(MODULE_ID, MODULE_NAME, MODULE_CODE, MODULE_DESCRIPTION, MODULE_URL, PARENT_ID, OWNER_ID, QA_CONTACT_ID) 
    values(1, 'Pacman JVM', 'PAC', 'Sample project', '/PacmanJVM/', 0, 2, 2);
insert into SCARAB_MODULE(MODULE_ID, MODULE_NAME, MODULE_CODE, MODULE_DESCRIPTION, MODULE_URL, PARENT_ID, OWNER_ID, QA_CONTACT_ID) 
    values(3, 'Turbine', 'TBN', 'The Turbine Project', '/Turbine/', 0, 2, 2);

/*
 * Sample Component
 */

insert into SCARAB_MODULE(MODULE_ID, MODULE_NAME, MODULE_CODE, MODULE_DESCRIPTION, MODULE_URL, PARENT_ID, OWNER_ID, QA_CONTACT_ID) 
    values(2, 'Docs', 'PACD', 'Documentation', '/PacmanJVM/docs/', 1, 2, 2);
insert into SCARAB_MODULE(MODULE_ID, MODULE_NAME, MODULE_CODE, MODULE_DESCRIPTION, MODULE_URL, PARENT_ID, OWNER_ID, QA_CONTACT_ID) 
    values(5, 'Source', 'PACS', 'Source', '/PacmanJVM/source/', 1, 2, 2);
insert into SCARAB_MODULE(MODULE_ID, MODULE_NAME, MODULE_CODE, MODULE_DESCRIPTION, MODULE_URL, PARENT_ID, OWNER_ID, QA_CONTACT_ID) 
    values(4, 'Docs', 'TBND', 'Documentation', '/Turbine/docs/', 3, 2, 2);
insert into SCARAB_MODULE(MODULE_ID, MODULE_NAME, MODULE_CODE, MODULE_DESCRIPTION, MODULE_URL, PARENT_ID, OWNER_ID, QA_CONTACT_ID) 
    values(6, 'Source', 'TBNS', 'Source', '/Turbine/source/', 3, 2, 2);

/*
 * id_table entries for the module_codes
 */
insert into ID_TABLE (table_name, next_id, quantity) VALUES ('PAC', 1, 1);
insert into ID_TABLE (table_name, next_id, quantity) VALUES ('PACD', 2, 1);
insert into ID_TABLE (table_name, next_id, quantity) VALUES ('PACS', 1, 1);
insert into ID_TABLE (table_name, next_id, quantity) VALUES ('TBN', 1, 1);
insert into ID_TABLE (table_name, next_id, quantity) VALUES ('TBND', 1, 1);
insert into ID_TABLE (table_name, next_id, quantity) VALUES ('TBNS', 1, 1);


/*
 * Module 2.
 * module_id, attr_id, display_value, active, required, preferred order, 
 * dedupe, quick_search
 */
insert into SCARAB_R_MODULE_ATTRIBUTE(MODULE_ID, ATTRIBUTE_ID) values(2,1);
#insert into SCARAB_R_MODULE_ATTRIBUTE(MODULE_ID, ATTRIBUTE_ID) values(2,2);
insert into SCARAB_R_MODULE_ATTRIBUTE(MODULE_ID, ATTRIBUTE_ID) values(2,3);
insert into SCARAB_R_MODULE_ATTRIBUTE(MODULE_ID, ATTRIBUTE_ID) values(2,4);
insert into SCARAB_R_MODULE_ATTRIBUTE(MODULE_ID, ATTRIBUTE_ID) values(2,5);
insert into SCARAB_R_MODULE_ATTRIBUTE(MODULE_ID, ATTRIBUTE_ID) values(2,6);
insert into SCARAB_R_MODULE_ATTRIBUTE(MODULE_ID, ATTRIBUTE_ID) values(2,7);
insert into SCARAB_R_MODULE_ATTRIBUTE(MODULE_ID, ATTRIBUTE_ID) values(2,9);
#insert into SCARAB_R_MODULE_ATTRIBUTE(MODULE_ID, ATTRIBUTE_ID) values(2,10);
INSERT INTO SCARAB_R_MODULE_ATTRIBUTE VALUES (2,11,NULL,1,1,1,0,0);


/*
 * Insert some values for project 5
 * module_id, attr_id, display_value, active, required, preferred order, 
 * dedupe, quick_search
 */
INSERT INTO SCARAB_R_MODULE_ATTRIBUTE VALUES (5,1,NULL,1,1,1,0,1);
INSERT INTO SCARAB_R_MODULE_ATTRIBUTE VALUES (5,2,NULL,0,0,2,0,0);
INSERT INTO SCARAB_R_MODULE_ATTRIBUTE VALUES (5,3,NULL,1,0,3,0,0);
INSERT INTO SCARAB_R_MODULE_ATTRIBUTE VALUES (5,4,NULL,1,0,4,0,0);
INSERT INTO SCARAB_R_MODULE_ATTRIBUTE VALUES (5,5,NULL,1,1,5,1,1);
INSERT INTO SCARAB_R_MODULE_ATTRIBUTE VALUES (5,6,NULL,1,1,6,1,1);
INSERT INTO SCARAB_R_MODULE_ATTRIBUTE VALUES (5,7,NULL,1,0,7,0,0);
INSERT INTO SCARAB_R_MODULE_ATTRIBUTE VALUES (5,8,NULL,1,0,8,0,0);
INSERT INTO SCARAB_R_MODULE_ATTRIBUTE VALUES (5,9,NULL,1,0,9,0,0);
INSERT INTO SCARAB_R_MODULE_ATTRIBUTE VALUES (5,10,NULL,0,0,10,0,0);
INSERT INTO SCARAB_R_MODULE_ATTRIBUTE VALUES (5,11,NULL,1,1,11,0,1);
INSERT INTO SCARAB_R_MODULE_ATTRIBUTE VALUES (5,13,NULL,1,0,12,0,0);

/*
 * Insert a relationship between user_id 1 and module_id 1
 * Insert a relationship between user_id 1 and module_id 3
 * Insert a relationship between user_id 2 and module_id 1
 * Insert a relationship between user_id 2 and module_id 3
 */
insert into SCARAB_R_MODULE_USER(USER_ID, MODULE_ID) values (1, 1);
insert into SCARAB_R_MODULE_USER(USER_ID, MODULE_ID) values (1, 3);
insert into SCARAB_R_MODULE_USER(USER_ID, MODULE_ID) values (2, 1);
insert into SCARAB_R_MODULE_USER(USER_ID, MODULE_ID) values (2, 3);


/*
 * Sample Issue
 */

insert into SCARAB_ISSUE(ISSUE_ID, MODULE_ID, ID_PREFIX, ID_COUNT) values (1, 2, 'PACD', 1);

/* description */
insert into SCARAB_ISSUE_ATTRIBUTE_VALUE(ISSUE_ID, ATTRIBUTE_ID, VALUE) values (1, 1, 'Documents are not as current as they should be.');
/* summary */
insert into SCARAB_ISSUE_ATTRIBUTE_VALUE(ISSUE_ID, ATTRIBUTE_ID, VALUE) values (1, 11, 'Docs are out of date.');
/* assigned to visitor id 1 */
#insert into SCARAB_ISSUE_ATTRIBUTE_VALUE(ISSUE_ID, ATTRIBUTE_ID, USER_ID, VALUE) values (1, 2, 1, 'jon');
/* status is New */
insert into SCARAB_ISSUE_ATTRIBUTE_VALUE(ISSUE_ID, ATTRIBUTE_ID, OPTION_ID, VALUE) values (1, 3, 2, 'New');
/* resolution is verified */
insert into SCARAB_ISSUE_ATTRIBUTE_VALUE(ISSUE_ID, ATTRIBUTE_ID, OPTION_ID, VALUE) values (1, 4, 6, 'verified');
/* platform is SGI */
insert into SCARAB_ISSUE_ATTRIBUTE_VALUE(ISSUE_ID, ATTRIBUTE_ID, OPTION_ID, VALUE) values (1, 5, 21, 'SGI');
/* os is OpenVMS */
insert into SCARAB_ISSUE_ATTRIBUTE_VALUE(ISSUE_ID, ATTRIBUTE_ID, OPTION_ID, VALUE) values (1, 6, 48, 'OpenVMS');
/* priority is p3 */
insert into SCARAB_ISSUE_ATTRIBUTE_VALUE(ISSUE_ID, ATTRIBUTE_ID, OPTION_ID, VALUE) values (1, 7, 56, 'P3');
/* severity is major */
insert into SCARAB_ISSUE_ATTRIBUTE_VALUE(ISSUE_ID, ATTRIBUTE_ID, OPTION_ID, VALUE) values (1, 9, 66, 'major');

