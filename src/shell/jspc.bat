@echo off
rem $Id: jspc.bat,v 1.7 2004/02/27 05:32:58 billbarker Exp $
rem A batch file to run the JspC Compiler
rem
rem   Copyright 1999-2004 The Apache Software Foundation
rem 
rem   Licensed under the Apache License, Version 2.0 (the "License");
rem   you may not use this file except in compliance with the License.
rem   You may obtain a copy of the License at
rem 
rem       http://www.apache.org/licenses/LICENSE-2.0
rem 
rem   Unless required by applicable law or agreed to in writing, software
rem   distributed under the License is distributed on an "AS IS" BASIS,
rem   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
rem   See the License for the specific language governing permissions and
rem   limitations under the License.
rem

rem This batch file written and tested under Windows NT
rem Improvements to this file are welcome

set _TC_BIN_DIR=%TOMCAT_HOME%\bin
if not "%_TC_BIN_DIR%" == "\bin" goto start

set _TC_BIN_DIR=.\bin
if exist "%_TC_BIN_DIR%\tomcat.bat" goto start

set _TC_BIN_DIR=.
if exist "%_TC_BIN_DIR%\tomcat.bat" goto start

set _TC_BIN_DIR=..\bin
if exist "%_TC_BIN_DIR%\tomcat.bat" goto start

echo Unable to determine the location of Tomcat.
goto eof

:start
call "%_TC_BIN_DIR%\tomcat" jspc %1 %2 %3 %4 %5 %6 %7 %8 %9

:eof
set _TC_BIN_DIR=
