#!/bin/sh

#--------------------------------------------
# No need to edit anything past here
#--------------------------------------------
if test -z "${JAVA_HOME}" ; then
    echo "ERROR: JAVA_HOME not found in your environment."
    echo "Please, set the JAVA_HOME variable in your environment to match the"
    echo "location of the Java Virtual Machine you want to use."
    exit
fi

CLASSPATH=""

if [ -f ${JAVA_HOME}/lib/tools.jar ] ; then
    CLASSPATH=${JAVA_HOME}/lib/tools.jar
fi

# convert the existing path to unix
if [ "$OSTYPE" = "cygwin32" ] || [ "$OSTYPE" = "cygwin" ] ; then
   CLASSPATH=`cygpath --path --unix "$CLASSPATH"`
fi

# order of the for loops matters here
for i in `find ../src -name "crimson*.jar" -print | tail -1` ; do
   CLASSPATH="$CLASSPATH:$i"
done

for i in `find ../src -name "jaxp*.jar" -print | tail -1` ; do
   CLASSPATH="$CLASSPATH:$i"
done

for i in `find . -name "ant*.jar" -print` ; do
   CLASSPATH="$CLASSPATH:$i"
done

# convert the unix path to windows
if [ "$OSTYPE" = "cygwin32" ] || [ "$OSTYPE" = "cygwin" ] ; then
   CLASSPATH=`cygpath --path --windows "$CLASSPATH"`
fi

echo "Classpath:"
echo "$CLASSPATH"
echo ""

BUILDFILE=build.xml

${JAVA_HOME}/bin/java -classpath ${CLASSPATH} org.apache.tools.ant.Main \
                      -buildfile ${BUILDFILE} "$@"



