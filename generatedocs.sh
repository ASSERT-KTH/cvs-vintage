#!/bin/sh
#
# $Id: generatedocs.sh,v 1.2 2000/01/14 01:42:51 rubys Exp $

# Ksh wrapper to build the documentation
# Currently this only works on my personal setups as it uses Java2
# to create the documentation. But since we are generating the javadoc
# only as needed and not with every build, it's not a big issue.

javadoc -verbose -sourcepath src/share -d src/webpages/docs/api -use -version -author -windowtitle "Java Servlet API Reference, v2.2" -doctitle "Java Servlet API Reference, v2.2" javax.servlet javax.servlet.http

#C:/java/jdk1.2/bin/javadoc -sourcepath src/share -d src/docs/api-server -use -version -author -windowtitle "Project Tomcat API Documentation" -doctitle "Project Tomcat API" javax.servlet javax.servlet.http com.sun.tomcat.server com.sun.tomcat.shell com.sun.tomcat.util com.sun.tomcat.protocol.tfile

