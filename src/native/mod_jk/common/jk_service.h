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
 *              Dan Milstein <danmil@shore.net> 
 * Version:     $Revision: 1.5 $                                               *
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

/*
 * The web server service 'class'.  An instance of this class is created
 * for each request which is forwarded from the web server to the servlet
 * container.  Contains the basic information about the request
 * (e.g. protocol, req_uri, etc), and also contains a series of methods
 * which provide access to core web server functionality (start_response,
 * read, write).  This class might be more accurately called ws_request.
 *
 * As with all the core jk classes, this is essentially an abstract base
 * class which is implemented/extended by classes which are specific to a
 * particular web server.  By using an abstract base class in this manner,
 * workers can be written for different protocols (e.g. ajp12, ajp13)
 * without the workers having to worry about which web server they are
 * talking to.
 *
 * This particular OO-in-C system uses a 'ws_private' pointer to point to
 * the platform-specific data.  So in the subclasses, the methods do most
 * of their work by getting their hands on the ws_private pointer and then
 * using that to get at the correctly formatted data and functions for
 * their platform.
 *
 * Try imagining this as a 'public abstract class', and the ws_private
 * pointer as a sort of extra 'this' reference.  Or imagine that you are
 * seeing the internal vtables of your favorite OO language.  Whatever
 * works for you.
 *
 * See apache1.3/mod_jk.c and iis/jk_isapi_plugin.c for examples.  
 */
struct jk_ws_service {
  
    /* 
     * A 'this' pointer which is used by the subclasses of this class to
     * point to data which is specific to a given web server platform
     * (e.g. Apache or IIS).  
     */
    void *ws_private;
    
    /*
     * Provides memory management.  All data specific to this request is
     * allocated within this pool, which can then be reclaimed at the end
     * of the request handling cycle. 
     *
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
    unsigned is_chunked;        /* 1 if content length is unknown (chunked rq) */
    unsigned no_more_chunks;    /* 1 if last chunk has been read */
    unsigned content_read;      /* number of bytes read */

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
     * Request attributes. 
     *
     * These attributes that were extracted from the web server and are 
     * sent to Tomcat.
     *
     * The developer should be able to read them from the ServletRequest
     * attributes. Tomcat is required to append org.apache.tomcat. to 
     * these attrinbute names.
     */
    char    **attributes_names;    /* Names of the request attributes  */
    char    **attributes_values;   /* Values of the request attributes */
    unsigned num_attributes;       /* Number of request attributes     */

    /*
     * The jvm route is in use when the adapter load balance among
     * several JVMs. It is the ID of a specific JVM in the load balance
     * group. We are using this variable to implement JVM session 
     * affinity
     */
    char    *jvm_route;

    /*
     * Callbacks into the web server.  For each, the first argument is
     * essentially a 'this' pointer.  All return JK_TRUE on success
     * and JK_FALSE on failure.
     */

    /*
     * Send the response headers to the browser.
     */
    int (JK_METHOD *start_response)(jk_ws_service_t *s,
                                    int status,
                                    const char *reason,
                                    const char * const *header_names,
                                    const char * const *header_values,
                                    unsigned num_of_headers);

    /*
     * Read a chunk of the request body into a buffer.  Attempt to read len
     * bytes into the buffer.  Write the number of bytes actually read into
     * actually_read.  
     */
    int (JK_METHOD *read)(jk_ws_service_t *s,
                          void *buffer,
                          unsigned len,
                          unsigned *actually_read);

    /*
     * Write a chunk of response data back to the browser.
     */
    int (JK_METHOD *write)(jk_ws_service_t *s,
                           const void *buffer,
                           unsigned len);
};

/*
 * The endpoint 'class', which represents one end of a connection to the
 * servlet engine.  Basically, supports nothing other than forwarding the
 * request to the servlet engine.  Endpoints can be persistent (as with
 * ajp13, where a single connection is reused many times), or can last for a
 * single request (as with ajp12, where a new connection is created for
 * every request).
 *
 * An endpoint for a given protocol is obtained by the web server plugin
 * from a worker object for that protocol.  See below for details.
 *
 * As with all the core jk classes, this is essentially an abstract base
 * class which is implemented/extended by classes which are specific to a
 * particular protocol.  By using an abstract base class in this manner,
 * plugins can be written for different servers (e.g. IIS, Apache) without
 * the plugins having to worry about which protocol they are talking.
 *
 * This particular OO-in-C system uses a 'endpoint_private' pointer to
 * point to the protocol-specific data/functions.  So in the subclasses, the
 * methods do most of their work by getting their hands on the
 * endpoint_private pointer and then using that to get at the functions for
 * their protocol.
 *
 * Try imagining this as a 'public abstract class', and the
 * endpoint_private pointer as a sort of extra 'this' reference.  Or
 * imagine that you are seeing the internal vtables of your favorite OO
 * language.  Whatever works for you.
 *
 * See jk_ajp13_worker.c and jk_ajp12_worker.c for examples.  
 */
struct jk_endpoint {
  
    /* 
     * A 'this' pointer which is used by the subclasses of this class to
     * point to data/functions which are specific to a given protocol 
     * (e.g. ajp12 or ajp13).  
     */
    void *endpoint_private;

    /*
     * Forward a request to the servlet engine.  The request is described
     * by the jk_ws_service_t object.  I'm not sure exactly how
     * is_recoverable_error is being used.  
     */
    int (JK_METHOD *service)(jk_endpoint_t *e, 
                             jk_ws_service_t *s,
                             jk_logger_t *l,
                             int *is_recoverable_error);

    /*
     * Called when this particular endpoint has finished processing a
     * request.  For some protocols (e.g. ajp12), this frees the memory
     * associated with the endpoint.  For others (e.g. ajp13), this can
     * return the endpoint to a cache of already opened endpoints.  
     *
     * Note that the first argument is *not* a 'this' pointer, but is
     * rather a pointer to a 'this' pointer.  This is necessary, because
     * we may need to free this object.
     */
    int (JK_METHOD *done)(jk_endpoint_t **p,
                          jk_logger_t *l);
};

/*
 * The worker 'class', which represents something to which the web server
 * can delegate requests. 
 *
 * This can mean communicating with a particular servlet engine instance,
 * using a particular protocol.  A single web server instance may have
 * multiple workers communicating with a single servlet engine (it could be
 * using ajp12 for some requests and ajp13 for others).  Or, a single web
 * server instance could have multiple workers communicating with different
 * servlet engines using the same protocol (it could be load balancing
 * among many engines, using ajp13 for all communication).
 *
 * There is also a load balancing worker (jk_lb_worker.c), which itself
 * manages a group of workers.
 *
 * Web servers are configured to forward requests to a given worker.  To
 * handle those requests, the worker's get_endpoint method is called, and
 * then the service() method of that endpoint is called.
 *
 * As with all the core jk classes, this is essentially an abstract base
 * class which is implemented/extended by classes which are specific to a
 * particular protocol (or request-handling system).  By using an abstract
 * base class in this manner, plugins can be written for different servers
 * (e.g. IIS, Apache) without the plugins having to worry about which
 * protocol they are talking.
 *
 * This particular OO-in-C system uses a 'worker_private' pointer to
 * point to the protocol-specific data/functions.  So in the subclasses, the
 * methods do most of their work by getting their hands on the
 * worker_private pointer and then using that to get at the functions for
 * their protocol.
 *
 * Try imagining this as a 'public abstract class', and the
 * worker_private pointer as a sort of extra 'this' reference.  Or
 * imagine that you are seeing the internal vtables of your favorite OO
 * language.  Whatever works for you.
 *
 * See jk_ajp13_worker.c and jk_ajp12_worker.c for examples.  
 */
struct jk_worker {

    /* 
     * A 'this' pointer which is used by the subclasses of this class to
     * point to data/functions which are specific to a given protocol 
     * (e.g. ajp12 or ajp13).  
     */
    void *worker_private;

    /*
     * For all of the below (except destroy), the first argument is
     * essentially a 'this' pointer.  
     */

    /*
     * Given a worker which is in the process of being created, and a list
     * of configuration options (or 'properties'), check to see if it the
     * options are.  This will always be called before the init() method.
     * The init/validate distinction is a bit hazy to me.
     * See jk_ajp13_worker.c and jk_worker.c->wc_create_worker() 
     */
    int (JK_METHOD *validate)(jk_worker_t *w,
                              jk_map_t *props, 
                              jk_logger_t *l);

    /*
     * Do whatever initialization needs to be done to start this worker up.
     * Configuration options are passed in via the props parameter.  
     */
    int (JK_METHOD *init)(jk_worker_t *w,
                          jk_map_t *props, 
                          jk_logger_t *l);


    /*
     * Obtain an endpoint to service a particular request.  A pointer to
     * the endpoint is stored in pend.  
     */
    int (JK_METHOD *get_endpoint)(jk_worker_t *w,
                                  jk_endpoint_t **pend,
                                  jk_logger_t *l);

    /*
     * Shutdown this worker.  The first argument is not a 'this' pointer,
     * but rather a pointer to 'this', so that the object can be free'd (I
     * think -- though that doesn't seem to be happening.  Hmmm).  
     */
    int (JK_METHOD *destroy)(jk_worker_t **w,
                             jk_logger_t *l);
};

/*
 * Essentially, an abstract base class (or factory class) with a single
 * method -- think of it as createWorker() or the Factory Method Design
 * Pattern.  There is a different worker_factory function for each of the
 * different types of workers.  The set of all these functions is created
 * at startup from the list in jk_worker_list.h, and then the correct one
 * is chosen in jk_worker.c->wc_create_worker().  See jk_worker.c and
 * jk_ajp13_worker.c for examples.
 *
 * This allows new workers to be written without modifing the plugin code
 * for the various web servers (since the only link is through
 * jk_worker_list.h).  
 */
typedef int (JK_METHOD *worker_factory)(jk_worker_t **w,
                                        const char *name,
                                        jk_logger_t *l);

#ifdef __cplusplus
}
#endif /* __cplusplus */

#endif /* JK_SERVICE_H */
