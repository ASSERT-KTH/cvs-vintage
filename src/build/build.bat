@echo off
@if not "%ECHO%" == ""  echo %ECHO%
@if "%OS%" == "Windows_NT"  setlocal

REM convenience bat file to build with


for %%i in (..\..\lib\*.jar) do call lcp.bat %%i

set CP=%CP%;..\..\build\classes



java -classpath "%CP%" org.apache.tools.ant.Main %1 %2 %3 %4 %5
