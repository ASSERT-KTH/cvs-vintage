/*
 * Add the 'Module | Configure' permission
 * to the 'project owner' and 'root roles'.
 *
 * Created By: Jon Scott Stevens
 */

INSERT INTO TURBINE_PERMISSION (PERMISSION_ID, PERMISSION_NAME) 
    VALUES (17, 'Module | Configure');

/* create a temporary table. */
create table xxxx_populate_RolePermission  (
    ROLE_ID		integer NOT NULL,
    PERMISSION_ID	        integer NOT NULL
);

delete from xxxx_populate_RolePermission;

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

/*
 *  ROOT ROLE
 *  Root has all project permissions of project owner.
 */
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
