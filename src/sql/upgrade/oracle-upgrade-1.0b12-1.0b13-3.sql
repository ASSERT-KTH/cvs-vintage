/*
 * Create the parameter table and insert the current parameters.
 * The l10n flag is set as done, as any previous versions will already
 * have been localized thru the ui or directly.
 *
 * Created By: John McNally
 * Copied but not modified for Oracle By: Thierry Lach
 */

CREATE TABLE SCARAB_GLOBAL_PARAMETER
(
        PARAMETER_ID INTEGER NOT NULL,
        NAME VARCHAR (255) NOT NULL,
        VALUE VARCHAR (255) NOT NULL,
        MODULE_ID INTEGER,
    PRIMARY KEY(PARAMETER_ID),
    FOREIGN KEY (MODULE_ID) REFERENCES SCARAB_MODULE (MODULE_ID),
    UNIQUE (NAME)
);

insert into SCARAB_GLOBAL_PARAMETER (PARAMETER_ID, NAME, VALUE)
    values (1, 'db-l10n-state', 'post-l10n');
