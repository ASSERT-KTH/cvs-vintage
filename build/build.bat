@echo off
REM convenience bat file to build with
java -classpath "%CLASSPATH%;..\..\lib\ant.jar;..\..\lib\xml.jar;..\..\lib\xmlbeans.jar;..\..\build\classes" org.apache.tools.ant.Main %1 %2 %3 %4 %5
