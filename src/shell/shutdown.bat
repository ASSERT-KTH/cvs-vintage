@echo off
rem $Id: shutdown.bat,v 1.3 2000/02/05 18:28:43 rubys Exp $
rem Startup batch file for tomcat server.

if "%TOMCAT_HOME%" == "" goto bin
cmd /c "cd %TOMCAT_HOME% & bin\tomcat stop"
goto :eof

:bin
call bin\tomcat stop

:eof
