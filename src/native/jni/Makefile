# predifined
# HOSTTYPE
# JAVA_HOME
# OS=linux

LIB_DEST=.

ifeq ($(HOSTTYPE),sparc)
OS=solaris
LIBS=-lmalloc -ldoor -lpthread
LD_FLAGS=-B dynamic	
SHARED_FLAG=-G
CFLAGS=-I${JAVA_HOME}/include -I${JAVA_HOME}/include/${OS} -g
endif

ifeq ($(HOSTTYPE),i386)
## DOORS=/home/costin/src/var/doors/doors-0.9.1
CFLAGS=-fpic -I${JAVA_HOME}/include -I${JAVA_HOME}/include/${OS} -I${JAVA_HOME}/include/genunix -g
# -I ${DOORS}  LIBS=-L ${DOORS}/libdoor -lpthread
LIBS=-ldoor -lpthread
SHARED_FLAG=-shared
OS=linux
endif






all: ${LIB_DEST}/doorTest ${LIB_DEST}/libdoorJNI.so

${LIB_DEST}/doorTest: doorTest.c
	${CC} ${LDFLAGS} ${CFLAGS} -o $@ $< ${LIBS}


${LIB_DEST}/libdoorJNI.so: doorJNI.c
	${CC} ${SHARED_FLAG}  ${CFLAGS} -o $@ $< -ldoor
# 		${LD} ${LD_FLAGS} ${LIBS} -o $@ $< ${LIBS}

xxx:
	${CC} ${CFLAGS} -c -fpic $< 

clean: 
	rm -f ${LIB_DEST}/libdoorJNI.so
	rm -f *.o	
	rm -f ${LIB_DEST}/doorTest
