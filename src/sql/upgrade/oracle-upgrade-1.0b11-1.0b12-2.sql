/*
 * Add columns to ISSUE_TYPE
 *
 * $Id: oracle-upgrade-1.0b11-1.0b12-2.sql,v 1.2 2002/09/27 15:16:18 thierrylach Exp $
 *
 * Created By: Elicia David
 * Modified for Oracle By: Thierry Lach
 */

ALTER TABLE SCARAB_ISSUE_TYPE ADD ISDEFAULT NUMBER (1) default 0;
