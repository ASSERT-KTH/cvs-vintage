/*
 * This upgrade script adds a column to the SCARAB_R_MODULE_USER_ATTRIBUTE
 * table which is a new primary key and adds an entry for the table to
 * the ID_TABLE.  This is to allow some of the previous pk columns to be
 * null
 *
 * Created By: John McNally
 */

# create a new table that will add a pk to SCARAB_R_MODULE_USER_ATTRIBUTE
drop table if exists tmp_RMUA;
CREATE TABLE tmp_RMUA
(
    RMUA_ID INTEGER NOT NULL AUTO_INCREMENT,
    LIST_ID INTEGER,
    MODULE_ID INTEGER,
    USER_ID INTEGER NOT NULL,
    ISSUE_TYPE_ID INTEGER,
    ATTRIBUTE_ID INTEGER NOT NULL,
    PREFERRED_ORDER INTEGER default 0 NOT NULL,
    PRIMARY KEY(RMUA_ID),
    FOREIGN KEY (LIST_ID) REFERENCES SCARAB_MIT_LIST (LIST_ID),
    FOREIGN KEY (MODULE_ID) REFERENCES SCARAB_MODULE (MODULE_ID),
    FOREIGN KEY (USER_ID) REFERENCES TURBINE_USER (USER_ID),
    FOREIGN KEY (ISSUE_TYPE_ID) REFERENCES SCARAB_ISSUE_TYPE (ISSUE_TYPE_ID),
    FOREIGN KEY (ATTRIBUTE_ID) REFERENCES SCARAB_ATTRIBUTE (ATTRIBUTE_ID)
);

#copy the data from the old table into the tmp table
insert into tmp_RMUA (MODULE_ID, USER_ID, ISSUE_TYPE_ID, ATTRIBUTE_ID, PREFERRED_ORDER)
	select MODULE_ID, USER_ID, ISSUE_TYPE_ID, ATTRIBUTE_ID, PREFERRED_ORDER from SCARAB_R_MODULE_USER_ATTRIBUTE;

#remove the auto-increment
ALTER TABLE tmp_RMUA CHANGE RMUA_ID RMUA_ID INTEGER NOT NULL;

#create ID_TABLE entry 
insert into ID_TABLE (id_table_id, table_name, next_id, quantity)
 select 46, 'SCARAB_R_MODULE_USER_ATTRIBUTE', count(*) + 1, 10 from SCARAB_R_MODULE_USER_ATTRIBUTE;

#swap the new table for the old
drop table SCARAB_R_MODULE_USER_ATTRIBUTE;
alter table tmp_RMUA rename SCARAB_R_MODULE_USER_ATTRIBUTE;