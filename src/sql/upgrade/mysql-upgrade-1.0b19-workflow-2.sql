/*
 * b19-Workflow Migration
 *
 * Sets the id_table record for scarab_transition.
 *
 */
delete from ID_TABLE where table_name = 'SCARAB_TRANSITION';
insert into ID_TABLE (id_table_id, table_name, next_id, quantity) VALUES (100, 'SCARAB_TRANSITION', 100, 10);
delete from ID_TABLE where table_name = 'SCARAB_CONDITION';
insert into ID_TABLE (id_table_id, table_name, next_id, quantity) VALUES (101, 'SCARAB_CONDITION', 100, 10);

