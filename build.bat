@echo off

REM  ======================================================================
REM
REM  This is the main entry point for the build system.
REM
REM  ======================================================================
REM
REM $Id: build.bat,v 1.8 2003/08/16 15:04:41 slaboure Exp $
REM
REM Authors:
REM     Jason Dillon <jason@planet57.com>
REM     Sacha Labourey <sacha.labourey@cogito-info.ch>
REM

REM ******************************************************
REM Ignore the ANT_HOME variable: we want to use *our*
REM ANT version and associated JARs.
REM ******************************************************
REM Ignore the users classpath, cause it might mess
REM things up
REM ******************************************************

SETLOCAL

set CLASSPATH=
set ANT_HOME=
set JAXP_DOM_FACTORY=org.apache.crimson.jaxp.DocumentBuilderFactoryImpl
set JAXP_SAX_FACTORY=org.apache.crimson.jaxp.SAXParserFactoryImpl
REM set JAXP_DOM_FACTORY=org.apache.xerces.jaxp.DocumentBuilderFactoryImpl
REM set JAXP_SAX_FACTORY=org.apache.xerces.jaxp.SAXParserFactoryImpl

set ANT_OPTS=-Djavax.xml.parsers.DocumentBuilderFactory=%JAXP_DOM_FACTORY% -Djavax.xml.parsers.SAXParserFactory=%JAXP_SAX_FACTORY%

REM ******************************************************
REM - "for" loops have been unrolled for compatibility
REM   with some WIN32 systems.
REM ******************************************************

set NAMES=tools;
set SUBFOLDERS=..;..\..;..\..\..;..\..\..\..

REM ******************************************************
REM ******************************************************

SET EXECUTED=FALSE
for %%i in (%NAMES%) do call :subLoop %%i %*

goto :EOF


REM ******************************************************
REM ********* Search for names in the subfolders *********
REM ******************************************************

:subLoop
SET SUBDIR=%1
SHIFT

set OTHER_ARGS=
:setupArgs
if %1a==a goto doneSetupArgs
set OTHER_ARGS=%OTHER_ARGS% %1
shift
goto setupArgs
:doneSetupArgs

for %%j in (%SUBFOLDERS%) do call :testIfExists %%j\%SUBDIR%\bin\ant.bat -logger org.apache.tools.ant.NoBannerLogger %OTHER_ARGS%

goto :EOF


REM ******************************************************
REM ************ Test if ANT Batch file exists ***********
REM ******************************************************

:testIfExists
if exist %1 call :BatchFound %*

goto :EOF


REM ******************************************************
REM ************** Batch file has been found *************
REM ******************************************************

:BatchFound
if (%EXECUTED%)==(FALSE) call :ExecuteBatch %*
set EXECUTED=TRUE

goto :EOF

REM ******************************************************
REM ************* Execute Batch file only once ***********
REM ******************************************************

:ExecuteBatch
echo Calling %*
call %*

:end

if "%NOPAUSE%" == "" pause
