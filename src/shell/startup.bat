@echo off
rem $Id: startup.bat,v 1.7 2000/03/31 19:40:02 craigmcc Exp $
rem Startup batch file for tomcat servner.

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
call %TOMCAT_HOME%\bin\tomcat start %1 %2 %3 %4 %5 %6 %7 %8 %9

:eof
