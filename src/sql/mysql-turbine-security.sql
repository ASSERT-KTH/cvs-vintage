# Script to fill the tables with default roles and permissions
# Currently tested with MySQL, Oracle, Postgres and Hypersonic only.

# Create the global group
# this group is used to assign system-wide roles to users
# this table is not used in Scarab it is replace by the Module table 
# INSERT INTO TURBINE_GROUP (GROUP_ID, GROUP_NAME) VALUES (1,'global');

# Create the root role

INSERT INTO TURBINE_ROLE (ROLE_ID, ROLE_NAME) VALUES (1, 'turbine_root');
INSERT INTO TURBINE_ROLE (ROLE_ID, ROLE_NAME) VALUES (2, 'Developer');

# Create an account 'turbine' for system administartor
# Remeber to set a good password for this user in a production system!

INSERT INTO TURBINE_USER 
    (USER_ID, LOGIN_NAME, PASSWORD_VALUE, FIRST_NAME, LAST_NAME, CONFIRM_VALUE) 
    VALUES
    (0, 'turbine@collab.net', 'NWoZK3kTsExUV00Ywo1G5jlU', 'turbine', 'turbine', 'CONFIRMED');

# Assign the user 'turbine' a system-wide role 'turbine_root'
# this must be done after the Module have been defined so the
# sql has moved to mysql-scarab-default-data.sql 


# Add some default permissions

INSERT INTO TURBINE_PERMISSION 
    (PERMISSION_ID, PERMISSION_NAME) 
    VALUES 
    (1, 'admin_users');
INSERT INTO TURBINE_PERMISSION 
    (PERMISSION_ID, PERMISSION_NAME) 
    VALUES 
    (2, 'edit_issues');

# Add some permissions for the root role

INSERT INTO TURBINE_ROLE_PERMISSION (ROLE_ID,PERMISSION_ID) 
SELECT TURBINE_ROLE.ROLE_ID, TURBINE_PERMISSION.PERMISSION_ID FROM 
TURBINE_ROLE, TURBINE_PERMISSION
WHERE TURBINE_PERMISSION.PERMISSION_NAME = 'admin_users' AND 
TURBINE_ROLE.ROLE_NAME = 'turbine_root';

INSERT INTO TURBINE_ROLE_PERMISSION (ROLE_ID,PERMISSION_ID) 
SELECT TURBINE_ROLE.ROLE_ID, TURBINE_PERMISSION.PERMISSION_ID FROM 
TURBINE_ROLE, TURBINE_PERMISSION
WHERE TURBINE_PERMISSION.PERMISSION_NAME = 'edit_issues' AND 
TURBINE_ROLE.ROLE_NAME = 'Developer';


