
/* ---------------------------------------------------------------------- */
/* TURBINE_PERMISSION                                                      */
/* ---------------------------------------------------------------------- */

IF EXISTS (SELECT 1 FROM sysobjects WHERE type = 'U' AND name = 'TURBINE_PERMISSION')
BEGIN
     DECLARE @reftable_1 nvarchar(60), @constraintname_1 nvarchar(60)
     DECLARE refcursor CURSOR FOR
     select reftables.name tablename, cons.name constraintname
      from sysobjects tables,
           sysobjects reftables,
           sysobjects cons,
           sysreferences ref
       where tables.id = ref.rkeyid
         and cons.id = ref.constid
         and reftables.id = ref.fkeyid
         and tables.name = 'TURBINE_PERMISSION'
     OPEN refcursor
     FETCH NEXT from refcursor into @reftable_1, @constraintname_1
     while @@FETCH_STATUS = 0
     BEGIN
       exec ('alter table '+@reftable_1+' drop constraint '+@constraintname_1)
       FETCH NEXT from refcursor into @reftable_1, @constraintname_1
     END
     CLOSE refcursor
     DEALLOCATE refcursor
     DROP TABLE TURBINE_PERMISSION
END


CREATE TABLE TURBINE_PERMISSION
(
                    PERMISSION_ID INT NOT NULL,
                    PERMISSION_NAME VARCHAR (99) NOT NULL,

    CONSTRAINT TURBINE_PERMISSION_PK PRIMARY KEY(PERMISSION_ID),
    UNIQUE (PERMISSION_NAME));





/* ---------------------------------------------------------------------- */
/* TURBINE_ROLE                                                      */
/* ---------------------------------------------------------------------- */

IF EXISTS (SELECT 1 FROM sysobjects WHERE type = 'U' AND name = 'TURBINE_ROLE')
BEGIN
     DECLARE @reftable_2 nvarchar(60), @constraintname_2 nvarchar(60)
     DECLARE refcursor CURSOR FOR
     select reftables.name tablename, cons.name constraintname
      from sysobjects tables,
           sysobjects reftables,
           sysobjects cons,
           sysreferences ref
       where tables.id = ref.rkeyid
         and cons.id = ref.constid
         and reftables.id = ref.fkeyid
         and tables.name = 'TURBINE_ROLE'
     OPEN refcursor
     FETCH NEXT from refcursor into @reftable_2, @constraintname_2
     while @@FETCH_STATUS = 0
     BEGIN
       exec ('alter table '+@reftable_2+' drop constraint '+@constraintname_2)
       FETCH NEXT from refcursor into @reftable_2, @constraintname_2
     END
     CLOSE refcursor
     DEALLOCATE refcursor
     DROP TABLE TURBINE_ROLE
END


CREATE TABLE TURBINE_ROLE
(
                    ROLE_ID INT NOT NULL,
                    ROLE_NAME VARCHAR (99) NOT NULL,

    CONSTRAINT TURBINE_ROLE_PK PRIMARY KEY(ROLE_ID),
    UNIQUE (ROLE_NAME));





/* ---------------------------------------------------------------------- */
/* TURBINE_GROUP                                                      */
/* ---------------------------------------------------------------------- */

IF EXISTS (SELECT 1 FROM sysobjects WHERE type = 'U' AND name = 'TURBINE_GROUP')
BEGIN
     DECLARE @reftable_3 nvarchar(60), @constraintname_3 nvarchar(60)
     DECLARE refcursor CURSOR FOR
     select reftables.name tablename, cons.name constraintname
      from sysobjects tables,
           sysobjects reftables,
           sysobjects cons,
           sysreferences ref
       where tables.id = ref.rkeyid
         and cons.id = ref.constid
         and reftables.id = ref.fkeyid
         and tables.name = 'TURBINE_GROUP'
     OPEN refcursor
     FETCH NEXT from refcursor into @reftable_3, @constraintname_3
     while @@FETCH_STATUS = 0
     BEGIN
       exec ('alter table '+@reftable_3+' drop constraint '+@constraintname_3)
       FETCH NEXT from refcursor into @reftable_3, @constraintname_3
     END
     CLOSE refcursor
     DEALLOCATE refcursor
     DROP TABLE TURBINE_GROUP
END


CREATE TABLE TURBINE_GROUP
(
                    GROUP_ID INT NOT NULL,
                    GROUP_NAME VARCHAR (99) NOT NULL,

    CONSTRAINT TURBINE_GROUP_PK PRIMARY KEY(GROUP_ID),
    UNIQUE (GROUP_NAME));





/* ---------------------------------------------------------------------- */
/* TURBINE_ROLE_PERMISSION                                                      */
/* ---------------------------------------------------------------------- */

IF EXISTS (SELECT 1 FROM sysobjects WHERE type ='RI' AND name='TURBINE_ROLE_PERMISSION_FK_1')
    ALTER TABLE TURBINE_ROLE_PERMISSION DROP CONSTRAINT TURBINE_ROLE_PERMISSION_FK_1;
IF EXISTS (SELECT 1 FROM sysobjects WHERE type ='RI' AND name='TURBINE_ROLE_PERMISSION_FK_2')
    ALTER TABLE TURBINE_ROLE_PERMISSION DROP CONSTRAINT TURBINE_ROLE_PERMISSION_FK_2;
IF EXISTS (SELECT 1 FROM sysobjects WHERE type = 'U' AND name = 'TURBINE_ROLE_PERMISSION')
BEGIN
     DECLARE @reftable_4 nvarchar(60), @constraintname_4 nvarchar(60)
     DECLARE refcursor CURSOR FOR
     select reftables.name tablename, cons.name constraintname
      from sysobjects tables,
           sysobjects reftables,
           sysobjects cons,
           sysreferences ref
       where tables.id = ref.rkeyid
         and cons.id = ref.constid
         and reftables.id = ref.fkeyid
         and tables.name = 'TURBINE_ROLE_PERMISSION'
     OPEN refcursor
     FETCH NEXT from refcursor into @reftable_4, @constraintname_4
     while @@FETCH_STATUS = 0
     BEGIN
       exec ('alter table '+@reftable_4+' drop constraint '+@constraintname_4)
       FETCH NEXT from refcursor into @reftable_4, @constraintname_4
     END
     CLOSE refcursor
     DEALLOCATE refcursor
     DROP TABLE TURBINE_ROLE_PERMISSION
END


CREATE TABLE TURBINE_ROLE_PERMISSION
(
                    ROLE_ID INT NOT NULL,
                    PERMISSION_ID INT NOT NULL,

    CONSTRAINT TURBINE_ROLE_PERMISSION_PK PRIMARY KEY(ROLE_ID,PERMISSION_ID));





/* ---------------------------------------------------------------------- */
/* TURBINE_USER                                                      */
/* ---------------------------------------------------------------------- */

IF EXISTS (SELECT 1 FROM sysobjects WHERE type = 'U' AND name = 'TURBINE_USER')
BEGIN
     DECLARE @reftable_5 nvarchar(60), @constraintname_5 nvarchar(60)
     DECLARE refcursor CURSOR FOR
     select reftables.name tablename, cons.name constraintname
      from sysobjects tables,
           sysobjects reftables,
           sysobjects cons,
           sysreferences ref
       where tables.id = ref.rkeyid
         and cons.id = ref.constid
         and reftables.id = ref.fkeyid
         and tables.name = 'TURBINE_USER'
     OPEN refcursor
     FETCH NEXT from refcursor into @reftable_5, @constraintname_5
     while @@FETCH_STATUS = 0
     BEGIN
       exec ('alter table '+@reftable_5+' drop constraint '+@constraintname_5)
       FETCH NEXT from refcursor into @reftable_5, @constraintname_5
     END
     CLOSE refcursor
     DEALLOCATE refcursor
     DROP TABLE TURBINE_USER
END


CREATE TABLE TURBINE_USER
(
                    USER_ID INT NOT NULL,
                    LOGIN_NAME VARCHAR (99) NOT NULL,
                    PASSWORD_VALUE VARCHAR (32) NOT NULL,
                    FIRST_NAME VARCHAR (99) NOT NULL,
                    LAST_NAME VARCHAR (99) NOT NULL,
                    EMAIL VARCHAR (99) NULL,
                    CONFIRM_VALUE VARCHAR (99) NULL,
                    MODIFIED DATETIME NULL,
                    CREATED DATETIME NULL,
                    LAST_LOGIN DATETIME NULL,
                    OBJECTDATA IMAGE NULL,

    CONSTRAINT TURBINE_USER_PK PRIMARY KEY(USER_ID),
    UNIQUE (LOGIN_NAME));





/* ---------------------------------------------------------------------- */
/* TURBINE_USER_GROUP_ROLE                                                      */
/* ---------------------------------------------------------------------- */

IF EXISTS (SELECT 1 FROM sysobjects WHERE type ='RI' AND name='TURBINE_USER_GROUP_ROLE_FK_1')
    ALTER TABLE TURBINE_USER_GROUP_ROLE DROP CONSTRAINT TURBINE_USER_GROUP_ROLE_FK_1;
IF EXISTS (SELECT 1 FROM sysobjects WHERE type ='RI' AND name='TURBINE_USER_GROUP_ROLE_FK_2')
    ALTER TABLE TURBINE_USER_GROUP_ROLE DROP CONSTRAINT TURBINE_USER_GROUP_ROLE_FK_2;
IF EXISTS (SELECT 1 FROM sysobjects WHERE type = 'U' AND name = 'TURBINE_USER_GROUP_ROLE')
BEGIN
     DECLARE @reftable_6 nvarchar(60), @constraintname_6 nvarchar(60)
     DECLARE refcursor CURSOR FOR
     select reftables.name tablename, cons.name constraintname
      from sysobjects tables,
           sysobjects reftables,
           sysobjects cons,
           sysreferences ref
       where tables.id = ref.rkeyid
         and cons.id = ref.constid
         and reftables.id = ref.fkeyid
         and tables.name = 'TURBINE_USER_GROUP_ROLE'
     OPEN refcursor
     FETCH NEXT from refcursor into @reftable_6, @constraintname_6
     while @@FETCH_STATUS = 0
     BEGIN
       exec ('alter table '+@reftable_6+' drop constraint '+@constraintname_6)
       FETCH NEXT from refcursor into @reftable_6, @constraintname_6
     END
     CLOSE refcursor
     DEALLOCATE refcursor
     DROP TABLE TURBINE_USER_GROUP_ROLE
END


CREATE TABLE TURBINE_USER_GROUP_ROLE
(
                    USER_ID INT NOT NULL,
                    GROUP_ID INT NOT NULL,
                    ROLE_ID INT NOT NULL,

    CONSTRAINT TURBINE_USER_GROUP_ROLE_PK PRIMARY KEY(USER_ID,GROUP_ID,ROLE_ID));





/* ---------------------------------------------------------------------- */
/* TURBINE_USER_GROUP_ROLE                                                      */
/* ---------------------------------------------------------------------- */




/* ---------------------------------------------------------------------- */
/* TURBINE_PERMISSION                                                      */
/* ---------------------------------------------------------------------- */




/* ---------------------------------------------------------------------- */
/* TURBINE_ROLE                                                      */
/* ---------------------------------------------------------------------- */




/* ---------------------------------------------------------------------- */
/* TURBINE_GROUP                                                      */
/* ---------------------------------------------------------------------- */

BEGIN
ALTER TABLE TURBINE_ROLE_PERMISSION
    ADD CONSTRAINT TURBINE_ROLE_PERMISSION_FK_1 FOREIGN KEY (ROLE_ID)
    REFERENCES TURBINE_ROLE (ROLE_ID)
END    
;

BEGIN
ALTER TABLE TURBINE_ROLE_PERMISSION
    ADD CONSTRAINT TURBINE_ROLE_PERMISSION_FK_2 FOREIGN KEY (PERMISSION_ID)
    REFERENCES TURBINE_PERMISSION (PERMISSION_ID)
END    
;




/* ---------------------------------------------------------------------- */
/* TURBINE_ROLE_PERMISSION                                                      */
/* ---------------------------------------------------------------------- */




/* ---------------------------------------------------------------------- */
/* TURBINE_USER                                                      */
/* ---------------------------------------------------------------------- */

BEGIN
ALTER TABLE TURBINE_USER_GROUP_ROLE
    ADD CONSTRAINT TURBINE_USER_GROUP_ROLE_FK_1 FOREIGN KEY (USER_ID)
    REFERENCES TURBINE_USER (USER_ID)
END    
;

BEGIN
ALTER TABLE TURBINE_USER_GROUP_ROLE
    ADD CONSTRAINT TURBINE_USER_GROUP_ROLE_FK_2 FOREIGN KEY (ROLE_ID)
    REFERENCES TURBINE_ROLE (ROLE_ID)
END    
;



