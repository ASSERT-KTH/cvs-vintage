@echo off
REM convenience bat file to build with

REM get the pwd

@echo off
dir | find "Directory" > }1{.bat
set quote="
echo if %%3!==! set quote=> directory.bat
echo set PWD=%%quote%%%%2 %%3 %%4 %%5 %%6 %%7 %%8 %%9 %%quote%%>> directory.bat
call }1{
for %%a in (}1{ directory) do del %%a.bat
set quote=


set CP=%PWD%\..\..\lib\ant.jar
set CP=%CP%;%PWD%\..\..\lib\parser.jar
set CP=%CP%;%PWD%\..\..\lib\jaxp.jar
set CP=%CP%;%PWD%\..\..\lib\xmlbeans.jar
set CP=%CP%;%PWD%\..\..\build\classes
set CP=%CP%;%PWD%\..\..\lib\javac.jar

java -classpath "%CP%" org.apache.tools.ant.Main %1 %2 %3 %4 %5

