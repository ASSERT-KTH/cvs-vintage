
--------------------------------------------------------------------------
-- SCARAB_ACTIVITY
--------------------------------------------------------------------------
drop table if exists SCARAB_ACTIVITY;

CREATE TABLE SCARAB_ACTIVITY
(
    ISSUE_ID INTEGER NOT NULL,
    ATTRIBUTE_ID INTEGER NOT NULL,
    TRANSACTION_ID INTEGER NOT NULL,
    OLD_VALUE VARCHAR (255),
    NEW_VALUE VARCHAR (255),
    PRIMARY KEY(ISSUE_ID,ATTRIBUTE_ID,TRANSACTION_ID),
    INDEX(ATTRIBUTE_ID,TRANSACTION_ID),
    INDEX(TRANSACTION_ID),
    FOREIGN KEY (ISSUE_ID) REFERENCES SCARAB_ISSUE (ISSUE_ID),
    FOREIGN KEY (ATTRIBUTE_ID) REFERENCES SCARAB_ATTRIBUTE (ATTRIBUTE_ID),
    FOREIGN KEY (TRANSACTION_ID) REFERENCES SCARAB_TRANSACTION (TRANSACTION_ID)
);

--------------------------------------------------------------------------
-- SCARAB_ATTACHMENT
--------------------------------------------------------------------------
drop table if exists SCARAB_ATTACHMENT;

CREATE TABLE SCARAB_ATTACHMENT
(
    ATTACHMENT_ID INTEGER NOT NULL,
    ISSUE_ID INTEGER,
    ATTACHMENT_TYPE_ID INTEGER NOT NULL,
    ATTACHMENT_NAME VARCHAR (255) NOT NULL,
    ATTACHMENT_DATA MEDIUMBLOB,
    ATTACHMENT_FILE_PATH VARCHAR (255),
    ATTACHMENT_MIME_TYPE VARCHAR (25) NOT NULL,
    MODIFIED_BY INTEGER,
    CREATED_BY INTEGER,
    MODIFIED_DATE TIMESTAMP,
    CREATED_DATE TIMESTAMP,
    DELETED INTEGER (1) default 0,
    PRIMARY KEY(ATTACHMENT_ID),
    FOREIGN KEY (ISSUE_ID) REFERENCES SCARAB_ISSUE (ISSUE_ID),
    FOREIGN KEY (ATTACHMENT_TYPE_ID) REFERENCES SCARAB_ATTACHMENT_TYPE (ATTACHMENT_TYPE_ID)
);

--------------------------------------------------------------------------
-- SCARAB_ATTACHMENT_TYPE
--------------------------------------------------------------------------
drop table if exists SCARAB_ATTACHMENT_TYPE;

CREATE TABLE SCARAB_ATTACHMENT_TYPE
(
    ATTACHMENT_TYPE_ID INTEGER NOT NULL,
    ATTACHMENT_TYPE_NAME VARCHAR (255) NOT NULL,
    PRIMARY KEY(ATTACHMENT_TYPE_ID)
);

--------------------------------------------------------------------------
-- SCARAB_ATTRIBUTE
--------------------------------------------------------------------------
drop table if exists SCARAB_ATTRIBUTE;

CREATE TABLE SCARAB_ATTRIBUTE
(
    ATTRIBUTE_ID INTEGER NOT NULL,
    ATTRIBUTE_NAME VARCHAR (255) NOT NULL,
    ATTRIBUTE_TYPE_ID INTEGER NOT NULL,
    DELETED INTEGER (1) default 0,
    PRIMARY KEY(ATTRIBUTE_ID),
    FOREIGN KEY (ATTRIBUTE_TYPE_ID) REFERENCES SCARAB_ATTRIBUTE_TYPE (ATTRIBUTE_TYPE_ID)
);

--------------------------------------------------------------------------
-- SCARAB_ATTRIBUTE_CLASS
--------------------------------------------------------------------------
drop table if exists SCARAB_ATTRIBUTE_CLASS;

CREATE TABLE SCARAB_ATTRIBUTE_CLASS
(
    ATTRIBUTE_CLASS_ID INTEGER NOT NULL,
    ATTRIBUTE_CLASS_NAME VARCHAR (255) NOT NULL,
    ATTRIBUTE_CLASS_DESC VARCHAR (255) NOT NULL,
    JAVA_CLASS_NAME VARCHAR (255),
    PRIMARY KEY(ATTRIBUTE_CLASS_ID)
);

--------------------------------------------------------------------------
-- SCARAB_ATTRIBUTE_OPTION
--------------------------------------------------------------------------
drop table if exists SCARAB_ATTRIBUTE_OPTION;

CREATE TABLE SCARAB_ATTRIBUTE_OPTION
(
    OPTION_ID INTEGER NOT NULL,
    ATTRIBUTE_ID INTEGER NOT NULL,
    DISPLAY_VALUE VARCHAR (255) NOT NULL,
    NUMERIC_VALUE INTEGER,
    PRIMARY KEY(OPTION_ID),
    FOREIGN KEY (ATTRIBUTE_ID) REFERENCES SCARAB_ATTRIBUTE (ATTRIBUTE_ID)
);

--------------------------------------------------------------------------
-- SCARAB_ATTRIBUTE_TYPE
--------------------------------------------------------------------------
drop table if exists SCARAB_ATTRIBUTE_TYPE;

CREATE TABLE SCARAB_ATTRIBUTE_TYPE
(
    ATTRIBUTE_TYPE_ID INTEGER NOT NULL,
    ATTRIBUTE_CLASS_ID INTEGER NOT NULL,
    ATTRIBUTE_TYPE_NAME VARCHAR (255) NOT NULL,
    JAVA_CLASS_NAME VARCHAR (255),
    PRIMARY KEY(ATTRIBUTE_TYPE_ID),
    FOREIGN KEY (ATTRIBUTE_CLASS_ID) REFERENCES SCARAB_ATTRIBUTE_CLASS (ATTRIBUTE_CLASS_ID)
);

--------------------------------------------------------------------------
-- SCARAB_DEPEND
--------------------------------------------------------------------------
drop table if exists SCARAB_DEPEND;

CREATE TABLE SCARAB_DEPEND
(
    OBSERVED_ID INTEGER NOT NULL,
    OBSERVER_ID INTEGER NOT NULL,
    DEPEND_TYPE_ID INTEGER NOT NULL,
    DELETED INTEGER (1) default 0,
    PRIMARY KEY(OBSERVED_ID,OBSERVER_ID),
    INDEX(OBSERVER_ID),
    FOREIGN KEY (OBSERVED_ID) REFERENCES SCARAB_ISSUE (ISSUE_ID),
    FOREIGN KEY (OBSERVER_ID) REFERENCES SCARAB_ISSUE (ISSUE_ID),
    FOREIGN KEY (DEPEND_TYPE_ID) REFERENCES SCARAB_DEPEND_TYPE (DEPEND_TYPE_ID)
);

--------------------------------------------------------------------------
-- SCARAB_DEPEND_TYPE
--------------------------------------------------------------------------
drop table if exists SCARAB_DEPEND_TYPE;

CREATE TABLE SCARAB_DEPEND_TYPE
(
    DEPEND_TYPE_ID INTEGER NOT NULL,
    DEPEND_TYPE_NAME VARCHAR (100) NOT NULL,
    PRIMARY KEY(DEPEND_TYPE_ID)
);

--------------------------------------------------------------------------
-- SCARAB_ISSUE
--------------------------------------------------------------------------
drop table if exists SCARAB_ISSUE;

CREATE TABLE SCARAB_ISSUE
(
    ISSUE_ID INTEGER NOT NULL,
    MODULE_ID INTEGER NOT NULL,
    MODIFIED_BY INTEGER,
    CREATED_BY INTEGER,
    MODIFIED_DATE TIMESTAMP,
    CREATED_DATE TIMESTAMP,
    DELETED INTEGER (1) default 0,
    PRIMARY KEY(ISSUE_ID),
    FOREIGN KEY (MODULE_ID) REFERENCES SCARAB_MODULE (MODULE_ID),
    FOREIGN KEY (CREATED_BY) REFERENCES TURBINE_USER (USER_ID),
    FOREIGN KEY (MODIFIED_BY) REFERENCES TURBINE_USER (USER_ID)
);

--------------------------------------------------------------------------
-- SCARAB_ISSUE_ATTRIBUTE_VALUE
--------------------------------------------------------------------------
drop table if exists SCARAB_ISSUE_ATTRIBUTE_VALUE;

CREATE TABLE SCARAB_ISSUE_ATTRIBUTE_VALUE
(
    ISSUE_ID INTEGER NOT NULL,
    ATTRIBUTE_ID INTEGER NOT NULL,
    OPTION_ID INTEGER,
    USER_ID INTEGER,
    VALUE VARCHAR (255),
    DELETED INTEGER (1) default 0,
    PRIMARY KEY(ISSUE_ID,ATTRIBUTE_ID),
    INDEX(ATTRIBUTE_ID),
    FOREIGN KEY (ISSUE_ID) REFERENCES SCARAB_ISSUE (ISSUE_ID),
    FOREIGN KEY (ATTRIBUTE_ID) REFERENCES SCARAB_ATTRIBUTE (ATTRIBUTE_ID),
    FOREIGN KEY (OPTION_ID) REFERENCES SCARAB_ATTRIBUTE_OPTION (OPTION_ID),
    FOREIGN KEY (USER_ID) REFERENCES TURBINE_USER (USER_ID)
);

--------------------------------------------------------------------------
-- SCARAB_MODIFICATION
--------------------------------------------------------------------------
drop table if exists SCARAB_MODIFICATION;

CREATE TABLE SCARAB_MODIFICATION
(
    TABLE_ID INTEGER NOT NULL,
    COLUMN_ID INTEGER NOT NULL,
    MODIFIED_BY INTEGER,
    CREATED_BY INTEGER,
    MODIFIED_DATE TIMESTAMP,
    CREATED_DATE TIMESTAMP,
    PRIMARY KEY(TABLE_ID,COLUMN_ID),
    INDEX(COLUMN_ID)
);

--------------------------------------------------------------------------
-- SCARAB_MODULE
--------------------------------------------------------------------------
drop table if exists SCARAB_MODULE;

CREATE TABLE SCARAB_MODULE
(
    MODULE_ID INTEGER NOT NULL,
    MODULE_NAME VARCHAR (255) NOT NULL,
    MODULE_DESCRIPTION VARCHAR (255) NOT NULL,
    MODULE_URL VARCHAR (255),
    PARENT_ID INTEGER NOT NULL,
    OWNER_ID INTEGER NOT NULL,
    QA_CONTACT_ID INTEGER NOT NULL,
    DELETED INTEGER (1) default 0,
    PRIMARY KEY(MODULE_ID),
    FOREIGN KEY (PARENT_ID) REFERENCES SCARAB_MODULE (MODULE_ID),
    FOREIGN KEY (OWNER_ID) REFERENCES TURBINE_USER (USER_ID),
    FOREIGN KEY (QA_CONTACT_ID) REFERENCES TURBINE_USER (USER_ID)
);

--------------------------------------------------------------------------
-- SCARAB_R_MODULE_ATTRIBUTE
--------------------------------------------------------------------------
drop table if exists SCARAB_R_MODULE_ATTRIBUTE;

CREATE TABLE SCARAB_R_MODULE_ATTRIBUTE
(
    ATTRIBUTE_ID INTEGER NOT NULL,
    MODULE_ID INTEGER NOT NULL,
    DELETED INTEGER (1) default 0,
    PRIMARY KEY(ATTRIBUTE_ID,MODULE_ID),
    INDEX(MODULE_ID),
    FOREIGN KEY (ATTRIBUTE_ID) REFERENCES SCARAB_ATTRIBUTE (ATTRIBUTE_ID),
    FOREIGN KEY (MODULE_ID) REFERENCES SCARAB_MODULE (MODULE_ID)
);

--------------------------------------------------------------------------
-- SCARAB_R_MODULE_USER
--------------------------------------------------------------------------
drop table if exists SCARAB_R_MODULE_USER;

CREATE TABLE SCARAB_R_MODULE_USER
(
    MODULE_ID INTEGER NOT NULL,
    USER_ID INTEGER NOT NULL,
    DELETED INTEGER (1) default 0,
    PRIMARY KEY(MODULE_ID,USER_ID),
    INDEX(USER_ID),
    FOREIGN KEY (MODULE_ID) REFERENCES SCARAB_MODULE (MODULE_ID),
    FOREIGN KEY (USER_ID) REFERENCES TURBINE_USER (USER_ID)
);

--------------------------------------------------------------------------
-- SCARAB_R_MODULE_USER_ROLE
--------------------------------------------------------------------------
drop table if exists SCARAB_R_MODULE_USER_ROLE;

CREATE TABLE SCARAB_R_MODULE_USER_ROLE
(
    MODULE_ID INTEGER NOT NULL,
    USER_ID INTEGER NOT NULL,
    ROLE_ID INTEGER NOT NULL,
    DELETED INTEGER (1) default 0,
    PRIMARY KEY(MODULE_ID,USER_ID,ROLE_ID),
    INDEX(USER_ID,ROLE_ID),
    INDEX(ROLE_ID),
    FOREIGN KEY (MODULE_ID, USER_ID) REFERENCES SCARAB_R_MODULE_USER (MODULE_ID, USER_ID)
);

--------------------------------------------------------------------------
-- SCARAB_TRANSACTION
--------------------------------------------------------------------------
drop table if exists SCARAB_TRANSACTION;

CREATE TABLE SCARAB_TRANSACTION
(
    TRANSACTION_ID INTEGER NOT NULL,
    CREATED_BY INTEGER,
    CREATED_DATE TIMESTAMP,
    PRIMARY KEY(TRANSACTION_ID)
);

--------------------------------------------------------------------------
-- SCARAB_ISSUE_ATTRIBUTE_VOTE
--------------------------------------------------------------------------
drop table if exists SCARAB_ISSUE_ATTRIBUTE_VOTE;

CREATE TABLE SCARAB_ISSUE_ATTRIBUTE_VOTE
(
    ISSUE_ID INTEGER NOT NULL,
    ATTRIBUTE_ID INTEGER NOT NULL,
    USER_ID INTEGER NOT NULL,
    OPTION_ID INTEGER,
    PRIMARY KEY(ISSUE_ID,ATTRIBUTE_ID,USER_ID),
    INDEX(ATTRIBUTE_ID,USER_ID),
    INDEX(USER_ID),
    FOREIGN KEY (OPTION_ID) REFERENCES SCARAB_ATTRIBUTE_OPTION (OPTION_ID),
    FOREIGN KEY (ISSUE_ID, ATTRIBUTE_ID) REFERENCES SCARAB_ISSUE_ATTRIBUTE_VALUE (ISSUE_ID, ATTRIBUTE_ID),
    FOREIGN KEY (USER_ID) REFERENCES TURBINE_USER (USER_ID)
);

--------------------------------------------------------------------------
-- TURBINE_USER
--------------------------------------------------------------------------
drop table if exists TURBINE_USER;

CREATE TABLE TURBINE_USER
(
    USER_ID INTEGER NOT NULL,
    PRIMARY KEY(USER_ID)
);
