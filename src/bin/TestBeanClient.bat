@echo off

REM This line should be set up correctly by the build process
set JDK=JDK.VER

REM This line should be set up correctly by the build process
set VERSION=PROD.VER

REM all clients need this
set CP=../client/ejb.jar
set CP=%CP%;../client/jndi.jar
set CP=%CP%;../client/jta-spec1_0_1.jar
set CP=%CP%;../client/jboss-client.jar
set CP=%CP%;../client/jnp-client.jar
set CP=%CP%;..

REM only the test client needs the following
set CP=%CP%;../client/TestBeanClient.jar
set CP=%CP%;.;..;../client/AccountClient.jar


java -cp %CP%  org.jboss.zol.testbean.client.EjbossClient  %1 %2 %3 %4 %5 %6 %7 %8 %9

echo.
echo.

REM echo Running Account EJB Tests...
REM echo These will test EJB references, findByPrimaryKey operations, simple transaction
REM echo rollbacks on failed entity creation and SQL INSERT, SELECT and UPDATE operations.
REM echo.

REM java -cp %CP%  org.jboss.zol.accountmanager.client.AccountManagerClient  %1 %2 %3 %4 %5 %6 %7 %8 %9

