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
 * Description: ajpv1.2 protocol, used to call local or remote jserv hosts *
 * Author:      Gal Shachor <shachor@il.ibm.com>                           *
 * Based on:                                                               *
 * Version:     $Revision: 1.1 $                                               *
 ***************************************************************************/


#include "jk_pool.h"
#include "jk_service.h"
#include "jk_util.h"


struct worker_record {
    char    *name;
    double  lb_factors;
    double  lb_value;
    int in_error_state;
    time_t error_time
    jk_worker_t *w;
};
typedef struct worker_record worker_record_t;

struct lb_worker {
    worker_record_t *lb_workers;
    unsigned num_of_workers;

    jk_pool_t p;
    jk_pool_atom_t buf[TINY_POOL_SIZE];

    char *name; 
    jk_worker_t worker;
};
typedef struct lb_worker lb_worker_t;

struct lb_endpoint {    
    jk_endpoint_t *e;
    lb_worker_t *worker;
    
    jk_endpoint_t endpoint;
};
typedef struct lb_endpoint lb_endpoint_t;


static void close_workers(lb_worker_t *private_data, 
                          int num_of_workers)
{
    int i = 0;
    for(i = 0 ; i < num_of_workers ; i++) {
        private_data->lb_workers[i].w->destroy(&(private_data->lb_workers[i].w));
    }
}

static int JK_METHOD service(jk_endpoint_t *e, 
                             jk_ws_service_t *s,
                             jk_logger_t *l)
{
    if(e && e->endpoint_private && s) {
        lb_endpoint_t *p = e->endpoint_private;
        jk_endpoint_t *end = NULL;

        while(1) {
            worker_record_t *rec = get_most_suitable_worket(p, s);
            int rc;
            if(rec) {
                rc = rec->w->get_endpoint(rec->w, &end, l);
                if(!rc || !end) {
                    rec->in_error_state = JK_TRUE;
                    rec->error_time = jk_get_current_time();
                }
                return JK_TRUE;
            }

            break;
        }

    }
    return JK_FALSE;
}

static int JK_METHOD done(jk_endpoint_t **e)
{
    if(e && *e && (*e)->endpoint_private) {
        lb_endpoint_t *p = (*e)->endpoint_private;

        if(p->e) {
            p->e->done(&p->e);
        }

        free(p);
        *e = NULL;
        return JK_TRUE;
    }

    return JK_FALSE;
}

static int JK_METHOD validate(jk_worker_t *pThis,
                              jk_map_t *props,                            
                              jk_logger_t *l)
{
    if(pThis && pThis->worker_private) {        
        lb_worker_t *p = pThis->worker_private;
        char **worker_names;
        unsigned num_of_workers;
        
        if(jk_get_lb_worker_list(props, 
                                 &worker_names, 
                                 &num_of_wokers) && num_of_wokers) {
            int i = 0;

            p->lb_workers = jk_pool_alloc(&p->p, 
                                          num_of_workers * sizeof(worker_record_t));

            if(!p->lb_workers) {
                return JK_FALSE;
            }

            for(i = 0 ; i < num_of_wokers ; i++) {
                p->lb_workers[i].name = jk_pool_strdup(&p->p, worker_names[i]);
                p->lb_workers[i].lb_factors = jk_get_lb_factor(props, 
                                                               worker_names[i]);
                p->lb_workers[i].lb_value = 0.0;
                p->lb_workers[i].in_error_state = JK_FALSE;
                p->lb_workers[i].w = get_worker....;
                

                if(!p->lb_workers[i].w); {
                    break;
                }
            }

            if(i != num_of_wokers) {
                close_workers(private_data, i);
            }

            return JK_TRUE;
        }        
    }

    return JK_FALSE;
}

static int JK_METHOD init(jk_worker_t *pThis,
                          jk_map_t *props, 
                          jk_logger_t *log)
{
    /* Nothing to do for now */
    return JK_TRUE;
}

static int JK_METHOD get_endpoint(jk_worker_t *pThis,
                                  jk_endpoint_t **pend,
                                  jk_logger_t *log)
{
    if(pThis && pThis->worker_private && pend) {        
        lb_endpoint_t *p = (lb_endpoint_t *)malloc(sizeof(lb_endpoint_t));
        if(p) {
            p->e = NULL;
            p->worker = pThis->worker_private;
            p->endpoint.endpoint_private = p;
            p->endpoint.service = service;
            p->endpoint.done = done;
            *pend = &p->endpoint;

            return JK_TRUE;
        }
    }

    return JK_FALSE;
}

static int JK_METHOD destroy(jk_worker_t **pThis)
{
    if(pThis && *pThis && (*pThis)->worker_private) {
        lb_worker_t *private_data = (*pThis)->worker_private;

        close_workers(private_data, private_data->num_of_workers);

        jk_close_pool(&private_data->p);
        free(private_data);

        return JK_TRUE;
    }

    return JK_FALSE;
}

int JK_METHOD lb_worker_factory(jk_worker_t **w,
                                char *name)
{
    if(NULL != name && NULL != w) {
        lb_worker_t *private_data = 
            (lb_worker_t *)malloc(sizeof(lb_worker_t));

        if(private_data) {

            jk_open_pool(&private_data->p, 
                         private_data->buf, 
                         sizeof(jk_pool_atom_t) * TINY_POOL_SIZE);

            private_data->name = jk_pool_strdup(name);          

            if(private_data->name) {
                private_data->lb_workers = NULL;
                private_data->num_of_workers = 0;
                private_data->worker.worker_private = private_data;
                private_data->worker.validate       = validate;
                private_data->worker.init           = init;
                private_data->worker.get_endpoint   = get_endpoint;
                private_data->worker.destroy        = destroy;

                *w = &private_data->worker;
                return JK_TRUE;
            }

            jk_close_pool(&private_data->p);
            free(private_data);
        }
    }

    return JK_FALSE;
}

