@echo off
rem java -DINFO_ALL -jar run.jar
rem java -Djboss.verifier.isEnabled=true -jar run.jar

set CLASSPATH=%CLASSPATH%;run.jar

if not "%TOMCAT_HOME%" == "" if not "%SERVLETAPI_HOME%" == "" goto gotTomcatHome
echo WARNING
echo If you want to run jBoss with Tomcat integrated, 
echo set the TOMCAT_HOME environment variable to point to the root dir of Tomcat
echo Set the env variable SERVLET_API to the directory 'jakarta-servletapi' (CVS checkout of Tomcat's modules)
goto startJBoss

:gotTomcatHome
set CLASSPATH=%CLASSPATH%;%TOMCAT_HOME%\classes
set CLASSPATH=%CLASSPATH%;%SERVLETAPI_HOME%\lib\servlet.jar

:startJBoss

java -classpath "%CLASSPATH%" -Dtomcat.home=%TOMCAT_HOME% org.jboss.Main

pause
