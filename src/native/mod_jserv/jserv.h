/*
 * Copyright (c) 1997-1999 The Java Apache Project.  All rights reserved.
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
 * Description: Apache JServ global include file                             *
 * Author:      Pierpaolo Fumagalli <ianosh@iname.com>                       *
 * Version:     $Revision: 1.2 $                                            *
 *****************************************************************************/
#ifndef __JSERV_H__
#define __JSERV_H__

#include "httpd.h"
#include "http_config.h"
#include "http_core.h"
#include "http_log.h"
#include "http_main.h"
#include "http_protocol.h"
#include "http_request.h"
#include "util_script.h"
#include "util_md5.h"

/* This is where we turn on all load-balancing code */
#define LOAD_BALANCE

/* XXX used only by jserv_status - need a bit of discussion as
   probably status will be a bit different in tomcat anyway */
#define JSERV_NAME "tomcat"
#define JSERV_VERSION "1.0"

/*****************************************************************************
 * Name of the servlet to call for JServ status                              *
 *****************************************************************************/
#define JSERV_SERVLET "org.apache.jserv.JServ"

/*****************************************************************************
 * Some defined values and our defaults                                      *
 *****************************************************************************/

/* Our TRUE, FALSE, DEFAULT and DISABLED values (for configuration) */
#define JSERV_FALSE 0
#define JSERV_TRUE -1
#define JSERV_DEFAULT -2
#define JSERV_DISABLED -3

/* Our LOG definition (used for jserv_error) */
#define JSERV_LOG_DEBUG __FILE__,__LINE__,APLOG_DEBUG
#define JSERV_LOG_INFO  __FILE__,__LINE__,APLOG_INFO
#define JSERV_LOG_ERROR __FILE__,__LINE__,APLOG_ERR
#define JSERV_LOG_EMERG __FILE__,__LINE__,APLOG_EMERG

/* Our LOG file mode and flags (OS/2 and W32 don't support users and groups) */
#define JSERV_LOGFILE_FLAGS O_WRONLY|O_APPEND|O_CREAT
#if defined(__EMX__) || defined(WIN32)
#define JSERV_LOGFILE_MODE S_IREAD|S_IWRITE
#else /* if defined(__EMX__) || defined(WIN32) */
#define JSERV_LOGFILE_MODE S_IRUSR|S_IWUSR|S_IRGRP|S_IROTH
#endif /* if defined(__EMX__) || defined(WIN32) */

/* Configuration defaults */
#define JSERV_DEFAULT_MANUAL        JSERV_FALSE
#define JSERV_DEFAULT_PROPERTIES    "./conf/jserv.properties"
#define JSERV_DEFAULT_PROTOCOL      "ajpv12"
#define JSERV_DEFAULT_HOST          "localhost"
#define JSERV_DEFAULT_MOUNTCOPY     JSERV_TRUE
#define JSERV_DEFAULT_LOGFILE       "./logs/mod_jserv.log"
#define JSERV_DEFAULT_SECRETFILE    "./conf/jserv.secret.key"
#define JSERV_DEFAULT_VMTIMEOUT     10
#define JSERV_DEFAULT_VMINTERVAL    10


/* Currently defined protocol functions (many could be added) */
#define JSERV_SHUTDOWN 1
#define JSERV_RESTART  2
/* This function checks if JVM accepts connections */
#define JSERV_PING     3

/* Return values for functions */
#define JSERV_FUNC_OK 			0
#define JSERV_FUNC_ERROR 		-1
#define JSERV_FUNC_NOTIMPLEMENTED 	-2
#define JSERV_FUNC_COMMERROR 		-3

/* Environment pointer */
#ifndef WIN32
extern char **environ;
#endif /* ifndef WIN32 */

/* Image */
extern unsigned char jserv_image[];
extern long jserv_image_size;

/*****************************************************************************
 * Structures definition                                                     *
 *****************************************************************************/
typedef struct jserv_config jserv_config;
typedef struct jserv_mount jserv_mount;
typedef struct jserv_protocol jserv_protocol;
#ifdef LOAD_BALANCE
typedef struct jserv_balance jserv_balance;
typedef struct jserv_host jserv_host;
#endif

typedef struct jserv_request jserv_request;

/* ========================================================================= */
/* Apache JServ configuration structure */
struct jserv_config {
    server_rec *server;         /* The server we are configured for */
    int manual;                 /* Manual Mode TRUE/FALSE */
    char *properties;           /* jserv.properties file name */
    jserv_protocol *protocol;   /* Default Apache JServ protocol */
    char *host;                 /* Default Apache JServ host */
    unsigned long hostaddr;     /* Default Apache JServ host (32bit ip address) */
    short port;                 /* Default Apache JServ port */
    jserv_mount *mount;         /* Pointer to mount structures */
#ifdef LOAD_BALANCE
    char *shmfile;              /* shared memory file name */
    jserv_balance *balancers;   /* Available JServ load-balancers */
    jserv_host *hosturls;       /* Available JServ hosts */
#endif
    int mountcopy;              /* Should we copy base host mounts */
    char *logfile;              /* log file name */
    int logfilefd;              /* log file descriptor */
    int loglevel;               /* log level */
    char *secretfile;           /* Our secret file name */
    char *secret;               /* Our secret value */
    long secretsize;            /* Our secret value lenght */
    table *actions;             /* Actions table (extension->servlet map) */
    jserv_config *next;         /* Next server strucure in servers chain*/
    int retryattempts;          /* Number of times to attempt to connect */
    int vmtimeout;              /* Seconds to give the JVM to start/stop */
    int vminterval;             /* interval to poll the JVM  */
    table *envvars;             /* table of env vars to send */

};

/* ========================================================================= */
/* Apache JServ mount structure */
struct jserv_mount {
    char *mountpoint;           /* The mount point for this */
    jserv_config *config;       /* In which server it was added */
    jserv_protocol *protocol;   /* The protocol for this (NULL=default) */
    char *host;                 /* The host for this (NULL=default) */
    unsigned long hostaddr;     /* The host for this (32bit ip address) */
    short port;                 /* The port for this (0=default) */
    char *secretfile;           /* Our secret file name */
    char *secret;               /* Our secret value */
    long secretsize;            /* Our secret value lenght */
    char *zone;                 /* The zone for this (NULL means that
                                   zone is in the request_rec->uri */
#ifdef LOAD_BALANCE
    jserv_host *curr;           /* current server */
    jserv_host *hosturls;       /* Balance of servers for this mount (circular list) */
#endif
    jserv_mount *next;          /* Next mount point (NULL=end-of-list) */
};

#ifdef LOAD_BALANCE
/* ========================================================================= */
/* Apache JServ balance list structure */
struct jserv_balance {
    char *name;                /* The name of this set of hosts */   
    char *host_name;           /* The name for this host */   
    int weight;                /* Weight for this host */   
    jserv_balance *next;       /* Next balancer in the list (NULL=end-of-list) */
};

/* ========================================================================= */
/* Apache JServ host structure */
struct jserv_host {
    char *name;                 /* The name of this host */
    char *id;                   /* The id for this host (appended to cookie for routing) */
    jserv_config *config;       /* In which server it was added */
    jserv_protocol *protocol;   /* The protocol for this (NULL=default) */
    char *host;                 /* The host for this (NULL=default) */
    unsigned long hostaddr;     /* The host for this (32bit ip address) */
    short port;                 /* The port for this (0=default) */
    char *secretfile;           /* Our secret file name */
    char *secret;               /* Our secret value */
    long secretsize;            /* Our secret value lenght */
    jserv_host *next;           /* Next host (NULL=end-of-list or circular in a mount) */
};

/*****************************************************************************
 * Description: mmaped file description                                      *
 *                                                                           *
 *****************************************************************************/

#define NB_MAX_JSERVS  256

#define DOWN '-'
#define SHUTDOWN_IMMEDIATE 'X'
#define SHUTDOWN_GRACEFUL  '/'
#define UP '+'


/*****************************************************************************
 * File structure                                                            *
 * we completely encapsulate the shared memory attributes & offer methods    *
 * The C langage is too permissive to allow salvage casts on this file       *
 * So, I decided to make all these struct casts in jserv_mmap.c and let      *
 * outside just  see an generic "ShmHost" reference.                         *
 * quoting SM: "a language that doesn't affect the way you program ... "     *
 *****************************************************************************/

struct ShmHost {
    char name[64];
    char state;
    unsigned long ip;
    unsigned short port;
    unsigned int opaque;
};

typedef struct ShmHost ShmHost;

int mmapjservfile(jserv_config *cfg, char * filename);
void munmapjservfile();

pid_t jserv_getwatchdogpid();
void jserv_setwatchdogpid(pid_t pid);

ShmHost *jserv_get1st_host(ShmHost *host);
ShmHost *jserv_getnext_host(ShmHost *host);

void jserv_changeexistingstate(char *id, char *fromstates, char tostate);
void jserv_changestate(jserv_config *cfg, jserv_host *cur, char *fromstates, char tostate);
void jserv_setalive(jserv_config *cfg, jserv_host *cur);
void jserv_setdead(jserv_config *cfg, jserv_host *cur);
int jserv_isup(jserv_config *cfg, jserv_host *cur);
int jserv_isdead(jserv_config *cfg, jserv_host *cur);
char jserv_getstate(jserv_config *cfg, jserv_host *cur);
ShmHost * jserv_get1st_host(ShmHost *host);
ShmHost * jserv_getnext_host(ShmHost *host);
void jserv_dbgshm(jserv_config *cfg);

int watchdog_cleanup (jserv_config *cfg);
int watchdog_init (jserv_config *cfg);
#endif

/* ========================================================================= */
/* Apache JServ protocol structure */
struct jserv_protocol {
    const char *name;           /* Name of the protocol */
    short port;                 /* Default port for this protocol */
    int (*init)                 /* Initializes this protocol */
        (jserv_config *cfg);
    int (*cleanup)              /* Cleans up this protocol */
        (jserv_config *cfg);
    int (*child_init)           /* Initializes protocol for Apache child */
        (jserv_config *cfg);
    int (*child_cleanup)        /* Cleans-up protocol when Apache child dies */
        (jserv_config *cfg);
    int (*handler)              /* Handles a request thru this protocol */
        (jserv_config *cfg, jserv_request *req, request_rec *r);
    int (*function)             /* Handles a function request */
        (jserv_config *cfg, int function, char *data);
    const char *(*parameter)    /* Notifies a parameter */
        (jserv_config *cfg, char *name, char *value);
};

/* ========================================================================= */
/* Apache JServ request structure */
struct jserv_request {
    int isdir;                  /* A directory is in current request */
    jserv_mount *mount;         /* The mnt structure matching uri */
    char *zone;                 /* The zone if MNT does not specify it */
    char *servlet;              /* The servlet matched in uri */
};


/*****************************************************************************
 * mod_jserv.c - strictly Apache dependant things                            *
 *****************************************************************************/

/* Our module */
extern module MODULE_VAR_EXPORT jserv_module;
/* Our configurations list */
extern jserv_config *jserv_servers;
/* Our memory pool */
extern pool *jserv_pool;

/*****************************************************************************
 * jserv_utils.c - utilities for Apache JServ                                       *
 *****************************************************************************/

/* Get a Apache JServ server configuration from a server structure */
jserv_config *jserv_server_config_get(server_rec *s);
/* Get a Apache JServ request from an apache request */
jserv_request *jserv_request_config_get(request_rec *r);
/* Resolve host adresses */
unsigned long jserv_resolve(char *value);
/* Read file contents and and length */
const char *jserv_readfile(pool *p, char *name, int relative, char **buffer,
                           long *size);
/* Opens a file descriptor */
const char *jserv_openfile(pool *p, char *name, int relative, int *descriptor,
                           int flags, int mode);
/* Logs an error to Apache JServ log file, apache log file or stderr */
void jserv_error(const char *file, int line, int level, jserv_config *cfg,
                 const char *fmt, ...);
/* Logs an error to Apache JServ log file, apache log file or stderr */
void jserv_error_exit(const char *file, int line, int level, jserv_config *cfg,
                 const char *fmt, ...);
/* Log something to Apache JServ log file thru argument list */
void jserv_error_var(const char *file, int line, int level, jserv_config *cfg,
                 const char *fmt, va_list ap);
/* Returns a string with current date/time (up to milliseconds) */
char *jserv_time(pool *p);

/*****************************************************************************
 * jserv_protocol.c - protocol handling related things                       *
 *****************************************************************************/

/* Our protocols list (NULL terminated) */
extern jserv_protocol *jserv_protocols[];

/* Returns a protocol matching name */
jserv_protocol *jserv_protocol_getbyname(const char *name);
/* Initialize all protocols (if child is TRUE calls child_init()) */
int jserv_protocol_initall (jserv_config *cfg, int child);
/* Cleans up all protocols (if child is TRUE calls child_cleanup()) */
int jserv_protocol_cleanupall (jserv_config *cfg, int child);
/* Initialize a protocol */
int jserv_protocol_init (jserv_protocol *proto, jserv_config *cfg);
/* Cleans up a protocol */
int jserv_protocol_cleanup (jserv_protocol *proto, jserv_config *cfg);
/* Initialize a protocol for an Apache child starting */
int jserv_protocol_child_init (jserv_protocol *proto, jserv_config *cfg);
/* Cleans up a protocol for an Apache child dying */
int jserv_protocol_child_cleanup (jserv_protocol *proto, jserv_config *cfg);
/* Process a request thru the specified protocol */
int jserv_protocol_handler (jserv_protocol *proto, jserv_config *cfg,
                            jserv_request *req, request_rec *r);
/* Process a function request thru the specified protocol */
int jserv_protocol_function (jserv_protocol *proto, jserv_config *cfg,
                             int function, char *data);
/* Notifies a parameter (ApJServProtocolParameter) to the specified protocol */
const char *jserv_protocol_parameter (jserv_protocol *proto, jserv_config *cfg,
                                      char *name, char *value);

/*****************************************************************************
 * jserv_????.c -protocols implementations                                   *
 *****************************************************************************/

/* ========================================================================= */
/* The protocols */
extern jserv_protocol jserv_status;
extern jserv_protocol jserv_ajpv11;
extern jserv_protocol jserv_ajpv12;
#ifdef LOAD_BALANCE
extern jserv_protocol jserv_balancep;
#endif
extern jserv_protocol jserv_wrapper;

#endif /*__JSERV_H__*/
