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
 * $Id: mysql-upgrade-1.0b13-1.0b14-8.sql,v 1.4 2002/12/20 05:35:07 jmcnally Exp $
 */

drop table if exists xxxx_SCARAB_DEPEND;

/* create a temporary table. */
create table xxxx_SCARAB_DEPEND  (
    DEPEND_ID       integer NOT NULL AUTO_INCREMENT,
    OBSERVED_ID		integer NOT NULL,
    OBSERVER_ID		integer NOT NULL,
    DEPEND_TYPE_ID  integer NOT NULL,
    DELETED         integer(1) NULL,
    PRIMARY KEY     (DEPEND_ID),
    FOREIGN KEY (OBSERVED_ID) REFERENCES SCARAB_ISSUE (ISSUE_ID),
    FOREIGN KEY (OBSERVER_ID) REFERENCES SCARAB_ISSUE (ISSUE_ID),
    FOREIGN KEY (DEPEND_TYPE_ID) REFERENCES SCARAB_DEPEND_TYPE (DEPEND_TYPE_ID)
);

insert into xxxx_SCARAB_DEPEND (OBSERVED_ID, OBSERVER_ID, DEPEND_TYPE_ID, DELETED)
    select OBSERVED_ID, OBSERVER_ID, DEPEND_TYPE_ID, DELETED
        from SCARAB_DEPEND;

/* 
 * remove the autoincrement and update the id_table to be after the last entry
 */
alter table xxxx_SCARAB_DEPEND modify DEPEND_ID integer NOT NULL; 
delete from ID_TABLE where table_name='SCARAB_DEPEND';
insert into ID_TABLE (id_table_id, table_name, next_id, quantity) select 28, 'SCARAB_DEPEND', count(*) + 1, 10 from xxxx_SCARAB_DEPEND;


drop table SCARAB_DEPEND;

alter table xxxx_SCARAB_DEPEND rename SCARAB_DEPEND;

CREATE INDEX IX_DEPEND_OBSERVED ON 
    SCARAB_DEPEND (OBSERVED_ID, DEPEND_TYPE_ID);

CREATE INDEX IX_DEPEND_OBSERVER ON 
    SCARAB_DEPEND (OBSERVER_ID, DEPEND_TYPE_ID);

alter table SCARAB_ACTIVITY add column DEPEND_ID INTEGER NULL;
alter table SCARAB_ACTIVITY add FOREIGN KEY (DEPEND_ID) REFERENCES SCARAB_DEPEND(DEPEND_ID);
