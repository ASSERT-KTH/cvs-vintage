@echo off
rem $Id: jspc.bat,v 1.3 2000/02/26 20:44:15 rubys Exp $
rem A batch file to run the JspC Compiler

rem This batch file written and tested under Windows NT
rem Improvements to this file are welcome

if not "%TOMCAT_HOME%" == "" goto start

SET TOMCAT_HOME=.
if exist %TOMCAT_HOME%\bin\tomcat.bat goto start

SET TOMCAT_HOME=..
if exist %TOMCAT_HOME%\bin\tomcat.bat goto start

SET TOMCAT_HOME=
echo Unable to determine the value of TOMCAT_HOME.
goto eof

:start
call %TOMCAT_HOME%\bin\tomcat jspc %*

:eof
