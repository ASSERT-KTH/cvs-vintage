@echo off
REM convience bat file to build with

if not "%JAVA_HOME%" == "" goto gotJavaHome
echo You must set the JAVA_HOME environment variable to point at your JDK
goto finish
:gotJavaHome

set _ANTHOME=%ANT_HOME%
if "%ANT_HOME%" == "" set ANT_HOME=..\jakarta-ant-1.3

"%ANT_HOME%\bin\ant" %1 %2 %3 %4 %5 %6 %7 %8 %9

:clean

rem clean up
set ANT_HOME=%_ANTHOME%
set _ANTHOME=

:finish
