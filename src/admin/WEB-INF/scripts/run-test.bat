rem
rem Copyright 2001-2004 The Apache Software Foundation
rem
rem Licensed under the Apache License, Version 2.0 (the "License");
rem you may not use this file except in compliance with the License.
rem You may obtain a copy of the License at
rem
rem     http://www.apache.org/licenses/LICENSE-2.0
rem
rem Unless required by applicable law or agreed to in writing, software
rem distributed under the License is distributed on an "AS IS" BASIS,
rem WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
rem See the License for the specific language governing permissions and
rem limitations under the License.
rem
@echo off

rem Batch file to run the tomcat sanity test suite
rem Note: You may send the output to a file using "run-tomcat -l file"

if not "%TOMCAT_HOME%" == "" goto gotTomcatHome
echo You need to set TOMCAT_HOME
goto exit

:gotTomcatHome

set _OLDCP=%CLASSPATH%

set CLASSPATH=%TOMCAT_HOME%/lib/container/parser.jar
set CLASSPATH=%TOMCAT_HOME%/lib/container/crimson.jar;%CLASSPATH%
set CLASSPATH=%TOMCAT_HOME%/lib/container/xerces.jar;%CLASSPATH%
set CLASSPATH=%TOMCAT_HOME%/lib/container/jaxp.jar;%CLASSPATH%
set CLASSPATH=%TOMCAT_HOME%/webapps/admin/WEB-INF/lib/ant.jar;%CLASSPATH%
set CLASSPATH=%TOMCAT_HOME%/webapps/admin/WEB-INF/lib/tomcat_util_test.jar;%CLASSPATH%
set CLASSPATH=%TOMCAT_HOME%/webapps/admin/WEB-INF/classes;%CLASSPATH%

if "%_OLDCP%" == "" goto runant
set CLASSPATH=%CLASSPATH%;%_OLDCP%

:runant
call ant -Dgdir="%TOMCAT_HOME%/webapps/test/Golden" -f "%TOMCAT_HOME%/webapps/test/WEB-INF/test-tomcat.xml" %1 %2 %3 %4 %5 %6 %7 %8 %9 client

set CLASSPATH=%_OLDCP%

:exit