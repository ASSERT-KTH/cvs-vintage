-- Create an account for system administrator (also used for initial
-- data population, etc.).
-- Remember to set a good password for this user in a production system!

INSERT INTO TURBINE_USER (USER_ID, LOGIN_NAME, PASSWORD_VALUE, FIRST_NAME, LAST_NAME, EMAIL, CONFIRM_VALUE) 
    VALUES (9, '@ANONYMOUS_USERNAME@', 'NWoZK3kTsExUV00Ywo1G5jlUKKs=', 'Scarab', 'anonymous', 'anonymous@scarab.example.org', 'CONFIRMED');


-- Script to fill the tables with default roles and permissions

INSERT INTO TURBINE_ROLE (ROLE_ID, ROLE_NAME) VALUES (8, 'Anonymous');

-- create a temporary table.
create table xxxx_populate_RolePermission  (
    ROLE_ID		integer NOT NULL,
    PERMISSION_ID	        integer NOT NULL
);

delete from xxxx_populate_RolePermission;

--  ANONYMOUS ROLE
--  ANonymous has all project permissions of Observer.

insert into xxxx_populate_RolePermission
	       select  ToRole.ROLE_ID, ToCopy.PERMISSION_ID
         from  TURBINE_ROLE FromRole, TURBINE_ROLE ToRole,
               TURBINE_ROLE_PERMISSION ToCopy
         where ToCopy.ROLE_ID = FromRole.ROLE_ID
	   and FromRole.ROLE_NAME = 'Observer'
	   and ToRole.ROLE_NAME = 'Anonymous'
;

insert into TURBINE_ROLE_PERMISSION 
	select * from xxxx_populate_RolePermission;
delete from xxxx_populate_RolePermission;

drop table xxxx_populate_RolePermission;


-- Assign the user '@ANONYMOUS_USERNAME@' a system-wide role 'Anonymous'

INSERT INTO TURBINE_USER_GROUP_ROLE ( USER_ID, GROUP_ID, ROLE_ID ) 
SELECT TURBINE_USER.USER_ID, SCARAB_MODULE.MODULE_ID, TURBINE_ROLE.ROLE_ID from 
TURBINE_USER, SCARAB_MODULE, TURBINE_ROLE 
WHERE TURBINE_USER.LOGIN_NAME = '@ANONYMOUS_USERNAME@' AND 
SCARAB_MODULE.MODULE_ID = 0
AND TURBINE_ROLE.ROLE_NAME in ('Anonymous');