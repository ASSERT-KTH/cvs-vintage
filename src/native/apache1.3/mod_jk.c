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
 * Description: Apache 2 plugin for Jakarta/Tomcat                         *
 * Author:      Gal Shachor <shachor@il.ibm.com>                           *
 * Version:     $ $                                                        *
 ***************************************************************************/

/*
 * mod_jk: keeps all servlet/jakarta related ramblings together.
 */

#include "ap_config.h"
#include "httpd.h"
#include "http_config.h"
#include "http_request.h"
#include "http_core.h"
#include "http_protocol.h"
#include "http_main.h"
#include "http_log.h"
#include "util_script.h"
#include "util_date.h"
#include "http_conf_globals.h"

/*
 * Jakarta (jk_) include files
 */
#include "jk_global.h"
#include "jk_util.h"
#include "jk_map.h"
#include "jk_pool.h"
#include "jk_service.h"
#include "jk_worker.h"
#include "jk_uri_worker_map.h"

#define JK_WORKER_ID        ("jakarta.worker")
#define JK_HANDLER          ("jakarta-servlet")
#define JK_MAGIC_TYPE       ("application/x-jakarta-servlet")
#define NULL_FOR_EMPTY(x)   ((x && !strlen(x)) ? NULL : x) 

module MODULE_VAR_EXPORT jk_module;

typedef struct {
    char *log_file;
    int  log_level;
    jk_logger_t *log;

    char *worker_file;
    int  mountcopy;
    jk_map_t *uri_to_context;
    jk_uri_worker_map_t *uw_map;

    server_rec *s;
} jk_server_conf_t;

struct apache_private_data {
    jk_pool_t p;
    
    int response_started;
    int read_body_started;
    request_rec *r;
};
typedef struct apache_private_data apache_private_data_t;

static jk_logger_t *main_log = NULL;

static int JK_METHOD ws_start_response(jk_ws_service_t *s,
                                       int status,
                                       const char *reason,
                                       const char * const *header_names,
                                       const char * const *header_values,
                                       unsigned num_of_headers);

static int JK_METHOD ws_read(jk_ws_service_t *s,
                             void *b,
                             unsigned l,
                             unsigned *a);

static int JK_METHOD ws_write(jk_ws_service_t *s,
                              const void *b,
                              unsigned l);


/* ========================================================================= */
/* JK Service step callbacks                                                 */
/* ========================================================================= */

static int JK_METHOD ws_start_response(jk_ws_service_t *s,
                                       int status,
                                       const char *reason,
                                       const char * const *header_names,
                                       const char * const *header_values,
                                       unsigned num_of_headers)
{
    if(s && s->ws_private) {
        unsigned h;
        apache_private_data_t *p = s->ws_private;
        request_rec *r = p->r;
        
        if(!reason) {
            reason = "";
        }
	    r->status = status;
	    r->status_line = ap_psprintf(r->pool, "%d %s", status, reason);

        for(h = 0 ; h < num_of_headers ; h++) {
            if(!strcasecmp(header_names[h], "Content-type")) {
                char *tmp = ap_pstrdup(r->pool, header_values[h]);
                ap_content_type_tolower(tmp);
                r->content_type = tmp;
            } else if(!strcasecmp(header_names[h], "Location")) {
	            ap_table_set(r->headers_out, header_names[h], header_values[h]);
	        } else if(!strcasecmp(header_names[h], "Content-Length")) {
	            ap_table_set(r->headers_out, header_names[h], header_values[h]);
	        } else if(!strcasecmp(header_names[h], "Transfer-Encoding")) {
	            ap_table_set(r->headers_out, header_names[h], header_values[h]);
            } else if(!strcasecmp(header_names[h], "Last-Modified")) {
	            /*
	             * If the script gave us a Last-Modified header, we can't just
	             * pass it on blindly because of restrictions on future values.
	             */
	            ap_update_mtime(r, ap_parseHTTPdate(header_values[h]));
	            ap_set_last_modified(r);
	        } else {	            
	            ap_table_add(r->headers_out, header_names[h], header_values[h]);
            }
        }

        ap_send_http_header(r);
        p->response_started = JK_TRUE;
        
        return JK_TRUE;
    }
    return JK_FALSE;
}

static int JK_METHOD ws_read(jk_ws_service_t *s,
                             void *b,
                             unsigned l,
                             unsigned *a)
{
    if(s && s->ws_private && b && a) {
        apache_private_data_t *p = s->ws_private;
        if(!p->read_body_started) {
            if(!ap_setup_client_block(p->r, REQUEST_CHUNKED_DECHUNK)) {
                if(ap_should_client_block(p->r)) { 
                    p->read_body_started = JK_TRUE; 
                }
            }
        }

        if(p->read_body_started) {
            *a = ap_get_client_block(p->r, b, l);
            return JK_TRUE;
        }
    }
    return JK_FALSE;
}

static int JK_METHOD ws_write(jk_ws_service_t *s,
                              const void *b,
                              unsigned l)
{
    if(s && s->ws_private && b) {
        apache_private_data_t *p = s->ws_private;

        if(l) {
            BUFF *bf = p->r->connection->client;
            char *buf = (char *)b;
            int w = (int)l;
            int r = 0;

            if(!p->response_started) {
                if(!s->start_response(s, 200, NULL, NULL, NULL, 0)) {
                    return JK_FALSE;
                }
            }

            while(l && !p->r->connection->aborted) {
                w = ap_bwrite(p->r->connection->client, &buf[r], l);
                if (w > 0) {
                    ap_reset_timeout(p->r); /* reset timeout after successful write */
                    r += w;
                    l -= w;
                } else if (w < 0) {
                    if(!p->r->connection->aborted) {
                        ap_bsetflag(p->r->connection->client, B_EOUT, 1);
                        p->r->connection->aborted = 1;
                    }
                    return JK_FALSE;
                }
                
            }
           
            /*
             * To allow server push.
             */
            ap_bflush(bf);
        }

        return JK_TRUE;
    }
    return JK_FALSE;
}

/* ========================================================================= */
/* Utility functions                                                         */
/* ========================================================================= */

/* ========================================================================= */
/* Log something to JServ log file then exit */
static void jk_error_exit(const char *file, 
                          int line, 
                          int level, 
                          const server_rec *s,
                          ap_pool *p,
                          const char *fmt, ...) 
{
    va_list ap;
    char *res;

    va_start(ap, fmt);
    res = ap_pvsprintf(p, fmt, ap);
    va_end(ap);

    ap_log_error(file, line, level, s, res);

    /* Exit process */
    exit(1);
}

static int get_content_length(request_rec *r)
{
    if(r->clength > 0) {
        return r->clength;
    } else {
        char *lenp = (char *)ap_table_get(r->headers_in, "Content-Length");

        if(lenp) {
            int rc = atoi(lenp);
            if(rc > 0) {
                return rc;
            }
        }
    }

    return 0;
}

static int init_ws_service(apache_private_data_t *private_data,
                           jk_ws_service_t *s)
{
    request_rec *r      = private_data->r;
    s->jvm_route        = NULL;
    s->start_response   = ws_start_response;
    s->read             = ws_read;
    s->write            = ws_write;

    s->auth_type    = NULL_FOR_EMPTY(r->connection->ap_auth_type);
    s->remote_user  = NULL_FOR_EMPTY(r->connection->user);

    s->protocol     = r->protocol;
    s->remote_host  = (char *)ap_get_remote_host(r->connection,
                                                 r->per_dir_config,
										         REMOTE_HOST);

    s->remote_host  = NULL_FOR_EMPTY(s->remote_host);

    s->remote_addr  = NULL_FOR_EMPTY(r->connection->remote_ip);
    s->server_name  = (r->hostname ? r->server->server_hostname : r->hostname);
    s->server_port  = r->server->port;
    s->server_software = (char *)ap_get_server_version();

    s->method       = (char *)r->method;
    s->content_length = get_content_length(r);
    s->query_string = r->args;
    s->req_uri      = r->uri;
    
    s->is_ssl       = JK_FALSE;
    s->ssl_cert     = NULL;
    s->ssl_cert_len = 0;
    s->ssl_cipher   = NULL;
    s->ssl_session  = NULL;

    s->headers_names    = NULL;
    s->headers_values   = NULL;
    s->num_headers      = 0;
    if(r->headers_in && ap_table_elts(r->headers_in)) {
        array_header *t = ap_table_elts(r->headers_in);        
        if(t && t->nelts) {
            int i;
            table_entry *elts = (table_entry *)t->elts;
            s->num_headers = t->nelts;
            s->headers_names  = ap_palloc(r->pool, sizeof(char *) * t->nelts);
            s->headers_values = ap_palloc(r->pool, sizeof(char *) * t->nelts);
            for(i = 0 ; i < t->nelts ; i++) {
                char *hname = ap_pstrdup(r->pool, elts[i].key);
                s->headers_values[i] = ap_pstrdup(r->pool, elts[i].val);
                s->headers_names[i] = hname;
                while(*hname) {
                    *hname = tolower(*hname);
                    hname++;
                }
            }
        }
    }

    return JK_TRUE;
}

/* ========================================================================= */
/* The JK module command processors                                          */
/* ========================================================================= */

static const char *jk_set_mountcopy(cmd_parms *cmd, 
                                    void *dummy, 
                                    int flag) 
{
    server_rec *s = cmd->server;
    jk_server_conf_t *conf =
        (jk_server_conf_t *)ap_get_module_config(s->module_config, &jk_module);
    
    /* Set up our value */
    conf->mountcopy = flag ? JK_TRUE : JK_FALSE;

    return NULL;
}

static const char *jk_mount_context(cmd_parms *cmd, 
                                    void *dummy, 
                                    char *context,
                                    char *worker,
                                    char *maybe_cookie)
{
    server_rec *s = cmd->server;
    jk_server_conf_t *conf =
        (jk_server_conf_t *)ap_get_module_config(s->module_config, &jk_module);

    /*
     * Add the new worker to the alias map.
     */
    char *old;
    map_put(conf->uri_to_context, context, worker, &old);
    return NULL;
}

static const char *jk_set_wroker_file(cmd_parms *cmd, 
                                      void *dummy, 
                                      char *worker_file)
{
    server_rec *s = cmd->server;
    jk_server_conf_t *conf =
        (jk_server_conf_t *)ap_get_module_config(s->module_config, &jk_module);

    conf->worker_file = worker_file;

    return NULL;
}

static const char *jk_set_log_file(cmd_parms *cmd, 
                                   void *dummy, 
                                   char *log_file)
{
    server_rec *s = cmd->server;
    jk_server_conf_t *conf =
        (jk_server_conf_t *)ap_get_module_config(s->module_config, &jk_module);

    conf->log_file = log_file;

    return NULL;
}

static const char *jk_set_log_level(cmd_parms *cmd, 
                                    void *dummy, 
                                    char *log_level)
{
    server_rec *s = cmd->server;
    jk_server_conf_t *conf =
        (jk_server_conf_t *)ap_get_module_config(s->module_config, &jk_module);

    conf->log_level = jk_parse_log_level(log_level);

    return NULL;
}

static const command_rec jk_cmds[] =
{
    {"JkWorkersFile", jk_set_wroker_file, NULL, RSRC_CONF, TAKE1,
     "the name of a worker file for the Jakarta servlet containers"},
    {"JkMount", jk_mount_context, NULL, RSRC_CONF, TAKE23,
     "A mount point from a context to a Tomcat worker"},
    {"JkMountCopy", jk_set_mountcopy, NULL, RSRC_CONF, FLAG,
     "Should the base server mounts be copied to the virtual server"},
    {"JkLogFile", jk_set_log_file, NULL, RSRC_CONF, TAKE1,
     "Full path to the Jakarta Tomcat module log file"},
    {"JkLogLevel", jk_set_log_level, NULL, RSRC_CONF, TAKE1,
     "The Jakarta Tomcat module log level, can be debug, info, error or emerg"},
    {NULL}
};

/* ========================================================================= */
/* The JK module handlers                                                    */
/* ========================================================================= */
static int jk_handler(request_rec *r)
{   
    const char *worker_name = ap_table_get(r->notes, JK_WORKER_ID);

    /* If this is a proxy request, we'll notify an error */
    if(r->proxyreq) {
        return HTTP_INTERNAL_SERVER_ERROR;
    }
      
    if(worker_name) {
        jk_server_conf_t *conf =
            (jk_server_conf_t *)ap_get_module_config(r->server->module_config, &jk_module);
        jk_logger_t *l = conf->log ? conf->log : main_log;

        jk_worker_t *worker = wc_get_worker_for_name(worker_name, l);

        if(worker) {
            int rc = JK_FALSE;
            apache_private_data_t private_data;
            jk_ws_service_t s;
            jk_pool_atom_t buf[SMALL_POOL_SIZE];
            jk_open_pool(&private_data.p, buf, sizeof(buf));

            private_data.response_started = JK_FALSE;
            private_data.read_body_started = JK_FALSE;
            private_data.r = r;

            s.ws_private = &private_data;
            s.pool = &private_data.p;            
            
            if(init_ws_service(&private_data, &s)) {
                jk_endpoint_t *end = NULL;
                if(worker->get_endpoint(worker, &end, l)) {
                    int is_recoverable_error = JK_FALSE;
                    rc = end->service(end, 
                                      &s, 
                                      l, 
                                      &is_recoverable_error);
                
                    end->done(&end, l);
                }
            }

            if(rc) {
                return OK;	/* NOT r->status, even if it has changed. */
            }
        }
    }

    return HTTP_INTERNAL_SERVER_ERROR;
}

static void *create_jk_config(ap_pool *p, server_rec *s)
{
    jk_server_conf_t *c =
        (jk_server_conf_t *) ap_pcalloc(p, sizeof(jk_server_conf_t));

    c->worker_file = NULL;
    c->log_file    = NULL;
    c->log_level   = -1;
    c->log         = NULL;
    c->mountcopy   = JK_FALSE;

    if(!map_alloc(&(c->uri_to_context))) {
        jk_error_exit(APLOG_MARK, APLOG_EMERG, s, p, "Memory error");
    }
    c->uw_map = NULL;
    c->s = s;

    return c;
}


static void *merge_jk_config(ap_pool *p, 
                             void *basev, 
                             void *overridesv)
{
    jk_server_conf_t *base = (jk_server_conf_t *) basev;
    jk_server_conf_t *overrides = (jk_server_conf_t *)overridesv;
 
    if(overrides->mountcopy) {
        int sz = map_size(base->uri_to_context);
        int i;
        for(i = 0 ; i < sz ; i++) {
            void *old;
            char *name = map_name_at(base->uri_to_context, i);
            if(NULL == map_get(overrides->uri_to_context, name, NULL)) {
                if(!map_put(overrides->uri_to_context, 
                            name,
                            ap_pstrdup(p, map_get_string(base->uri_to_context, name, NULL)),
                            &old)) {
                    jk_error_exit(APLOG_MARK, APLOG_EMERG, overrides->s, p, "Memory error");
                }
            }
        }
    }
    if(overrides->log_file && overrides->log_level >= 0) {
        if(!jk_open_file_logger(&(overrides->log), overrides->log_file, overrides->log_level)) {
            overrides->log = NULL;
        }
    }
    if(!uri_worker_map_alloc(&(overrides->uw_map), 
                             overrides->uri_to_context, 
                             overrides->log)) {
        jk_error_exit(APLOG_MARK, APLOG_EMERG, overrides->s,  p, "Memory error");
    }
    
    return overrides;
}

static void jk_init(server_rec *s, ap_pool *p)
{
    char *env = getenv("WAS_BORN_BY_APACHE");
    jk_map_t *init_map = NULL;
    jk_server_conf_t *conf =
        (jk_server_conf_t *)ap_get_module_config(s->module_config, &jk_module);

    fprintf(stdout, "jk_post_config %s\n", env ? env : "NULL"); fflush(stdout);
        
    if(conf->log_file && conf->log_level >= 0) {
        if(!jk_open_file_logger(&(conf->log), conf->log_file, conf->log_level)) {
            conf->log = NULL;
        } else {
            main_log = conf->log;
        }
    }
    
    if(!uri_worker_map_alloc(&(conf->uw_map), conf->uri_to_context, conf->log)) {
        jk_error_exit(APLOG_MARK, APLOG_EMERG, s, p, "Memory error");
    }

    if(map_alloc(&init_map)) {
        if(map_read_properties(init_map, conf->worker_file)) {
            if(!env) {
                putenv("WAS_BORN_BY_APACHE=true");
                return;
            } else {
                if(wc_open(init_map, conf->log)) {
                    return;
                }            
            }
        }
    }

    jk_error_exit(APLOG_MARK, APLOG_EMERG, s, p, "Error while opening the workers");
}

static int jk_translate(request_rec *r)
{    
    if(!r->proxyreq) {        
        jk_server_conf_t *conf =
            (jk_server_conf_t *)ap_get_module_config(r->server->module_config, &jk_module);

        if(conf) {
            char *worker = map_uri_to_worker(conf->uw_map, 
                                             r->uri, 
                                             conf->log ? conf->log : main_log);

            if(worker) {
                r->handler=ap_pstrdup(r->pool,JK_HANDLER);
                ap_table_setn(r->notes, JK_WORKER_ID, worker);
                return OK;
            }
        }
    }

    return DECLINED;
}

static const handler_rec jk_handlers[] =
{
    { JK_MAGIC_TYPE, jk_handler },
    { JK_HANDLER, jk_handler },    
    { NULL }
};

module MODULE_VAR_EXPORT jk_module = {
    STANDARD_MODULE_STUFF,
    jk_init,                    /* module initializer */
    NULL,                       /* per-directory config creator */
    NULL,                       /* dir config merger */
    create_jk_config,           /* server config creator */
    merge_jk_config,            /* server config merger */
    jk_cmds,                    /* command table */
    jk_handlers,                /* [7] list of handlers */
    jk_translate,               /* [2] filename-to-URI translation */
    NULL,                       /* [5] check/validate user_id */
    NULL,                       /* [6] check user_id is valid *here* */
    NULL,                       /* [4] check access by host address */
    NULL,                       /* XXX [7] MIME type checker/setter */
    NULL,                       /* [8] fixups */
    NULL,                       /* [10] logger */
    NULL,                       /* [3] header parser */
#if MODULE_MAGIC_NUMBER > 19970622
    NULL,                       /* apache child process initializer */
    NULL,                       /* apache child process exit/cleanup */
    NULL                        /* [1] post read_request handling */
#endif
};
