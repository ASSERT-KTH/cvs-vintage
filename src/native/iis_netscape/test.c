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

#include <stdio.h>
#include <winsock.h>

#include "jk_map.h"
#include "jk_service.h"
#include "jk_worker.h"
#include "jk_uri_worker_map.h"

static int JK_METHOD start_response(jk_ws_service_t *s,
                                    int status,
                                    char *reason,
                                    const char * const *header_names,
                                    const char * const *header_values,
                                    unsigned num_of_headers)
{
    unsigned i;
    printf("Final results \n");
    printf("Status %d %s\n", status, reason ? reason : "NULL");
    for(i = 0 ; i < num_of_headers ; i++) {
        printf("Header %s is %s\n", header_names[i], header_values[i]);
    }

    return JK_TRUE;
}


static int JK_METHOD write(jk_ws_service_t *s,
                           const void *buf,
                           unsigned len)
{
    fwrite(buf, 1, len, stdout);
    return JK_TRUE;
}

void main(void)
{
    char *names[] = {"Accept-Language", "Connection", "User-Agent", "Host", "Accept-Encoding", "Accept", "Cookie"};
    char *values[] = {"en-us", "Keep-Alive", "Mozilla/4.0 (compatible; MSIE 4.01; Windows NT)", "localhost:8080", "gzip, deflate", "image/gif, image/x-xbitmap, image/jpeg, image/pjpeg, application/vnd.ms-excel, application/msword, application/vnd.ms-powerpoint, */*", "JSESSIONID=To1021mC22584319650438112At"};

    jk_map_t *map;
    jk_worker_t *worker;
    jk_endpoint_t *e;
    jk_ws_service_t s = {
        NULL,
        "http",
        "GET",
        "http",
        "/examples/snoop",
        "127.0.0.1",
        "localhost",
        "gal",
        "my",
        "gal=lag",
        "galserver",
        8007,
        "gal/1.1",

        FALSE,
        NULL,
        0,
        NULL,

        NULL,
        0,
    
        names,
        values,
        7,

        start_response, NULL, write
    };

    WORD wVersionRequested;
    WSADATA wsaData;
    int err; 
    jk_uri_worker_map_t *uw_map;

    wVersionRequested = MAKEWORD( 2, 0 ); 
    err = WSAStartup( wVersionRequested, &wsaData );
    if ( err != 0 ) 
    {
        fprintf(stderr, "Error connecting to winosck");
        return;
    } 

    /* Confirm that the WinSock DLL supports 2.0.*/
    /* Note that if the DLL supports versions greater    */
    /* than 2.0 in addition to 2.0, it will still return */
    /* 2.0 in wVersion since that is the version we      */
    /* requested.                                        */ 
    if (LOBYTE( wsaData.wVersion ) != 2 ||
        HIBYTE( wsaData.wVersion ) != 0 ) 
    {
        /* Tell the user that we couldn't find a usable */
        /* WinSock DLL.                                  */    
        fprintf(stderr, "Error version is %d %d \n", LOBYTE( wsaData.wVersion ),HIBYTE( wsaData.wVersion ));
        WSACleanup( );
        return; 
    } /* The WinSock DLL is acceptable. Proceed. */

    fprintf(stderr, "version is %d %d \n", LOBYTE( wsaData.wVersion ),HIBYTE( wsaData.wVersion ));


    map_alloc(&map);   
    map_read_properties(map, 
                        "d:\\Microsoft Visual Studio\\VC98\\MyProjects\\jk\\uwmap.properties");
    uri_worker_map_alloc(&uw_map, map, NULL);
    

    printf("The worker for %s is %s\n", "/gal/t", map_uri_to_worker(uw_map, "/gal/t", NULL));
    printf("The worker for %s is %s\n", "/gal/tt", map_uri_to_worker(uw_map, "/gal/tt", NULL));
    printf("The worker for %s is %s\n", "/gal/", map_uri_to_worker(uw_map, "/gal/", NULL));
    printf("The worker for %s is %s\n", "/gal/tt.jsp", map_uri_to_worker(uw_map, "/gal/tt.jsp", NULL));
    printf("The worker for %s is %s\n", "/gal/gil/t.jsp", map_uri_to_worker(uw_map, "/gal/gil/t.jsp", NULL));

    /*
    map_alloc(&map);   

    map_read_properties(map, 
                        "d:\\Microsoft Visual Studio\\VC98\\MyProjects\\jk\\test.properties");

    wc_open(map, NULL);
    worker = wc_get_worker_for_name("ajp12", NULL);
    
    worker->get_endpoint(worker, &e, NULL);
    e->service(e, &s, NULL);
    e->done(&e);

    */

    WSACleanup( );
}