# Simple makefile - autoconf later. You need to set APACHE_HOME

APXS=${APACHE_HOME}/bin/apxs
AUX_FILES=msg_buffer_simple.c rmethods.c module_c.c
DEBUG=-g

all: ${APACHE_HOME}/libexec/mod_tcp.so

door: ${APACHE_HOME}/libexec/mod_door.so 

${APACHE_HOME}/libexec/mod_door.so: mod_door.c connection_door.c ${AUX_FILES}
	${APXS} -c mod_door.c ${DEBUG} -o $@ -ldoor -lpthread
	rm mod_door.o

${APACHE_HOME}/libexec/mod_tcp.so: mod_tcp.c connection_tcp.c ${AUX_FILES}
	${APXS} -c mod_tcp.c ${DEBUG} -o $@
	rm mod_tcp.o

clean: 
	rm -f ${APACHE_HOME}/libexec/mod_tcp.so
	rm -f ${APACHE_HOME}/libexec/mod_door.so
