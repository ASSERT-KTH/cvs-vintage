@echo off
@if not "%ECHO%" == ""  echo %ECHO%
@if "%OS%" == "Windows_NT"  setlocal

REM Include the JDK javac compiler for JSP pages. The default is for a Sun JDK
REM compatible distribution to which JAVA_HOME points
set JAVAC_JAR=%JAVA_HOME%\lib\tools.jar
set JBOSS_CLASSPATH=%JBOSS_CLASSPATH%;%JAVAC_JAR%;run.jar

REM Setup JBoss sepecific properties
set JAVA_OPTS=%JAVA_OPTS% -Djboss.boot.loader.name=run.bat

REM JPDA options. Uncomment and modify as appropriate to enable remote debugging.
REM set JAVA_OPTS=%JAVA_OPTS% -classic -Xdebug -Xnoagent -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,address=8787,server=y,suspend=y

REM Set the JAXP factory names
REM Crimson parser JAXP setup(default)
set JAXP=-Djavax.xml.parsers.DocumentBuilderFactory=org.apache.crimson.jaxp.DocumentBuilderFactoryImpl
set JAXP=%JAXP% -Djavax.xml.parsers.SAXParserFactory=org.apache.crimson.jaxp.SAXParserFactoryImpl

echo JBOSS_CLASSPATH=%JBOSS_CLASSPATH%
java %JAVA_OPTS% %JAXP% -classpath "%JBOSS_CLASSPATH%" org.jboss.Main %1 %2 %3 %4 %5 %6 %7 %8 %9

pause
