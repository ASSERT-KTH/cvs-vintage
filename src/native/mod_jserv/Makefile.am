
## $Id: Makefile.am,v 1.1 2000/08/26 01:21:50 alex Exp $

LIBEXECDIR=@LIBEXECDIR@
TEST=@TEST@
CP=@CP@

SRCS = mod_jserv.c \
     jserv_protocols.c jserv_ajpv11.c jserv_ajpv12.c \
     jserv_balance.c jserv_mmap.c jserv_watchdog.c \
     jserv_utils.c jserv_status.c \
     jserv_wrapper.c jserv_wrapper_unix.c \
     jserv_image.c

HDRS = jserv_wrapper.h
APACHE_FILES = Makefile.tmpl Makefile.libdir libjserv.module

EXTRA_DIST = ${APACHE_FILES} ${HDRS}

libjserv_la_LDFLAGS = -avoid-version -o libjserv.la -rpath ${LIBEXECDIR}

if MAKE_LIBJSERV
noinst_LTLIBRARIES = libjserv.la
libjserv_la_SOURCES = ${SRCS}

MODSO=mod_jserv.so
LIBJSO=libjserv.so
LIBJA=libjserv.a
LIBJSL=libjserv.sl

install-data-local:
	@echo ""
	@echo "Copying files to Apache Modules Directory..."
	${top_srcdir}/src/scripts/build/unix/mkinstalldirs $(DESTDIR)${LIBEXECDIR}

	# on AIX, the .so file is created with a number, ie:
	# libjserv.so.0.0.0. It seems as though on though a libjserv.a
	# file is also created (and on Solaris, the .a file isn't created)
	# so check for that to make things work.
	# HPUX uses .sl
	@ if ${TEST} -e ./.libs/${LIBJA} ; then \
		echo "${CP} ./.libs/${LIBJA} $(DESTDIR)${LIBEXECDIR}/${MODSO}" ; \
		${CP} ./.libs/${LIBJA} $(DESTDIR)${LIBEXECDIR}/${MODSO} ; \
        elif ${TEST} -e ./.libs/${LIBJSO} ; then \
		echo "${CP} ./.libs/${LIBJSO} $(DESTDIR)${LIBEXECDIR}/${MODSO}" ; \
		${CP} ./.libs/${LIBJSO} $(DESTDIR)${LIBEXECDIR}/${MODSO} ; \
        elif ${TEST} -e ./.libs/${LIBJSL} ; then \
		echo "${CP} ./.libs/${LIBJSL} $(DESTDIR)${LIBEXECDIR}/${MODSO}" ; \
		${CP} ./.libs/${LIBJSL} $(DESTDIR)${LIBEXECDIR}/${MODSO} ; \
	else \
		echo "Could not find and install the shared library." ; \
		echo "Please check in the src/c/.libs directory for it." ; \
	fi
	@echo ""
else

noinst_LIBRARIES = libjserv.a
libjserv_a_SOURCES = ${SRCS}

install-data-local:
	@echo ""
	@echo "Copying files to Apache Modules Directory..."
	${CP} ${APACHE_FILES} ${APACHE_DIR}/src/modules/jserv
	${CP} ${noinst_LIBRARIES} ${APACHE_DIR}/src/modules/jserv
	@echo ""
	@echo "Please be sure to re-compile Apache..."
	@echo ""
	@echo "cd ${APACHE_DIR}"
	@echo "./config-status --activate-module=src/modules/jserv/libjserv.a"
	@echo "make"
	@echo ""
endif



