@echo off
REM convience bat file to build with

set _ANTHOME=%ANT_HOME%
if "%ANT_HOME%" == "" set ANT_HOME=..\jakarta-ant

set _SERVLETAPIHOME=%SERVLETAPI_HOME%
if "%SERVLETAPI_HOME%" == "" set SERVLETAPI_HOME=..\jakarta-servletapi

if "%CLASSPATH%" == "" goto noclasspath

rem else
set _CLASSPATH=%CLASSPATH%
set CLASSPATH=%CLASSPATH%;%ANT_HOME%\lib\ant.jar;%SERVLETAPI_HOME%\lib\servlet.jar;..\jakarta-tools\moo.jar;%JAVA_HOME%\lib\tools.jar
goto next

:noclasspath
set _CLASSPATH=
set CLASSPATH=%ANT_HOME%\lib\ant.jar;%SERVLETAPI_HOME%\lib\servlet.jar;..\jakarta-tools\moo.jar;%JAVA_HOME%\lib\tools.jar
goto next

:next

java %ANT_OPTS% org.apache.tools.ant.Main %1 %2 %3 %4 %5 %6 %7 %8 %9

:clean

rem clean up classpath after
set CLASSPATH=%_CLASSPATH%
set _CLASSPATH=
set SERVLETAPI_HOME=%_SERVLETAPIHOME%
set _SERVLETAPIHOME=
set ANT_HOME=%_ANTHOME%
set _ANTHOME=
