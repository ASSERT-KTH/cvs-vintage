@echo off
rem $Id: startup.bat,v 1.5 2000/02/05 18:28:43 rubys Exp $
rem Startup batch file for tomcat servner.

rem This batch file written and tested under Windows NT
rem Improvements to this file are welcome


if "%TOMCAT_HOME%" == "" goto bin
cmd /c "cd %TOMCAT_HOME% & bin\tomcat start %1 %2 %3 %4 %5 %6 %7 %8 %9"
goto :eof

:bin
call bin\tomcat start %1 %2 %3 %4 %5 %6 %7 %8 %9

:eof
