---------------------------------------------------------------------------
-- Script to fill the tables with default roles and permissions
-- Currently tested with MySQL, Oracle, Postgres and Hypersonic only.
---------------------------------------------------------------------------

---------------------------------------------------------------------------
-- Create the global group
-- this group is used to assign system-wide roles to users
---------------------------------------------------------------------------

INSERT INTO TURBINE_GROUP (GROUP_ID, GROUP_NAME) VALUES (1,'global');

---------------------------------------------------------------------------
-- Create the root role
---------------------------------------------------------------------------

INSERT INTO TURBINE_ROLE (ROLE_ID, ROLE_NAME) VALUES (1, 'turbine_root');

---------------------------------------------------------------------------
-- Create an account 'turbine' for system administartor
-- Remeber to set a good password for this user in a production system!
---------------------------------------------------------------------------

INSERT INTO TURBINE_USER 
    (LOGIN_NAME, PASSWORD_VALUE, FIRST_NAME, LAST_NAME) 
    VALUES
    ('turbine', 'turbine', 'turbine', 'turbine');

---------------------------------------------------------------------------
-- Assign the user 'turbine' a system-wide role 'turbine_root'
---------------------------------------------------------------------------
INSERT INTO TURBINE_USER_GROUP_ROLE ( USER_ID, GROUP_ID, ROLE_ID ) 
SELECT TURBINE_USER.USER_ID, TURBINE_GROUP.GROUP_ID, TURBINE_ROLE.ROLE_ID from 
TURBINE_USER, TURBINE_GROUP, TURBINE_ROLE 
WHERE TURBINE_USER.LOGIN_NAME = 'turbine' AND 
TURBINE_GROUP.GROUP_NAME = 'global' AND TURBINE_ROLE.ROLE_NAME = 'turbine_root';

---------------------------------------------------------------------------
-- Add some default permissions
---------------------------------------------------------------------------

INSERT INTO TURBINE_PERMISSION 
    (PERMISSION_ID, PERMISSION_NAME) 
    VALUES 
    (1, 'admin_users');

---------------------------------------------------------------------------
-- Add some permissions for the root role
---------------------------------------------------------------------------
INSERT INTO TURBINE_ROLE_PERMISSION (ROLE_ID,PERMISSION_ID) 
SELECT TURBINE_ROLE.ROLE_ID, TURBINE_PERMISSION.PERMISSION_ID FROM 
TURBINE_ROLE, TURBINE_PERMISSION
WHERE TURBINE_PERMISSION.PERMISSION_NAME = 'admin_users' AND 
TURBINE_ROLE.ROLE_NAME = 'turbine_root';

-- Use this script to bootstrap you system into usage of secure passwords.
-- It will set the administrator's password to 'turbine' (using SHA digest)
-- Then, you'll be able to log in and change administrator's passwords,
-- and then set initial passwords for your users.

UPDATE TURBINE_USER SET PASSWORD_VALUE = '4vUZ7HvhmiKt/W54Xyuz7oza' WHERE LOGIN_NAME = 'turbine';

