/*
 * Changes to index names since some non-MySQL databases require
 * index names to be unique by schema rather than by table.
 *
 * Created By: Thierry Lach
 */

CREATE INDEX IX_ISSUETYPE_ATTR_REQUIRED ON SCARAB_R_ISSUETYPE_ATTRIBUTE (REQUIRED);
CREATE INDEX IX_ISSUETYPE_ATTR_QUICKSEARCH ON SCARAB_R_ISSUETYPE_ATTRIBUTE (QUICK_SEARCH);


