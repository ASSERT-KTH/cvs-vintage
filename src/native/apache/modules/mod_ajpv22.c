/* Deprecated, this is the first implementation that worked, see 
   mod_ajp22-X.c 
*/
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

#include "httpd.h"
#include "http_config.h"
#include "http_log.h"
#include "http_main.h"
#include "http_protocol.h"
#include "http_request.h"
#include "util_script.h"
#include "util_md5.h"

/* XXX
   The protocol is not final - I just want to have something the run,
   to get a feeling of how it works.

   Next thing must be to do test variations of the protocol, that will minimize the 
   overhead. You can add to this file ( if it is an improvement to the current protocol)
   or create a new mod_xxx.c and xxxConnectionHandler if it's a new protocol.
*/

/** XXX sending the body is not implemented yet, post will not work 
 */

/** 
    XXX Add high level description
    XXX Add configuration documentation
*/

module ajpv22_module;

/** If we'll abstract the communication protocol from Stream protocol
( or even Method invocation / Wire protocol / Stream ), the best place to
do that would be ( IMHO ) at the module_config level.

Inside Apache, each module has a module_config structure containing private
informations about the module. This C structure can be public, and 
it can also include pointers to various functions - sort of a C++ class or 
Java interface.
*/
#define DEFAULT_PORT "8007"
#define DEFAULT_HOST "localhost"

#define MAX_HOSTS 20

typedef struct RemoteHost RemoteHost;
typedef struct Ajpv22Connection Ajpv22Connection;

typedef struct {
    /** XXX not used */
    void *(*startSession)( request_rec *req, char *host );
    int  (*endSession)( void *session );

    /* XXX What else? */

    /* "public" fields, in all protocol modules */
    int debug;
    pool *config_pool;
    server_rec *server_rec;

    /* Private fileds */
    int hostc;
    RemoteHost *hosts[MAX_HOSTS]; 
    /* XXX Use a Hashtable - Apache table support only string values, but it 
       would be nice to have a generic version instead of the array
    */
} Ajp22Module;

/**
   XXX Add implementation description
*/

/* Implementation specific data structures */

/* An open TCP connection to a host
   Used by startSession, endSession, etc 
*/
struct Ajpv22Connection {
    pool *pool;
    int socket; /* XXX sock_t */
    BUFF *bsocket;
    RemoteHost *host;
};

/* Informations about a remote TCP host
 */
struct RemoteHost {
    pool *pool;
    char *name;
    char *host;
    int port;
    struct hostent *hinfo; /* in pool */
    struct sockaddr_in addr;
    Ajpv22Connection *connection;
};

/* The default size of a buffer
   XXX make it configurable
 */
#define DEFAULT_SIZE 0x1000

typedef struct {
    char head[8];

    int buffSize;
    int msglen;
    char *message;

    /* XXX redundant */
    int type;
} ServerRequest;


/* XXX Use APR when ready */
/* XXX error handling? */
/* XXX BUG: if a connection is broken, next request will fail ( and a new connection
   will be create on the second )
*/

/* ---------------------------------------- */
/* XXX Add an initial packet to server - auth, etc */

/* Remote Host connection handling 
   If one connection/request - optimize it
   If connection reuse - everything is ok.
 */
static void *startSession( RemoteHost *rhost, char *host ) {
    Ajpv22Connection *conn;
    pool *p;
    int err;
    int fd_flags;

    /*  alloc host in main pool
    // XXX pass it as parameter or configuration 
    // XXX BUG: need to  create sub-pool, and free it on close
    */
    p=rhost->pool; 

    /* check if we have an open connection */
    if( rhost->connection != NULL ) {
	return rhost->connection;
    }

    /* create a new connection
    // XXX use WARN or lower level
    */
    ap_log_error( APLOG_MARK, APLOG_EMERG, NULL,
		  "Open connection to %s", host);
    conn=(Ajpv22Connection *)ap_palloc( p, sizeof ( Ajpv22Connection ));
    conn->pool=p;
    conn->host=rhost;
    conn->bsocket= ap_bcreate( p, B_SOCKET | B_RDWR );
    if( conn->bsocket == NULL ) {
	/* XXX  clean the subpool */
	return NULL;
    }

    conn->socket=  ap_psocket(conn->pool, AF_INET, SOCK_STREAM, IPPROTO_TCP);
    
    err=connect(conn->socket, &rhost->addr ,sizeof(struct sockaddr_in));
    if( err==-1 ) {
	ap_log_error( APLOG_MARK, APLOG_EMERG, NULL,
			  "Error connecting %d\n", err);
	return NULL; 
	/* XXX clean subpool
	// XXX add WIN stuff
	*/
    }
    
    /* XXX add TCP_NODELAY sfuff */

    ap_bpushfd( conn->bsocket, conn->socket, conn->socket );
    
    fd_flags = fcntl(conn->socket, F_GETFL, 0);
    fd_flags &= ~O_NONBLOCK;
    fcntl(conn->socket, F_SETFL, fd_flags);

    rhost->connection=conn; 
    return (void *)conn;
}

static int endSession( void *conn ) {
    Ajpv22Connection *aconn=(Ajpv22Connection *)conn;

    ap_log_error( APLOG_MARK, APLOG_EMERG, NULL,
		  "Closing connection");

    if(aconn==NULL) return 0;

    if( aconn->bsocket!=NULL ) {
	ap_bclose( aconn->bsocket );
	ap_pclosesocket( aconn->pool, aconn->socket );
    }

    aconn->bsocket=NULL;
    aconn->socket=0;
    aconn->host->connection=NULL;
    return 0;
}


static int flush( void *ses ) {
    BUFF *bsock=((Ajpv22Connection *)ses)->bsocket;
    ap_bflush( bsock );
    return 0;
}

/** Read a full buffer, deal with partial reads
    XXX rewrite it - we should read as much as possible 
    and then interpret it and read more if needed, right now there
    are at least 2 reads per request 
 */
static int read_full( void *ses, char *buff, int msglen ) {
    int rdlen=0;
    int i;

    while( rdlen < msglen ) {
	i=read( ((Ajpv22Connection *)ses)->socket, buff + rdlen, msglen - rdlen );
	/* 	printf( "Read: %d %d %x %x %x\n", i, rdlen, i, rdlen, msglen ); */
	
	if(i==-1) {
	    if(errno==EAGAIN) {
		ap_log_error( APLOG_MARK, APLOG_EMERG, NULL,
			      "EAGAIN, continue reading");
	    } 
	    else {
		ap_log_error( APLOG_MARK, APLOG_EMERG, NULL,
			      "ERRNO=%s", strerror(errno));
		/* 		//		closeConnection( ses ); */
		return -6;
	    }
	}
	if(i==0) return -7;
	rdlen += i;
    }
    return 0;
}


/* -------------------- Marshaling -------------------- */
/* XXX optimize it ( less function calls, macros )
 */

/* XXX replace send_X with add_X - and use local buffer
   instead of BUFF since we'll have more control.
   XXX check writev!!!
*/

/** Send an int, handle swap to Java order
 */
static int send_I( void *ses, unsigned int val ) {
    BUFF *bsock=((Ajpv22Connection *)ses)->bsocket;
    int sent;
    unsigned char bytes[2];
    /* XXX optimize */

    bytes[0] = (unsigned char) ( (val >> 8) & 0xff );
    bytes[1] = (unsigned char) ( val & 0xff );

    if( bsock==NULL) {
	/* 	//	printf("Closed connection\n"); */
	return -2;
    }
    sent=ap_bwrite( bsock, bytes , 2);
    
    /*     swap( & val ); */
    /* sent=ap_bwrite( bsock, &val , 2); */

    return (sent==2)? sent : -1;
}

static int send_S( void *ses, char *param ) {
    BUFF *bsock=((Ajpv22Connection *)ses)->bsocket;
    int len, total_len;
    int sent;

    if( param==NULL ) {
	send_I( ses, 0xFFFF );
	return 0; 
    }
    len=strlen(param);
    
    if( bsock==NULL) {
/* 	//	printf("Closed connection\n"); */
	return -2;
    }
    sent=send_I( ses, len );
    if( sent<0) {
/* 	//	printf("Error in sendI %d\n", sent); */
	return sent;
    }

/*     //    printf("Sending %s\n", param); */
    sent=ap_bwrite( bsock, param, len+1 ); /* // including \0 */
    if( sent != len +1 ) return -1;
    return sent + 2;
}

/* // Ugly pointer arithmetic code */
static int get_I( char *msg, int *pos) {
    int i= ((msg[*pos]&0xff)<<8)  + (msg[(*pos) + 1] & 0xFF);
    *pos = (*pos) + 2;
    return i;
}

static char *get_S( char *msg, int *pos) {
    int size=get_I(msg, pos);
    int start=*pos;
    *pos += size;
    *pos += 1;
    return msg + start; 
}

/** Send a table ( used for Headers or Env )
 */
static int sendTable( void *ses, table *tbl) {
    array_header *env_arr = ap_table_elts(tbl);
    
    table_entry *elts = (table_entry *) env_arr->elts;
    int i, err;

    err=send_I( ses, env_arr->nelts);
    if(err<0) return err;

    for (i = 0; i < env_arr->nelts; ++i) {
/* 	//if (!elts[i].key) continue; */
/* 	// XXX do not send headers as environment variables */
/* 	//if ( type== ENV && !strncmp(elts[i].key, "HTTP_", 5)) continue; */
	
	err=send_S( ses, elts[i].key );
	if (err<0)  return err;

	err=send_S( ses, elts[i].val );
	if (err<0)  return err;
    }
    return 0;
}

/** Find how many bytes we need to send a table
 */
static int table_size( table *tbl ) {
    int size=0;
    int i;

    array_header *env_arr = ap_table_elts(tbl);
    
    table_entry *elts = (table_entry *) env_arr->elts;

    size+=2; 

    for (i = 0; i < env_arr->nelts; ++i) {
	if (!elts[i].key) continue;
	size += strlen( elts[i].key );
	size += strlen( elts[i].val );
    }
    size+=6*env_arr->nelts; /* 2 byte for each string length + 1 ending 0 */
    return size;
}


/* ---------------------------------------- */

/* // XXX replace all return values with error codes */
#define ERR_BAD_PACKET -5

/** 
    Read a callback 
 */
static int getRequest( void *ses, ServerRequest *sreq ) {
    int rdlen;
    int i;
    int pos;
    int msglen;
    int off;
    char *message;
    int *imessage;

    Ajpv22Connection *ajpses=(Ajpv22Connection *)ses;
    pool *p=ajpses->pool;

    /*     // mark[2] + len[2] + code[2] */
    i=read_full( ajpses, sreq->head, 6 );
    /*     printf( "XXX %d \n" , i ) ;  */
    if(i<0) return i;
    
/*     printf("Dump: %x %x %x %x %x %x\n", sreq->head[0],sreq->head[1],sreq->head[2], */
/* 	   sreq->head[3],sreq->head[4],sreq->head[5]); */
   

    if( (sreq->head[0] != 'A') || (sreq->head[1] != 'B' )) {
	return ERR_BAD_PACKET ;
    }

    pos=2;
    sreq->msglen=get_I( sreq->head, &pos );
    sreq->type= get_I( sreq->head, &pos );

    /*     //    printf( "Packet len %d %x\n", sreq->msglen, sreq->msglen ); */

    if( sreq->msglen == 2 ) {
	/* 	//	printf("Void callback"); */
	return 0; 
    }

    if(sreq->msglen > sreq->buffSize) {
/* 	// XXX reuse buff, create a list ? Does it worth it?  */
	sreq->message=(char *)ap_palloc( p, sreq->msglen );
	ap_log_error( APLOG_MARK, APLOG_EMERG, NULL,
		      "Re-alocating msg buffer, %d %d\n", sreq->buffSize, sreq->msglen);
	sreq->buffSize = sreq->msglen;
    }
    
    i=read_full(ajpses, sreq->message, sreq->msglen -2 ); 
    if( i<0) return i;
    
/*     //    printf( "Message: %d\n" , (int)sreq->type ); */
    return 0;
}

/* -------------------- High level methods -------------------- */

/* // XXX REMOVE, put in a separate module */

/** Method codes */
/* XXX all method codes should start with 0xFF,
   For methods not starting with 0xFF type is a length, and 
   the method _name_ follows 
*/
#define SET_HEADERS 2
#define SEND_BODY_CHUNK 3
#define SEND_HEADERS 4
#define REQUEST_FORWARD 0xff01
#define END_RESPONSE 5
#define GET_BODY_CHUNK 6


/** SetHeaders callback - all headers are added to headers->out, no 
    more parsing 
*/
static void setHeaders( ServerRequest *sreq, request_rec *r) {
    char *msg=sreq->message;
    int i;
    int count;
    int pos=0; 

    count= get_I( msg, &pos );
/*     //    printf( "Header count: %x %x %x %x\n", count, pos, (int)msg[2], (int)msg[3] ); */
    for( i=0; i< count; i++ ) {
	char *n=get_S( msg, &pos );
	char *v=get_S( msg, &pos );
	ap_table_add( r->headers_out, n, v);
/* 	//	printf( "Setting header: %s=%s\n", n , v ); */
	if( 0==strcmp(n,"Content-Type") ) {
/* 	    // XXX Todo Content-encoding!!! */
/* 	    // XXX special function to send "special" headers */
/* 	    //	    printf("Setting content-type %s\n", v); */
	    r->content_type=v;
	}
	if( 0==strcmp(n, "Status")) {
/* 	    //	    printf("Setting status %s\n", v); */
	    r->status=atoi(v);
	}
    }
}

/** Callback demux 
    Small methods inlined
    XXX add support for user-defined methods
 */
static void process_callback( void *sess, ServerRequest *sreq, request_rec *r) {
    int len;
    char *msg=sreq->message;
    int pos=0;

/*     //printf("Callback %x\n", (int)sreq->type); */
    switch( sreq->type ) {
    case SET_HEADERS:
	setHeaders( sreq , r);
	ap_send_http_header(r);
	break;
    case SEND_HEADERS:
	ap_send_http_header(r);
	break;
    case SEND_BODY_CHUNK:
	len=get_I( msg, &pos );
	ap_rwrite( msg+2, len, r);
	break;
    case GET_BODY_CHUNK:
	break;
    case END_RESPONSE:
	break;
    default:
	ap_log_error( APLOG_MARK, APLOG_EMERG, NULL,
		      "Invalid code: %d\n", sreq->type);
	sreq->type=END_RESPONSE;
    }
    
}

/** Forward request info to the remote engine.
    XXX do not forward everything, only what's used
    XXX configure what is sent per request, fine tune
    XXX integrate with mod_session (notes)
    XXX integrate with special module to find the context (notes)
*/
static int send_request( void *ses, request_rec *r ) {
    int size=0;
    table *t;
    int err;
    
/*     //    ap_table_add( r->subprocess_env, "CONTEXT", "default" ); */
/*     //    ap_table_add( r->subprocess_env, "SERVLET", "SnoopServlet" ); */
    
    size += 2; 
/*     // ???   size += strlen( "handleRequest" ) + 2; */
    size +=table_size( r->subprocess_env );
    size +=table_size( r->headers_in );
    
/*     // XXX on error, close connection */
/*     // XXX rewrite using buffer,  */
/*     //     printf("Sending %d %x %d %d\n", size, size,  */
/*     //              table_size(r->subprocess_env), table_size(r->headers_in)); */

    err=send_I( ses, 0x1234 );
    if(err<0) return err;
    err=send_I( ses, size ); 
    if(err<0) return err;
    err=send_I( ses, REQUEST_FORWARD ); 
    if(err<0) return err;
    err=sendTable( ses, r->subprocess_env );
    if(err<0) return err;
    err=sendTable( ses, r->headers_in);
    if(err<0) return err;
    return 0;
}


/* ---------------------------------------- */

/** XXX Clean up, move it to a separate module.
    This module will handle only the transport protocol. 
    Request Forwarding ( and what/how to forward ) should be
    in a separate module. 
 */
static int test_handler(request_rec *r) {
    ServerRequest sreq;
    void *sess;
    int err;
    Ajp22Module *rpm;
    RemoteHost *rhost;

    rpm=(Ajp22Module *)ap_get_module_config(r->server->module_config,
						     &ajpv22_module);

    rhost=rpm->hosts[0];

    sreq.message=(char *)ap_palloc( r->pool, DEFAULT_SIZE );
    sreq.buffSize=DEFAULT_SIZE;

    ap_add_cgi_vars(r);
    ap_add_common_vars(r);

    sess=startSession( rhost, "default" );
    if( sess==NULL) return NOT_FOUND;

    err=send_request( sess , r);
    if(err<0) {
	ap_log_error( APLOG_MARK, APLOG_EMERG, NULL,
			  "Error sending request  %d\n", err);
/* 	// try again, maybe the socket is invalid */
	endSession(sess);
	if( sess==NULL) return NOT_FOUND;
	
	err=send_request( sess , r);
	if( err < 0 ) return NOT_FOUND;

    }
/*     // XXX check for error, try to start a session if error */
/*     // XXX BUG - does not try to restart */

    flush( sess );
/*     //    printf("Sent request, wait for callbacks\n"); */

    while( 1 ) {
	int err=getRequest( sess, &sreq );
	if( err != 0 ) {
	    ap_log_error( APLOG_MARK, APLOG_EMERG, NULL,
			  "Error reading request %d\n", err);
/* 	    // XXX cleanup, close connection if packet error */
	    endSession( sess );
	    return NOT_FOUND;
	}
	process_callback( sess, &sreq, r );
	if( sreq.type==END_RESPONSE )
	    break;
    }

    return OK;
}

/* ---------------------------------------- */
/**
   Standard Apache configuration handling - create and maintain config struct
   Configuration directives
*/
static const char *add_host(cmd_parms *cmd, void *module_c, char *name, char *host, char *port);

static void *create_ajpv22_config(pool *p, server_rec *server)
{
    Ajp22Module *rpm = (Ajp22Module *) ap_palloc(p, sizeof(Ajp22Module));
    
    rpm->config_pool=p;
    rpm->debug=1;
    rpm->server_rec=server;
/*     //    ajpconf->hosts = (RemoteHost *)ap_palloc( p, MAX_HOSTS * sizeof(RemoteHost)); */
    rpm->hostc = 0;

    /* if no host is configured, add default 
       XXX move it somehere else */
    add_host( NULL, rpm, "default", DEFAULT_HOST, DEFAULT_PORT );

    /* XXX check alloc error, return NULL if any */
    return rpm;
}

static const char *add_host(cmd_parms *cmd, void *module_c, char *name, char *host, char *portS)
{
    /* XXX !!! Error checking - return real message since it's a config problem*/    
    Ajp22Module *rpm=(Ajp22Module *)module_c;

    RemoteHost *rh=ap_palloc( rpm->config_pool, sizeof(RemoteHost ));
    
    if( rpm->debug ) ap_log_error( APLOG_MARK, APLOG_EMERG, rpm->server_rec,
				   "Add host %s %s %s", name, host, portS);
    
    rh->pool=rpm->config_pool;
    rh->name=name;
    rh->host=host;
    rh->port=atoi(portS);
    rh->connection=NULL; 
    
    rh->hinfo=ap_pgethostbyname( rpm->config_pool, host ); 
    /* // palloc a new hostent, unlike gethostbyname */

    rh->addr.sin_family = AF_INET;
    rh->addr.sin_addr.s_addr = ((struct in_addr *)rh->hinfo->h_addr_list[0])->s_addr;
    rh->addr.sin_port = htons(rh->port);
    
    rpm->hosts[rpm->hostc]=rh;
    rpm->hostc++;

    return NULL;
}

static const char *set_debug(cmd_parms *cmd, void *module_c, char *arg1)
{
    Ajp22Module *rpm=(Ajp22Module *)module_c;
    rpm->debug=atoi(arg1);
    return NULL;
}

/* ---------------------------------------- */
/**
   Module registration data ( sort of Module Interface ) 
*/

/* List of handlers */

static handler_rec ajpv22_handlers[] = {
    {"ajpv22-handler", test_handler}, 
    {NULL}
};

/* List of configuration directives  */

static command_rec ajpv22_cmds[] = {
    {"AjpHost", add_host , NULL, RSRC_CONF, TAKE3,
     "Set a remote host and port name, if none defined localhost/8007 will be used as"
     " default, otherwise the first defined"},
    {"AjpDebug", set_debug, NULL, RSRC_CONF, TAKE1,
     "Debug level for AjpProtocol"},
    {NULL}
};

/* Apache module descriptor */

/* Assume at least Apache 1.3 - no check for >19970622 ( it was removed from 
  most Apache modules I saw) */
module MODULE_VAR_EXPORT ajpv22_module = {
    STANDARD_MODULE_STUFF,
    NULL,                       /* module initializer */
    NULL,                       /* per-directory config creator */
    NULL,                       /* dir config merger */
    create_ajpv22_config,       /* server config creator */
    NULL,                       /* server config merger */
    ajpv22_cmds,                /* command table */
    ajpv22_handlers,            /* [7] list of handlers */
    NULL,                       /* [2] filename-to-URI translation */
    NULL,                       /* [5] check/validate user_id */
    NULL,                       /* [6] check user_id is valid *here* */
    NULL,                       /* [4] check access by host address */
    NULL,                       /* [7] MIME type checker/setter */
    NULL,                       /* [8] fixups */
    NULL,                       /* [10] logger */
    NULL,                       /* [3] header parser */
    NULL,                       /* apache child process initializer */
    NULL,                       /* apache child process exit/cleanup */
    NULL                        /* [1] post read_request handling */
};


