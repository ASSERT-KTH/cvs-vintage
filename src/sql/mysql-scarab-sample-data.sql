/*
 * Sample user
 */
insert into TURBINE_USER (USER_ID, LOGIN_NAME, PASSWORD_VALUE, FIRST_NAME, LAST_NAME, EMAIL, CONFIRM_VALUE ) 
    values (2, 'jon@latchkey.com', 'NWoZK3kTsExUV00Ywo1G5jlU', 'Jon', 'Stevens', 'jon@latchkey.com', 'CONFIRMED' );
insert into TURBINE_USER (USER_ID, LOGIN_NAME, PASSWORD_VALUE, FIRST_NAME, LAST_NAME, EMAIL, CONFIRM_VALUE ) 
    values (3, 'jss@latchkey.com', 'NWoZK3kTsExUV00Ywo1G5jlU', 'Jon', 'Stevens', 'jon@latchkey.com', 'abcdef' );

/*
 * Sample Project
 */

insert into SCARAB_MODULE(MODULE_ID, MODULE_NAME, MODULE_DESCRIPTION, MODULE_URL, PARENT_ID, OWNER_ID, QA_CONTACT_ID) 
    values(1, 'Pacman JVM', 'Sample project', '/PacmanJVM/', 0, 2, 2);
insert into SCARAB_MODULE(MODULE_ID, MODULE_NAME, MODULE_DESCRIPTION, MODULE_URL, PARENT_ID, OWNER_ID, QA_CONTACT_ID) 
    values(3, 'Turbine', 'The Turbine Project', '/Turbine/', 0, 2, 2);

/*
 * Sample Component
 */

insert into SCARAB_MODULE(MODULE_ID, MODULE_NAME, MODULE_DESCRIPTION, MODULE_URL, PARENT_ID, OWNER_ID, QA_CONTACT_ID) 
    values(2, 'Docs', 'Documentation', '/PacmanJVM/docs/', 1, 2, 2);
insert into SCARAB_MODULE(MODULE_ID, MODULE_NAME, MODULE_DESCRIPTION, MODULE_URL, PARENT_ID, OWNER_ID, QA_CONTACT_ID) 
    values(5, 'Source', 'Source', '/PacmanJVM/source/', 1, 2, 2);
insert into SCARAB_MODULE(MODULE_ID, MODULE_NAME, MODULE_DESCRIPTION, MODULE_URL, PARENT_ID, OWNER_ID, QA_CONTACT_ID) 
    values(4, 'Docs', 'Documentation', '/Turbine/docs/', 3, 2, 2);
insert into SCARAB_MODULE(MODULE_ID, MODULE_NAME, MODULE_DESCRIPTION, MODULE_URL, PARENT_ID, OWNER_ID, QA_CONTACT_ID) 
    values(6, 'Source', 'Source', '/Turbine/source/', 3, 2, 2);

/*
 * Module 2.
 */
insert into SCARAB_R_MODULE_ATTRIBUTE(MODULE_ID, ATTRIBUTE_ID) values(2,1);
insert into SCARAB_R_MODULE_ATTRIBUTE(MODULE_ID, ATTRIBUTE_ID) values(2,2);
insert into SCARAB_R_MODULE_ATTRIBUTE(MODULE_ID, ATTRIBUTE_ID) values(2,3);
insert into SCARAB_R_MODULE_ATTRIBUTE(MODULE_ID, ATTRIBUTE_ID) values(2,4);
insert into SCARAB_R_MODULE_ATTRIBUTE(MODULE_ID, ATTRIBUTE_ID) values(2,5);
insert into SCARAB_R_MODULE_ATTRIBUTE(MODULE_ID, ATTRIBUTE_ID) values(2,6);
insert into SCARAB_R_MODULE_ATTRIBUTE(MODULE_ID, ATTRIBUTE_ID) values(2,7);
insert into SCARAB_R_MODULE_ATTRIBUTE(MODULE_ID, ATTRIBUTE_ID) values(2,9);
insert into SCARAB_R_MODULE_ATTRIBUTE(MODULE_ID, ATTRIBUTE_ID) values(2,10);

/*
 * Insert some values for project 5
 */
INSERT INTO SCARAB_R_MODULE_ATTRIBUTE VALUES (1,5,0,1);
INSERT INTO SCARAB_R_MODULE_ATTRIBUTE VALUES (2,5,1,0);
INSERT INTO SCARAB_R_MODULE_ATTRIBUTE VALUES (3,5,0,0);
INSERT INTO SCARAB_R_MODULE_ATTRIBUTE VALUES (4,5,0,0);
INSERT INTO SCARAB_R_MODULE_ATTRIBUTE VALUES (5,5,0,1);
INSERT INTO SCARAB_R_MODULE_ATTRIBUTE VALUES (6,5,0,1);
INSERT INTO SCARAB_R_MODULE_ATTRIBUTE VALUES (7,5,0,0);
INSERT INTO SCARAB_R_MODULE_ATTRIBUTE VALUES (8,5,0,0);
INSERT INTO SCARAB_R_MODULE_ATTRIBUTE VALUES (9,5,0,0);
INSERT INTO SCARAB_R_MODULE_ATTRIBUTE VALUES (10,5,1,0);
INSERT INTO SCARAB_R_MODULE_ATTRIBUTE VALUES (11,5,0,1);
INSERT INTO SCARAB_R_MODULE_ATTRIBUTE VALUES (12,5,0,0);
INSERT INTO SCARAB_R_MODULE_ATTRIBUTE VALUES (13,5,0,0);

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

insert into SCARAB_ISSUE(ISSUE_ID, MODULE_ID) values (1, 2);

/* description */
insert into SCARAB_ISSUE_ATTRIBUTE_VALUE(ISSUE_ID, ATTRIBUTE_ID, VALUE) values (1, 1, 'Docs are out of date.');
/* assigned to visitor id 1 */
insert into SCARAB_ISSUE_ATTRIBUTE_VALUE(ISSUE_ID, ATTRIBUTE_ID, USER_ID, VALUE) values (1, 2, 1, 'jon');
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
/* who`s tracking issue 1 */
insert into SCARAB_ISSUE_ATTRIBUTE_VALUE(ISSUE_ID, ATTRIBUTE_ID, VALUE) values (1, 10, 'jon, cam');
/* Jon wants to be notified of any changes */
insert into SCARAB_ISSUE_ATTRIBUTE_VOTE(ISSUE_ID, ATTRIBUTE_ID, USER_ID, OPTION_ID) values(1, 10, 1, 73);
/* Cam only cares about major changes */
insert into SCARAB_ISSUE_ATTRIBUTE_VOTE(ISSUE_ID, ATTRIBUTE_ID, USER_ID, OPTION_ID) values(1, 10, 2, 72);

