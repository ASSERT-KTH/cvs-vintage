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

/***************************************************************************
 * Description: Utility functions (mainly configuration)                   *
 * Author:      Gal Shachor <shachor@il.ibm.com>                           *
 * Version:     $Revision: 1.1 $                                               *
 ***************************************************************************/


#include "jk_util.h"
#include "jk_pool.h"
#include "jk_ajp12_worker.h"

#define PREFIX_OF_WORKER            ("worker")
#define HOST_OF_WORKER              ("host")
#define PORT_OF_WORKER              ("port")
#define TYPE_OF_WORKER              ("type")
#define WORKER_AJP12                ("ajp12")
#define DEFAULT_WORKER_TYPE         JK_AJP12_WORKER_NAME

#define DEFAULT_WORKER              JK_AJP12_WORKER_NAME
#define WORKER_LIST_PROPERTY_NAME   ("worker.list")

#define HUGE_BUFFER_SIZE (8*1024)
#define LOG_LINE_SIZE    (1024)

struct file_logger {
    FILE *logfile;
};
typedef struct file_logger file_logger_t;

static int JK_METHOD log_to_file(jk_logger_t *l,                                 
                                 int level,
                                 const char *what)
{
    if(l && l->level <= level && l->logger_private && what) {       
        unsigned sz = strlen(what);
        if(sz) {
            file_logger_t *p = l->logger_private;
            fwrite(what, 1, sz, p->logfile);
        }

        return JK_TRUE;
    }

    return JK_FALSE;
}

int jk_parse_log_level(const char *level)
{
    if(0 == stricmp(level, JK_LOG_INFO_VERB)) {
        return JK_LOG_INFO_LEVEL;
    }

    if(0 == stricmp(level, JK_LOG_ERROR_VERB)) {
        return JK_LOG_ERROR_LEVEL;
    }

    if(0 == stricmp(level, JK_LOG_EMERG_VERB)) {
        return JK_LOG_EMERG_LEVEL;
    }

    return JK_LOG_DEBUG_LEVEL;
}

int jk_open_file_logger(jk_logger_t **l,
                        const char *file,
                        int level)
{
    if(l && file) {     
        jk_logger_t *rc = (jk_logger_t *)malloc(sizeof(jk_logger_t));
        file_logger_t *p = (file_logger_t *)malloc(sizeof(file_logger_t));
        if(rc && p) {
            rc->log = log_to_file;
            rc->level = level;
            rc->logger_private = p;
            p->logfile = fopen(file, "a+");
            if(p->logfile) {
                *l = rc;
                return JK_TRUE;
            }           
        }
        if(rc) {
            free(rc);
        }
        if(p) {
            free(p);
        }

        *l = NULL;
    }
    return JK_FALSE;
}

int jk_close_file_logger(jk_logger_t **l)
{
    if(l && *l) {
        file_logger_t *p = (*l)->logger_private;
        fflush(p->logfile);
        fclose(p->logfile);
        free(p);
        free(*l);
        *l = NULL;

        return JK_TRUE;
    }
    return JK_FALSE;
}

int jk_log(jk_logger_t *l,
           const char *file,
           int line,
           int level,
           const char *fmt, ...)
{
    int rc = 0;
    if(!l || !file || !fmt) {
        return -1;
    }

    if(l->level <= level) {
        char buf[HUGE_BUFFER_SIZE];
        char *f = (char *)(file + strlen(file) - 1);
        va_list args;

        while(f != file && '\\' != *f && '/' != *f) {
            f--;
        }
        if(f != file) {
            f++;
        }
        sprintf(buf, "[%s (%d)]: ", f, line);
    
        va_start(args, fmt);

        rc = vsprintf(buf + strlen(buf), fmt, args);
        va_end(args);
        l->log(l, level, buf);
    }
    
    return rc;
}

char *jk_get_worker_type(jk_map_t *m,
                         const char *wname)
{
    char buf[1024];

    if(!m || !wname) {
        return NULL;
    }

    sprintf(buf, "%s.%s.%s", PREFIX_OF_WORKER, wname, TYPE_OF_WORKER);

    return map_get_string(m, buf, DEFAULT_WORKER_TYPE);
}

char *jk_get_worker_host(jk_map_t *m,
                         const char *wname,
                         const char *def)
{
    char buf[1024];

    if(!m || !wname) {
        return NULL;
    }

    sprintf(buf, "%s.%s.%s", PREFIX_OF_WORKER, wname, HOST_OF_WORKER);

    return map_get_string(m, buf, def);
}

int jk_get_worker_port(jk_map_t *m,
                       const char *wname,
                       int def)
{
    char buf[1024];

    if(!m || !wname) {
        return -1;
    }

    sprintf(buf, "%s.%s.%s", PREFIX_OF_WORKER, wname, PORT_OF_WORKER);

    return map_get_int(m, buf, def);
}

int jk_get_worker_list(jk_map_t *m,
                       char ***list,
                       unsigned *num_of_wokers)
{
    if(m && list && num_of_wokers) {
        char **ar = map_get_string_list(m, 
                                        WORKER_LIST_PROPERTY_NAME, 
                                        num_of_wokers, 
                                        DEFAULT_WORKER);
        if(ar)  {
            *list = ar;     
            return JK_TRUE;
        }
        *list = NULL;   
        *num_of_wokers = 0;
    }

    return JK_FALSE;
}