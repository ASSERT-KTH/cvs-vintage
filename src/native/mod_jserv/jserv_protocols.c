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
 * Description: JServ protocol function handlers                             *
 * Author:      Pierpaolo Fumagalli <ianosh@iname.com>                       *
 * Version:     $Revision: 1.1 $                                            *
 *****************************************************************************/
#include "jserv.h"

/*****************************************************************************
 * List of currently supported protocols                                     *
 *****************************************************************************/
jserv_protocol *jserv_protocols[] = {
    &jserv_wrapper,
    &jserv_ajpv11,
    &jserv_ajpv12,
#ifdef LOAD_BALANCE
    &jserv_balancep,
#endif
/*    &jserv_dummy, */
    &jserv_status,
    NULL,
};


/*****************************************************************************
 * Code for protocols handling                                               *
 *****************************************************************************/

/* ========================================================================= */
/* Get protocol by name */
jserv_protocol *jserv_protocol_getbyname(const char *name) {
    jserv_protocol *cur=jserv_protocols[0];
    int x=0;

    /* If name was not supplied get default protocol */
    if (name==NULL) return jserv_protocol_getbyname(JSERV_DEFAULT_PROTOCOL);

    /* Iterate name thru list to see matches */
    while (cur!=NULL) {
        int ret;

        /* Do a case insensitive string match */
        ret=strcasecmp(cur->name,name);

        /* If we get a match return it */
        if (ret==0) return (cur);

        /* If we didn't get a match proceed to next protocol */
        x++;
        cur=jserv_protocols[x];
    }
    /* We did not get a protocol. Return NULL (weird)*/
    return NULL;
}

/* ========================================================================= */
/* Initialize all protocols */
int jserv_protocol_initall (jserv_config *cfg, int child) {
    jserv_protocol *cur=jserv_protocols[0];
    int x=0;

    /* Run thru all protocols */
    while (cur!=NULL) {
        int ret;

        /* Do our initialization */
        if (child) ret=jserv_protocol_child_init(cur, cfg);
        else ret=jserv_protocol_init(cur, cfg);

        if (ret==-1) {
            /* The protocol had an error */
            jserv_error(JSERV_LOG_ERROR, cfg,
                "in init all protocols \"%s\" returned an error", cur->name);
            return ret;
        }
        cur=jserv_protocols[++x];
    }
    return 0;
}

/* ========================================================================= */
/* Clean-Up all protocols */
int jserv_protocol_cleanupall (jserv_config *cfg, int child) {
    jserv_protocol *cur=jserv_protocols[0];
    int x=0;

    /* Run thru all protocols */
    while (cur!=NULL) {
        int ret;

        /* Do our initialization */
        if (child) ret=jserv_protocol_child_cleanup(cur, cfg);
        else ret=jserv_protocol_cleanup(cur, cfg);

        if (ret==-1) {
            /* The protocol had an error */
            jserv_error(JSERV_LOG_ERROR, cfg,
                "in init all protocols \"%s\" returned an error", cur->name);
            return ret;
        }
        cur=jserv_protocols[++x];
    }
    return 0;
}


/*****************************************************************************
 * Code for specifig protocol handling                                       *
 *****************************************************************************/

/* ========================================================================= */
/* Initialize a protocol */
int jserv_protocol_init (jserv_protocol *proto, jserv_config *cfg){
    /* Check if the protocol is valid */
    if (proto!=NULL) {
        if (proto->init!=NULL) {
            int ret=(proto->init)(cfg);
            /* Protocol had an error */
            if (ret==-1) jserv_error(JSERV_LOG_ERROR, cfg,
                    "an error returned initializing protocol \"%s\"",
                    proto->name);
            return ret;
        } else return 0;
    /* Null protocol specified */
    } else jserv_error(JSERV_LOG_ERROR, cfg,
            "protocol_init() with no protocol called");
    return -1;
}


/* ========================================================================= */
/* Clean-Up a protocol */
int jserv_protocol_cleanup (jserv_protocol *proto, jserv_config *cfg){
    /* Check if the protocol is valid */
    if (proto!=NULL) {
        if (proto->cleanup!=NULL) {
            int ret=(proto->cleanup)(cfg);
            /* Protocol had an error */
            if (ret==-1) jserv_error(JSERV_LOG_ERROR, cfg,
                    "an error returned cleaning-up protocol \"%s\"",
                    proto->name);
            return ret;
        } else return 0;
    /* Null protocol specified */
    } else jserv_error(JSERV_LOG_ERROR, cfg,
            "protocol_cleanup() with no protocol called");
    return -1;
}

/* ========================================================================= */
/* Initialize a protocol for an apache child starting */
int jserv_protocol_child_init (jserv_protocol *proto, jserv_config *cfg){
    /* Check if the protocol is valid */
    if (proto!=NULL) {
        if (proto->child_init!=NULL) {
            int ret=(proto->child_init)(cfg);
            /* Protocol had an error */
            if (ret==-1) jserv_error(JSERV_LOG_ERROR, cfg,
                    "an error returned initializing protocol \"%s\" %s",
                    proto->name, "for an Apache child starting");
            return ret;
        } else return 0;
    /* Null protocol specified */
    } else jserv_error(JSERV_LOG_ERROR, cfg,
            "protocol_child_init() with no protocol called");
    return -1;
}

/* ========================================================================= */
/*  Clean-Up a protocol for an apache child exiting */
int jserv_protocol_child_cleanup (jserv_protocol *proto, jserv_config *cfg){
    /* Check if the protocol is valid */
    if (proto!=NULL) {
        if (proto->child_cleanup!=NULL) {
            int ret=(proto->child_cleanup)(cfg);
            /* Protocol had an error */
            if (ret==-1) jserv_error(JSERV_LOG_ERROR, cfg,
                    "an error returned cleaning-up protocol \"%s\" %s",
                    proto->name, "for an Apache child dying");
            return ret;
        } else return 0;
    /* Null protocol specified */
    } else jserv_error(JSERV_LOG_ERROR, cfg,
            "protocol_child_cleanup() with no protocol called");
    return -1;
}

/* ========================================================================= */
/* Handle current request */
int jserv_protocol_handler(jserv_protocol *proto, jserv_config *cfg,
                           jserv_request *req, request_rec *r) {
    /* Check if the protocol is valid */
    if (proto!=NULL) {
        if (proto->handler!=NULL) {
            int ret=(proto->handler)(cfg,req,r);
            if (ret==HTTP_INTERNAL_SERVER_ERROR)
                /* Protocol had an error */
                jserv_error(JSERV_LOG_ERROR, cfg,
                    "an error returned handling request via protocol \"%s\"",
                    proto->name);
            return ret;
        } else {
            jserv_error(JSERV_LOG_ERROR, cfg,
                "a request was passed to protocol \"%s\" wich does not %s",
                proto->name,"implement a request handler");
            return HTTP_INTERNAL_SERVER_ERROR;
        }
    /* Null protocol specified */
    } else jserv_error(JSERV_LOG_ERROR,  cfg,
            "protocol_handled() with no protocol called");
    return HTTP_INTERNAL_SERVER_ERROR;
}

/* ========================================================================= */
/* Handle function request */
int jserv_protocol_function (jserv_protocol *proto, jserv_config *cfg,
                             int function, char *data) {
    return (proto->function) (cfg,function,data);
}

/* ========================================================================= */
/* Notify a parameter configuration */
const char *jserv_protocol_parameter (jserv_protocol *proto, jserv_config *cfg,
                                      char *name, char *value) {
    /* Check if the protocol is valid */
    if (proto!=NULL) {
        if (proto->parameter!=NULL)
            return(proto->parameter)(cfg, name, value);
        else return "protocol specified is not accepting parameters";
    } else return "protocol: notify parameter with no protocol called";
}

