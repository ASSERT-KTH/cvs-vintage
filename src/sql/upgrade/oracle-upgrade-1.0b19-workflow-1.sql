/*
 * b19-Workflow Migration.
 *
 * Create the new table that defines the transitions between diferent values of a
 * dropdown list attribute.
 */
CREATE TABLE SCARAB_TRANSITION
(
                            TRANSITION_ID NUMBER NOT NULL,
                            ROLE_ID NUMBER,
                            ATTRIBUTE_ID NUMBER NOT NULL,
                            FROM_OPTION_ID NUMBER,
                            TO_OPTION_ID NUMBER,
    PRIMARY KEY(TRANSITION_ID),
    FOREIGN KEY (ATTRIBUTE_ID) REFERENCES SCARAB_ATTRIBUTE (ATTRIBUTE_ID),
    FOREIGN KEY (FROM_OPTION_ID) REFERENCES SCARAB_ATTRIBUTE_OPTION (OPTION_ID),
    FOREIGN KEY (TO_OPTION_ID) REFERENCES SCARAB_ATTRIBUTE_OPTION (OPTION_ID)
);

CREATE TABLE SCARAB_CONDITION (
  TRANSITION_ID NUMBER default '0',
  MODULE_ID NUMBER default '0',
  ISSUE_TYPE_ID NUMBER default '0',
  ATTRIBUTE_ID NUMBER default '0',
  OPTION_ID NUMBER default '0',
  PRIMARY KEY  (TRANSITION_ID,MODULE_ID,ISSUE_TYPE_ID,ATTRIBUTE_ID,OPTION_ID)
);
