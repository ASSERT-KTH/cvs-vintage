-- Create an account for system administrator (also used for initial
-- data population, etc.).
-- Remember to set a good password for this user in a production system!

INSERT INTO TURBINE_USER (USER_ID, LOGIN_NAME, PASSWORD_VALUE, FIRST_NAME, LAST_NAME, EMAIL, CONFIRM_VALUE) 
    VALUES (1, '@ADMIN_USERNAME@', 'NWoZK3kTsExUV00Ywo1G5jlUKKs=', 'Scarab', 'Administrator', 'Administrator@scarab.example.org', 'CONFIRMED');


-- Script to fill the tables with default roles and permissions

INSERT INTO TURBINE_ROLE (ROLE_ID, ROLE_NAME) VALUES (2, 'Partner');
INSERT INTO TURBINE_ROLE (ROLE_ID, ROLE_NAME) VALUES (3, 'Observer');
INSERT INTO TURBINE_ROLE (ROLE_ID, ROLE_NAME) VALUES (4, 'Developer');
INSERT INTO TURBINE_ROLE (ROLE_ID, ROLE_NAME) VALUES (5, 'QA');
INSERT INTO TURBINE_ROLE (ROLE_ID, ROLE_NAME) VALUES (6, 'Project Owner');
INSERT INTO TURBINE_ROLE (ROLE_ID, ROLE_NAME) VALUES (7, 'Root');

-- Add some default permissions

INSERT INTO TURBINE_PERMISSION (PERMISSION_ID, PERMISSION_NAME) 
    VALUES (1, 'Issue | Edit');
INSERT INTO TURBINE_PERMISSION (PERMISSION_ID, PERMISSION_NAME) 
    VALUES (2, 'Issue | Enter');
INSERT INTO TURBINE_PERMISSION (PERMISSION_ID, PERMISSION_NAME) 
    VALUES (3, 'Module | Edit');
INSERT INTO TURBINE_PERMISSION (PERMISSION_ID, PERMISSION_NAME) 
    VALUES (4, 'Module | Add');
INSERT INTO TURBINE_PERMISSION (PERMISSION_ID, PERMISSION_NAME) 
    VALUES (5, 'Domain | Edit');
INSERT INTO TURBINE_PERMISSION (PERMISSION_ID, PERMISSION_NAME) 
    VALUES (6, 'Item | Approve');
INSERT INTO TURBINE_PERMISSION (PERMISSION_ID, PERMISSION_NAME) 
    VALUES (7, 'Item | Delete');
INSERT INTO TURBINE_PERMISSION (PERMISSION_ID, PERMISSION_NAME) 
    VALUES (8, 'Issue | Assign');
INSERT INTO TURBINE_PERMISSION (PERMISSION_ID, PERMISSION_NAME) 
    VALUES (9, 'Vote | Manage');
INSERT INTO TURBINE_PERMISSION (PERMISSION_ID, PERMISSION_NAME) 
    VALUES (10, 'Issue | Attach');
INSERT INTO TURBINE_PERMISSION (PERMISSION_ID, PERMISSION_NAME) 
    VALUES (11, 'User | Edit Preferences');
INSERT INTO TURBINE_PERMISSION (PERMISSION_ID, PERMISSION_NAME) 
    VALUES (12, 'Issue | Search');
INSERT INTO TURBINE_PERMISSION (PERMISSION_ID, PERMISSION_NAME) 
    VALUES (13, 'Issue | View');
INSERT INTO TURBINE_PERMISSION (PERMISSION_ID, PERMISSION_NAME) 
    VALUES (14, 'Domain | Admin');
INSERT INTO TURBINE_PERMISSION (PERMISSION_ID, PERMISSION_NAME) 
    VALUES (17, 'Module | Configure');
-- User with this permission is allowed to approve roles requested by other
-- users.

INSERT INTO TURBINE_PERMISSION (PERMISSION_ID, PERMISSION_NAME) 
    VALUES (18, 'User | Approve Roles');
INSERT INTO TURBINE_PERMISSION (PERMISSION_ID, PERMISSION_NAME) 
    VALUES (19, 'Issue | Move');



-- create a temporary table.
create table xxxx_populate_RolePermission  (
    ROLE_ID		integer NOT NULL,
    PERMISSION_ID	        integer NOT NULL
);

delete from xxxx_populate_RolePermission;

--  PARTNER ROLE

insert into TURBINE_ROLE_PERMISSION (ROLE_ID, PERMISSION_ID)
       select  TURBINE_ROLE.ROLE_ID, TURBINE_PERMISSION.PERMISSION_ID
         from  TURBINE_ROLE, TURBINE_PERMISSION
         where TURBINE_ROLE.ROLE_NAME = 'Partner'
           and TURBINE_PERMISSION.PERMISSION_NAME in (
                  'User | Edit Preferences',
                  'Issue | Attach',
                  'Issue | Search',
                  'Issue | View')
;

--  OBSERVER ROLE
--  Observer has all project permissions of partner.

insert into xxxx_populate_RolePermission
	       select  ToRole.ROLE_ID, ToCopy.PERMISSION_ID
         from  TURBINE_ROLE FromRole, TURBINE_ROLE ToRole,
               TURBINE_ROLE_PERMISSION ToCopy
         where ToCopy.ROLE_ID = FromRole.ROLE_ID
	   and FromRole.ROLE_NAME = 'Partner'
	   and ToRole.ROLE_NAME = 'Observer'
;

insert into TURBINE_ROLE_PERMISSION 
	select * from xxxx_populate_RolePermission;
delete from xxxx_populate_RolePermission;

insert into TURBINE_ROLE_PERMISSION (ROLE_ID, PERMISSION_ID)
       select  TURBINE_ROLE.ROLE_ID, TURBINE_PERMISSION.PERMISSION_ID
         from  TURBINE_ROLE, TURBINE_PERMISSION
         where TURBINE_ROLE.ROLE_NAME = 'Observer'
           and TURBINE_PERMISSION.PERMISSION_NAME = 'Issue | Enter'
;

--  DEVELOPER ROLE
--  Developer has all project permissions of observer.

insert into xxxx_populate_RolePermission
	       select  ToRole.ROLE_ID, ToCopy.PERMISSION_ID
         from  TURBINE_ROLE FromRole, TURBINE_ROLE ToRole,
               TURBINE_ROLE_PERMISSION ToCopy
         where ToCopy.ROLE_ID = FromRole.ROLE_ID
	   and FromRole.ROLE_NAME = 'Observer'
	   and ToRole.ROLE_NAME = 'Developer'
;
insert into TURBINE_ROLE_PERMISSION 
	select * from xxxx_populate_RolePermission;
delete from xxxx_populate_RolePermission;

insert into TURBINE_ROLE_PERMISSION (ROLE_ID, PERMISSION_ID)
       select  TURBINE_ROLE.ROLE_ID, TURBINE_PERMISSION.PERMISSION_ID
         from  TURBINE_ROLE, TURBINE_PERMISSION
         where TURBINE_ROLE.ROLE_NAME = 'Developer'
           and TURBINE_PERMISSION.PERMISSION_NAME in (
                'Module | Add',
                'Issue | Edit',
                'Issue | Move',
                'Issue | Assign')
;

--  QA ROLE
--  QA has all project permissions of developer.

insert into xxxx_populate_RolePermission
	       select  ToRole.ROLE_ID, ToCopy.PERMISSION_ID
         from  TURBINE_ROLE FromRole, TURBINE_ROLE ToRole,
               TURBINE_ROLE_PERMISSION ToCopy
         where ToCopy.ROLE_ID = FromRole.ROLE_ID
	   and FromRole.ROLE_NAME = 'Developer'
	   and ToRole.ROLE_NAME = 'QA'
;
insert into TURBINE_ROLE_PERMISSION 
	select * from xxxx_populate_RolePermission;
delete from xxxx_populate_RolePermission;

--  PROJECT OWNER ROLE
--  Project Owner has all project permissions of developer.

insert into xxxx_populate_RolePermission
	       select  ToRole.ROLE_ID, ToCopy.PERMISSION_ID
         from  TURBINE_ROLE FromRole, TURBINE_ROLE ToRole,
               TURBINE_ROLE_PERMISSION ToCopy
         where ToCopy.ROLE_ID = FromRole.ROLE_ID
	   and FromRole.ROLE_NAME = 'Developer'
	   and ToRole.ROLE_NAME = 'Project Owner'
;
insert into TURBINE_ROLE_PERMISSION 
	select * from xxxx_populate_RolePermission;
delete from xxxx_populate_RolePermission;

insert into TURBINE_ROLE_PERMISSION (ROLE_ID, PERMISSION_ID)
       select  TURBINE_ROLE.ROLE_ID, TURBINE_PERMISSION.PERMISSION_ID
         from  TURBINE_ROLE, TURBINE_PERMISSION
         where TURBINE_ROLE.ROLE_NAME = 'Project Owner'
           and TURBINE_PERMISSION.PERMISSION_NAME in (
                'User | Approve Roles',
                'Module | Edit',
                'Module | Configure',
                'Item | Approve',
                'Item | Delete',
                'Vote | Manage')
;

--  ROOT ROLE
--  Root has all project permissions of project owner.

insert into xxxx_populate_RolePermission
	       select  ToRole.ROLE_ID, ToCopy.PERMISSION_ID
         from  TURBINE_ROLE FromRole, TURBINE_ROLE ToRole,
               TURBINE_ROLE_PERMISSION ToCopy
         where ToCopy.ROLE_ID = FromRole.ROLE_ID
	   and FromRole.ROLE_NAME = 'Project Owner'
	   and ToRole.ROLE_NAME = 'Root'
;
insert into TURBINE_ROLE_PERMISSION 
	select * from xxxx_populate_RolePermission;
delete from xxxx_populate_RolePermission;

insert into TURBINE_ROLE_PERMISSION (ROLE_ID, PERMISSION_ID)
       select  TURBINE_ROLE.ROLE_ID, TURBINE_PERMISSION.PERMISSION_ID
         from  TURBINE_ROLE, TURBINE_PERMISSION
         where TURBINE_ROLE.ROLE_NAME = 'Root'
           and TURBINE_PERMISSION.PERMISSION_NAME in (
                'Domain | Admin',
                'Domain | Edit')
;


drop table xxxx_populate_RolePermission;


-- Assign the user '@ADMIN_USERNAME@' a system-wide role 'Root'

INSERT INTO TURBINE_USER_GROUP_ROLE ( USER_ID, GROUP_ID, ROLE_ID ) 
SELECT TURBINE_USER.USER_ID, SCARAB_MODULE.MODULE_ID, TURBINE_ROLE.ROLE_ID from 
TURBINE_USER, SCARAB_MODULE, TURBINE_ROLE 
WHERE TURBINE_USER.LOGIN_NAME = '@ADMIN_USERNAME@' AND 
SCARAB_MODULE.MODULE_ID = 0
AND TURBINE_ROLE.ROLE_NAME in ('Root');
