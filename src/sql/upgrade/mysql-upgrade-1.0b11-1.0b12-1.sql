/*
 * new tables and changes to associate global issue types with attribute groups
 *
 * Created By: Elicia David
 */

# -----------------------------------------------------------------------
# SCARAB_R_ISSUETYPE_ATTRIBUTE
# -----------------------------------------------------------------------

CREATE TABLE SCARAB_R_ISSUETYPE_ATTRIBUTE
(
    ATTRIBUTE_ID INTEGER NOT NULL,
    ISSUE_TYPE_ID INTEGER NOT NULL,
    PREFERRED_ORDER INTEGER NOT NULL,
    ACTIVE INTEGER (1) NOT NULL default 1,
    REQUIRED INTEGER (1) NOT NULL default 0,
    QUICK_SEARCH INTEGER (1) NOT NULL default 0,
    DEFAULT_TEXT_FLAG INTEGER (1) NOT NULL default 0,
    LOCKED INTEGER (1) default 0,
    PRIMARY KEY(ATTRIBUTE_ID, ISSUE_TYPE_ID),
    FOREIGN KEY (ATTRIBUTE_ID) REFERENCES SCARAB_ATTRIBUTE (ATTRIBUTE_ID),
    FOREIGN KEY (ISSUE_TYPE_ID) REFERENCES SCARAB_ISSUE_TYPE (ISSUE_TYPE_ID)
);

# -----------------------------------------------------------------------
# SCARAB_R_ISSUETYPE_OPTION
# -----------------------------------------------------------------------

CREATE TABLE SCARAB_R_ISSUETYPE_OPTION
(
    OPTION_ID INTEGER NOT NULL,
    ISSUE_TYPE_ID INTEGER NOT NULL,
    PREFERRED_ORDER INTEGER,
    WEIGHT INTEGER,
    ACTIVE INTEGER (1) NOT NULL default 1,
    LOCKED INTEGER (1) default 0,
    PRIMARY KEY(OPTION_ID, ISSUE_TYPE_ID),
    FOREIGN KEY (OPTION_ID) REFERENCES SCARAB_ATTRIBUTE_OPTION (OPTION_ID),
    FOREIGN KEY (ISSUE_TYPE_ID) REFERENCES SCARAB_ISSUE_TYPE (ISSUE_TYPE_ID)
);

# Add columns to MODULE
ALTER TABLE SCARAB_MODULE ADD LOCKED INTEGER (1) default 0;

# Add columns to ISSUE_TYPE
ALTER TABLE SCARAB_ISSUE_TYPE ADD LOCKED INTEGER (1) default 0;
ALTER TABLE SCARAB_ISSUE_TYPE ADD DEDUPE INTEGER (1) default 0;

# Make the module_id column not required for the ATTRIBUTE_GROUP column
# If Module_id is null, it is a group for a global issue type
ALTER TABLE SCARAB_ATTRIBUTE_GROUP MODIFY MODULE_ID INTEGER null;
