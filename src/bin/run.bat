@echo off
@if not "%ECHO%" == ""  echo %ECHO%
@if "%OS%" == "Windows_NT"  setlocal

set CLASSPATH=%CLASSPATH%;run.jar

REM Add all login modules for JAAS-based security
REM and all libraries that are used by them here
set CLASSPATH=%CLASSPATH%

REM Add the XML parser jars and set the JAXP factory names
REM Crimson parser JAXP setup(default)
set CLASSPATH=%CLASSPATH%;../lib/crimson.jar
set JAXP=-Djavax.xml.parsers.DocumentBuilderFactory=org.apache.crimson.jaxp.DocumentBuilderFactoryImpl
set JAXP=%JAXP% -Djavax.xml.parsers.SAXParserFactory=org.apache.crimson.jaxp.SAXParserFactoryImpl

echo CLASSPATH=%CLASSPATH%
java %JAXP% -classpath "%CLASSPATH%" org.jboss.Main %1 %2 %3 %4 %5 %6 %7 %8 %9

pause
