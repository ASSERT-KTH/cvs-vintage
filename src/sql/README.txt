---------------------------------------------------------------------
| Steps to update the SQL Schema for Scarab                         |
---------------------------------------------------------------------
$Id: README.txt,v 1.2 2001/02/23 23:54:58 jon Exp $

We currently use Torque as our system for building the .sql files for Scarab. 
This means that our initial schema is defined in a .xml file and is then 
"transformed" (by using Torque) into a .sql file for the appropriate database 
vendor. This has the advantage of being able to not only easily support 
multiple databases, but also generate Java Object Relational mapping code 
around our schema.

Below are the instructions for re-generating updated .sql files. Because we are 
currently in heavy development mode, we are focusing on supporting MySQL as our 
primary database. Therefore, our documentation will be reflecting that fact. 
Adding support for other databases is as easy as modifying step #3 below to 
generate schemas for other databases.

#1. Edit schema.xml and change the table definitions.

The following may change as we integrate better with Torque.

#2. Copy schema.xml into a standalone version of Torque* as
    <torque-dir>/schema/scarab-schema.xml

#3. Edit <torque-dir>/config/torque.props to set the project name to
    scarab and the target database as well as set a few other defaults.

    project=scarab
    database=mysql
    targetPackage=org.tigris.scarab.baseom
    addSaveMethod=true
    addGetByNameMethod=true
    objectModelType=complex

#4. cd <torque-dir>; ./torque.sh project-sql

#5. Replace the appropriate database sql in this directory 
    (e.g. scarab-mysql.sql) with <torque-dir>/output/scarab-schema.sql 

* To create a standalone version of Torque, check Turbine out of CVS.
       cd turbine/build; ./build-turbine.sh torque
  This will build a standalone version of Torque in turbine/bin/torque/torque.

If you have questions or comments please subscribe to the Scarab mailing list
and ask there. <http://scarab.tigris.org/>

Thanks

-The Scarab Team 
