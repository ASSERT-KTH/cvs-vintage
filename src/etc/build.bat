@echo off
REM convenience bat file to build with

set CLASSPATH=..\..\lib\ant.jar
set CLASSPATH=%CLASSPATH%;..\..\lib\xml.jar
set CLASSPATH=%CLASSPATH%;..\..\lib\xmlbeans.jar
set CLASSPATH=%CLASSPATH%;..\..\build\classes
set CLASSPATH=%CLASSPATH%;..\..\lib\javac.jar

java -classpath "%CLASSPATH%" org.apache.tools.ant.Main %1 %2 %3 %4 %5
