@echo off
REM convience bat file to build with

SET _ANTHOME=%ANT_HOME%
if "%ANT_HOME%" == "" SET ANT_HOME=..\jakarta-ant

if "%CLASSPATH%" == "" goto noclasspath

rem else
set _CLASSPATH=%CLASSPATH%
set CLASSPATH=%CLASSPATH%;%ANT_HOME%\lib\ant.jar;%ANT_HOME%\lib\xml.jar;..\jakarta-tools\moo.jar
goto next

:noclasspath
set _CLASSPATH=
set CLASSPATH=%ANT_HOME%\lib\ant.jar;%ANT_HOME%\lib\xml.jar;..\jakarta-tools\moo.jar
goto next

:next

java org.apache.tools.ant.Main %1 %2 %3 %4 %5 %6 %7 %8 %9

:clean

rem clean up classpath after
set CLASSPATH=%_CLASSPATH%
set _CLASSPATH=
SET ANT_HOME=%_ANTHOME%
SET _ANTHOME=
