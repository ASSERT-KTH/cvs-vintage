/*
 * Create a new table with the primary key DEPEND_ID
 * Add DEPEND_ID to the SCARAB_ACTIVITY table
 *
 * Create the new table
 * Copy data out of DEPEND table into new table
 * Remove the auto_increment on the DEPEND_ID column
 * Delete the old table and rename the new table to the old
 * Create the indexes
 *
 * Created By: Jon Scott Stevens
 * Modified for Oracle By: Quinton McCombs
 * $Id: oracle-upgrade-1.0b13-1.0b14-8.sql,v 1.1 2003/02/03 08:20:05 jon Exp $
 */

drop table xxxx_SCARAB_DEPEND;

/* create a temporary table. */
create table xxxx_SCARAB_DEPEND  (
    DEPEND_ID       	NUMBER NOT NULL,
    OBSERVED_ID		NUMBER NOT NULL,
    OBSERVER_ID		NUMBER NOT NULL,
    DEPEND_TYPE_ID  	NUMBER NOT NULL,
    DELETED         	NUMBER(1) NULL,
    PRIMARY KEY     (DEPEND_ID),
    FOREIGN KEY (OBSERVED_ID) REFERENCES SCARAB_ISSUE (ISSUE_ID),
    FOREIGN KEY (OBSERVER_ID) REFERENCES SCARAB_ISSUE (ISSUE_ID),
    FOREIGN KEY (DEPEND_TYPE_ID) REFERENCES SCARAB_DEPEND_TYPE (DEPEND_TYPE_ID)
);

insert into xxxx_SCARAB_DEPEND (DEPEND_ID, OBSERVED_ID, OBSERVER_ID, DEPEND_TYPE_ID, DELETED)
    select ROWNUM, OBSERVED_ID, OBSERVER_ID, DEPEND_TYPE_ID, DELETED
        from SCARAB_DEPEND;

delete from ID_TABLE where table_name='SCARAB_DEPEND';
insert into ID_TABLE (id_table_id, table_name, next_id, quantity) select 28, 'SCARAB_DEPEND', count(*) + 1, 10 from xxxx_SCARAB_DEPEND;


drop table SCARAB_DEPEND;

RENAME xxxx_SCARAB_DEPEND TO SCARAB_DEPEND;

CREATE INDEX IX_DEPEND_OBSERVED ON 
    SCARAB_DEPEND (OBSERVED_ID, DEPEND_TYPE_ID);

CREATE INDEX IX_DEPEND_OBSERVER ON 
    SCARAB_DEPEND (OBSERVER_ID, DEPEND_TYPE_ID);

alter table SCARAB_ACTIVITY add DEPEND_ID NUMBER NULL;
alter table SCARAB_ACTIVITY add FOREIGN KEY (DEPEND_ID) REFERENCES SCARAB_DEPEND(DEPEND_ID);
