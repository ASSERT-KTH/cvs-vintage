@echo off
rem $Id: shutdown.bat,v 1.8 2001/09/09 21:59:55 larryi Exp $
rem Startup batch file for tomcat server.

set _TC_BIN_DIR=%TOMCAT_INSTALL%\bin
if "%_TC_BIN_DIR%" == "\bin" goto search
if exist "%_TC_BIN_DIR%\tomcat.bat" goto start
echo tomcat.bat not found at TOMCAT_INSTALL = %TOMCAT_INSTALL%
goto eof

:search
set _TC_BIN_DIR=.\bin
if exist "%_TC_BIN_DIR%\tomcat.bat" goto start

set _TC_BIN_DIR=.
if exist "%_TC_BIN_DIR%\tomcat.bat" goto start

set _TC_BIN_DIR=..\bin
if exist "%_TC_BIN_DIR%\tomcat.bat" goto start

set _TC_BIN_DIR=%TOMCAT_HOME%\bin
if "%_TC_BIN_DIR%" == "\bin" goto notFound
if exist "%_TC_BIN_DIR%\tomcat.bat" goto start

:notFound
echo Unable to determine the location of Tomcat.
goto eof

:start
call "%_TC_BIN_DIR%\tomcat" stop %1 %2 %3 %4 %5 %6 %7 %8 %9

:eof
set _TC_BIN_DIR=
