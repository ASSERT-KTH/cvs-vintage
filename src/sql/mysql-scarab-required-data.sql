/*
 *  Attribute classes
 */
insert into SCARAB_ATTRIBUTE_CLASS(ATTRIBUTE_CLASS_ID, ATTRIBUTE_CLASS_NAME, ATTRIBUTE_CLASS_DESC, JAVA_CLASS_NAME)
        values(1, 'free-form', 'Free-form atribute', 'org.tigris.scarab.attribute.FreeFormAttribute');
insert into SCARAB_ATTRIBUTE_CLASS(ATTRIBUTE_CLASS_ID, ATTRIBUTE_CLASS_NAME, ATTRIBUTE_CLASS_DESC, JAVA_CLASS_NAME)
        values(2, 'select-one', 'Select_one attribute', 'org.tigris.scarab.attribute.SelectOneAttribute');


insert into SCARAB_ATTRIBUTE_CLASS(ATTRIBUTE_CLASS_ID, ATTRIBUTE_CLASS_NAME, ATTRIBUTE_CLASS_DESC, JAVA_CLASS_NAME)
        values(4, 'user', 'User attribute', 'org.tigris.scarab.attribute.UserAttribute');

/*
 *  Attribute types
 */
insert into SCARAB_ATTRIBUTE_TYPE(ATTRIBUTE_TYPE_ID, ATTRIBUTE_CLASS_ID, ATTRIBUTE_TYPE_NAME, JAVA_CLASS_NAME)
        values(1, 1, 'string', 'org.tigris.scarab.attribute.StringAttribute');
insert into SCARAB_ATTRIBUTE_TYPE(ATTRIBUTE_TYPE_ID, ATTRIBUTE_CLASS_ID, ATTRIBUTE_TYPE_NAME, JAVA_CLASS_NAME)
        values(2, 1, 'date', 'org.tigris.scarab.attribute.DateAttribute');
insert into SCARAB_ATTRIBUTE_TYPE(ATTRIBUTE_TYPE_ID, ATTRIBUTE_CLASS_ID, ATTRIBUTE_TYPE_NAME, JAVA_CLASS_NAME)
        values(3, 1, 'integer', 'org.tigris.scarab.attribute.IntegerAttribute');
insert into SCARAB_ATTRIBUTE_TYPE(ATTRIBUTE_TYPE_ID, ATTRIBUTE_CLASS_ID, ATTRIBUTE_TYPE_NAME, JAVA_CLASS_NAME)
        values(4, 1, 'float', 'org.tigris.scarab.attribute.FloatAttribute');
insert into SCARAB_ATTRIBUTE_TYPE(ATTRIBUTE_TYPE_ID, ATTRIBUTE_CLASS_ID, ATTRIBUTE_TYPE_NAME, JAVA_CLASS_NAME)
        values(5, 2, 'combo-box', 'org.tigris.scarab.attribute.ComboBoxAttribute');


insert into SCARAB_ATTRIBUTE_TYPE(ATTRIBUTE_TYPE_ID, ATTRIBUTE_CLASS_ID, ATTRIBUTE_TYPE_NAME, JAVA_CLASS_NAME)
        values(8, 4, 'user', 'org.tigris.scarab.attribute.UserAttribute');
insert into SCARAB_ATTRIBUTE_TYPE(ATTRIBUTE_TYPE_ID, ATTRIBUTE_CLASS_ID, ATTRIBUTE_TYPE_NAME, JAVA_CLASS_NAME)
        values(11, 1, 'email', 'org.tigris.scarab.attribute.StringAttribute');
insert into SCARAB_ATTRIBUTE_TYPE(ATTRIBUTE_TYPE_ID, ATTRIBUTE_CLASS_ID, ATTRIBUTE_TYPE_NAME, JAVA_CLASS_NAME)
        values(12, 1, 'long-string', 'org.tigris.scarab.attribute.StringAttribute');


/*
 * Option relationships
 * id, name
 */
insert into SCARAB_OPTION_RELATIONSHIP values (1, '1parent-child2'); 
insert into SCARAB_OPTION_RELATIONSHIP values (2, '1required-prior2'); 
insert into SCARAB_OPTION_RELATIONSHIP values (3, '1required-after2'); 


/*
 * Issue Dependencies
 */
insert into SCARAB_DEPEND_TYPE(DEPEND_TYPE_ID, DEPEND_TYPE_NAME)
        values(1, 'blocking');
insert into SCARAB_DEPEND_TYPE(DEPEND_TYPE_ID, DEPEND_TYPE_NAME)
        values(2, 'duplicate');
insert into SCARAB_DEPEND_TYPE(DEPEND_TYPE_ID, DEPEND_TYPE_NAME)
        values(3, 'non-blocking');

/*
 * Attachment types
 */
insert into SCARAB_ATTACHMENT_TYPE(ATTACHMENT_TYPE_ID, ATTACHMENT_TYPE_NAME)
        values(1, 'ATTACHMENT');
insert into SCARAB_ATTACHMENT_TYPE(ATTACHMENT_TYPE_ID, ATTACHMENT_TYPE_NAME)
        values(2, 'COMMENT');
insert into SCARAB_ATTACHMENT_TYPE(ATTACHMENT_TYPE_ID, ATTACHMENT_TYPE_NAME)
        values(3, 'URL');
insert into SCARAB_ATTACHMENT_TYPE(ATTACHMENT_TYPE_ID, ATTACHMENT_TYPE_NAME)
        values(4, 'MODIFICATION');

/*
 * Transaction types
 */
insert into SCARAB_TRANSACTION_TYPE(TYPE_ID, NAME)
        values(1, 'Create Issue');
insert into SCARAB_TRANSACTION_TYPE(TYPE_ID, NAME)
        values(2, 'Edit Issue');
insert into SCARAB_TRANSACTION_TYPE(TYPE_ID, NAME)
        values(3, 'Move Issue');
insert into SCARAB_TRANSACTION_TYPE(TYPE_ID, NAME)
        values(4, 'Retotaling Issue Votes');

/*
 * Frequency values
 */
insert into SCARAB_FREQUENCY(FREQUENCY_ID, FREQUENCY_NAME)
        values(1, 'every half hour');
insert into SCARAB_FREQUENCY(FREQUENCY_ID, FREQUENCY_NAME)
        values(2, 'hourly');
insert into SCARAB_FREQUENCY(FREQUENCY_ID, FREQUENCY_NAME)
        values(3, 'twice daily');
insert into SCARAB_FREQUENCY(FREQUENCY_ID, FREQUENCY_NAME)
        values(4, 'daily');
insert into SCARAB_FREQUENCY(FREQUENCY_ID, FREQUENCY_NAME)
        values(5, 'weekly');

/*
 * Scope values
 */
insert into SCARAB_SCOPE(SCOPE_ID, SCOPE_NAME)
        values(1, 'personal');
insert into SCARAB_SCOPE(SCOPE_ID, SCOPE_NAME)
        values(2, 'global');

/*
 * root module
 */
insert into SCARAB_MODULE(MODULE_ID, MODULE_NAME, MODULE_DESCRIPTION, MODULE_URL, CLASS_KEY)
        values(0, "Global", "Built-in root module, parent for all top-level modules(projects)", "/", 1);

/*
 * for issues entered against the global module, if any are allowed
 */
insert into ID_TABLE (table_name, next_id, quantity) VALUES ('GLO', 0, 1);


/*
 * default attributes to appear on the IssueList screen
 * if the user is not logged in or has not selected attributes,
 * these attribute for user id=0 are used.
 * module_id, user_id, issue_type_id, attribute_id, option_id)
 */
INSERT INTO SCARAB_R_MODULE_USER_ATTRIBUTE VALUES (0,0,1,11,1);
INSERT INTO SCARAB_R_MODULE_USER_ATTRIBUTE VALUES (0,0,1,9,2);

INSERT INTO SCARAB_R_MODULE_USER_ATTRIBUTE VALUES (0,0,3,11,1);
INSERT INTO SCARAB_R_MODULE_USER_ATTRIBUTE VALUES (0,0,3,9,2);

INSERT INTO SCARAB_R_MODULE_USER_ATTRIBUTE VALUES (0,0,5,11,1);
INSERT INTO SCARAB_R_MODULE_USER_ATTRIBUTE VALUES (0,0,5,9,2);

INSERT INTO SCARAB_R_MODULE_USER_ATTRIBUTE VALUES (0,0,7,11,1);
INSERT INTO SCARAB_R_MODULE_USER_ATTRIBUTE VALUES (0,0,7,9,2);

INSERT INTO SCARAB_R_MODULE_USER_ATTRIBUTE VALUES (0,0,9,11,1);
INSERT INTO SCARAB_R_MODULE_USER_ATTRIBUTE VALUES (0,0,9,9,2);

