@echo off
rem build.bat -- Build Script for the "Hello, World" Application
rem $Id: build.bat,v 1.6 2004/02/29 22:42:51 billbarker Exp $
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

set _CP=%CP%

rem Identify the custom class path components we need
set CP=%TOMCAT_HOME%\webapps\admin\WEB-INF\lib\ant.jar;%TOMCAT_HOME%\lib\common\servlet.jar
set CP=%CP%;%TOMCAT_HOME%\lib\container\crimson.jar
set CP=%CP%;%JAVA_HOME%\lib\tools.jar

rem Execute ANT to perform the required build target
java -classpath %CP%;%CLASSPATH% org.apache.tools.ant.Main -Dtomcat.home=%TOMCAT_HOME% %1 %2 %3 %4 %5 %6 %7 %8 %9

set CP=%_CP%
set _CP=
