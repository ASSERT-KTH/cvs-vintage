#!/bin/sh

# This line should be set up correctly by the build process
JDK=1.3

# This line should be set up correctly by the build process

# All clients need this
LIB=../lib/ext
CP=${LIB}/ejb.jar
CP=$CP:${LIB}/jndi.jar
CP=$CP:${LIB}/jta-spec1_0_1.jar
CP=$CP:jboss-client.jar
CP=$CP:TestBeanClient.jar
CP=$CP:${LIB}/jdbc2_0-stdext.jar

echo $CP
java -classpath $CP org.jboss.zol.testbean.client.EjbossClient $*
