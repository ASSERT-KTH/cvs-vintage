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
 * Author:      Henri Gomez <hgomez@slib.fr>                               *
 * Author:      Gal Shachor <shachor@il.ibm.com>                           *
 * Version:     $Revision: 1.8 $                                           *
 ***************************************************************************/


#include "jk_util.h"
#include "jk_ajp12_worker.h"

#define SYSPROPS_OF_WORKER          ("sysprops")
#define STDERR_OF_WORKER            ("stderr")
#define STDOUT_OF_WORKER            ("stdout")
#define MX_OF_WORKER                ("mx")
#define MS_OF_WORKER                ("ms")
#define CP_OF_WORKER                ("class_path")
#define JVM_OF_WORKER               ("jvm_lib")
#define LIBPATH_OF_WORKER           ("ld_path")
#define CMD_LINE_OF_WORKER          ("cmd_line")
#define NATIVE_LIB_OF_WORKER        ("native_lib")
#define PREFIX_OF_WORKER            ("worker")
#define HOST_OF_WORKER              ("host")
#define PORT_OF_WORKER              ("port")
#define TYPE_OF_WORKER              ("type")
#define CACHE_OF_WORKER             ("cachesize")
#define LOAD_FACTOR_OF_WORKER       ("lbfactor")
#define BALANCED_WORKERS            ("balanced_workers")
#define WORKER_AJP12                ("ajp12")
#define DEFAULT_WORKER_TYPE         JK_AJP12_WORKER_NAME

#define DEFAULT_WORKER              JK_AJP12_WORKER_NAME
#define WORKER_LIST_PROPERTY_NAME   ("worker.list")
#define DEFAULT_LB_FACTOR           (1.0)
#define LOG_FORMAT		    ("log_format")

#define HUGE_BUFFER_SIZE (8*1024)
#define LOG_LINE_SIZE    (1024)

struct file_logger {
    FILE *logfile;
};
typedef struct file_logger file_logger_t;

/* 
 * define the log format, we're using by default the one from error.log 
 *
 * [Mon Mar 26 19:44:48 2001] [jk_uri_worker_map.c (155)]: Into jk_uri_worker_map_t::uri_worker_map_alloc
 * log format used by apache in error.log
 */
#ifndef JK_TIME_FORMAT 
#define JK_TIME_FORMAT "[%a %b %d %H:%M:%S %Y] "
#endif

char * jk_log_fmt = JK_TIME_FORMAT;

static void set_time_str(char * str, int len)
{
	time_t		t = time(NULL);
    	struct tm 	*tms;

    	tms = localtime(&t);
	strftime(str, len, jk_log_fmt, tms);
}

static int JK_METHOD log_to_file(jk_logger_t *l,                                 
                                 int level,
                                 const char *what)
{
    if(l && l->level <= level && l->logger_private && what) {       
        unsigned sz = strlen(what);
        if(sz) {
            file_logger_t *p = l->logger_private;
            fwrite(what, 1, sz, p->logfile);
	    /* [V] Flush the dam' thing! */
	    fflush(p->logfile);
        }

        return JK_TRUE;
    }

    return JK_FALSE;
}

int jk_parse_log_level(const char *level)
{
    if(0 == strcasecmp(level, JK_LOG_INFO_VERB)) {
        return JK_LOG_INFO_LEVEL;
    }

    if(0 == strcasecmp(level, JK_LOG_ERROR_VERB)) {
        return JK_LOG_ERROR_LEVEL;
    }

    if(0 == strcasecmp(level, JK_LOG_EMERG_VERB)) {
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
#ifdef NETWARE
/* On NetWare, this can get called on a thread that has a limited stack so */
/* we will allocate and free the temporary buffer in this function         */
        char *buf;
#else
        char buf[HUGE_BUFFER_SIZE];
#endif
        char *f = (char *)(file + strlen(file) - 1);
        va_list args;
        int used = 0;

        while(f != file && '\\' != *f && '/' != *f) {
            f--;
        }
        if(f != file) {
            f++;
        }

#ifdef WIN32
	set_time_str(buf, HUGE_BUFFER_SIZE);
	used = strlen(buf);
        used += _snprintf(&buf[used], HUGE_BUFFER_SIZE, " [%s (%d)]: ", f, line);        
#elif defined(NETWARE) // until we get a snprintf function
        buf = (char *) malloc(HUGE_BUFFER_SIZE);
        if (NULL == buf)
           return -1;

	set_time_str(buf, HUGE_BUFFER_SIZE);
	used = strlen(buf);
        used += sprintf(&buf[used], " [%s (%d)]: ", f, line);
#else 
	set_time_str(buf, HUGE_BUFFER_SIZE);
	used = strlen(buf);
        used += snprintf(&buf[used], HUGE_BUFFER_SIZE, " [%s (%d)]: ", f, line);        
#endif
        if(used < 0) {
            return 0; /* [V] not sure what to return... */
        }
    
        va_start(args, fmt);
#ifdef WIN32
        rc = _vsnprintf(buf + used, HUGE_BUFFER_SIZE - used, fmt, args);
#elif defined(NETWARE) // until we get a vsnprintf function
        rc = vsprintf(buf + used, fmt, args);
#else 
        rc = vsnprintf(buf + used, HUGE_BUFFER_SIZE - used, fmt, args);
#endif
        va_end(args);
        l->log(l, level, buf);
#ifdef NETWARE
        free(buf);
#endif
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

/* [V] I suggest that the following general purpose functions be used.       */
/*     More should be added (double etc.), but now these were enough for me. */
/*     Functions that can be simulated with these should be "deprecated".    */

int jk_get_worker_str_prop(jk_map_t *m,
			   const char *wname,
			   const char *pname,
			   char **prop)
{
    char buf[1024];

    if(m && prop && wname && pname) {
        sprintf(buf, "%s.%s.%s", PREFIX_OF_WORKER, wname, pname);
        *prop = map_get_string(m, buf, NULL);
        if(*prop) {
            return JK_TRUE;
        }
    }
    return JK_FALSE;
}

int jk_get_worker_int_prop(jk_map_t *m,
			   const char *wname,
			   const char *pname,
			   int *prop)
{
    char buf[1024];

    if(m && prop && wname && pname) {
        int i;
        sprintf(buf, "%s.%s.%s", PREFIX_OF_WORKER, wname, pname);
        i = map_get_int(m, buf, -1);
        if(-1 != i) {
            *prop = i;
            return JK_TRUE;
        }
    }
    return JK_FALSE;
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

int jk_get_worker_cache_size(jk_map_t *m, 
                             const char *wname,
                             int def)
{
    char buf[1024];

    if(!m || !wname) {
        return -1;
    }

    sprintf(buf, "%s.%s.%s", PREFIX_OF_WORKER, wname, CACHE_OF_WORKER);

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

void jk_set_log_format(char * logformat)
{
	jk_log_fmt = (logformat) ? logformat : JK_TIME_FORMAT;
}

double jk_get_lb_factor(jk_map_t *m, 
                        const char *wname)
{
    char buf[1024];
    
    if(!m || !wname) {
        return DEFAULT_LB_FACTOR;
    }

    sprintf(buf, "%s.%s.%s", PREFIX_OF_WORKER, wname, LOAD_FACTOR_OF_WORKER);

    return map_get_double(m, buf, DEFAULT_LB_FACTOR);
}

int jk_get_lb_worker_list(jk_map_t *m, 
                          const char *lb_wname,
                          char ***list, 
                          unsigned *num_of_wokers)
{
    char buf[1024];

    if(m && list && num_of_wokers && lb_wname) {
        char **ar = NULL;

        sprintf(buf, "%s.%s.%s", PREFIX_OF_WORKER, lb_wname, BALANCED_WORKERS);
        ar = map_get_string_list(m, 
                                 buf, 
                                 num_of_wokers, 
                                 NULL);
        if(ar)  {
            *list = ar;     
            return JK_TRUE;
        }
        *list = NULL;   
        *num_of_wokers = 0;
    }

    return JK_FALSE;
}

int jk_get_worker_mx(jk_map_t *m, 
                     const char *wname,
                     unsigned *mx)
{
    char buf[1024];
    
    if(m && mx && wname) {
        int i;
        sprintf(buf, "%s.%s.%s", PREFIX_OF_WORKER, wname, MX_OF_WORKER);

        i = map_get_int(m, buf, -1);
        if(-1 != i) {
            *mx = (unsigned)i;
            return JK_TRUE;
        }
    }

    return JK_FALSE;
}

int jk_get_worker_ms(jk_map_t *m, 
                     const char *wname,
                     unsigned *ms)
{
    char buf[1024];
    
    if(m && ms && wname) {
        int i;
        sprintf(buf, "%s.%s.%s", PREFIX_OF_WORKER, wname, MS_OF_WORKER);

        i = map_get_int(m, buf, -1);
        if(-1 != i) {
            *ms = (unsigned)i;
            return JK_TRUE;
        }
    }

    return JK_FALSE;
}

int jk_get_worker_classpath(jk_map_t *m, 
                            const char *wname, 
                            char **cp)
{
    char buf[1024];
    
    if(m && cp && wname) {
        sprintf(buf, "%s.%s.%s", PREFIX_OF_WORKER, wname, CP_OF_WORKER);

        *cp = map_get_string(m, buf, NULL);
        if(*cp) {
            return JK_TRUE;
        }
    }

    return JK_FALSE;
}

int jk_get_worker_jvm_path(jk_map_t *m, 
                           const char *wname, 
                           char **vm_path)
{
    char buf[1024];
    
    if(m && vm_path && wname) {
        sprintf(buf, "%s.%s.%s", PREFIX_OF_WORKER, wname, JVM_OF_WORKER);

        *vm_path = map_get_string(m, buf, NULL);
        if(*vm_path) {
            return JK_TRUE;
        }
    }

    return JK_FALSE;
}

/* [V] This is unused. currently. */
int jk_get_worker_callback_dll(jk_map_t *m, 
                               const char *wname, 
                               char **cb_path)
{
    char buf[1024];

    if(m && cb_path && wname) {
        sprintf(buf, "%s.%s.%s", PREFIX_OF_WORKER, wname, NATIVE_LIB_OF_WORKER);

        *cb_path = map_get_string(m, buf, NULL);
        if(*cb_path) {
            return JK_TRUE;
        }
    }

    return JK_FALSE;
}

int jk_get_worker_cmd_line(jk_map_t *m, 
                           const char *wname, 
                           char **cmd_line)
{
    char buf[1024];

    if(m && cmd_line && wname) {
        sprintf(buf, "%s.%s.%s", PREFIX_OF_WORKER, wname, CMD_LINE_OF_WORKER);

        *cmd_line = map_get_string(m, buf, NULL);
        if(*cmd_line) {
            return JK_TRUE;
        }
    }

    return JK_FALSE;
}


int jk_file_exists(const char *f)
{
    if(f) {
        struct stat st;
        if((0 == stat(f, &st)) && (st.st_mode & S_IFREG)) {
            return JK_TRUE;
        }
    }
    return JK_FALSE;
}

int jk_is_path_poperty(const char *prp_name)
{
    char *path_suffix = "path";
    if(prp_name && (strlen(prp_name) >= strlen(path_suffix))) {
        const char *suffix = prp_name + strlen(prp_name) - strlen(path_suffix);
        if(0 == strcmp(suffix, path_suffix)) {
            return JK_TRUE;
        }
    }

    return JK_FALSE;
}

int jk_is_cmd_line_poperty(const char *prp_name)
{
    if(prp_name && (strlen(prp_name) >= strlen(CMD_LINE_OF_WORKER))) {
        const char *suffix = prp_name + strlen(prp_name) - strlen(CMD_LINE_OF_WORKER);
        if(0 == strcmp(suffix, CMD_LINE_OF_WORKER)) {
            return JK_TRUE;
        }
    }

    return JK_FALSE;
}

int jk_get_worker_stdout(jk_map_t *m, 
                         const char *wname, 
                         char **stdout_name)

{
    char buf[1024];

    if(m && stdout_name && wname) {
        sprintf(buf, "%s.%s.%s", PREFIX_OF_WORKER, wname, STDOUT_OF_WORKER);

        *stdout_name = map_get_string(m, buf, NULL);
        if(*stdout_name) {
            return JK_TRUE;
        }
    }

    return JK_FALSE;
}

int jk_get_worker_stderr(jk_map_t *m, 
                         const char *wname, 
                         char **stderr_name)

{
    char buf[1024];

    if(m && stderr_name && wname) {
        sprintf(buf, "%s.%s.%s", PREFIX_OF_WORKER, wname, STDERR_OF_WORKER);

        *stderr_name = map_get_string(m, buf, NULL);
        if(*stderr_name) {
            return JK_TRUE;
        }
    }

    return JK_FALSE;
}

int jk_get_worker_sysprops(jk_map_t *m, 
                           const char *wname, 
                           char **sysprops)

{
    char buf[1024];

    if(m && sysprops && wname) {
        sprintf(buf, "%s.%s.%s", PREFIX_OF_WORKER, wname, SYSPROPS_OF_WORKER);

        *sysprops = map_get_string(m, buf, NULL);
        if(*sysprops) {
            return JK_TRUE;
        }
    }

    return JK_FALSE;
}

int jk_get_worker_libpath(jk_map_t *m, 
                          const char *wname, 
                          char **libpath)
{
    char buf[1024];

    if(m && libpath && wname) {
        sprintf(buf, "%s.%s.%s", PREFIX_OF_WORKER, wname, LIBPATH_OF_WORKER);

        *libpath = map_get_string(m, buf, NULL);
        if(*libpath) {
            return JK_TRUE;
        }
    }

    return JK_FALSE;
}

char **jk_parse_sysprops(jk_pool_t *p, 
                         const char *sysprops)
{
    char **rc = NULL;

    if(p && sysprops) {
        char *prps = jk_pool_strdup(p, sysprops);
        if(prps && strlen(prps)) {
            unsigned num_of_prps;

            for(num_of_prps = 1; *sysprops ; sysprops++) {
                if('*' == *sysprops) {
                    num_of_prps++;
                }
            }            

            rc = jk_pool_alloc(p, (num_of_prps + 1) * sizeof(char *));
            if(rc) {
                unsigned i = 0;
                char *tmp = strtok(prps, "*");

                while(tmp && i < num_of_prps) {
                    rc[i] = tmp;
                    tmp = strtok(NULL, "*");
                    i++;
                }
                rc[i] = NULL;
            }
        }
    }

    return rc;
}

void jk_append_libpath(jk_pool_t *p, 
                       const char *libpath)
{
    char *env = NULL;
    char *current = getenv(PATH_ENV_VARIABLE);

    if(current) {
        env = jk_pool_alloc(p, strlen(PATH_ENV_VARIABLE) + 
                               strlen(current) + 
                               strlen(libpath) + 5);
        if(env) {
            sprintf(env, "%s=%s%c%s", 
                    PATH_ENV_VARIABLE, 
                    libpath, 
                    PATH_SEPERATOR, 
                    current);
        }
    } else {
        env = jk_pool_alloc(p, strlen(PATH_ENV_VARIABLE) +                               
                               strlen(libpath) + 5);
        if(env) {
            sprintf(env, "%s=%s", PATH_ENV_VARIABLE, libpath);
        }
    }

    if(env) {
        putenv(env);
    }
}

void jk_init_ws_service(jk_ws_service_t *s)
{
    s->ws_private           = NULL;
    s->pool                 = NULL;
    s->method               = NULL;
    s->protocol             = NULL;
    s->req_uri              = NULL;
    s->remote_addr          = NULL;
    s->remote_host          = NULL;
    s->remote_user          = NULL;
    s->auth_type            = NULL;
    s->query_string         = NULL;
    s->server_name          = NULL;
    s->server_port          = 80;
    s->server_software      = NULL;
    s->content_length       = 0;
    s->is_chunked           = 0;
    s->no_more_chunks       = 0;
    s->content_read         = 0;
    s->is_ssl               = JK_FALSE;
    s->ssl_cert             = NULL;
    s->ssl_cert_len         = 0;
    s->ssl_cipher           = NULL;
    s->ssl_session          = NULL;
    s->headers_names        = NULL;
    s->headers_values       = NULL;
    s->num_headers          = 0;
    s->attributes_names     = NULL;
    s->attributes_values    = NULL;
    s->num_attributes       = 0;
    s->jvm_route            = NULL;
}
