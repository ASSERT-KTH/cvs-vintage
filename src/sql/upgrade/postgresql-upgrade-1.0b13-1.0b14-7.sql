/*
 * Adds indices useful for reporting
 *
 * Created By: Sean Jackson <sean@pnc.com.au>
 */

CREATE INDEX IX_NEW_OPTION_ISSUE ON 
    SCARAB_ACTIVITY (NEW_OPTION_ID, ISSUE_ID);
CREATE INDEX IX_NEW_USER_ISSUE ON 
    SCARAB_ACTIVITY (NEW_USER_ID, ISSUE_ID);
