@echo off
@if not "%ECHO%" == ""  echo %ECHO%
@if "%OS%" == "Windows_NT"  setlocal

set JBOSS_CLASSPATH=%JBOSS_CLASSPATH%;shutdown.jar

echo JBOSS_CLASSPATH=%JBOSS_CLASSPATH%
java %JAVA_OPTS% -classpath "%JBOSS_CLASSPATH%" org.jboss.Shutdown %1 %2 %3 %4 %5 %6 %7 %8 %9

pause
