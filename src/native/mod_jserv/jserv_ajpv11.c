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
 * Description: ajpv1.1 protocol, used to call local or remote jserv hosts   *
 * Author:      Pierpaolo Fumagalli <ianosh@iname.com>                       *
 * Version:     $Revision: 1.2 $                                             *
 *****************************************************************************/
#include "jserv.h"
#ifdef CHARSET_EBCDIC
#include "ebcdic.h"
#endif

/*****************************************************************************
 * Code for ajpv11 protocol                                                   *
 *****************************************************************************/

/* ========================================================================= */
/* Open a socket to JServ host */
/* FIXME: short port gets overflowed - it's not unsigned */
static int ajpv11_open(jserv_config *cfg, pool *p, unsigned long address,
                       unsigned short port) {
    struct sockaddr_in addr;
    int sock;
    int ret;

    /* Check if we have a valid host address */
    if (address==0) {
        jserv_error(JSERV_LOG_EMERG,cfg,"ajp11: %s",
                    "cannot connect to unspecified host");
        return -1;
    }

    /* Check if we have a valid port number. */
    if (port < 1024) {
        jserv_error(JSERV_LOG_INFO,cfg,"ajp11: %d: %s",
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
        jserv_error(JSERV_LOG_EMERG,cfg,"ajp11: %s","can not open socket");
        return -1;
    }
    jserv_error(JSERV_LOG_DEBUG,cfg,"ajp11: opening port %d",port);

    /* Tries to connect to JServ (continues trying while error is EINTR) */
    do {
        ret=connect(sock,(struct sockaddr *)&addr,sizeof(struct sockaddr_in));
#ifdef WIN32
        if (ret==SOCKET_ERROR) errno=WSAGetLastError()-WSABASEERR;
#endif /* WIN32 */
    } while (ret==-1 && errno==EINTR);

    /* Check if we connected */
    if (ret==-1) {
        jserv_error(JSERV_LOG_EMERG,cfg,"ajp11: %s %s:%d",
                    "can not connect to host",
                    inet_ntoa(addr.sin_addr),
                    port);
        return -1;
    }

    /* Return the socket number */
    return sock;
}

/* ========================================================================= */
/* Send and AJPv1.1 packet */
static int ajpv11_sendpacket(jserv_config *cfg, pool *p, int sock,
                             const char type, char *name, char *value) {
    char *buffer;
    char *message;
    int i,bufferlen;

    /* Handle our end properly */
    if (type=='\0') {
#ifndef CHARSET_EBCDIC
        i=send(sock,"0000",4,0);
#else
	i=send(sock,"\x30\x30\x30\x30",4,0);
#endif
        if (i==4) return 0;
        else return -1; /* Was return -1 in tomcat CVS */
    }

    /* Format message */
    if (name==NULL) {
        if (value==NULL) message=ap_psprintf(p,"%c", type);
        else message=ap_psprintf(p,"%c%s", type, value);
    } else { 
        if (value==NULL) message=ap_psprintf(p,"%c%s\t", type, name);
        else message=ap_psprintf(p,"%c%s\t%s", type, name, value);
    }

    /* Print data into buffer and evaluate packet length */
    buffer=ap_psprintf(p,"%04x%s",strlen(message),message);

    /* Send data to peer */
    bufferlen=strlen(buffer);
#ifdef CHARSET_EBCDIC
    ebcdic2ascii(buffer,buffer,bufferlen);
#endif

    i=send(sock,buffer,bufferlen,0);
    if (i!=bufferlen) return -1;
    return 0;
}

/* ========================================================================= */
/* Authenticate a socket for JServ operation */
static int ajpv11_auth(jserv_config *cfg, pool *p, int sock, char *secret,
                       long secretsize) {
    AP_MD5_CTX md5context;
    long challengesize;
    unsigned char hash[16];
    unsigned char *challenge;
    unsigned char *tmp;
    int ret;


    /* Check if we had an auth, otherwise we suppose authentication disabled */
    if (secretsize==JSERV_DISABLED) {
        jserv_error(JSERV_LOG_DEBUG,cfg,"ajp11: %s", "auth is disabled");
        return 0;
    }

    /* Check if we had an auth, otherwise we suppose authentication disabled */
    if (secret==NULL) {
        jserv_error(JSERV_LOG_ERROR,cfg,"ajp11: %s",
                    "auth is disabled (size was not disabled, but pass was)");
        return 0;
    }

    /* Receive the size of challenge string. Cast a long int to char buffer */
    ret=recv(sock, (char *)(&challengesize), 4,0);
    if (ret!=4) {
        jserv_error(JSERV_LOG_EMERG,cfg,"ajp11: %s",
                    "auth did not receive challenge size");
        return -1;
    }
    challengesize=ntohl(challengesize);

    /* Allocate space for challenge and receive the string */
    challenge=(unsigned char *)ap_pcalloc(p,challengesize+secretsize+1);
    ret=recv(sock, challenge, challengesize,0);
    if (ret!=challengesize) {
        jserv_error(JSERV_LOG_EMERG,cfg,"ajp11: %s",
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
        jserv_error(JSERV_LOG_EMERG,cfg,"ajp11: %s",
                    "can not send the md5 hashed auth");
        return -1;
    }

    /* If we did not return a valid confirmation, socket was closed*/
    return 0;
}

/* ========================================================================= */
/* Our request handler */
static int ajpv11_handler(jserv_config *cfg, jserv_request *req, 
                          request_rec *r) {
    int ret, sock;
    BUFF *buffsocket;
    const char *header;

    /* Check for correct config member */
    if (cfg==NULL) {
        jserv_error(JSERV_LOG_EMERG,cfg,"ajp11: %s",
                    "unknown configuration member for request");
        return SERVER_ERROR;
    }

    /* Check for correct jserv request member */
    if (req==NULL) {
        jserv_error(JSERV_LOG_EMERG,cfg,"ajp11: %s",
                    "null request not handled");
        return SERVER_ERROR;
    }
    if (req->mount==NULL) {
        jserv_error(JSERV_LOG_EMERG,cfg,"ajp11: %s",
                    "unknown mount for request");
        return SERVER_ERROR;
    }

    /* Open connection to JServ */
    ap_hard_timeout("ajpv11-open", r);
    {
        unsigned long address=0;
        unsigned short port=0;
        int attempts_made=0;

        /* Grab host address and port number from configurations */
        if (req->mount->hostaddr!=0) address=req->mount->hostaddr;
        else address=cfg->hostaddr;

        if (req->mount->port!=0) port=req->mount->port;
        else if (cfg->port!=0) port=cfg->port;
        else port=jserv_ajpv11.port;

        do {
            /* Open socket */
            sock=ajpv11_open(cfg, r->pool, address, port);

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
            jserv_error(JSERV_LOG_EMERG,cfg,"ajp11: %s","connection fail");
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
        ret=ajpv11_auth(cfg,r->pool, sock, secret, secretsize);
        if (ret==-1) {
            jserv_error(JSERV_LOG_EMERG,cfg,"ajp11: %s","auth fail");
            return SERVER_ERROR;
        }
    }
    ap_kill_timeout(r);

    /* Setup timeout for sending */
    ap_hard_timeout("ajpv11-send", r);

    /* Send the servlet zone and name we got from request matcher */
    ap_reset_timeout(r);
    if (req->zone==NULL) {
        ret=ajpv11_sendpacket(cfg, r->pool, sock, 'C', "", req->servlet);
    } else {
        ret=ajpv11_sendpacket(cfg, r->pool, sock, 'C', req->zone, req->servlet);
    }
    if (ret!=0) {
        ap_kill_timeout(r);
        jserv_error(JSERV_LOG_EMERG,cfg,"ajp11: %s","cannot send class name");
        return SERVER_ERROR;
    }

    /* Send the host name */
    ap_reset_timeout(r);
    ret=ajpv11_sendpacket(cfg, r->pool, sock, 'S',
                          NULL,cfg->server->server_hostname);
    if (ret!=0) {
        ap_kill_timeout(r);
        jserv_error(JSERV_LOG_EMERG,cfg,"ajp11: %s","cannot send server name");
        return SERVER_ERROR;
    }

    /* Set vars and environment in request_rec */
    ap_add_cgi_vars(r);
    ap_add_common_vars(r);

    /* Require client to send Content-Length header */
    if ((ret = ap_setup_client_block(r, REQUEST_CHUNKED_ERROR))) return ret;
    ap_reset_timeout(r);

    /* Send environment vars, except for headers (beginning with HTTP) */
    if (r->subprocess_env) {
        array_header *env_arr = ap_table_elts(r->subprocess_env);
        table_entry *elts = (table_entry *) env_arr->elts;
        int i;

        for (i = 0; i < env_arr->nelts; ++i) {
            if (!elts[i].key) continue;
            if (!strncmp(elts[i].key, "HTTP_", 5)) continue;
            ret=ajpv11_sendpacket(cfg, r->pool, sock, 'E', elts[i].key,
                                  elts[i].val);
            if (ret!=0) {
                ap_kill_timeout(r);
                jserv_error(JSERV_LOG_EMERG,cfg,"ajp11: %s",
                            "cannot send environment");
                return SERVER_ERROR;
            }
            ap_reset_timeout(r);
        }
    }

    /* Send the request headers */
    if (r->headers_in) {
        array_header *hdr_arr = ap_table_elts(r->headers_in);
        table_entry *elts = (table_entry *) hdr_arr->elts;
        int i;

        for (i = 0; i < hdr_arr->nelts; ++i) {
            if (!elts[i].key) continue;
            ret=ajpv11_sendpacket(cfg, r->pool, sock, 'H', elts[i].key,
                                  elts[i].val);
            if (ret!=0) {
                ap_kill_timeout(r);
                jserv_error(JSERV_LOG_EMERG,cfg,"ajp11: %s",
                            "cannot send headers");
                return SERVER_ERROR;
            }
            ap_reset_timeout(r);
        }
    }

    /* Send the terminating entry */
    ret=ajpv11_sendpacket(cfg, r->pool, sock, '\0', NULL, NULL);
    if (ret!=0) {
        ap_kill_timeout(r);
        jserv_error(JSERV_LOG_EMERG,cfg,"ajp11: %s",
                    "cannot send termination packet");
        return SERVER_ERROR;
    }
    ap_reset_timeout(r);

    /* We create a buffered socket for reading and writing */
    buffsocket=ap_bcreate(r->pool,B_SOCKET+B_RDWR);
    ap_bpushfd(buffsocket,sock,sock);

    /* If there is a request entity, send it */
    if (ap_should_client_block(r)) {
        char buffer[HUGE_STRING_LEN];
        long buffersize=1;

        /* If we did read something we'll post it to JServ */
        while ((buffersize=ap_get_client_block(r,buffer,HUGE_STRING_LEN))>0) {
            /* Reset our writing timeout */
            ap_reset_timeout(r);
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
    ap_hard_timeout("ajpv11-read", r);
    if ((ret=ap_scan_script_header_err_buff(r,buffsocket,NULL))) {
        ap_kill_timeout(r);

        if( ret>=500 || ret < 0)
          jserv_error(JSERV_LOG_EMERG,cfg,"ajp11: %s (%d)",
                    "cannot scan servlet headers", ret);

        r->status_line = NULL;

        return ret;
    }

    /* Check for our Error headers */
    if ((header=ap_table_get(r->err_headers_out, "Servlet-Error"))) {
        /* Backup the original status */
        int status=r->status;

        /* Close buffer and kill timeouts */
        ap_bclose(buffsocket);
        ap_kill_timeout(r);

        /* Log error to Apache logfile */
        jserv_error(JSERV_LOG_ERROR,cfg,"ajp11: Servlet Error: %s",header);

        /* Remove headers and return */
        ap_table_unset(r->err_headers_out, "Servlet-Error");
      /*  r->status = HTTP_OK;*/
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
    if (!r->header_only) ap_send_fb(buffsocket, r);

    /* Kill timeouts, close buffer and socket and return */
    ap_kill_timeout(r);
    ap_bclose(buffsocket);
    ap_pclosesocket(r->pool,sock);
    return OK;
}

/* ========================================================================= */
/* Our function handler */
static int ajpv11_function(jserv_config *cfg, int function, char *data) {
    pool *p=ap_make_sub_pool(NULL);
    int sock,ret;
    char *signal;

    if (function==JSERV_SHUTDOWN) {
        jserv_error(JSERV_LOG_INFO,cfg,"ajp11: %s",
                    "sending shutdown signal");
        signal="15";
    }
    else if (function==JSERV_RESTART) {
        jserv_error(JSERV_LOG_INFO,cfg,"ajp11: %s",
                    "sending restart signal");
        signal="01";
    }
    else {
        jserv_error(JSERV_LOG_ERROR,cfg,"ajp11: %s",
                    "unknown function requested");
        ap_destroy_pool(p);
        return JSERV_FUNC_NOTIMPLEMENTED;
    }

    /* Check for correct config member */
    if (cfg==NULL) {
        jserv_error(JSERV_LOG_EMERG,cfg,"ajp11: %s",
                    "unknown configuration member for function");
        ap_destroy_pool(p);
        return JSERV_FUNC_ERROR;
    }

    /* Open connection to JServ */
    sock=ajpv11_open(cfg, p, cfg->hostaddr, cfg->port);
    if (sock==-1) {
        jserv_error(JSERV_LOG_EMERG,cfg,"ajp11: %s",
                    "function connection fail");
        ap_destroy_pool(p);
        return JSERV_FUNC_COMMERROR;
    }

    /* Authenticate socket */
    ret=ajpv11_auth(cfg, p, sock, cfg->secret, cfg->secretsize);
    if (ret==-1) {
        jserv_error(JSERV_LOG_EMERG,cfg,"ajp11: %s","function auth fail");
        ap_destroy_pool(p);
        return JSERV_FUNC_COMMERROR;
    }

    /* Send the function request */
    ret=ajpv11_sendpacket(cfg, p, sock, 's', NULL, signal);
    if (ret!=0) {
        jserv_error(JSERV_LOG_EMERG,cfg,"ajp11: %s","cannot send function");
        ap_destroy_pool(p);
        return JSERV_FUNC_COMMERROR;
    }

    /* Send the terminating entry */
    ret=ajpv11_sendpacket(cfg, p, sock, '\0', NULL, NULL);

    ap_destroy_pool(p);
    return(0);
}

/*****************************************************************************
 * AJPv11 Protocol Structure definition                                      *
 *****************************************************************************/
jserv_protocol jserv_ajpv11 = {
    "ajpv11",                   /* Name for this protocol */
    8007,                       /* Default port for this protocol */
    NULL,                       /* init() */
    NULL,                       /* cleanup() */
    NULL,                       /* child_init() */
    NULL,                       /* child_cleanup() */
    ajpv11_handler,             /* handler() */
    ajpv11_function,            /* function() */
    NULL,                       /* parameter() */
};

