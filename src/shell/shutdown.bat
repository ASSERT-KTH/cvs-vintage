@echo off
rem $Id: shutdown.bat,v 1.1 1999/10/09 00:20:56 duncan Exp $
rem Startup batch file for servlet runner.

rem This batch file written and tested under Windows NT
rem Improvements to this file are welcome

set jsdkJars=.\webserver.jar;.\lib\servlet.jar
set jspJars=.\lib\jasper.jar
set beanJars=.\webpages\WEB-INF\classes\jsp\beans;.\examples\WEB-INF\classes\jsp\beans
set miscJars=.\lib\xml.jar

set appJars=%jsdkJars%;%jspJars%;%beanJars%;%miscJars%
set sysJars=%JAVA_HOME%\lib\tools.jar

set appClassPath=.\classes;%appJars%
set cp=%CLASSPATH%

set CLASSPATH=%appClassPath%;%sysJars%

if "%cp%" == "" goto next
 
rem else 
set CLASSPATH=%CLASSPATH%;%cp% 
 
:next
echo Using classpath: %CLASSPATH%

java org.apache.tomcat.shell.Shutdown %1 %2 %3 %4 %5 %6 %7 %8 %9

rem clean up

set CLASSPATH=%cp%
set port=
set host=
set test=
set jsdkJars=
set jspJars=
set beanJars=
set miscJars=
set appJars=
set appClassPath=
set cp=

rem pause
