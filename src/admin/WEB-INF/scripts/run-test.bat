@echo off

rem Batch file to run the tomcat sanity test suite
rem Note: You may send the output to a file using "run-tomcat -l file"

if not "%TOMCAT_HOME%" == "" goto gotTomcatHome
echo You need to set TOMCAT_HOME
goto exit

:gotTomcatHome

set _OLDCP=%CLASSPATH%

set CLASSPATH=%TOMCAT_HOME%/lib/container/tomcat_util.jar
set CLASSPATH=%TOMCAT_HOME%/lib/common/core_util.jar;%CLASSPATH%
set CLASSPATH=%TOMCAT_HOME%/lib/container/parser.jar;%CLASSPATH%
set CLASSPATH=%TOMCAT_HOME%/lib/container/crimson.jar;%CLASSPATH%
set CLASSPATH=%TOMCAT_HOME%/lib/container/xerces.jar;%CLASSPATH%
set CLASSPATH=%TOMCAT_HOME%/lib/container/jaxp.jar;%CLASSPATH%
set CLASSPATH=%TOMCAT_HOME%/webapps/admin/WEB-INF/lib/ant.jar;%CLASSPATH%
set CLASSPATH=%TOMCAT_HOME%/webapps/admin/WEB-INF/classes;%CLASSPATH%

if "%_OLDCP%" == "" goto runant
set CLASSPATH=%CLASSPATH%;%_OLDCP%

:runant
call ant -Dgdir="%TOMCAT_HOME%/webapps/test/Golden" -f "%TOMCAT_HOME%/webapps/test/WEB-INF/test-tomcat.xml" %1 %2 %3 %4 %5 %6 %7 %8 %9 client

set CLASSPATH=%_OLDCP%

:exit