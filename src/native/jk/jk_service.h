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
 * Description: Definitions of the objects used during the service step.   *
 *              These are the web server (ws) the worker and the connection*
 *              JVM connection point                                       *
 * Author:      Gal Shachor <shachor@il.ibm.com>                           *
 * Version:     $Revision: 1.1 $                                               *
 ***************************************************************************/

#ifndef JK_SERVICE_H
#define JK_SERVICE_H

#include "jk_map.h"
#include "jk_global.h"
#include "jk_logger.h"
#include "jk_pool.h"

#ifdef __cplusplus
extern "C" {
#endif /* __cplusplus */

struct jk_ws_service;
struct jk_endpoint;
struct jk_worker;
typedef struct jk_ws_service jk_ws_service_t;
typedef struct jk_endpoint   jk_endpoint_t;
typedef struct jk_worker     jk_worker_t;

struct jk_ws_service {
    void *ws_private;
    
    /*
     * Alive as long as the request is alive.
     */
    jk_pool_t *pool;

    /* 
     * CGI Environment needed by servlets
     */
    char    *method;        
    char    *protocol;      
    char    *req_uri;       
    char    *remote_addr;   
    char    *remote_host;   
    char    *remote_user;   
    char    *auth_type;     
    char    *query_string;  
    char    *server_name;   
    unsigned server_port;   
    char    *server_software;
    unsigned content_length;    /* integer that represents the content  */
                                /* length should be 0 if unknown.        */

    /*
     * SSL information
     *
     * is_ssl       - True if request is in ssl connection
     * ssl_cert     - If available, base64 ASN.1 encoded client certificates.
     * ssl_cert_len - Length of ssl_cert, 0 if certificates are not available.
     * ssl_cipher   - The ssl cipher suite in use.
     * ssl_session  - The ssl session string
     *
     * In some servers it is impossible to extract all this information, in this 
     * case, we are passing NULL.
     */
    int      is_ssl;
    char     *ssl_cert;
    unsigned ssl_cert_len;
    char     *ssl_cipher;
    char     *ssl_session;

    /*
     * Headers, names and values.
     */
    char    **headers_names;    /* Names of the request headers  */
    char    **headers_values;   /* Values of the request headers */
    unsigned num_headers;       /* Number of request headers     */


    /*
     * The jvm route is in use when the adapter load balance among
     * several JVMs. It is the ID of a specific JVM in the load balance
     * group. We are using this variable to implement JVM session 
     * affinity
     */
    char    *jvm_route;

    /*
     * Callbacks into the web server.
     */
    int (JK_METHOD *start_response)(jk_ws_service_t *s,
                                    int status,
                                    const char *reason,
                                    const char * const *header_names,
                                    const char * const *header_values,
                                    unsigned num_of_headers);

    int (JK_METHOD *read)(jk_ws_service_t *s,
                          void *b,
                          unsigned l,
                          unsigned *a);

    int (JK_METHOD *write)(jk_ws_service_t *s,
                           const void *b,
                           unsigned l);
};

struct jk_endpoint {
    void *endpoint_private;

    int (JK_METHOD *service)(jk_endpoint_t *e, 
                             jk_ws_service_t *s,
                             jk_logger_t *l,
                             int *is_recoverable_error);

    int (JK_METHOD *done)(jk_endpoint_t **p,
                          jk_logger_t *l);
};

struct jk_worker {
    void *worker_private;

    int (JK_METHOD *validate)(jk_worker_t *w,
                              jk_map_t *props, 
                              jk_logger_t *l);

    int (JK_METHOD *init)(jk_worker_t *w,
                          jk_map_t *props, 
                          jk_logger_t *l);


    int (JK_METHOD *get_endpoint)(jk_worker_t *w,
                                  jk_endpoint_t **pend,
                                  jk_logger_t *l);

    int (JK_METHOD *destroy)(jk_worker_t **w,
                             jk_logger_t *l);
};

typedef int (JK_METHOD *worker_factory)(jk_worker_t **w,
                                        const char *name,
                                        jk_logger_t *l);

#ifdef __cplusplus
}
#endif /* __cplusplus */

#endif /* JK_SERVICE_H */
