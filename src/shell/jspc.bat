@echo off
rem $Id: jspc.bat,v 1.1 2000/02/07 08:02:14 shemnon Exp $
rem A batch file to run the JspC Compiler

rem This batch file written and tested under Windows NT
rem Improvements to this file are welcome

set jsdkJars=.\lib\webserver.jar;.\lib\servlet.jar
set jspJars=.\lib\jasper.jar
set beanJars=.\webpages\WEB-INF\classes\jsp\beans
set miscJars=.\lib\xml.jar
set appJars=%jsdkJars%;%jspJars%;%beanJars%;%miscJars%
set sysJars=%JAVA_HOME%\lib\tools.jar

set appClassPath=.\classes;%appJars%
set cp=%CLASSPATH%

set CLASSPATH=%appClassPath%;%sysJars%

if "%cp%" == "" goto next

rem else
set CLASSPATH=%CLASSPATH%;%cp%

echo Using classpath: %CLASSPATH%
rem start java org.apache.tomcat.shell.Startup %2 %3 %4 %5 %6 %7 %8 %9
java org.apache.japser.JspC %1 %2 %3 %4 %5 %6 %7 %8 %9

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
