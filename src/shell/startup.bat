@echo off
rem $Id: startup.bat,v 1.10 2001/08/30 11:44:32 larryi Exp $
rem Startup batch file for tomcat server.

rem This batch file written and tested under Windows NT
rem Improvements to this file are welcome

set _TC_BIN_DIR=%TOMCAT_HOME%\bin
if not "%_TC_BIN_DIR%" == "\bin" goto start

set _TC_BIN_DIR=.\bin
if exist "%_TC_BIN_DIR%\tomcat.bat" goto start

set _TC_BIN_DIR=.
if exist "%_TC_BIN_DIR%\tomcat.bat" goto start

set _TC_BIN_DIR=..\bin
if exist "%_TC_BIN_DIR%\tomcat.bat" goto start

echo Unable to determine the location of Tomcat.
goto eof

:start
call "%_TC_BIN_DIR%\tomcat" start %1 %2 %3 %4 %5 %6 %7 %8 %9

:eof
set _TC_BIN_DIR=
