@echo off
@if not "%ECHO%" == ""  echo %ECHO%
@if "%OS%" == "Windows_NT"  setlocal

REM convenience bat file to build with


REM get the pwd

@echo off
dir | find "Directory" > }1{.bat
set quote="
echo if %%3!==! set quote=> directory.bat
rem echo set PWD=%%quote%%%%2 %%3 %%4 %%5 %%6 %%7 %%8 %%9 %%quote%%>>directory.bat
echo set PWD=%%quote%%>>directory.bat
echo if not %%2!==! set PWD=%%PWD%%%%2>>directory.bat
echo if not %%3!==! set PWD=%%PWD%%%%3>>directory.bat
echo if not %%4!==! set PWD=%%PWD%%%%4>>directory.bat
echo if not %%5!==! set PWD=%%PWD%%%%5>>directory.bat
echo if not %%6!==! set PWD=%%PWD%%%%6>>directory.bat
echo if not %%7!==! set PWD=%%PWD%%%%7>>directory.bat
echo if not %%8!==! set PWD=%%PWD%%%%8>>directory.bat
echo if not %%9!==! set PWD=%%PWD%%%%9>>directory.bat
echo set PWD=%%PWD%%%%quote%%>>directory.bat
call }1{

rem tidy up and remove temp scripts and unset environment var
for %%a in (}1{ directory) do del %%a.bat
set quote=

for %%i in (..\..\lib\*.jar) do call lcp.bat %%i


set CP=%CP%;%PWD%\..\..\build\classes

echo CLASSPATH= %CP%


java -classpath "%CP%" org.apache.tools.ant.Main %1 %2 %3 %4 %5
