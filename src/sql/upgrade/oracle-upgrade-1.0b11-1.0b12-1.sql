/*
 * new tables and changes to associate global issue types with attribute groups
 *
 * Created By: Elicia David
 * Modified for Oracle By: Thierry Lach
 */

/* -----------------------------------------------------------------------
 * SCARAB_R_ISSUETYPE_ATTRIBUTE
 * -----------------------------------------------------------------------
 */

CREATE TABLE SCARAB_R_ISSUETYPE_ATTRIBUTE
(
    ATTRIBUTE_ID INTEGER NOT NULL,
    ISSUE_TYPE_ID INTEGER NOT NULL,
    PREFERRED_ORDER INTEGER NOT NULL,
    ACTIVE NUMBER (1) default 1 NOT NULL,
    REQUIRED NUMBER (1) default 0 NOT NULL,
    QUICK_SEARCH NUMBER (1) default 0 NOT NULL,
    DEFAULT_TEXT_FLAG NUMBER (1) default 0 NOT NULL,
    LOCKED NUMBER (1) default 0,
    PRIMARY KEY(ATTRIBUTE_ID, ISSUE_TYPE_ID),
    FOREIGN KEY (ATTRIBUTE_ID) REFERENCES SCARAB_ATTRIBUTE (ATTRIBUTE_ID),
    FOREIGN KEY (ISSUE_TYPE_ID) REFERENCES SCARAB_ISSUE_TYPE (ISSUE_TYPE_ID)
);

/* -----------------------------------------------------------------------
 * SCARAB_R_ISSUETYPE_OPTION
 * -----------------------------------------------------------------------
 */

CREATE TABLE SCARAB_R_ISSUETYPE_OPTION
(
    OPTION_ID INTEGER NOT NULL,
    ISSUE_TYPE_ID INTEGER NOT NULL,
    PREFERRED_ORDER INTEGER,
    WEIGHT INTEGER,
    ACTIVE NUMBER (1) default 1 NOT NULL,
    LOCKED NUMBER (1) default 0,
    PRIMARY KEY(OPTION_ID, ISSUE_TYPE_ID),
    FOREIGN KEY (OPTION_ID) REFERENCES SCARAB_ATTRIBUTE_OPTION (OPTION_ID),
    FOREIGN KEY (ISSUE_TYPE_ID) REFERENCES SCARAB_ISSUE_TYPE (ISSUE_TYPE_ID)
);

/* Add columns to MODULE */
ALTER TABLE SCARAB_MODULE ADD LOCKED NUMBER (1) default 0;

/* Add columns to ISSUE_TYPE */
ALTER TABLE SCARAB_ISSUE_TYPE ADD LOCKED NUMBER (1) default 0;
ALTER TABLE SCARAB_ISSUE_TYPE ADD DEDUPE NUMBER (1) default 0;

/* Make the module_id column not required for the ATTRIBUTE_GROUP column
 * If Module_id is null, it is a group for a global issue type
 */
ALTER TABLE SCARAB_ATTRIBUTE_GROUP MODIFY MODULE_ID NUMBER null;

