@echo off
mysqladmin drop scarab
echo y

mysqladmin create scarab

echo "Importing MySQL_users_roles_permissions.sql..."
mysql scarab < MySQL_users_roles_permissions.sql

echo "Importing MySQL_id_table.sql..."
mysql scarab < MySQL_id_table.sql

echo "Importing id_broker_init.sql..."
mysql scarab < id_broker_init.sql

echo "Importing scarab-mysql.sql..."
mysql scarab < scarab-mysql.sql

echo "Importing default_roles_permissions.sql..."
mysql scarab < default_roles_permissions.sql

echo "Importing MySQL_default_data.sql..."
mysql scarab < MySQL_default_data.sql

echo "Importing MySQL_sample_data.sql..."
mysql scarab < MySQL_sample_data.sql
