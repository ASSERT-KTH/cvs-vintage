@echo off
rem -------------------------------------------------------------------------
rem tomcat.bat - Start/Stop Script for the TOMCAT Server
rem
rem Environment Variable Prerequisites:
rem
rem   TOMCAT_HOME  (Optional) May point at your Tomcat distribution
rem                directory.  If not present, the current working
rem                directory is assumed.
rem                Note: This batch file does not function properly
rem                if TOMCAT_HOME contains spaces.
rem
rem   TOMCAT_OPTS  (Optional) Java runtime options used when the "start",
rem                "stop", or "run" command is executed
rem
rem   CLASSPATH    (Optional) This batch file will automatically add
rem                what Tomcat needs to the CLASSPATH.  This consists
rem                of TOMCAT_HOME\classes and all the jar files in
rem                TOMCAT_HOME\lib. This will include the "jaxp.jar"
rem                and "parser.jar" files from the JAXP Reference
rem                implementation, and the "tools.jar" from the JDK.
rem
rem   JAVA_HOME    Must point at your Java Development Kit installation.
rem
rem $Id: tomcat.bat,v 1.29 2000/09/04 21:24:49 larryi Exp $
rem -------------------------------------------------------------------------


rem ----- Save Environment Variables That May Change ------------------------

set _CP=%CP%
set _TOMCAT_HOME=%TOMCAT_HOME%
set _CLASSPATH=%CLASSPATH%


rem ----- Verify and Set Required Environment Variables ---------------------

if not "%JAVA_HOME%" == "" goto gotJavaHome
echo You must set JAVA_HOME to point at your Java Development Kit installation
goto cleanup
:gotJavaHome

if not "%TOMCAT_HOME%" == "" goto gotTomcatHome
set TOMCAT_HOME=.
:gotTomcatHome
if exist "%TOMCAT_HOME%\lib\webserver.jar" goto okTomcatHome
echo Unable to locate webserver.jar, check the value of TOMCAT_HOME.
goto cleanup
:okTomcatHome


rem ----- Prepare Appropriate Java Execution Commands -----------------------

if not "%OS%" == "Windows_NT" goto noTitle
set _SECSTARTJAVA=start "Secure Tomcat 3.2" "%JAVA_HOME%\bin\java"
set _STARTJAVA=start "Tomcat 3.2" "%JAVA_HOME%\bin\java"
set _RUNJAVA="%JAVA_HOME%\bin\java"
goto setClasspath

:noTitle
set _SECSTARTJAVA=start "%JAVA_HOME%\bin\java"
set _STARTJAVA=start "%JAVA_HOME%\bin\java"
set _RUNJAVA="%JAVA_HOME%\bin\java"


rem ----- Set Up The Runtime Classpath --------------------------------------

:setClasspath
set CP=%TOMCAT_HOME%\classes

rem Try to determine if TOMCAT_HOME contains spaces
if exist %TOMCAT_HOME%\lib\webserver.jar goto dynClasspath
echo Your TOMCAT_HOME appears to contain spaces.
echo Unable to set CLASSPATH dynamically.
goto staticClasspath

:dynClasspath
set _LIBJARS=
for %%i in (%TOMCAT_HOME%\lib\*.jar) do call %TOMCAT_HOME%\bin\cpappend.bat %%i
if not "%_LIBJARS%" == "" goto gotLibJars
echo Unable to set CLASSPATH dynamically.
if "%OS%" == "Windows_NT" goto staticClasspath
echo Note: To set the CLASSPATH dynamically on Win9x systems
echo       only DOS 8.3 names may be used in TOMCAT_HOME!
goto staticClasspath

:gotLibJars
echo Including all jars in %TOMCAT_HOME%\lib in your CLASSPATH.
rem Note: _LIBJARS already contains a leading semicolon
set CP=%CP%%_LIBJARS%
goto chkClasspath

:staticClasspath
echo Setting your CLASSPATH statically.
set CP=%CP%;%TOMCAT_HOME%\lib\ant.jar
set CP=%CP%;%TOMCAT_HOME%\lib\jasper.jar
set CP=%CP%;%TOMCAT_HOME%\lib\jaxp.jar
set CP=%CP%;%TOMCAT_HOME%\lib\parser.jar
set CP=%CP%;%TOMCAT_HOME%\lib\servlet.jar
set CP=%CP%;%TOMCAT_HOME%\lib\webserver.jar

:chkClasspath
if "%CLASSPATH%" == "" goto noClasspath
set CP=%CP%;%CLASSPATH%
:noClasspath
if not exist "%JAVA_HOME%\lib\tools.jar" goto installClasspath
set CP=%CP%;%JAVA_HOME%\lib\tools.jar
:installClasspath
echo.
echo Using CLASSPATH: %CP%
echo.
set CLASSPATH=%CP%


rem ----- Execute The Requested Command -------------------------------------

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

:doEnv
goto finish

:startServer
echo Starting Tomcat in new window
if "%2" == "-security" goto startSecure
%_STARTJAVA% %TOMCAT_OPTS% -Dtomcat.home="%TOMCAT_HOME%" org.apache.tomcat.startup.Tomcat %2 %3 %4 %5 %6 %7 %8 %9
goto cleanup

:startSecure
echo Starting Tomcat with a SecurityManager
%_SECSTARTJAVA% %TOMCAT_OPTS% -Djava.security.manager -Djava.security.policy=="%TOMCAT_HOME%/conf/tomcat.policy" -Dtomcat.home="%TOMCAT_HOME%" org.apache.tomcat.startup.Tomcat %3 %4 %5 %6 %7 %8 %9
goto cleanup

:runServer
rem Running Tomcat in this window
if "%2" == "-security" goto runSecure
%_RUNJAVA% %TOMCAT_OPTS% -Dtomcat.home="%TOMCAT_HOME%" org.apache.tomcat.startup.Tomcat %2 %3 %4 %5 %6 %7 %8 %9
goto cleanup

:runSecure
rem Running Tomcat with a SecurityManager
%_RUNJAVA% %TOMCAT_OPTS% -Djava.security.manager -Djava.security.policy=="%TOMCAT_HOME%/conf/tomcat.policy" -Dtomcat.home="%TOMCAT_HOME%" org.apache.tomcat.startup.Tomcat %3 %4 %5 %6 %7 %8 %9
goto cleanup

:stopServer
rem Stopping the Tomcat Server
%_RUNJAVA% %TOMCAT_OPTS% -Dtomcat.home="%TOMCAT_HOME%" org.apache.tomcat.startup.Tomcat -stop %2 %3 %4 %5 %6 %7 %8 %9
goto cleanup

:runAnt
rem Run ANT in Tomcat's Environment
set CP=%CP%;%TOMCAT_HOME%\lib\ant.jar
%_RUNJAVA% %ANT_OPTS% -Dant.home="%TOMCAT_HOME%" -Dtomcat.home="%TOMCAT_HOME%" org.apache.tools.ant.Main %2 %3 %4 %5 %6 %7 %8 %9
goto cleanup

:runJspc
rem Run JSPC in Tomcat's Environment
%_RUNJAVA% %JSPC_OPTS% -Dtomcat.home="%TOMCAT_HOME%" org.apache.jasper.JspC %2 %3 %4 %5 %6 %7 %8 %9
goto cleanup


rem ----- Restore Environment Variables ---------------------------------------

:cleanup
set _LIBJARS=
set _SECSTARTJAVA=
set _STARTJAVA=
set _RUNJAVA=
set CLASSPATH=%_CLASSPATH%
set _CLASSPATH=
set TOMCAT_HOME=%_TOMCAT_HOME%
set _TOMCAT_HOME=
set CP=%_CP%
set _CP=
:finish
