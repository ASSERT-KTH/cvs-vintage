## 
## This table provides the primary keys for all
## other tables in the system.  Should be used
## with util.db.IDBroker.
##
drop table if exists ID_TABLE;

CREATE TABLE ID_TABLE 
(
  ID_TABLE_ID int(11) NOT NULL,
  TABLE_NAME varchar(255) NOT NULL,
  NEXT_ID int(11),
  QUANTITY int(11),
  PRIMARY KEY(ID_TABLE_ID),
  UNIQUE (TABLE_NAME)
);

insert into ID_TABLE (id_table_id, table_name, next_id, quantity)
 values (1, 'Permission', 20, 10);
insert into ID_TABLE (id_table_id, table_name, next_id, quantity)
 values (2, 'UserRole', 20, 10);
insert into ID_TABLE (id_table_id, table_name, next_id, quantity)
 values (3, 'Visitor', 20, 10);
insert into ID_TABLE (id_table_id, table_name, next_id, quantity)
 values (4, 'Jobentry', 20, 10);
