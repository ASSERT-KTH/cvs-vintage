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
 * $Id: postgresql-upgrade-1.0b13-1.0b14-8.sql,v 1.1 2003/02/03 07:36:39 jon Exp $
 */

drop table xxxx_SCARAB_DEPEND;
drop SEQUENCE depend_id_seq;

/* create a temporary table. */
create table xxxx_SCARAB_DEPEND  (
    DEPEND_ID       integer NOT NULL PRIMARY KEY default nextval('depend_id_seq'),
    OBSERVED_ID		integer NOT NULL,
    OBSERVER_ID		integer NOT NULL,
    DEPEND_TYPE_ID  integer NOT NULL,
    DELETED         integer NULL,
    FOREIGN KEY (OBSERVED_ID) REFERENCES SCARAB_ISSUE (ISSUE_ID),
    FOREIGN KEY (OBSERVER_ID) REFERENCES SCARAB_ISSUE (ISSUE_ID),
    FOREIGN KEY (DEPEND_TYPE_ID) REFERENCES SCARAB_DEPEND_TYPE (DEPEND_TYPE_ID)
);

create sequence depend_id_seq;

insert into xxxx_SCARAB_DEPEND (OBSERVED_ID, OBSERVER_ID, DEPEND_TYPE_ID, DELETED)
    select OBSERVED_ID, OBSERVER_ID, DEPEND_TYPE_ID, DELETED
        from SCARAB_DEPEND;

/* 
 * remove the autoincrement and update the id_table to be after the last entry
 */
alter table xxxx_SCARAB_DEPEND ALTER DEPEND_ID DROP default;
drop SEQUENCE depend_id_seq;
delete from ID_TABLE where table_name='SCARAB_DEPEND';
insert into ID_TABLE (id_table_id, table_name, next_id, quantity) select 28, 'SCARAB_DEPEND', count(*) + 1, 10 from xxxx_SCARAB_DEPEND;


drop table SCARAB_DEPEND;

alter table xxxx_SCARAB_DEPEND rename to SCARAB_DEPEND;

/* 
 * Rename the index for the primary key
 */
alter table SCARAB_DEPEND drop constraint xxxx_scarab_depend_pkey;
alter table SCARAB_DEPEND add constraint  scarab_depend_pkey PRIMARY KEY(depend_id);

CREATE INDEX IX_DEPEND_OBSERVED ON 
    SCARAB_DEPEND (OBSERVED_ID, DEPEND_TYPE_ID);

CREATE INDEX IX_DEPEND_OBSERVER ON 
    SCARAB_DEPEND (OBSERVER_ID, DEPEND_TYPE_ID);

alter table SCARAB_ACTIVITY add column DEPEND_ID INTEGER NULL;
alter table SCARAB_ACTIVITY add FOREIGN KEY (DEPEND_ID) REFERENCES SCARAB_DEPEND(DEPEND_ID);
