@echo off
rem ---------------------------------------------------------------------------
rem startup.bat - Start Script for the CATALINA Server
rem
rem $Id: startup.bat,v 1.14 2001/10/14 21:30:36 jon Exp $
rem ---------------------------------------------------------------------------

set _CATALINA_HOME=%CATALINA_HOME%
if not "%CATALINA_HOME%" == "" goto gotHome
set CATALINA_HOME=.
if exist "%CATALINA_HOME%\bin\catalina.bat" goto gotHome
set CATALINA_HOME=..
if exist "%CATALINA_HOME%\bin\catalina.bat" goto gotHome
echo Unable to determine the value of CATALINA_HOME
goto cleanup
:gotHome
"%CATALINA_HOME%\bin\catalina" start %1 %2 %3 %4 %5 %6 %7 %8 %9
:cleanup
set CATALINA_HOME=%_CATALINA_HOME%
set _CATALINA_HOME=

