@echo off
rem java -DINFO_ALL -jar run.jar
rem java -Djboss.verifier.isEnabled=true -jar run.jar

set CLASSPATH=%CLASSPATH%;run.jar

if not "%TOMCAT_HOME%" == "" if not "%SERVLETAPI_HOME%" == "" goto gotTomcatHome
goto startJBoss

:gotTomcatHome
set CLASSPATH=%CLASSPATH%;%TOMCAT_HOME%\lib
set CLASSPATH=%CLASSPATH%;%SERVLETAPI_HOME%\lib\servlet.jar

:startJBoss

java -classpath "%CLASSPATH%" -Dtomcat.home=%TOMCAT_HOME% org.jboss.Main

pause
