@echo off
echo.

rem #--------------------------------------------
rem # No need to edit anything past here
rem #--------------------------------------------

set BUILDFILE=%0\..\build.xml

if "%JAVA_HOME%" == "" goto JavaHomeError

set BUILDCLASSPATH=%JAVA_HOME%\lib\tools.jar;%0\..\..\src\tomcat-4.0\server\lib\jaxp.jar;%0\..\..\src\tomcat-4.0\server\lib\crimson.jar;%0\..\ant-1.3.jar

%JAVA_HOME%\bin\java -classpath %BUILDCLASSPATH% org.apache.tools.ant.Main -buildfile %BUILDFILE% %1 %2 %3 %4 %5 %6 %7 %8 %9

goto End

:JavaHomeError
    echo ERROR: JAVA_HOME not found in your environment.
    echo Please, set the JAVA_HOME variable in your environment to match the
    echo location of the Java Virtual Machine you want to use.
    
    goto end

:End
