@echo off
@if not "%ECHO%" == ""  echo %ECHO%
@if "%OS%" == "Windows_NT"  setlocal


rem java -DINFO_ALL -jar run.jar
rem java -Djboss.verifier.isEnabled=true -jar run.jar

set CLASSPATH=%CLASSPATH%;run.jar

if not "%TOMCAT_HOME%" == "" goto gotTomcatHome
goto noTomcatHome

:gotTomcatHome
set CLASSPATH=%CLASSPATH%;%TOMCAT_HOME%\lib
set CLASSPATH=%CLASSPATH%;%TOMCAT_HOME%\lib\jasper.jar
set CLASSPATH=%CLASSPATH%;%TOMCAT_HOME%\lib\webserver.jar
set CLASSPATH=%CLASSPATH%;%TOMCAT_HOME%\lib\xml.jar
set CLASSPATH=%CLASSPATH%;%TOMCAT_HOME%\lib\servlet.jar

REM Add the tools.jar file so that Tomcat can find the 
REM Java compiler.

set CLASSPATH=%CLASSPATH%;%JAVA_HOME%\lib\tools.jar

:noTomcatHome

if "%SPYDERMQ_HOME%" == "" goto startJBoss

:gotSpyderMQHome
set CLASSPATH=%CLASSPATH%;%SPYDERMQ_HOME%\lib
set CLASSPATH=%CLASSPATH%;%SPYDERMQ_HOME%\lib\spydermq.jar
set CLASSPATH=%CLASSPATH%;%SPYDERMQ_HOME%\lib\jms.jar
set CLASSPATH=%CLASSPATH%;%SPYDERMQ_HOME%\lib\jnpserver.jar

:startJBoss

REM Add the conf directory so that Classloader.getResource() will
REM find the configuration and properties files.

set CLASSPATH=%CLASSPATH%;..\conf


java -classpath "%CLASSPATH%" -Dtomcat.home=%TOMCAT_HOME% org.jboss.Main

pause
