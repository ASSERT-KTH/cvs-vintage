-- Script to fill the tables with default roles and permissions
-- Currently tested with MySQL, Oracle, Postgres and Hypersonic only.

-- Add some default permissions

insert into Permission (PERMISSIONID, PERMISSION) values (1, 'view_user');
insert into Permission (PERMISSIONID, PERMISSION) values (2, 'add_user');
insert into Permission (PERMISSIONID, PERMISSION) values (3, 'modify_user');
insert into Permission (PERMISSIONID, PERMISSION) values (4, 'view_group');
insert into Permission (PERMISSIONID, PERMISSION) values (5, 'add_group');
insert into Permission (PERMISSIONID, PERMISSION) values (6, 'modify_group');
insert into Permission (PERMISSIONID, PERMISSION) values (7, 'view_permission');
insert into Permission (PERMISSIONID, PERMISSION) values (8, 'add_permission');
insert into Permission (PERMISSIONID, PERMISSION) values (9, 'modify_permission');
insert into Permission (PERMISSIONID, PERMISSION) values (10, 'view_role');
insert into Permission (PERMISSIONID, PERMISSION) values (11, 'add_role');
insert into Permission (PERMISSIONID, PERMISSION) values (12, 'modify_role');

-- Create a Role or Group in this case turbine root
insert into UserRole (ROLEID, ROLENAME) values (1, 'turbine_root');

-- Add some permissions for turbine root
insert into RolePermission (ROLEID,PERMISSIONID) select UserRole.ROLEID, Permission.PERMISSIONID from UserRole, Permission where Permission.PERMISSION = 'view_user' and  UserRole.ROLENAME = 'turbine_root';

insert into RolePermission (ROLEID,PERMISSIONID) select UserRole.ROLEID, Permission.PERMISSIONID from UserRole, Permission where Permission.PERMISSION = 'add_user' and  UserRole.ROLENAME = 'turbine_root';

insert into RolePermission (ROLEID,PERMISSIONID) select UserRole.ROLEID, Permission.PERMISSIONID from UserRole, Permission where Permission.PERMISSION = 'modify_user' and  UserRole.ROLENAME = 'turbine_root';

insert into RolePermission (ROLEID,PERMISSIONID) select UserRole.ROLEID, Permission.PERMISSIONID from UserRole, Permission where Permission.PERMISSION = 'view_group' and  UserRole.ROLENAME = 'turbine_root';

insert into RolePermission (ROLEID,PERMISSIONID) select UserRole.ROLEID, Permission.PERMISSIONID from UserRole, Permission where Permission.PERMISSION = 'add_group' and  UserRole.ROLENAME = 'turbine_root';

insert into RolePermission (ROLEID,PERMISSIONID) select UserRole.ROLEID, Permission.PERMISSIONID from UserRole, Permission where Permission.PERMISSION = 'modify_group' and  UserRole.ROLENAME = 'turbine_root';

insert into RolePermission (ROLEID,PERMISSIONID) select UserRole.ROLEID, Permission.PERMISSIONID from UserRole, Permission where Permission.PERMISSION = 'view_permission' and  UserRole.ROLENAME = 'turbine_root';

insert into RolePermission (ROLEID,PERMISSIONID) select UserRole.ROLEID, Permission.PERMISSIONID from UserRole, Permission where Permission.PERMISSION = 'add_permission' and  UserRole.ROLENAME = 'turbine_root';

insert into RolePermission (ROLEID,PERMISSIONID) select UserRole.ROLEID, Permission.PERMISSIONID from UserRole, Permission where Permission.PERMISSION = 'modify_permission' and  UserRole.ROLENAME = 'turbine_root';

insert into RolePermission (ROLEID,PERMISSIONID) select UserRole.ROLEID, Permission.PERMISSIONID from UserRole, Permission where Permission.PERMISSION = 'view_role' and  UserRole.ROLENAME = 'turbine_root';

insert into RolePermission (ROLEID,PERMISSIONID) select UserRole.ROLEID, Permission.PERMISSIONID from UserRole, Permission where Permission.PERMISSION = 'add_role' and  UserRole.ROLENAME = 'turbine_root';

insert into RolePermission (ROLEID,PERMISSIONID) select UserRole.ROLEID, Permission.PERMISSIONID from UserRole, Permission where Permission.PERMISSION = 'modify_role' and  UserRole.ROLENAME = 'turbine_root';



-- Create an account 'turbine' for system administartor
-- Remeber to set a good password for this user in a production system!
insert into Visitor (LOGINID, PASSWORD_VALUE, FIRST_NAME, LAST_NAME, MODIFIED, CREATED ) values ('turbine', 'turbine', 'turbine', 'turbine', null, null );

-- Finally, add the user turbine to the turbine_root group
insert into VisitorRole ( VISITORID, ROLEID ) select Visitor.VISITORID, UserRole.ROLEID from Visitor, UserRole where Visitor.LOGINID = 'turbine' AND UserRole.ROLENAME = 'turbine_root';

