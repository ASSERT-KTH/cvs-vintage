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
 * Description: Workers controller                                         *
 * Author:      Gal Shachor <shachor@il.ibm.com>                           *
 * Version:     $Revision: 1.1 $                                               *
 ***************************************************************************/

#include "jk_ajp12_worker.h"
#include "jk_worker.h"
#include "jk_util.h"

struct worker_factory_record {
    const char *name;
    worker_factory fac;
};
typedef struct worker_factory_record worker_factory_record_t;

static jk_map_t *worker_map;

static worker_factory_record_t worker_factories[] = {
    { JK_AJP12_WORKER_NAME, ajp12_worker_factory},
    { NULL, NULL}
};

static void close_workers(void);

static worker_factory get_factory_for(char *type);

static int build_worker_map(jk_map_t *init_data, 
                            char **worker_list, 
                            unsigned num_of_workers,
                            jk_logger_t *l);

int wc_open(jk_map_t *init_data,
            jk_logger_t *l)
{
    char **worker_list  = NULL;
    unsigned num_of_workers = 0;

    jk_log(l, JK_LOG_DEBUG, "Into wc_open\n"); 

    if(!map_alloc(&worker_map)) {
        return JK_FALSE;
    }
    
    if(!jk_get_worker_list(init_data, 
                           &worker_list, 
                           &num_of_workers)) {
        return JK_FALSE;
    }
    
    if(!build_worker_map(init_data, 
                         worker_list, 
                         num_of_workers,
                         l)) {
        close_workers();
        return JK_FALSE;
    }

    jk_log(l, JK_LOG_DEBUG, "wc_open, done\n"); 
    return JK_TRUE;
}


void wc_close(jk_logger_t *l)
{
    jk_log(l, JK_LOG_DEBUG, "Into wc_close\n"); 
    close_workers();
    jk_log(l, JK_LOG_DEBUG, "wc_close, done\n"); 
}

jk_worker_t *wc_get_worker_for_name(const char *name, 
                                    jk_logger_t *l)
{
    jk_worker_t * rc;

    if(!name) {
        jk_log(l, JK_LOG_ERROR, "wc_get_worker_for_name NULL name\n"); 
    }

    jk_log(l, JK_LOG_DEBUG, "Into wc_get_worker_for_name %s\n", name); 

    rc = map_get(worker_map, name, NULL);

    jk_log(l, JK_LOG_DEBUG, "wc_get_worker_for_name, done %s found a worker\n", 
        rc ? "" : "did not"); 
    return rc;
}


int wc_create_worker(const char *name, 
                     jk_map_t *init_data,
                     jk_worker_t **rc,
                     jk_logger_t *l)
{
    jk_log(l, JK_LOG_DEBUG, "Into wc_create_worker\n"); 

    if(rc) {
        char *type = jk_get_worker_type(init_data, name);
        worker_factory fac = get_factory_for(type);
        jk_worker_t *w = NULL;

        *rc = NULL;

        if(!fac) {
            jk_log(l, JK_LOG_ERROR, "wc_create_worker NULL factory for %s\n", type); 
            return JK_FALSE;
        }

        jk_log(l, JK_LOG_DEBUG, "wc_create_worker, about to create instance %s of %s\n", 
               name, type);         

        if(!fac(&w, name, l) || !w) {
            jk_log(l, JK_LOG_ERROR, "wc_create_worker factory for %s failed for %s\n", 
                   type, name); 
            return JK_FALSE;
        }
        
        jk_log(l, JK_LOG_DEBUG, "wc_create_worker, about to validate and init %s\n", name);         
        if(!w->validate(w, init_data, l) ||
           !w->init(w, init_data, l)) {
            w->destroy(&w, l);
            jk_log(l, JK_LOG_ERROR, "wc_create_worker validate/init failed for %s\n", 
                   name); 
            return JK_FALSE;
        }
    
        *rc = w;
        jk_log(l, JK_LOG_DEBUG, "wc_create_worker, done\n"); 
        return JK_TRUE;
    }

    jk_log(l, JK_LOG_ERROR, "wc_create_worker, NUll input\n"); 
    return JK_FALSE;
}

static void close_workers(void)
{
    int sz = map_size(worker_map);
    if(sz > 0) {
        int i;
        for(i = 0 ; i < sz ; i++) {
            jk_worker_t *w = map_value_at(worker_map, i);
            if(w) {
                w->destroy(&w, NULL);
            }
        }
    }
    map_free(&worker_map);
}

static int build_worker_map(jk_map_t *init_data, 
                            char **worker_list, 
                            unsigned num_of_workers,
                            jk_logger_t *l)
{
    unsigned i;

    jk_log(l, JK_LOG_DEBUG, 
           "Into build_worker_map, creating %d workers\n", num_of_workers); 

    for(i = 0 ; i < num_of_workers ; i++) {
        jk_worker_t *w = NULL;

        jk_log(l, JK_LOG_DEBUG, 
               "build_worker_map, creating worker %s\n", worker_list[i]); 

        if(wc_create_worker(worker_list[i], init_data, &w, l)) {
            jk_worker_t *oldw = NULL;
            if(!map_put(worker_map, worker_list[i], w, (void *)&oldw)) {
                w->destroy(&w, l);
                return JK_FALSE;
            }

            jk_log(l, JK_LOG_DEBUG, 
                   "build_worker_map, removing old %s worker \n", worker_list[i]); 
            if(oldw) {
                oldw->destroy(&oldw, l);
            }
        } else {        
            jk_log(l, JK_LOG_ERROR, "build_worker_map failed to create worker%s\n", 
                   worker_list[i]); 
            return JK_FALSE;
        }
    }

    jk_log(l, JK_LOG_DEBUG, "build_worker_map, done\n"); 
    return JK_TRUE;
}

static worker_factory get_factory_for(char *type)
{
    worker_factory_record_t *factory = &worker_factories[0];
    while(factory->name) {
        if(0 == strcmp(factory->name, type)) {
            return factory->fac;
        }

        factory ++;
    }

    return NULL;
}