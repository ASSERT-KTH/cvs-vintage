@echo off
rem -------------------------------------------------------------------------
rem tomcat.bat - Start/Stop Script for the TOMCAT Server
rem
rem Environment Variable Prerequisites:
rem
rem   JAVA_HOME      Must point at your Java Development Kit installation.
rem
rem   TOMCAT_HOME    (Optional) Should point to the directory containing
rem                  Tomcat's "conf" and "webapps" directory.
rem                  If not present, the current working directory is
rem                  assumed.
rem                  Note: This batch file does not function properly
rem                  if TOMCAT_HOME contains spaces.
rem
rem   TOMCAT_INSTALL (Optional) Should point to the directory containing
rem                  Tomcat's "lib" directory.
rem                  If not present, the current working directory is
rem                  assumed.  If this doesn't contain a "lib" directory,
rem                  or if the "lib" directory doesn't contain tomcat.jar.
rem                  TOMCAT_HOME is used.
rem
rem   TOMCAT_OPTS    (Optional) Java runtime options used when the "start",
rem                  "stop", or "run" command is executed
rem
rem   NOTE: Tomcat does not use your system's CLASSPATH setting.  Instead
rem         Tomcat starts using only tomcat.jar on the classpath and builds
rem         its "classpath" internally.  To add your classes to those of
rem         Tomcat, refer to the Tomcat Users Guide (tomcat_ug.html found
rem         in the "doc" directory.
rem
rem $Id: tomcat.bat,v 1.36 2001/08/19 22:52:51 larryi Exp $
rem -------------------------------------------------------------------------


rem ----- Save Environment Variables That May Change ------------------------

set _TOMCAT_HOME=%TOMCAT_HOME%
set _TOMCAT_INSTALL=%TOMCAT_INSTALL%
set _CLASSPATH=%CLASSPATH%

rem ----- Internal Environment Vars used somewhere --------------------------

set _NULL=nul
set _CONTAINER=contai~1
if not "%OS%" == "Windows_NT" goto cont
set _NULL=
set _CONTAINER=container
:cont

rem ----- Verify and Set Required Environment Variables ---------------------

if not "%JAVA_HOME%" == "" goto gotJavaHome
echo You must set JAVA_HOME to point at your Java Development Kit installation
goto cleanup
:gotJavaHome

if not "%TOMCAT_HOME%" == "" goto gotTcHome
set TOMCAT_HOME=.
:gotTcHome
if exist "%TOMCAT_HOME%\conf\%_NULL%" goto okTcHome
echo "%TOMCAT_HOME%\conf" not found.
echo Unable to locate Tomcat's "conf" directory, check the value of TOMCAT_HOME.
goto cleanup
:okTcHome

if not "%TOMCAT_INSTALL%" == "" goto gotTcInstall
set TOMCAT_INSTALL=.
if exist "%TOMCAT_INSTALL%\lib\tomcat.jar" goto okTcInstall
set TOMCAT_INSTALL=%TOMCAT_HOME%
:gotTcInstall
if exist "%TOMCAT_INSTALL%\lib\tomcat.jar" goto okTcInstall
echo "%TOMCAT_INSTALL%\lib\tomcat.jar" not found.
echo Unable to locate "lib\tomcat.jar", check the value of TOMCAT_INSTALL.
goto cleanup
:okTcInstall

rem ----- Prepare Appropriate Java Execution Commands -----------------------

if not "%OS%" == "Windows_NT" goto noTitle
set _SECSTARTJAVA=start "Secure Tomcat 3.3" "%JAVA_HOME%\bin\java"
set _STARTJAVA=start "Tomcat 3.3" "%JAVA_HOME%\bin\java"
set _RUNJAVA="%JAVA_HOME%\bin\java"
goto setClasspath

:noTitle
set _SECSTARTJAVA=start "%JAVA_HOME%\bin\java"
set _STARTJAVA=start "%JAVA_HOME%\bin\java"
set _RUNJAVA="%JAVA_HOME%\bin\java"

:setClasspath

set CLASSPATH=%TOMCAT_INSTALL%\lib\tomcat.jar

rem ----- Execute The Requested Command -------------------------------------

:execute

if "%1" == "start" goto startServer
if "%1" == "stop" goto stopServer
if "%1" == "run" goto runServer
if "%1" == "ant" goto runAnt
if "%1" == "env" goto doEnv
if "%1" == "jspc" goto runJspc

:doUsage
echo Usage:  tomcat ( ant ^| env ^| jspc ^| run ^| start ^| stop )
echo Commands:
echo   ant -   Run Ant in Tomcat's environment
echo   env -   Set up environment variables that Tomcat would use
echo   jspc -  Run JSPC in Tomcat's environment
echo   run -   Start Tomcat in the current window
echo   start - Start Tomcat in a separate window
echo   stop -  Stop Tomcat
goto cleanup

:startServer
echo Starting Tomcat in new window
if "%2" == "-security" goto startSecure
%_STARTJAVA% %TOMCAT_OPTS% -Dtomcat.home=%TOMCAT_HOME% org.apache.tomcat.startup.Main %2 %3 %4 %5 %6 %7 %8 %9
goto cleanup

:startSecure
echo Starting Tomcat with a SecurityManager
%_SECSTARTJAVA% %TOMCAT_OPTS% -Djava.security.manager -Djava.security.policy=="%TOMCAT_HOME%/conf/tomcat.policy" -Dtomcat.home=%TOMCAT_HOME% org.apache.tomcat.startup.Main %3 %4 %5 %6 %7 %8 %9
goto cleanup

:runServer
rem Running Tomcat in this window
if "%2" == "-security" goto runSecure
%_RUNJAVA% %TOMCAT_OPTS% -Dtomcat.home=%TOMCAT_HOME% org.apache.tomcat.startup.Main %2 %3 %4 %5 %6 %7 %8 %9
goto cleanup

:runSecure
rem Running Tomcat with a SecurityManager
%_RUNJAVA% %TOMCAT_OPTS% -Djava.security.manager -Djava.security.policy=="%TOMCAT_HOME%/conf/tomcat.policy" -Dtomcat.home=%TOMCAT_HOME% org.apache.tomcat.startup.Main %3 %4 %5 %6 %7 %8 %9
goto cleanup

:stopServer
rem Stopping the Tomcat Server
%_RUNJAVA% %TOMCAT_OPTS% -Dtomcat.home=%TOMCAT_HOME% org.apache.tomcat.startup.Main -stop %2 %3 %4 %5 %6 %7 %8 %9
goto cleanup

:runAnt
rem Run ANT in Tomcat's Environment
set CP=%CP%;%TOMCAT_HOME%\lib\ant.jar
%_RUNJAVA% %ANT_OPTS% -Dant.home=%TOMCAT_HOME% -Dtomcat.home=%TOMCAT_HOME% org.apache.tools.ant.Main %2 %3 %4 %5 %6 %7 %8 %9
goto cleanup

:runJspc
rem Run JSPC in Tomcat's Environment
%_RUNJAVA% %JSPC_OPTS% -Dtomcat.home=%TOMCAT_HOME% org.apache.jasper.JspC %2 %3 %4 %5 %6 %7 %8 %9
goto cleanup

rem ----- Set CLASSPATH to Tomcat's Runtime Environment ----------------------- 

:doEnv
rem Try to determine if TOMCAT_INSTALL contains spaces
if exist %TOMCAT_INSTALL%\lib\%_NULL% goto dynClasspath
echo Your TOMCAT_INSTALL or TOMCAT_HOME appears to contain spaces.
echo Unable to set CLASSPATH dynamically.
goto staticClasspath

:dynClasspath
set _LIBJARS=
for %%i in (%TOMCAT_HOME%\lib\%_CONTAINER%\*.*) do call %TOMCAT_HOME%\bin\cpappend.bat %%i
if not "%_LIBJARS%" == "" goto getLibJars
echo Unable to set CLASSPATH dynamically.
if "%OS%" == "Windows_NT" goto staticClasspath
echo Note: To set the CLASSPATH dynamically on Win9x systems
echo       only DOS 8.3 names may be used in TOMCAT_HOME or TOMCAT_INSTALL!
goto staticClasspath

:getLibJars
for %%i in (%TOMCAT_HOME%\lib\common\*.*) do call %TOMCAT_HOME%\bin\cpappend.bat %%i
for %%i in (%TOMCAT_HOME%\lib\apps\*.*) do call %TOMCAT_HOME%\bin\cpappend.bat %%i

echo Setting your CLASSPATH to Tomcat's runtime set of jars.
rem Note: _LIBJARS already contains a leading semicolon
set CLASSPATH=%CLASSPATH%%_LIBJARS%
goto finish

:staticClasspath
echo Setting your CLASSPATH statically.
rem Add lib jars
if exist "%TOMCAT_HOME%\lib\tomcat.jar" set CLASSPATH=%CLASSPATH%;%TOMCAT_HOME%\lib\tomcat.jar
rem Add lib\container jars
if exist "%TOMCAT_HOME%\lib\container\crimson.jar" set CLASSPATH=%CLASSPATH%;%TOMCAT_HOME%\lib\container\crimson.jar
if exist "%TOMCAT_HOME%\lib\container\facade22.jar" set CLASSPATH=%CLASSPATH%;%TOMCAT_HOME%\lib\container\facade22.jar
if exist "%TOMCAT_HOME%\lib\container\jasper.jar" set CLASSPATH=%CLASSPATH%;%TOMCAT_HOME%\lib\container\jasper.jar
if exist "%TOMCAT_HOME%\lib\container\jaxp.jar" set CLASSPATH=%CLASSPATH%;%TOMCAT_HOME%\lib\container\jaxp.jar
if exist "%TOMCAT_HOME%\lib\container\tomcat_modules.jar" set CLASSPATH=%CLASSPATH%;%TOMCAT_HOME%\lib\container\tomcat_modules.jar
if exist "%TOMCAT_HOME%\lib\container\tomcat_util.jar" set CLASSPATH=%CLASSPATH%;%TOMCAT_HOME%\lib\container\tomcat_util.jar
if exist "%TOMCAT_HOME%\lib\container\tomcat-startup.jar" set CLASSPATH=%CLASSPATH%;%TOMCAT_HOME%\lib\container\tomcat-startup.jar
if exist "%TOMCAT_HOME%\lib\container\xalan.jar" set CLASSPATH=%CLASSPATH%;%TOMCAT_HOME%\lib\container\xalan.jar
rem Add lib\common jars
if exist "%TOMCAT_HOME%\lib\common\connector_util.jar" set CLASSPATH=%CLASSPATH%;%TOMCAT_HOME%\lib\common\connector_util.jar
if exist "%TOMCAT_HOME%\lib\common\core_util.jar" set CLASSPATH=%CLASSPATH%;%TOMCAT_HOME%\lib\common\core_util.jar
if exist "%TOMCAT_HOME%\lib\common\jasper-runtime.jar" set CLASSPATH=%CLASSPATH%;%TOMCAT_HOME%\lib\common\jasper-runtime.jar
if exist "%TOMCAT_HOME%\lib\common\servlet.jar" set CLASSPATH=%CLASSPATH%;%TOMCAT_HOME%\lib\common\servlet.jar
if exist "%TOMCAT_HOME%\lib\common\tomcat_core.jar" set CLASSPATH=%CLASSPATH%;%TOMCAT_HOME%\lib\common\tomcat_core.jar

goto finish


rem ----- Restore Environment Variables ---------------------------------------

:cleanup
set TEST_JAR=
set _LIBJARS=
set _SECSTARTJAVA=
set _STARTJAVA=
set _RUNJAVA=
set CLASSPATH=%_CLASSPATH%
set _CLASSPATH=
set TOMCAT_HOME=%_TOMCAT_HOME%
set _TOMCAT_HOME=
set TOMCAT_INSTALL=%_TOMCAT_INSTALL%
set _TOMCAT_INSTALL=
:finish
