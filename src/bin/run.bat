@echo off
@if not "%ECHO%" == ""  echo %ECHO%
@if "%OS%" == "Windows_NT"  setlocal

set JBOSS_CLASSPATH=%JBOSS_CLASSPATH%;run.jar

REM Add all login modules for JAAS-based security
REM and all libraries that are used by them here
set JBOSS_CLASSPATH=%JBOSS_CLASSPATH%

REM Add the XML parser jars and set the JAXP factory names
REM Crimson parser JAXP setup(default)
set JBOSS_CLASSPATH=%JBOSS_CLASSPATH%;../lib/crimson.jar
set JAXP=-Djavax.xml.parsers.DocumentBuilderFactory=org.apache.crimson.jaxp.DocumentBuilderFactoryImpl
set JAXP=%JAXP% -Djavax.xml.parsers.SAXParserFactory=org.apache.crimson.jaxp.SAXParserFactoryImpl

echo JBOSS_CLASSPATH=%JBOSS_CLASSPATH%
java %JAXP% -classpath "%JBOSS_CLASSPATH%" org.jboss.Main %1 %2 %3 %4 %5 %6 %7 %8 %9

pause
