/*
 * Adds indices useful for reporting
 *
 * Created By: John McNally
 * Copied for Oracle by: Thierry Lach
 */

CREATE INDEX IX_NEW_OPTION_ISSUE ON 
    SCARAB_ACTIVITY (NEW_OPTION_ID, ISSUE_ID);
CREATE INDEX IX_NEW_USER_ISSUE ON 
    SCARAB_ACTIVITY (NEW_USER_ID, ISSUE_ID);
