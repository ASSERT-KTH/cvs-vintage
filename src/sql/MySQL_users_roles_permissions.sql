## This is a schema for MySQL for 
## the concept of Users/Roles/Permissions
##
## Please see the Html documentation for more information 
## on this SQL. Equivalent contributions of this for other 
## databases are welcome.

## The default database should be named "Turbine"
## This is created with the command "mysqladmin create Turbine"
#drop database if exists Turbine;
#create database Turbine;

## This schema is imported into MySQL with the command:
## mysql Turbine < MySQL_users_roles_permissions.sql


drop table if exists Permission;

create table Permission  (
    PERMISSIONID        integer AUTO_INCREMENT PRIMARY KEY,
    PERMISSION          varchar (99) NOT NULL,
    UNIQUE (PERMISSION)
);



drop table if exists UserRole;

create table UserRole  (
    ROLEID      integer AUTO_INCREMENT PRIMARY KEY,
    ROLENAME    varchar (99) NOT NULL,
    UNIQUE (ROLENAME)
);



drop table if exists RolePermission;

create table RolePermission  (
    ROLEID              integer NOT NULL,
    PERMISSIONID        integer NOT NULL,
    PRIMARY KEY (ROLEID, PERMISSIONID),
    INDEX roleID_permissionID_index ( ROLEID, PERMISSIONID )
);



drop table if exists Visitor;

create table Visitor(
    VISITORID   integer AUTO_INCREMENT PRIMARY KEY,
    LOGINID     varchar (32) NOT NULL,
    PASSWORD_VALUE    varchar (32) NOT NULL,
    FIRST_NAME  varchar (99) NOT NULL,
    LAST_NAME   varchar (99) NOT NULL,
    ADDRESS1    varchar (255),
    ADDRESS2    varchar (255),
    CITY        varchar (255),
    STATE       varchar (32),
    POSTALCODE  varchar (32),
    COUNTRY     varchar (99),
    CITIZENSHIP varchar (32),
    PHONE       varchar (32),
    ALTPHONE    varchar (32),
    FAX         varchar (32),
    CELL        varchar (32),
    PAGER       varchar (32),
    EMAIL       varchar (99),
    MODIFIED    timestamp,
    CREATED     datetime,
    LASTLOGIN   timestamp,
    OBJECTDATA  mediumblob,
    UNIQUE (LOGINID)
);

alter table Visitor add PREFIX_NAME varchar (16);
alter table Visitor add MIDDLE_NAME varchar (99);
alter table Visitor add SUFFIX_NAME varchar (16);
alter table Visitor add COMPANY     varchar (255);
alter table Visitor add CONFIRM_VALUE varchar (99);

drop table if exists VisitorRole;

create table VisitorRole  (
    VISITORID   integer NOT NULL,
    ROLEID      integer NOT NULL,
    PRIMARY KEY (VISITORID, ROLEID),
    INDEX visitorID_roleID_index ( VISITORID, ROLEID )
);

drop table if exists Jobentry;

create table Jobentry  (
    JOB_ID	          int(11) NOT NULL PRIMARY KEY,
    MINUTE        integer default -1 NOT NULL,
    HOUR          integer default -1 NOT NULL,
    WEEKDAY       integer default -1 NOT NULL,
    DAY_OF_MONTH  integer default -1 NOT NULL,
    TASK          varchar(99) NOT NULL,
    EMAIL         varchar(99)
);







