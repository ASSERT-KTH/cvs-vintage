rem ---------------------------------------------------------------------------
rem Append a path onto the CLASSPATH
rem
rem $Id: cpappend.bat,v 1.5 2002/02/11 18:41:56 jon Exp $
rem ---------------------------------------------------------------------------

rem Get remaining unshifted command line arguments and save them in the
set CMD_LINE_ARGS=%1
shift
:setArgs
if ""%1""=="""" goto doneSetArgs
set CMD_LINE_ARGS=%CMD_LINE_ARGS% %1
shift
goto setArgs
:doneSetArgs

