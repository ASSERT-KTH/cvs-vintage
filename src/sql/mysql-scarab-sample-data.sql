/*
 * Sample user
 */
insert into Visitor (VISITORID, LOGINID, PASSWORD_VALUE, FIRST_NAME, LAST_NAME, CONFIRM_VALUE ) 
    values (2, 'jon@latchkey.com', '1', 'Jon', 'Stevens', 'CONFIRMED' );

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
 * Module 2 has all of the standard attributes.
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
 * Insert a relationship between visitor_id 1 and module_id 1
 * Insert a relationship between visitor_id 1 and module_id 3
 * Insert a relationship between visitor_id 2 and module_id 1
 * Insert a relationship between visitor_id 2 and module_id 3
 */
insert into SCARAB_R_MODULE_VISITOR(VISITOR_ID, MODULE_ID) values (1, 1);
insert into SCARAB_R_MODULE_VISITOR(VISITOR_ID, MODULE_ID) values (1, 3);
insert into SCARAB_R_MODULE_VISITOR(VISITOR_ID, MODULE_ID) values (2, 1);
insert into SCARAB_R_MODULE_VISITOR(VISITOR_ID, MODULE_ID) values (2, 3);


/*
 * Sample Issue
 */

insert into SCARAB_ISSUE(ISSUE_ID, MODULE_ID) values (1, 2);

/* description */
insert into SCARAB_ISSUE_ATTRIBUTE_VALUE(ISSUE_ID, ATTRIBUTE_ID, VALUE) values (1, 1, 'Docs are out of date.');
/* assigned to visitor id 1 */
insert into SCARAB_ISSUE_ATTRIBUTE_VALUE(ISSUE_ID, ATTRIBUTE_ID, VISITOR_ID, VALUE) values (1, 2, 1, 'jon');
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
insert into SCARAB_ISSUE_ATTRIBUTE_VOTE(ISSUE_ID, ATTRIBUTE_ID, VISITOR_ID, OPTION_ID) values(1, 10, 1, 73);
/* Cam only cares about major changes */
insert into SCARAB_ISSUE_ATTRIBUTE_VOTE(ISSUE_ID, ATTRIBUTE_ID, VISITOR_ID, OPTION_ID) values(1, 10, 2, 72);

