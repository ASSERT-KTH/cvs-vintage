/*
 * Adds the X-Module, X-Issuetype query tables
 *
 * Created By: John McNally 
 */

# -----------------------------------------------------------------------
# SCARAB_MIT_LIST
# -----------------------------------------------------------------------

CREATE TABLE SCARAB_MIT_LIST
(
    LIST_ID INTEGER NOT NULL,
    NAME VARCHAR (100),
    ACTIVE INTEGER (1) default 1,
    MODIFIABLE INTEGER (1) default 1,
    USER_ID INTEGER,
    PRIMARY KEY(LIST_ID),
    FOREIGN KEY (USER_ID) REFERENCES TURBINE_USER (USER_ID)
);

# -----------------------------------------------------------------------
# SCARAB_MIT_LISTITEM
# -----------------------------------------------------------------------

CREATE TABLE SCARAB_MIT_LISTITEM
(
    ITEM_ID INTEGER NOT NULL,
    MODULE_ID INTEGER,
    ISSUE_TYPE_ID INTEGER,
    LIST_ID INTEGER NOT NULL,
    PRIMARY KEY(ITEM_ID),
    FOREIGN KEY (LIST_ID) REFERENCES SCARAB_MIT_LIST (LIST_ID),
    FOREIGN KEY (MODULE_ID) REFERENCES SCARAB_MODULE (MODULE_ID),
    FOREIGN KEY (ISSUE_TYPE_ID) REFERENCES SCARAB_ISSUE_TYPE (ISSUE_TYPE_ID)
);

insert into ID_TABLE (id_table_id, table_name, next_id, quantity) 
  VALUES (44, 'SCARAB_MIT_LIST', 100, 10);
insert into ID_TABLE (id_table_id, table_name, next_id, quantity) 
  VALUES (45, 'SCARAB_MIT_LISTITEM', 100, 10);

