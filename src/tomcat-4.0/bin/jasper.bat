@echo off
rem ---------------------------------------------------------------------------
rem jasper.bat - Global Script for Jasper
rem
rem Environment Variable Prequisites:
rem   JASPER_HOME (Optional)
rem       May point at your Jasper "build" directory.
rem       If not present, the current working directory is assumed.
rem   JASPER_OPTS (Optional) 
rem       Java runtime options
rem   JAVA_HOME     
rem       Must point at your Java Development Kit installation.
rem
rem $Id: jasper.bat,v 1.13 2001/10/14 21:30:36 jon Exp $
rem ---------------------------------------------------------------------------

rem ----- Save Environment Variables That May Change --------------------------

set _JASPER_HOME=%JASPER_HOME%
set _CLASSPATH=%CLASSPATH%

rem ----- Verify and Set Required Environment Variables -----------------------

if not "%JAVA_HOME%" == "" goto gotJava
echo You must set JAVA_HOME to point at your Java Development Kit installation
goto cleanup
:gotJava

if not "%JASPER_HOME%" == "" goto gotHome
set JASPER_HOME=.
:gotHome
if exist %JASPER_HOME%\bin\jpappend.bat goto okHome
echo Using JASPER_HOME: %JASPER_HOME%
echo JASPER_HOME/bin/jpappend.bat does not exist. Please specify JASPER_HOME properly.
goto cleanup
:okHome

rem ----- Set Up The Runtime Classpath ----------------------------------------

rem FIXME set CLASSPATH=%JASPER_HOME%\dummy
rem FIXME below
set CLASSPATH=%JASPER_HOME%\classes
for %%i in (%JASPER_HOME%\lib\*.jar) do call %JASPER_HOME%\bin\jpappend.bat %%i
for %%i in (%JASPER_HOME%\jasper\*.jar) do call %JASPER_HOME%\bin\jpappend.bat %%i
set CLASSPATH=%CLASSPATH%;%JASPER_HOME%\common\lib\servlet.jar
echo Using CLASSPATH: %CLASSPATH%

rem ----- Execute The Requested Command ---------------------------------------

if "%1" == "jspc" goto doJspc

:doUsage
echo Usage:  jasper ( jspc )
echo Commands:
echo   jspc - Run the jasper offline JSP compiler
goto cleanup

:doJspc
java %JASPER_OPTS% -Djasper.home=%JASPER_HOME% org.apache.jasper.JspC %2 %3 %4 %5 %6 %7 %8 %9
goto cleanup

rem ----- Restore Environment Variables ---------------------------------------

:cleanup
set JASPER_HOME=%_JASPER_HOME%
set _JASPER_HOME=
set CLASSPATH=%_CLASSPATH%
set _CLASSPATH=
:finish
