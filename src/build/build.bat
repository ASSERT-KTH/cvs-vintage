@echo off
@if not "%ECHO%" == ""  echo %ECHO%
@if "%OS%" == "Windows_NT"  setlocal

REM convenience bat file to build with

set CP=%CP%;..\..\lib\ant.jar
set CP=%CP%;..\..\lib\jaxp.jar
set CP=%CP%;..\..\lib\parser.jar
set CP=%CP%;..\..\lib\xmlbeans.jar
set CP=%CP%;..\..\build\classes
set CP=%CP%;..\..\lib\javac.jar

java -classpath "%CP%" org.apache.tools.ant.Main %1 %2 %3 %4 %5
