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
        values(3, 1, 'integer', 'org.tigris.scarab.attribute.IntegerAttribute');
insert into SCARAB_ATTRIBUTE_TYPE(ATTRIBUTE_TYPE_ID, ATTRIBUTE_CLASS_ID, ATTRIBUTE_TYPE_NAME, JAVA_CLASS_NAME)
        values(5, 2, 'Dropdown list', 'org.tigris.scarab.attribute.ComboBoxAttribute');
insert into SCARAB_ATTRIBUTE_TYPE(ATTRIBUTE_TYPE_ID, ATTRIBUTE_CLASS_ID, ATTRIBUTE_TYPE_NAME, JAVA_CLASS_NAME)
        values(8, 4, 'user', 'org.tigris.scarab.attribute.UserAttribute');
insert into SCARAB_ATTRIBUTE_TYPE(ATTRIBUTE_TYPE_ID, ATTRIBUTE_CLASS_ID, ATTRIBUTE_TYPE_NAME, JAVA_CLASS_NAME)
        values(11, 1, 'email', 'org.tigris.scarab.attribute.StringAttribute');
insert into SCARAB_ATTRIBUTE_TYPE(ATTRIBUTE_TYPE_ID, ATTRIBUTE_CLASS_ID, ATTRIBUTE_TYPE_NAME, JAVA_CLASS_NAME)
        values(12, 1, 'long-string', 'org.tigris.scarab.attribute.StringAttribute');

/*
 *  Attributes
 */

/* Null Attribute */
insert into SCARAB_ATTRIBUTE(ATTRIBUTE_ID, ATTRIBUTE_NAME, ATTRIBUTE_TYPE_ID, DESCRIPTION, CREATED_DATE)
        values(0, 'Null Attribute', 1, 'Null Attribute', '2002-01-01 00:30:00');
update SCARAB_ATTRIBUTE set CREATED_BY='0';

/* null option */
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, OPTION_NAME)
        values(0, 0, 'Root');

/*
 * Types of issues (artifact/issue types).
 */
insert into SCARAB_ISSUE_TYPE(ISSUE_TYPE_ID, NAME, DESCRIPTION, PARENT_ID)
        values(0, 'Parent', 'Top level issue type. Do not modify.', 0);


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
        values(2, 'module');

/*
 * root module
 */
insert into SCARAB_MODULE(MODULE_ID, MODULE_NAME, MODULE_CODE, MODULE_DESCRIPTION, MODULE_URL, PARENT_ID, CLASS_KEY)
        values(0, 'Global', 'GLO', 'Built-in root module, parent for all top-level modules (projects)', '/', 0, 1);

/*
 * for issues entered against the global module, if any are allowed
 */
insert into ID_TABLE (id_table_id, table_name, next_id, quantity) VALUES (0, 'GLO', 0, 1);


/*
 * standard x-module search lists
 */
insert into SCARAB_MIT_LIST (LIST_ID, NAME, ACTIVE, MODIFIABLE)
    values (1, 'AllModulesAndIssueTypes', 1, 0);
insert into SCARAB_MIT_LISTITEM (ITEM_ID, LIST_ID, MODULE_ID, ISSUE_TYPE_ID)
    values (1, 1, 0, 0);
insert into SCARAB_MIT_LIST (LIST_ID, NAME, ACTIVE, MODIFIABLE)
    values (2, 'AllIssueTypesCurrentModule', 1, 0);
insert into SCARAB_MIT_LISTITEM (ITEM_ID, LIST_ID, MODULE_ID, ISSUE_TYPE_ID)
    values (2, 2, NULL, 0);
insert into SCARAB_MIT_LIST (LIST_ID, NAME, ACTIVE, MODIFIABLE)
    values (3, 'CurrentIssueTypeAllModules', 1, 0);
insert into SCARAB_MIT_LISTITEM (ITEM_ID, LIST_ID, MODULE_ID, ISSUE_TYPE_ID)
    values (3, 3, 0, NULL);
insert into SCARAB_MIT_LIST (LIST_ID, NAME, ACTIVE, MODIFIABLE)
    values (4, 'CurrentIssueTypeCurrentModule', 0, 0);
insert into SCARAB_MIT_LISTITEM (ITEM_ID, LIST_ID, MODULE_ID, ISSUE_TYPE_ID)
    values (4, 4, NULL, NULL);

/*
 * global application parameters, we start off needing to be localized
 */
insert into SCARAB_GLOBAL_PARAMETER (PARAMETER_ID, NAME, VALUE)
    values (1, 'db-l10n-state', 'pre-l10n');
insert into SCARAB_GLOBAL_PARAMETER values (2, 'email-enabled', 'T', NULL);
insert into SCARAB_GLOBAL_PARAMETER values (3, 'email-include-issue-details', 'T', NULL);
insert into SCARAB_GLOBAL_PARAMETER values (4, 'email-allow-module-overrides', 'T', NULL);
