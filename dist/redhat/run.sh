#!/bin/sh
cd /opt/columba-@version@
java -Djava.library.path="native/linux/lib/" -Dcolumba.class.path=native/linux/lib/jdic.jar -jar columba.jar $@
