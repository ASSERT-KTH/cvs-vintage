@echo off
rem -------------------------------------------------------------------------
rem tomcat.bat - Start/Stop Script for the TOMCAT Server
rem
rem Environment Variable Prerequisites:
rem
rem   TOMCAT_HOME  (Optional) May point at your Tomcat distribution
rem                directory.  If not present, the current working
rem                directory is assumed.
rem
rem   TOMCAT_OPTS  (Optional) Java runtime options used when the "start",
rem                "stop", or "run" command is executed
rem
rem   CLASSPATH    Must contain a JAXP-compatible XML parser, such as the
rem                "jaxp.jar" and "parser.jar" files from the JAXP Reference
rem                implementation.
rem
rem   JAVA_HOME    Must point at your Java Development Kit installation.
rem
rem $Id: tomcat.bat,v 1.24 2000/06/19 02:44:55 rubys Exp $
rem -------------------------------------------------------------------------


rem ----- Save Environment Variables That May Change ------------------------

set _CP=%CP%
set _TOMCAT_HOME=%TOMCAT_HOME%


rem ----- Verify and Set Required Environment Variables ---------------------

if not "%JAVA_HOME%" == "" goto gotJavaHome
echo You must have set JAVA_HOME to point at your Java Development Kit installation
goto cleanup
:gotJavaHome

if not "%TOMCAT_HOME%" == "" goto gotTomcatHome
set TOMCAT_HOME=.
:gotTomcatHome


rem ----- Set Up The Runtime Classpath --------------------------------------

set CP=%TOMCAT_HOME%\classes
for %%i in (%TOMCAT_HOME%\lib\*.jar) do call %TOMCAT_HOME%\bin\cpappend.bat %%i
if "%CLASSPATH%" == "" goto noClasspath
set CP=%CP%;%CLASSPATH%
:noClasspath
set CP=%CP%;%JAVA_HOME%\lib\tools.jar
echo Using CLASSPATH: %CP%


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
start java %TOMCAT_OPTS% -classpath %CP% -Dtomcat.home="%TOMCAT_HOME%" org.apache.tomcat.startup.Tomcat %2 %3 %4 %5 %6 %7 %8 %9
goto cleanup

:startSecure
echo Starting Tomcat with a SecurityManager
start java %TOMCAT_OPTS% -classpath %CP% -Djava.security.manager -Djava.security.policy=="%TOMCAT_HOME%/conf/tomcat.policy" -Dtomcat.home="%TOMCAT_HOME%" org.apache.tomcat.startup.Tomcat %3 %4 %5 %6 %7 %8 %9
goto cleanup

:runServer
rem Running Tomcat in this window
if "%2" == "-security" goto runSecure
java %TOMCAT_OPTS% -classpath %CP% -Dtomcat.home="%TOMCAT_HOME%" org.apache.tomcat.startup.Tomcat %2 %3 %4 %5 %6 %7 %8 %9
goto cleanup

:runSecure
rem Running Tomcat with a SecurityManager
java %TOMCAT_OPTS% -classpath %CP% -Djava.security.manager -Djava.security.policy=="%TOMCAT_HOME%/conf/tomcat.policy" -Dtomcat.home="%TOMCAT_HOME%" org.apache.tomcat.startup.Tomcat %3 %4 %5 %6 %7 %8 %9
goto cleanup

:stopServer
rem Stopping the Tomcat Server
java %TOMCAT_OPTS% -classpath %CP% -Dtomcat.home="%TOMCAT_HOME%" org.apache.tomcat.startup.Tomcat -stop %2 %3 %4 %5 %6 %7 %8 %9
goto cleanup

:runAnt
rem Run ANT in Tomcat's Environment
set CP=%CP%;%TOMCAT_HOME%\lib\ant.jar
java %ANT_OPTS% -classpath %CP% -Dant.home="%TOMCAT_HOME%" -Dtomcat.home="%TOMCAT_HOME%" org.apache.tools.ant.Main %2 %3 %4 %5 %6 %7 %8 %9
goto cleanup

:runJspc
rem Run JSPC in Tomcat's Environment
java %JSPC_OPTS% -classpath %CP% -Dtomcat.home="%TOMCAT_HOME%" org.apache.jasper.JspC %2 %3 %4 %5 %6 %7 %8 %9
goto cleanup


rem ----- Restore Environment Variables ---------------------------------------

:cleanup
set TOMCAT_HOME=%_TOMCAT_HOME%
set _TOMCAT_HOME=
set CP=%_CP%
set _CP=
:finish
