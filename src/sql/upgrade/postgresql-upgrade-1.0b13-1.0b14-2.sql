/*
 * Changes to index names since some non-MySQL databases require
 * index names to be unique by schema rather than by table.
 *
 * Created By: Sean Jackson <sean@pnc.com.au>
 */

DROP INDEX IX_REQUIRED;

DROP INDEX IX_QUICKSEARCH;

CREATE INDEX IX_MOD_ATTR_REQUIRED ON SCARAB_R_MODULE_ATTRIBUTE (REQUIRED);
CREATE INDEX IX_MOD_ATTR_QUICKSEARCH ON SCARAB_R_MODULE_ATTRIBUTE (QUICK_SEARCH);



