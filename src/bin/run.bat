@echo off
@if not "%ECHO%" == ""  echo %ECHO%
@if "%OS%" == "Windows_NT"  setlocal

set JBOSS_CLASSPATH=%JBOSS_CLASSPATH%;run.jar

REM Setup JBoss sepecific properties
set JAVA_OPTS="%JAVA_OPTS% -Djboss.boot.loader.name=run.bat"

REM Set the JAXP factory names
REM Crimson parser JAXP setup(default)
set JAXP=-Djavax.xml.parsers.DocumentBuilderFactory=org.apache.crimson.jaxp.DocumentBuilderFactoryImpl
set JAXP=%JAXP% -Djavax.xml.parsers.SAXParserFactory=org.apache.crimson.jaxp.SAXParserFactoryImpl

echo JBOSS_CLASSPATH=%JBOSS_CLASSPATH%
java %JAVA_OPTS% %JAXP% -classpath "%JBOSS_CLASSPATH%" org.jboss.Main %1 %2 %3 %4 %5 %6 %7 %8 %9

pause
