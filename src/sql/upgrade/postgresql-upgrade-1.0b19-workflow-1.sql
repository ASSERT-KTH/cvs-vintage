/*
 * b19-Workflow Migration.
 *
 * Create the new table that defines the transitions between diferent values of a
 * dropdown list attribute.
 */
CREATE TABLE SCARAB_TRANSITION
(
                            TRANSITION_ID INTEGER NOT NULL,
                            ROLE_ID INTEGER,
                            ATTRIBUTE_ID INTEGER NOT NULL,
                            FROM_OPTION_ID INTEGER,
                            TO_OPTION_ID INTEGER,
    PRIMARY KEY(TRANSITION_ID),
    FOREIGN KEY (ATTRIBUTE_ID) REFERENCES SCARAB_ATTRIBUTE (ATTRIBUTE_ID),
    FOREIGN KEY (FROM_OPTION_ID) REFERENCES SCARAB_ATTRIBUTE_OPTION (OPTION_ID),
    FOREIGN KEY (TO_OPTION_ID) REFERENCES SCARAB_ATTRIBUTE_OPTION (OPTION_ID)
);

