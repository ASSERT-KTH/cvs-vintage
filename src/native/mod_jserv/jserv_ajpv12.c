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
 * Description: ajpv1.2 protocol, used to call local or remote jserv hosts   *
 * Author:      Pierpaolo Fumagalli <ianosh@iname.com>                       *
 * Author:      Michal Mosiewicz <mimo@interdata.pl>                         *
 * Version:     $Revision: 1.2 $                                             *
 *****************************************************************************/
#include "jserv.h"

/*****************************************************************************
 * Code for ajpv12 protocol                                                   *
 *****************************************************************************/

/* copy + paste from src/main/http_main.c" */
#ifdef _OSD_POSIX
#define MAX_SECS_TO_LINGER 30
static void sock_enable_linger(jserv_config *cfg, int s)
{
    struct linger li;

    li.l_onoff = 1;
    li.l_linger = MAX_SECS_TO_LINGER;

    if (setsockopt(s, SOL_SOCKET, SO_LINGER,
                   (char *) &li, sizeof(struct linger)) < 0) {
        jserv_error(JSERV_LOG_INFO,cfg,"ajp12:setsockopt: (SO_LINGER)");
        /* not a fatal error */
    }
}
#endif

/* ========================================================================= */
/* Open a socket to JServ host */
/* FIXME: short port gets overflowed - it's not unsigned */
static int ajpv12_open(jserv_config *cfg, pool *p, unsigned long address,
                       unsigned short port) {
    struct sockaddr_in addr;
    int sock;
    int ret;

    /* Check if we have a valid host address */
    if (address==0) {
        jserv_error(JSERV_LOG_EMERG,cfg,"ajp12: %s",
                    "cannot connect to unspecified host");
        return -1;
    }

    /* Check if we have a valid port number. */
    if (port < 1024) {
        jserv_error(JSERV_LOG_INFO,cfg,"ajp12: %d: %s",
                    port,
                    "invalid port, reset to default 8007");
        port = 8007;
    }
    addr.sin_addr.s_addr = address;
    addr.sin_port = htons(port);
    addr.sin_family = AF_INET;

    /* Open the socket */
    sock=ap_psocket(p, AF_INET, SOCK_STREAM, 0);
    if (sock==-1) {
        jserv_error(JSERV_LOG_EMERG,cfg,"ajp12: %s","can not open socket");
        return -1;
    }
    jserv_error(JSERV_LOG_DEBUG,cfg,"ajp12: opening port %d",port);

    /* Tries to connect to JServ (continues trying while error is EINTR) */
    do {
        ret=connect(sock,(struct sockaddr *)&addr,sizeof(struct sockaddr_in));
#ifdef WIN32
        if (ret==SOCKET_ERROR) errno=WSAGetLastError()-WSABASEERR;
#endif /* WIN32 */
    } while (ret==-1 && errno==EINTR);

    /* Check if we connected */
    if (ret==-1) {
        jserv_error(JSERV_LOG_EMERG,cfg,"ajp12: %s %s:%d",
                    "can not connect to host",
                    inet_ntoa(addr.sin_addr),
                    port);
        if (sock != -1)
            ap_pclosesocket(p, sock);
        return -1;
    }
#ifdef TCP_NODELAY
    {
        int set = 1;
        setsockopt(sock, IPPROTO_TCP, TCP_NODELAY, (char *)&set, 
        sizeof(set));
    }
#endif

#ifdef _OSD_POSIX
    /* otherwise the shutdown signal is not received */
    sock_enable_linger(cfg, sock);
#endif

    /* Return the socket number */
    return sock;
}

/* ========================================================================= */
/* Authenticate a socket for JServ operation */
static int ajpv12_auth(jserv_config *cfg, pool *p, int sock, char *secret,
                       long secretsize) {
    AP_MD5_CTX md5context;
    long challengesize;
    unsigned char hash[16];
    unsigned char *challenge;
    unsigned char *tmp;
    int ret;


    /* Check if we had an auth, otherwise we suppose authentication disabled */
    if (secretsize==JSERV_DISABLED) {
        jserv_error(JSERV_LOG_DEBUG,cfg,"ajp12: %s", "auth is disabled");
        return 0;
    }

    /* Check if we had an auth, otherwise we suppose authentication disabled */
    if (secret==NULL) {
        jserv_error(JSERV_LOG_ERROR,cfg,"ajp12: %s",
                    "auth is disabled (size was not disabled, but pass was)");
        return 0;
    }

    /* Receive the size of challenge string. Cast a long int to char buffer */
    ret=recv(sock, (char *)(&challengesize), 4,0);
    if (ret!=4) {
        jserv_error(JSERV_LOG_EMERG,cfg,"ajp12: %s",
                    "auth did not receive challenge size");
        return -1;
    }
    challengesize=ntohl(challengesize);

    /* Allocate space for challenge and receive the string */
    challenge=(unsigned char *)ap_pcalloc(p,challengesize+secretsize+1);
    ret=recv(sock, challenge, challengesize,0);
    if (ret!=challengesize) {
        jserv_error(JSERV_LOG_EMERG,cfg,"ajp12: %s",
                    "auth did not receive full challenge");
        return -1;
    }

    /* Append our secret (from mount or configuration) to the challenge */
    tmp=&challenge[challengesize];
    memcpy(tmp,secret,secretsize);

    /* Perform md5 over challenge+secret */
    ap_MD5Init(&md5context);
    ap_MD5Update(&md5context, challenge, challengesize+secretsize);
    ap_MD5Final(hash, &md5context);

    /* Send authentication data over the socket byte by byte */
    ret=send(sock,hash,16,0);
    if (ret!=16) {
        jserv_error(JSERV_LOG_EMERG,cfg,"ajp12: %s",
                    "can not send the md5 hashed auth");
        return -1;
    }

    /* If we did not return a valid confirmation, socket was closed*/
    return 0;
}

static int ajpv12_sendnbytes(BUFF * bsock, const void *buffer, int bufferlen ) {
    unsigned char bytes[2];
    static char null_b[2];
    int ret;
#ifdef CHARSET_EBCDIC
    int flags;
#endif

    null_b[0] = 0xff;
    null_b[1] = 0xff;
#ifdef CHARSET_EBCDIC
    flags = bsock->flags;
    bsock->flags = bsock->flags & ~B_EBCDIC2ASCII; /* binary */
#endif

    if( buffer != NULL ) {
	bytes[0] = (unsigned char) ( (bufferlen >> 8) & 0xff );
	bytes[1] = (unsigned char) ( bufferlen & 0xff );

	ret = ap_bwrite(bsock, bytes, 2);
#ifdef CHARSET_EBCDIC
	bsock->flags = flags;
#endif
	if (ret == 2) {
	    return ap_bwrite(bsock,buffer,bufferlen);
	} else {
	    return 0;
	}
    } else {
	ret = ap_bwrite(bsock,null_b,2);
#ifdef CHARSET_EBCDIC
	bsock->flags = flags;
#endif
	return ret == 2 ? 0 : -1;
    }
}

static int ajpv12_sendstring(BUFF * bsock, const char * buffer) {

    int bufferlen;
    if( buffer != NULL ) {
	bufferlen=strlen(buffer);
	return ajpv12_sendnbytes(bsock, buffer, bufferlen) == bufferlen ? 0 : -1;
    } else {
	return ajpv12_sendnbytes(bsock, NULL, 0);
    }

}

static int ajpv12_mark(BUFF * bsock, unsigned char type) {
#ifdef CHARSET_EBCDIC
    int flags;
    flags = bsock->flags;
    bsock->flags = bsock->flags & ~B_EBCDIC2ASCII; /* binary */
#endif
    if( ap_bwrite(bsock, &type, 1) == 1) {
#ifdef CHARSET_EBCDIC
        bsock->flags = flags;
#endif
	return 0;
    } else {
#ifdef CHARSET_EBCDIC
        bsock->flags = flags;
#endif
	return -1;
    }
}
/**
 * Borrowed from util_script.c - too bad, it is not exported
 */

static char *original_uri(request_rec *r)
{
    char *first, *last;

    if (r->the_request == NULL) {
     return (char *) ap_pcalloc(r->pool, 1);
    }

    first = r->the_request;  /* use the request-line */

    while (*first && !ap_isspace(*first)) {
     ++first;                /* skip over the method */
    }
    while (ap_isspace(*first)) {
     ++first;                /*   and the space(s)   */
    }

    last = first;
    while (*last && !ap_isspace(*last) && *last != '?') {
     ++last;                 /* end at next whitespace */
    }

    return ap_pstrndup(r->pool, first, last - first);
}


#define THROW_EXCEPTION \
      ap_kill_timeout(r); \
      ap_bclose(buffsocket); \
      ap_pclosesocket(r->pool,sock); \
      return SERVER_ERROR 


static int data_available(int sock) {
    fd_set fdset;
    struct timeval tv;
    int rv;
    do {
        FD_ZERO(&fdset);
        FD_SET(sock, &fdset);
        tv.tv_sec = 0;
        tv.tv_usec = 0;
        rv = ap_select(sock+1, &fdset, NULL, NULL, &tv);
    } while( rv<0 && errno == EINTR);
    return (int) (rv == 1);
}    

static int ajpv12_pass_headers(jserv_config *cfg, jserv_request *req, request_rec *r, BUFF * buffsocket) {
    int ret;
    const char *header;
    if ((ret=ap_scan_script_header_err_buff(r, buffsocket, NULL))) {
        if( ret>=500 || ret < 0)
            jserv_error(JSERV_LOG_EMERG,cfg,"ajp12[1]: %s (%d)",
                        "cannot scan servlet headers ", ret);

        r->status_line = NULL;
        return ret;
    }

    if ((header=ap_table_get(r->err_headers_out, "Servlet-Error"))) {
        /* Backup the original status */
        int status=r->status;
                    
                    /* Close buffer and kill timeouts */
        ap_bclose(buffsocket);
        ap_kill_timeout(r);

                    /* Log error to Apache logfile */
        jserv_error(JSERV_LOG_ERROR,cfg,"ajp12: Servlet Error: %s",header);
                    
        /* Remove headers and return */
        ap_table_unset(r->err_headers_out, "Servlet-Error");
        /* r->status = HTTP_OK; */
        r->status_line = NULL;
        return status;
    }
    /* Check for CGI redirects: (Location: ...) */
    if (ap_table_get(r->headers_out, "Location") && r->status == 200) {
        /* Close buffer and kill timeouts */
        ap_bclose(buffsocket);
        ap_kill_timeout(r);
                    
        /* Set up request structure to allow proper redirection */
        r->status = HTTP_OK;
        r->status_line = NULL;
        return REDIRECT;
    }
                
    /* Send headers and data collected (if this was not a "header only" req. */
    ap_send_http_header(r);
    return 0;
}

static int ajpv12_handle_in(jserv_config *cfg, jserv_request *req, request_rec *r, int *state, BUFF * buffsocket) {
    int ret;
#ifdef HAVE_APFD /* IBM Apache */
    if( buffsocket->pfd_in->sd < 0 ) {
#else
    if( buffsocket->fd_in < 0 ) {
#endif
	jserv_error(JSERV_LOG_EMERG,cfg,"ajp12: ajpv12_handle_in: input socket non existing");
        return 500;
    }

    switch( *state ) {

        case 0:
#ifdef HAVE_APFD /* IBM Apache */
            if(data_available(buffsocket->pfd_in->sd)) {
#else
            if(data_available(buffsocket->fd_in)) {
#endif
                ret = ajpv12_pass_headers(cfg,req,r,buffsocket);
                if( ret==500 ) 
                    return ret;
                (*state)++;
                return ret;
            }

            break;

        case 1:
#ifdef HAVE_APFD /* IBM Apache */
            if(data_available(buffsocket->pfd_in->sd)) {
#else
            if(data_available(buffsocket->fd_in)) {
#endif
                char buffer[HUGE_STRING_LEN];
                int len;
                len = (int) ap_bread(buffsocket, buffer, HUGE_STRING_LEN);

#ifdef HAVE_APFD /* IBM Apache */
                if(r->connection->client->pfd->sd >= 0) {
                    if(ap_bwrite(r->connection->client, buffer, len) < len) {
                        r->connection->client->pfd->sd =-1;
#else
                if(r->connection->client->fd >= 0) {
                    if(ap_bwrite(r->connection->client, buffer, len) < len) {
                        r->connection->client->fd =-1;
#endif
                        jserv_error(JSERV_LOG_EMERG,cfg,"ajp12: Connnection reset by peer");
                    }
                } else {
                    return -1;
                }
            }
    }
    return 0;
}



/* ========================================================================= */
/* Our request handler */
static int ajpv12_handler(jserv_config *cfg, jserv_request *req, 
                          request_rec *r) {
    int ret, sock;
    BUFF *buffsocket;
    int in_state;
    in_state=0;

    /* Check for correct config member */
    if (cfg==NULL) {
        jserv_error(JSERV_LOG_EMERG,cfg,"ajp12: %s",
                    "unknown configuration member for request");
        return SERVER_ERROR;
    }

    /* Check for correct jserv request member */
    if (req==NULL) {
        jserv_error(JSERV_LOG_EMERG,cfg,"ajp12: %s",
                    "null request not handled");
        return SERVER_ERROR;
    }
    if (req->mount==NULL) {
        jserv_error(JSERV_LOG_EMERG,cfg,"ajp12: %s",
                    "unknown mount for request");
        return SERVER_ERROR;
    }

    /* Open connection to JServ */
    ap_hard_timeout("ajpv12-open", r);
    {
        unsigned long address=0;
        unsigned short port=0;
        int attempts_made=0;

        /* Grab host address and port number from configurations */
        if (req->mount->hostaddr!=0) address=req->mount->hostaddr;
        else address=cfg->hostaddr;

        if (req->mount->port!=0) port=req->mount->port;
        else if (cfg->port!=0) port=cfg->port;
        else port=jserv_ajpv12.port;

        do {
            /* Open socket */
            sock=ajpv12_open(cfg, r->pool, address, port);

            if (sock != -1) {
                /* if it worked, we're done */
                break;

            } else if (++attempts_made <= cfg->retryattempts) {
                /* if it failed, keep trying as many times as configured */
                ap_reset_timeout(r);          /* don't mix sleep & alarm */
                sleep(1);
                ap_hard_timeout("apjv11-open-retry", r);
            }
        } while (attempts_made <= cfg->retryattempts && sock == -1);

        if (sock==-1) {
            jserv_error(JSERV_LOG_EMERG,cfg,"ajp12: %s","connection fail");
            return SERVER_ERROR;
        }
    }
    ap_reset_timeout(r);

    /* Authenticate JServ connection */
    {
        long secretsize=0;
        char *secret=NULL;

        /* Grab secretsize from configurations */
        if (req->mount->secretsize!=JSERV_DISABLED) {
            secretsize=req->mount->secretsize;
            secret=req->mount->secret;
        } else {
            secretsize=cfg->secretsize;
            secret=cfg->secret;
        }

        /* Authenticate socket */
        ret=ajpv12_auth(cfg,r->pool, sock, secret, secretsize);
        if (ret==-1) {
            jserv_error(JSERV_LOG_EMERG,cfg,"ajp12: %s","auth fail");
            return SERVER_ERROR;
        }
    }
    ap_kill_timeout(r);

    /* Setup timeout for sending */
    ap_hard_timeout("ajpv12-send", r);

    /* We create a buffered socket for reading and writing */
    buffsocket=ap_bcreate(r->pool,B_SOCKET+B_RDWR);
    ap_bpushfd(buffsocket,sock,sock);


    ret = ( ajpv12_mark( buffsocket, 1) ||
	    ajpv12_sendstring( buffsocket, r->method) ||
	    ajpv12_sendstring( buffsocket, req->zone) ||
	    ajpv12_sendstring( buffsocket, req->servlet ) ||
	    ajpv12_sendstring( buffsocket, cfg->server->server_hostname ) );

    if (ret!=0) {
        jserv_error(JSERV_LOG_EMERG,cfg,"ajp12: Cannot initiate the request");
        THROW_EXCEPTION;
    }

    /* Require client to send Content-Length header */
    if ((ret = ap_setup_client_block(r, REQUEST_CHUNKED_DECHUNK))) return ret;

    ajpv12_sendstring( buffsocket, (const char *) ap_document_root(r));
    ajpv12_sendstring( buffsocket, r->path_info );
    if (r->path_info && r->path_info[0]) {
	/*
	 * To get PATH_TRANSLATED, treat PATH_INFO as a URI path.
	 * Need to re-escape it for this, since the entire URI was
	 * un-escaped before we determined where the PATH_INFO began.
	 */
	request_rec *pa_req;

	pa_req = ap_sub_req_lookup_uri(ap_escape_uri(r->pool, r->path_info), r);

	if (pa_req->filename) {
#ifdef WIN32
	    char buffer[HUGE_STRING_LEN];
#endif
	    char *pt = ap_pstrcat(r->pool, pa_req->filename, pa_req->path_info,
				  NULL);
#ifdef WIN32
	    /* We need to make this a real Windows path name */
	    GetFullPathName(pt, HUGE_STRING_LEN, buffer, NULL);
	    ajpv12_sendstring( buffsocket, ap_pstrdup(r->pool, buffer) );
#else
	    ajpv12_sendstring( buffsocket, pt);
#endif
	} else {
	    ajpv12_sendstring( buffsocket, 0);
	}
	ap_destroy_sub_req(pa_req);
    } else {
	    ajpv12_sendstring( buffsocket, 0);
    }

    ajpv12_sendstring( buffsocket, r->args );
    ajpv12_sendstring( buffsocket, r->connection->remote_ip);
    ajpv12_sendstring( buffsocket, ap_get_remote_host( r->connection, r->per_dir_config, REMOTE_HOST ));
    ajpv12_sendstring( buffsocket, r->connection->user);
    ajpv12_sendstring( buffsocket, r->connection->ap_auth_type);
    ajpv12_sendstring( buffsocket, ap_psprintf(r->pool, "%u", ap_get_server_port(r)));
    ajpv12_sendstring( buffsocket, r->method); 
    ajpv12_sendstring( buffsocket, original_uri(r) );
    ajpv12_sendstring( buffsocket, r->filename);

    /* SCRIPT_NAME calculation */
    if (!r->path_info || !*r->path_info) {
	ajpv12_sendstring( buffsocket, r->uri);
    } else {
	int path_info_start = ap_find_path_info(r->uri, r->path_info);
	ajpv12_sendstring( buffsocket, ap_pstrndup(r->pool, r->uri, path_info_start));
    }
    ajpv12_sendstring( buffsocket, (const char*) ap_get_server_name(r));
    ajpv12_sendstring( buffsocket, ap_psprintf(r->pool, "%u", ap_get_server_port(r)));
    ajpv12_sendstring( buffsocket, r->protocol);
    ajpv12_sendstring( buffsocket, ap_psignature("", r));
    ajpv12_sendstring( buffsocket, ap_get_server_version());

    /* Send routing info var */
    if (r->subprocess_env) {
        ajpv12_sendstring( buffsocket, ap_table_get(r->subprocess_env, "JSERV_ROUTE"));
    }
    else {
        ajpv12_sendstring( buffsocket, "");
    }
    /* these 2 lines are here for compatibility with Tomcat & older versions of */
    /* mod_jserv (JServ 1.1): the solution found by me (Jean-Luc) was bad, and  */
    /* is now replaced with the ApJServEnvVar parameter.                        */  

    ajpv12_sendstring( buffsocket, "");
    ajpv12_sendstring( buffsocket, "");

    /* Send the environment variables dynamically added in config file */
    /* see : ApJServEnvVar directive */
    /* we here use a new marker value = 5 for that */
    /* if cfg-envvars is empty nothing will be sent (no overhead) */
    /* if a value is null or empty it won't be sent */
    if (!ap_is_empty_table(cfg->envvars)) {
        int i;
        const char *value; 
        array_header *hdr_arr;
        table_entry *elts;
        ap_add_common_vars(r);
        hdr_arr = ap_table_elts(cfg->envvars);
        elts = (table_entry *) hdr_arr->elts;

        for (i = 0; i < hdr_arr->nelts; ++i) {
            if (!elts[i].key) continue;
	    value = ap_table_get(r->subprocess_env, elts[i].key);
            if (value && *value) {
/*              jserv_error(JSERV_LOG_DEBUG,cfg,"ajp12: env var %s %s %s",elts[i].key, elts[i].val, value ); */
	        ret = ( ajpv12_mark( buffsocket, 5) ||
		        ajpv12_sendstring( buffsocket, elts[i].val) ||
		        ajpv12_sendstring( buffsocket, value) );

                if ( ret != 0 ) {
                    jserv_error(JSERV_LOG_EMERG,cfg,"ajp12: cannot send env var");
                    THROW_EXCEPTION;
                }
            }
        }
    }  
  

    /* Send the request headers */
    if (r->headers_in) {
        array_header *hdr_arr = ap_table_elts(r->headers_in);
        table_entry *elts = (table_entry *) hdr_arr->elts;
        int i;

        for (i = 0; i < hdr_arr->nelts; ++i) {
            if (!elts[i].key) continue;
	    /* Was:
	       ret=ajpv12_sendpacket(cfg, r->pool, sock, 'H', elts[i].key,
	           elts[i].val);
	    */

	    ret = ( ajpv12_mark( buffsocket, 3) ||
		    ajpv12_sendstring( buffsocket, elts[i].key) ||
		    ajpv12_sendstring( buffsocket, elts[i].val) );

            if ( ret != 0 ) {
                jserv_error(JSERV_LOG_EMERG,cfg,"ajp12: cannot send headers");
                THROW_EXCEPTION;
            }

        }
    }
   
    ret =  ajpv12_mark( buffsocket, 4); /* End of headers */

    if ( ret != 0 ) {
	jserv_error(JSERV_LOG_EMERG,cfg,"ajp12: Cannot send end of headers marker");
	THROW_EXCEPTION;
    }


    /* Flush buffers and kill our writing timeout */
    ap_kill_timeout(r); /* This isn't in jserv CVS */


    /* If there is a request entity, send it */
    if (ap_should_client_block(r)) {
        char buffer[HUGE_STRING_LEN];
        long buffersize=1;

        /* If we did read something we'll post it to JServ */
        while ((buffersize=ap_get_client_block(r,buffer,HUGE_STRING_LEN))>0) {
            int rv;
            /* Reset our writing timeout */
            ap_reset_timeout(r);

            if ((rv= ajpv12_handle_in(cfg, req, r, &in_state,buffsocket))) {
                if( rv<0 || rv>=500) 
                    return rv;
            }

            /* Check that what we writed was the same of what we read */
            if (ap_bwrite(buffsocket,buffer,buffersize)<buffersize) {
                /* Discard all further characters left to read */
                while (ap_get_client_block(r, buffer, HUGE_STRING_LEN) > 0);
                break;
            }
        }
    }
    /* Flush buffers and kill our writing timeout */
    ap_bflush(buffsocket);
    ap_kill_timeout(r);

    /* Receive the response from JServ */
    ap_hard_timeout("ajpv12-read", r);
   
    /* We might have read it already, but not necessarily */
    if( in_state == 0) {
        if ((ret=ajpv12_pass_headers(cfg,req,r,buffsocket)))
            return ret;
    }
    if (!r->header_only) ap_send_fb(buffsocket, r);

    /* Kill timeouts, close buffer and socket and return */
    ap_kill_timeout(r);
    ap_bclose(buffsocket);
    ap_pclosesocket(r->pool,sock);
    return OK;
}


/* ========================================================================= */
/* Our function handler */
static int ajpv12_function(jserv_config *cfg, int function, char *data) {
    pool *p=ap_make_sub_pool(NULL);
    int sock,ret;
    char signal[2];
    
#ifdef WIN32
    signal[0] = (char) 254; /* a signal marker */
#else
    signal[0] = 254; /* a signal marker */
#endif
    
    if (function==JSERV_SHUTDOWN) {
        jserv_error(JSERV_LOG_INFO,cfg,"ajp12: %s",
                    "sending shutdown signal");
        signal[1]=15;
    }
    else if (function==JSERV_RESTART) {
        jserv_error(JSERV_LOG_INFO,cfg,"ajp12: %s",
                    "sending restart signal");
        signal[1]=1;
    }
    else if (function==JSERV_PING) {
      signal[1]=0;
    }
    else {
        jserv_error(JSERV_LOG_ERROR,cfg,"ajp12: %s",
                    "unknown function requested");
        ap_destroy_pool(p);
        return JSERV_FUNC_NOTIMPLEMENTED;
    }

    /* Check for correct config member */
    if (cfg==NULL) {
        jserv_error(JSERV_LOG_EMERG,cfg,"ajp12: %s",
                    "unknown configuration member for function");
        ap_destroy_pool(p);
        return JSERV_FUNC_ERROR;
    }

    /* Open connection to JServ */
    sock=ajpv12_open(cfg, p, cfg->hostaddr, cfg->port);
    if (sock==-1) {
        jserv_error(JSERV_LOG_EMERG,cfg,"ajp12: %s",
                    "function connection fail");
        ap_destroy_pool(p);
        return JSERV_FUNC_COMMERROR;
    }

    /* Authenticate socket */
    ret=ajpv12_auth(cfg, p, sock, cfg->secret, cfg->secretsize);
    if (ret==-1) {
        jserv_error(JSERV_LOG_EMERG,cfg,"ajp12: function auth fail");
        ap_destroy_pool(p);
        return JSERV_FUNC_ERROR;
    }

    /* Send the function request */
#ifdef WIN32
    ret = send( sock, signal, 2, 0 );
#else
    ret = write( sock, signal, 2);
#endif
    
    if (ret!=2) {
        jserv_error(JSERV_LOG_EMERG,cfg,"ajp12: cannot send function");
        ap_destroy_pool(p);
        return JSERV_FUNC_COMMERROR;
    }

    /* In case of PING wait for some reply */
    if( function==JSERV_PING ) {
	int pingret;
	pingret = read( sock, signal, 1);
	if( pingret != 1) {
	    jserv_error(JSERV_LOG_EMERG, cfg, "ajp12: ping: no reply (%d) \
            Please make sure that the wrapper.classpath is pointing \
            to the correct version of ApacheJServ.jar", \
            pingret);
	    ap_destroy_pool(p);
	    return JSERV_FUNC_COMMERROR;
	}
    }


    ap_destroy_pool(p);
    return(JSERV_FUNC_OK);
}

/*****************************************************************************
 * Ajpv12 Protocol Structure definition                                      *
 *****************************************************************************/
jserv_protocol jserv_ajpv12 = {
    "ajpv12",                   /* Name for this protocol */
    8007,                       /* Default port for this protocol */
    NULL,                       /* init() */
    NULL,                       /* cleanup() */
    NULL,                       /* child_init() */
    NULL,                       /* child_cleanup() */
    ajpv12_handler,             /* handler() */
    ajpv12_function,            /* function() */
    NULL,                       /* parameter() */
};

