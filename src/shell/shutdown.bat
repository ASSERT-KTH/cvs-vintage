@echo off
rem $Id: shutdown.bat,v 1.7 2001/08/30 11:44:32 larryi Exp $
rem Startup batch file for tomcat server.

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
call "%_TC_BIN_DIR%\tomcat" stop %1 %2 %3 %4 %5 %6 %7 %8 %9

:eof
set _TC_BIN_DIR=
