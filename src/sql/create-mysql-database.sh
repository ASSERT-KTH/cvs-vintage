#!/bin/sh

# testing if the base already exists and removing it if needed.
base_exists=1
mysqlshow scarab > /dev/null 2>&1 || base_exists=0
if [ $base_exists -eq 1 ] ; then
	echo "Removing existing database. All data will be lost."
        echo y | mysqladmin drop scarab > /dev/null
fi

# Creating new base and inputting default data

echo "Creating Database..."        
mysqladmin create scarab

echo "Importing mysql-scarab.sql..."
mysql scarab < mysql-scarab.sql

echo "Importing mysql-turbine.sql..."
mysql scarab < mysql-turbine.sql

echo "Importing mysql-id-table-schema.sql..."
mysql scarab < mysql-id-table-schema.sql

echo "Importing mysql-turbine-id-table-init.sql..."
mysql scarab < mysql-turbine-id-table-init.sql

echo "Importing mysql-turbine-security.sql..."
mysql scarab < mysql-turbine-security.sql

echo "Importing mysql-scarab-id-table-init.sql..."
mysql scarab < mysql-scarab-id-table-init.sql

echo "Importing mysql-scarab-default-data.sql..."
mysql scarab < mysql-scarab-default-data.sql

echo "Importing mysql-scarab-sample-data.sql..."
mysql scarab < mysql-scarab-sample-data.sql
