@echo off

set TOM_PREV_CLASSPATH=%CLASSPATH%

if "%TOMCAT_HOME%" == "" goto bin
SET CLASSPATH=%TOMCAT_HOME%\lib\ant.jar;%CLASSPATH%
call %TOMCAT_HOME%\bin\tomcat env
java org.apache.tools.ant.Main -Dtomcat.home %TOMCAT_HOME% -f conf/test-tomcat.xml %*
goto :restore

:bin
SET CLASSPATH=lib\ant.jar;%CLASSPATH%
CALL bin\tomcat env
java org.apache.tools.ant.Main -f conf/test-tomcat.xml %*

:restore
set CLASSPATH=%TOM_PREV_CLASSPATH%

