@echo off
rem build.bat -- Build Script for the "Hello, World" Application
rem $Id: build.bat,v 1.5 2001/10/21 04:21:36 larryi Exp $

set _CP=%CP%

rem Identify the custom class path components we need
set CP=%TOMCAT_HOME%\webapps\admin\WEB-INF\lib\ant.jar;%TOMCAT_HOME%\lib\common\servlet.jar
set CP=%CP%;%TOMCAT_HOME%\lib\container\crimson.jar
set CP=%CP%;%JAVA_HOME%\lib\tools.jar

rem Execute ANT to perform the required build target
java -classpath %CP%;%CLASSPATH% org.apache.tools.ant.Main -Dtomcat.home=%TOMCAT_HOME% %1 %2 %3 %4 %5 %6 %7 %8 %9

set CP=%_CP%
set _CP=
