/*
 * Copyright (c) 1997-2000 The Java Apache Project.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. All advertising materials mentioning features or use of this
 *    software must display the following acknowledgment:
 *    "This product includes software developed by the Java Apache 
 *    Project for use in the Apache JServ servlet engine project
 *    <http://java.apache.org/>."
 *
 * 4. The names "Apache JServ", "Apache JServ Servlet Engine" and 
 *    "Java Apache Project" must not be used to endorse or promote products 
 *    derived from this software without prior written permission.
 *
 * 5. Products derived from this software may not be called "Apache JServ"
 *    nor may "Apache" nor "Apache JServ" appear in their names without 
 *    prior written permission of the Java Apache Project.
 *
 * 6. Redistributions of any form whatsoever must retain the following
 *    acknowledgment:
 *    "This product includes software developed by the Java Apache 
 *    Project for use in the Apache JServ servlet engine project
 *    <http://java.apache.org/>."
 *    
 * THIS SOFTWARE IS PROVIDED BY THE JAVA APACHE PROJECT "AS IS" AND ANY
 * EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE JAVA APACHE PROJECT OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Java Apache Group. For more information
 * on the Java Apache Project and the Apache JServ Servlet Engine project,
 * please see <http://java.apache.org/>.
 *
 */

/*****************************************************************************
 * Description: jserv_utils.c provides a subset of functions used by jserv   *
 * Author:      Pierpaolo Fumagalli <ianosh@iname.com>                       *
 * Version:     $Revision: 1.1 $                                             *
 *****************************************************************************/
#include "jserv.h"

/* gettimeofday() */
#ifndef WIN32
#include <sys/time.h>
#include <unistd.h>
#endif


/*****************************************************************************
 * Local things                                                              *
 *****************************************************************************/
static const char *jserv_level(int level);

/*****************************************************************************
 * Shared JServ APIs                                                         *
 *****************************************************************************/

/* ========================================================================= */
/* Get JServ Configuration from server_rec structure */
jserv_config *jserv_server_config_get(server_rec *s) {
    jserv_config *cfg;

    cfg=(jserv_config *)ap_get_module_config(s->module_config,&jserv_module);
    return (cfg);
}

/* ========================================================================= */
/* Get JServ request from request_rec structure */
jserv_request *jserv_request_config_get(request_rec *r) {
    jserv_request *cfg;

    cfg=(jserv_request *)ap_get_module_config(r->request_config,&jserv_module);
    return (cfg);
}

/* ========================================================================= */
/* Returns a unsigned long representing the ip address of a string */
unsigned long jserv_resolve(char *value) {
    int x;

    /* Check if we only have digits in the string */
    for (x=0; value[x]!='\0'; x++)
        if (!isdigit(value[x]) && value[x] != '.') break;

    if (value[x] != '\0') {
        /* If we found also characters we use gethostbyname()*/
        struct hostent *host;

        host=gethostbyname(value);
        if (host==NULL) return 0;
        return ((struct in_addr *)host->h_addr_list[0])->s_addr;
    } else {
        /* If we found only digits we use inet_addr() */
        return inet_addr(value);
    }
    return 0;
}

/* ========================================================================= */
/* Open and read a file, returning contents and length */
const char *jserv_readfile(pool *p, char *name, int relative, char **buffer,
                           long *size) {
    FILE *file;
    char *filename;
    char *filebuffer;
    long filesize;
    long filelen;
    int x;

    /* Check our file name */
    if (name==NULL) return "filename was not specified";

    /* Check whether file is DISABLED */
    if (strcmp(name, "DISABLED")==0) {
        if (buffer!=NULL) *buffer=NULL;
        if (size!=NULL) *size=JSERV_DISABLED;
        return NULL;
    }

    /* Return our root file name */
    if (relative==JSERV_TRUE) filename=ap_server_root_relative(p,name);
    else filename=ap_pstrdup(p,name);

#ifdef WIN32
    /* Win32 slash/backslash conversion */
    for (x=0; filename[x]!=0; x++) if (filename[x]=='/') filename[x]='\\';
#endif /* ifdef WIN32 */

    /* Open file and check */
    file=fopen(filename,"rb");
    if (file==NULL)
        return ap_pstrcat(p,"file '",filename,"' cannot be opened",NULL);

    /* Seek file to end and check */
    x=fseek(file,0,SEEK_END);
    if (x!=0)
        return ap_pstrcat(p,"file '", filename,"' can not seek to end", NULL);

    /* Get file size, check and rewind */
    filesize=ftell(file);
    if (filesize==-1)
        return ap_pstrcat(p, "cannot get file '",filename,"' length", NULL);
    rewind(file);

    /* Check for ZERO length file */
    if (filesize==0)
        return ap_pstrcat(p, "file '",filename, "' has zero length",NULL);

    /* Allocate memory and then read */
    filebuffer=(char *)ap_pcalloc(p, filesize+1);
    filelen=fread(filebuffer, sizeof(char), filesize, file);

    /* Check if we got all bytes in file */
    if (filelen!=filesize)
        return ap_pstrcat(p,"cannot entirely read file '",filename,"'",NULL);

    /* Set length, close file and go on */
    if (buffer!=NULL) *buffer=filebuffer;
    if (size!=NULL) *size=filesize;
    fclose(file);
    return NULL;
}

/* ========================================================================= */
/* Open a file (used for logs) */
const char *jserv_openfile(pool *p, char *name, int relative, int *descriptor,
                           int flags, int mode) {
    char *filename;
    long filedesc;
#ifdef WIN32
    /* used in WIN32 */
    int x;
#endif

    /* Check our file name */
    if (name==NULL) return "filename was not specified";

    /* Check whether file is DISABLED */
    if (strcmp(name,"DISABLED")==0) {
        if (descriptor!=NULL) *descriptor=JSERV_DISABLED;
        return NULL;
    }

    /* Return our root file name */
    if (relative==JSERV_TRUE) filename=ap_server_root_relative(p,name);
    else filename=ap_pstrdup(p,name);

#ifdef WIN32
    /* Win32 slash/backslash conversion */
    for (x=0; filename[x]!=0; x++) if (filename[x]=='/') filename[x]='\\';
#endif /* ifdef WIN32 */

    /* Open file and check */
    filedesc=open(filename, flags, mode);
    if (filedesc==-1) {
        char *buf=ap_pstrcat(p,"file '",filename,"' can't be opened",NULL);
        return buf;
    }

    if (descriptor!=NULL) *descriptor=filedesc;
    return NULL;
}

/* ========================================================================= */
/* Log something to JServ log file then exit */
void jserv_error_exit(const char *file, int line, int level, jserv_config *cfg,
                 const char *fmt, ...) {
	va_list ap;

    /* Log our error */
    va_start(ap,fmt);
    jserv_error_var(file,line,level,cfg,fmt,ap);
    va_end(ap);

    if (cfg != NULL && cfg->server != NULL)
        ap_log_error(APLOG_MARK, APLOG_CRIT, cfg->server, "Apache JServ "
                     "encountered a fatal error; check your ApJServLogFile "
                     "for details if none are present in this file.  Exiting.");

    /* Exit process */
    exit(1);
}

/* ========================================================================= */
/* Log something to JServ log file thru argument */
void jserv_error(const char *file, int line, int level, jserv_config *cfg,
                 const char *fmt, ...) {
	va_list ap;

    /* Log our error */
    va_start(ap,fmt);
    jserv_error_var(file,line,level,cfg,fmt,ap);
    va_end(ap);
}

/* ========================================================================= */
/* Log something to JServ log file thru argument list */
void jserv_error_var(const char *file, int line, int level, jserv_config *cfg,
                 const char *fmt, va_list ap) {
    pool *p = NULL;
    char *buf;
    int buflen;
#ifdef JSERV_DEBUG
    int x;
#endif

#ifndef JSERV_DEBUG
    /* If it's a debug level return */
    if (level==APLOG_DEBUG) return;
#endif /* ifndef JSERV_DEBUG */

    /* Check if we have a valid configuration element */
    if (cfg!=NULL) {
#if APLOG_EMERG > APLOG_ERR
	if (level < cfg->loglevel)
#else
	if (level > cfg->loglevel)
#endif
	    return;
	/* don't create the pool unless we're actually going to use it.     *
	 * this should be replaced by passing in a pool, sine pool creation *
	 * isn't cheap. */
        p=ap_make_sub_pool(NULL);
        /* Check if jserv log file was opened */
        if (cfg->logfilefd>=0) {
            /* Prepare timestamp */
            buf=ap_psprintf(p,"[%s] (%s) ",jserv_time(p),jserv_level(level));
            buflen=strlen(buf);

            /* Write timestamp */
            write(cfg->logfilefd,buf,buflen);

            /* Write error */
            buf=ap_pvsprintf(p, fmt, ap);
            write(cfg->logfilefd,buf,strlen(buf));
            
#ifdef JSERV_DEBUG
            /* Log extended error informations */
            write(cfg->logfilefd,"\n",1);
            for (x=0; x<buflen; x++) write(cfg->logfilefd," ",1);
            buf=ap_psprintf(p, "File: %s (line=%d)", file, line);
#endif /* ifdef JSERV_DEBUG */

            /* Put newline character, cleanup and exit */
            write(cfg->logfilefd,"\n",1);
            ap_destroy_pool(p);
            return;

        /* Check if server was defined */
        } else if (cfg->server!=NULL) {
            /* Prepare error */
            buf=ap_pvsprintf(p, fmt, ap);

            ap_log_error(file, line, level|APLOG_NOERRNO, cfg->server,
                         "JServ: %s", buf);
            /* Cleanup and exit */
            ap_destroy_pool(p);
            return;
        }
    } else {
        /* we're going to need a pool ... so we have to create it. */
        p=ap_make_sub_pool(NULL);
    }

    /* We didn't get a JServ logfile or an Apache Server, log to stderr */
    buf=ap_psprintf(p, "[%s] JServ (%s) ", jserv_time(p), jserv_level(level));
    buflen=strlen(buf);

    /* Write timestamp */
    fprintf(stderr,"%s",buf);

    /* Write error */
    vfprintf(stderr, fmt, ap);
            
#ifdef JSERV_DEBUG
    /* Log extended error informations */
    fprintf(stderr, "\n");
    for (x=0; x<buflen; x++) fputc(' ',stderr);
    fprintf(stderr, "File: %s (line=%d)", file, line);
#endif /* ifdef JSERV_DEBUG */

    /* Put newline character, cleanup and exit */
    fprintf(stderr, "\n");
    ap_destroy_pool(p);
}

/* ========================================================================= */
/* Returns a string with current date/time (up to milliseconds) */
char *jserv_time(pool *p) {
#ifdef WIN32
    SYSTEMTIME current;
    
    GetSystemTime(&current);
    return ap_psprintf(p,"%02d/%02d/%04d %02d:%02d:%02d:%03d\0",
    current.wDay, current.wMonth, current.wYear,
    current.wHour, current.wMinute, current.wSecond,
    current.wMilliseconds);
#else /* ifdef WIN32*/
    struct timeval mytv;
    struct tm *mytm;
    
    gettimeofday(&mytv, NULL);
    mytm=localtime((time_t *)&mytv.tv_sec);
    
    return ap_psprintf(p,"%02d/%02d/%04d %02d:%02d:%02d:%03ld\0",
    mytm->tm_mday, mytm->tm_mon+1, mytm->tm_year+1900,
    mytm->tm_hour, mytm->tm_min, mytm->tm_sec,
    mytv.tv_usec/1000);
#endif /* ifdef WIN32*/
}

/*****************************************************************************
 * Local things                                                              *
 *****************************************************************************/

/* ========================================================================= */
/* Returns a string with error level (used by jserv_error) */
static const char *jserv_level(int level) {
    switch (level) {
        case APLOG_DEBUG:
            return "DEBUG"; break;
        case APLOG_INFO:
            return "INFO"; break;
        case APLOG_NOTICE:
            return "NOTICE"; break;
        case APLOG_WARNING:
            return "WARNING"; break;
        case APLOG_ERR:
            return "ERROR"; break;
        case APLOG_CRIT:
            return "CRITICAL"; break;
        case APLOG_ALERT:
            return "ALERT"; break;
        case APLOG_EMERG:
            return "EMERGENCY"; break;
    }
    return "UNKNOWN";
}
