@echo off
REM convience bat file to build with
java -classpath ..\jakarta-tools\ant.jar;..\jakarta-tools\projectx-tr2.jar;%CLASSPATH% org.apache.tools.ant.Main %1 %2 %3 %4 %5