/*
 * Adds ISSUE_ID to the index which greatly improves searches for issues
 * based on option_ids (AdvancedQuery.vm).  I think that is what the old index
 * was meant to do as well, so it is replaced.
 *
 * Created By: John McNally
 * Converted for Oracle By: Thierry Lach
 */

DROP INDEX IX_DELETED_OPTION;

CREATE INDEX IX_AV_OPTION_ISSUE_DEL ON 
    SCARAB_ISSUE_ATTRIBUTE_VALUE (OPTION_ID, ISSUE_ID, DELETED);

CREATE INDEX IX_AV_USER_ISSUE_DEL ON 
    SCARAB_ISSUE_ATTRIBUTE_VALUE (USER_ID, ISSUE_ID, DELETED);
CREATE INDEX IX_AV_INT_ISSUE_DEL ON 
    SCARAB_ISSUE_ATTRIBUTE_VALUE (NUMERIC_VALUE, ISSUE_ID, DELETED);
