
delete from ID_TABLE where id_table_id >= 1;

insert into ID_TABLE (table_name, next_id, quantity) 
       VALUES ('TURBINE_PERMISSION', 100, 10);
insert into ID_TABLE (table_name, next_id, quantity) 
       VALUES ('TURBINE_ROLE', 100, 10);
insert into ID_TABLE (table_name, next_id, quantity) 
       VALUES ('TURBINE_GROUP', 100, 10);
insert into ID_TABLE (table_name, next_id, quantity) 
       VALUES ('TURBINE_ROLE_PERMISSION', 100, 10);
insert into ID_TABLE (table_name, next_id, quantity) 
       VALUES ('TURBINE_USER', 100, 10);
insert into ID_TABLE (table_name, next_id, quantity) 
       VALUES ('TURBINE_USER_GROUP_ROLE', 100, 10);
insert into ID_TABLE (table_name, next_id, quantity) 
       VALUES ('TURBINE_SCHEDULED_JOB', 100, 10);

