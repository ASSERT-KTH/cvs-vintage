@echo off
@if not "%ECHO%" == ""  echo %ECHO%
@if "%OS%" == "Windows_NT"  setlocal

set CLASSPATH=%CLASSPATH%;run.jar

REM Add all login modules for JAAS-based security
REM and all libraries that are used by them here
set CLASSPATH=%CLASSPATH%;..\lib\jdbc2_0-stdext.jar;..\lib\jboss-jaas.jar

java -classpath "%CLASSPATH%" -Dtomcat.home=%TOMCAT_HOME% org.jboss.Main %1 %2 %3 %4 %5 %6 %7 %8 %9

pause
