if [ -d "$TOMCAT_HOME" -a -d "$SERVLETAPI_HOME" ] ; then 
   CLASSPATH=$CLASSPATH:$TOMCAT_HOME/lib
   CLASSPATH=$CLASSPATH:$SERVLETAPI_HOME/lib/servlet.jar
fi

CLASSPATH=$CLASSPATH:run.jar

java -classpath $CLASSPATH -Dtomcat.home=$TOMCAT_HOME org.jboss.Main
