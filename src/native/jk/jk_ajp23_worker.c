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

/** 
    Experimental bi-directionl protocol.
    ( updated from src/native/apache/connector )
    
    @author costin@costin.dnt.ro
*/

#include "jk_ajp23_worker.h"
#include "jk_pool.h"
#include "jk_connect.h"
#include "jk_util.h"
#include "jk_sockbuf.h"

#define AJP_DEF_HOST            ("localhost")
#define AJP_DEF_PORT            (8008)
#define READ_BUF_SIZE           (8*1024)
#define DEF_RETRY_ATTEMPTS      (1)

/** "PseudoObjects" in JK: 
    struct Base {   
       void (*method)();
       SubClass *subClass;
    }

    Base *SubClassFactory();
    struct SubClass {
       char *propertiesSubcl;
    }


    We pass around pointers to the Base object. 
    XXX We could check the type of the worker too to validate.
    
    All methods are returning int==error code ( 0==success )
    The first parameter is a pointer to this.
    XXX have second param a pointer to ctx ( include log in ctx)
    

    Conventions: _t == type
 */

/** Objects:
    class Worker {
      int validate(Properties props);
      int init(Properties props );
      int get_endpoint( Endpoint *endp );
      int destroy();
    }

    class Endpoint {
      int service( WsService s, int *isRecoverable);
      int done();
    }

    class WsService {
      Pool *pool;
      ... all fields in request_rec ...
      int startResponse( int status, String reason, String headers[], String values[], int hCount );
      int read( char buff[], int len, int *read);
      int write( char buff[], int len );
    }

*/

/**
   Utilities:
   
 */

/** ajp23Worker extends Worker -> worker will point to the extended object.
 */
struct ajp23_worker {
    struct sockaddr_in worker_inet_addr; /* Contains host and port */
    unsigned connect_retry_attempts;
    char *name; 
    jk_worker_t worker; 

    // no cache endpoint - it's up to caller to cache it if
    // he wants!
};

typedef struct ajp23_worker ajp23_worker_t;

struct ajp23_endpoint { 
    ajp23_worker_t *worker;
    
    int sd;
    jk_sockbuf_t sb;

    jk_endpoint_t endpoint;
};

typedef struct ajp23_endpoint ajp23_endpoint_t;

/* -------------------- Local objects */

/* // XXX replace all return values with error codes */
#define ERR_BAD_PACKET -5

/* Data marshaling.
   Uses a Buffer ( iovect later ), with 1 copy.
   Simple marshaling, based on Ajp21.
 */

/* strbuf ? */
struct MsgBuffer_Simple {
    jk_pool_t *pool;

    unsigned char *buf;
    int pos; /* XXX MT */
    int len;
    int maxlen;
};

typedef struct MsgBuffer_Simple MsgBuffer;

MsgBuffer *new_MsgBuffer();


/* -------------------- Method -------------------- */
static int JK_METHOD validate(jk_worker_t *pThis,
                              jk_map_t *props,                            
                              jk_logger_t *l)
{
    jk_log(l, JK_LOG_DEBUG, "Into jk_worker_t::validate\n");

    if(pThis && pThis->worker_private) {        
        ajp23_worker_t *p = pThis->worker_private;
        int port = jk_get_worker_port(props, 
                                      p->name,
                                      AJP_DEF_PORT);

        char *host = jk_get_worker_host(props, 
                                        p->name,
                                        AJP_DEF_HOST);

        jk_log(l, JK_LOG_DEBUG, "In jk_worker_t::validate for worker %s contact is %s:%d\n", 
               p->name, host, port);
	
        if(port > 1024 && host) {
            if(jk_resolve(host, (short)port, &p->worker_inet_addr)) {
                return JK_TRUE;
            }
            jk_log(l, JK_LOG_ERROR, "In jk_worker_t::validate, resolve failed\n");
        }
        jk_log(l, JK_LOG_ERROR, "In jk_worker_t::validate, Error %s %d\n", host, port);
    } else {
        jk_log(l, JK_LOG_ERROR, "In jk_worker_t::validate, NULL parameters\n");
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


static int JK_METHOD destroy(jk_worker_t **pThis,
                             jk_logger_t *l)
{
    jk_log(l, JK_LOG_DEBUG, "Into jk_worker_t::destroy\n");
    if(pThis && *pThis && (*pThis)->worker_private) {
        ajp23_worker_t *private_data = (*pThis)->worker_private;
        free(private_data->name);
        free(private_data);

        return JK_TRUE;
    }

    jk_log(l, JK_LOG_ERROR, "In jk_worker_t::destroy, NULL parameters\n");
    return JK_FALSE;
}


static int JK_METHOD done(jk_endpoint_t **e,
                          jk_logger_t *l)
{
    jk_log(l, JK_LOG_DEBUG, "Into jk_endpoint_t::done\n");
    if(e && *e && (*e)->endpoint_private) {
        ajp23_endpoint_t *p = (*e)->endpoint_private;
        if(p->sd > 0) {
            jk_close_socket(p->sd);
        }
        free(p);
        *e = NULL;
        return JK_TRUE;
    }

    jk_log(l, JK_LOG_ERROR, "In jk_endpoint_t::done, NULL parameters\n");
    return JK_FALSE;
}


// ---------------------------------------- START MARSHALL ----------------------------------------


/* XXX what's above this line can go to .h XXX */
static void b_dump( MsgBuffer *msg, char *err ) {
        int i=0;
	printf("%s %d/%d/%d %x %x %x %x - %x %x %x %x - %x %x %x %x - %x %x %x %x\n", err, msg->pos, msg->len, msg->maxlen,  
	       msg->buf[i++],msg->buf[i++],msg->buf[i++],msg->buf[i++],
	       msg->buf[i++],msg->buf[i++],msg->buf[i++],msg->buf[i++],
	       msg->buf[i++],msg->buf[i++],msg->buf[i++],msg->buf[i++],
	       msg->buf[i++],msg->buf[i++],msg->buf[i++],msg->buf[i++]);

	i=msg->pos - 4;
	if( i<0 ) i=0;
	
        printf("        %x %x %x %x - %x %x %x %x --- %x %x %x %x - %x %x %x %x\n", 
	       msg->buf[i++],msg->buf[i++],msg->buf[i++],msg->buf[i++],
	       msg->buf[i++],msg->buf[i++],msg->buf[i++],msg->buf[i++],
	       msg->buf[i++],msg->buf[i++],msg->buf[i++],msg->buf[i++],
	       msg->buf[i++],msg->buf[i++],msg->buf[i++],msg->buf[i++]);

}

static void b_reset( MsgBuffer *msg ) {
    msg->len =4;
    msg->pos =4;
}

static void b_set_int( MsgBuffer *msg, int pos, unsigned int val ) {
    /* XXX optimize - swap if needed or just copyb */
    /* #if SWAP */
    /*     swap_16( (unsigned char *)&val, msg->buf ) */
    /* #else */
    /*     ???	 */
    /* #endif  */
    msg->buf[pos++]=(unsigned char) ( (val >> 8) & 0xff );
    msg->buf[pos]= (unsigned char) ( val & 0xff );
}

static int b_append_int( MsgBuffer *msg, unsigned int val ) {
    if( msg->len + 2 > msg->maxlen ) 
	return -1;

    b_set_int( msg, msg->len, val );
    msg->len +=2;
    return 0;
}


static void b_end(MsgBuffer *msg) {
    /* Ugly way to set the size in the right position */
    b_set_int( msg, 2, msg->len - 4 ); /* see protocol */
    b_set_int( msg, 0, 0x1234 );
}


/* XXX optimize it ( less function calls, macros )
   Ugly pointer arithmetic code
 */
/* XXX io_vec ? XXX just send/map the pool !!! */

static MsgBuffer *b_new(jk_pool_t *p) {
    MsgBuffer *msg=(MsgBuffer *)jk_pool_alloc( p, sizeof ( MsgBuffer ));
    msg->pool=p;
    if(msg==NULL) return NULL;
}

static int b_set_buffer( MsgBuffer *msg, char *data, int buffSize ) {
    if(msg==NULL) return -1;

    msg->len=0;
    msg->buf=data;
    msg->maxlen=buffSize;
    /* XXX error checking !!! */
    
    return 0;
}


static int b_set_buffer_size( MsgBuffer *msg, int buffSize ) {

    unsigned char *data=(unsigned char *)jk_pool_alloc( msg->pool, buffSize );
    if( data==NULL ) {
	/* Free - sub-pools */
	return -1;
    }

    b_set_buffer( msg, data, buffSize );
}

static unsigned char *b_get_buff( MsgBuffer *msg ) {
    return msg->buf;
}

static unsigned int b_get_pos( MsgBuffer *msg ) {
    return msg->pos;
}

static unsigned int b_get_len( MsgBuffer *msg ) {
    return msg->len;
}

static  void b_set_len( MsgBuffer *msg, int len ) {
    msg->len=len;
}

static int b_get_size( MsgBuffer *msg ) {
    return msg->maxlen;
}

/** Shame-less copy from somewhere.
    assert (src != dst)
 */
static void swap_16( unsigned char *src, unsigned char *dst) {
    *dst++ = *(src + 1 );
    *dst= *src;
}

static int b_append_string( MsgBuffer *msg, char *param ) {
    int len;

    if( param==NULL ) {
	b_append_int( msg, 0xFFFF );
	return 0; 
    }

    len=strlen(param);
    if( msg->len + len + 2  > msg->maxlen )
	return -1;

    // ignore error - we checked once
    b_append_int( msg, len );

    // We checked for space !! 
    strncpy( msg->buf + msg->len , param, len+1 ); // including \0
    msg->len += len + 1;
    return 0;
}

static int b_get_int( MsgBuffer *msg) {
    int i;
    if( msg->pos + 1 > msg->len ) {
	printf( "Read after end \n");
	return 0;
    }
    i= ((msg->buf[msg->pos++]&0xff)<<8);
    i+= (msg->buf[(msg->pos++)] & 0xFF);
    return i;
}

static int b_pget_int( MsgBuffer *msg, int pos) {
    int i= ((msg->buf[pos++]&0xff)<<8);
    i+= (msg->buf[pos] & 0xFF);
    return i;
}


static int b_getCode( MsgBuffer *msg ) {
    return b_pget_int( msg, 0 );
}

static unsigned char *b_get_string( MsgBuffer *msg) {
    int size, start;
    char *str;

    /*     b_dump(msg, "Before GS: "); */
    
    size=b_get_int(msg);
    start=msg->pos;
    if(( size < 0 ) || ( size + start > msg->maxlen ) ) { 
	b_dump(msg, "After get int"); 
	printf("ERROR\n" );
	return "ERROR"; /* XXX */
    }

    msg->pos += size;
    msg->pos++; // end 0
    str= msg->buf + start;
    /*     printf( "Get_string %lx %lx %x\n", msg->buf,  str, size ); */
    /*     printf( "Get_string %s \n", str ); */
    return (unsigned char *)(msg->buf + start); 
}

static int b_append_headers( MsgBuffer *msg, jk_ws_service_t *s, jk_logger_t *l) {
    /* Send the request headers */
    int err=b_append_int( msg, s->num_headers);
    if(err<0) return err;

    if(s->num_headers) {
        unsigned  i;
        for(i = 0 ; i < s->num_headers ; ++i) {
	    err=b_append_string( msg, s->headers_names[i] );
	    if (err<0)  return err;
	    err=b_append_string( msg, s->headers_values[i] );
	    if (err<0)  return err;
	}
    }
    if(!err) {
	jk_log(l, JK_LOG_ERROR, "In ajpv12_handle_request, failed to send headers\n");
	return JK_FALSE;
    }
    
    return JK_TRUE;
}

// ---------------------------------------- START RMETHODS ----------------------------------------
/* Protocol independent remote methods. 
   Uses buffer.c to do (de)marshaling.
   Contain both remote methods impl + skel
   Separate later, now hard-coding may be faster to code and maybe more efficient
*/

#define NO_RESPONSE 0
#define HAS_RESPONSE 1


#define SET_HEADERS 2
#define SEND_BODY_CHUNK 3
#define SEND_HEADERS 4
#define REQUEST_FORWARD 1
#define END_RESPONSE 5
#define GET_BODY_CHUNK 6

/** Callback demux 
    Small methods inlined
    XXX add support for user-defined methods
 */
static void encode_request( MsgBuffer *msg, jk_ws_service_t *r, jk_logger_t *l );
static int process_callback(  MsgBuffer *msg, jk_ws_service_t *r, jk_logger_t *l);

/* XXX what's above this line should go to .h XXX */


/** Method codes */
/* XXX all method codes should start with 0xFF,
   For methods not starting with 0xFF type is a length, and 
   the method _name_ follows 
*/
static void encode_env(  MsgBuffer *msg, jk_ws_service_t *r ) {
    //    b_append_table( msg, r->subprocess_env );
    /* XXX use r instead of env */
    b_append_int( msg, 6 );
    b_append_string( msg, "REQUEST_METHOD" );
    b_append_string( msg, r->method );

    b_append_string( msg, "SERVER_PROTOCOL");
    b_append_string( msg, r->protocol );

    b_append_string( msg, "REQUEST_URI" );
    b_append_string( msg, r->req_uri );

    b_append_string( msg, "QUERY_STRING" );
    b_append_string( msg, r->query_string );

    b_append_string( msg, "SERVER_PORT" );
    b_append_string( msg, "8080" );

    b_append_string( msg, "REMOTE_ADDR");
    b_append_string( msg, r->remote_addr );

}

/** Forward request info to the remote engine.
    XXX do not forward everything, only what's used
    XXX configure what is sent per request, fine tune
    XXX integrate with mod_session (notes)
    XXX integrate with special module to find the context (notes)
*/
static void encode_request( MsgBuffer *msg, jk_ws_service_t *r, jk_logger_t *l ) {

    b_append_int( msg, REQUEST_FORWARD ); 
    encode_env( msg, r );
    b_append_headers( msg, r, l );

/*     // Append first chunk of request body ( up to the buffer size ) */
    /*     printf("Encode request \n"); */
/*     if ( ! ap_should_client_block(r)) { */
/* 	// no body, send 0 */
	/* printf("No body\n"); */
	b_append_int( msg, 0 );
/*     } else { */
/*         int maxsize=b_get_size( msg ); */
/* 	char *buffer=b_get_buff(msg); */
/* 	int posLen= b_get_len( msg ); */
/* 	int pos=posLen +2 ; */
/*         long rd; */

/* 	/* Read in buff, at pos + 2 ( let space for size ), up to  */
/* 	   maxsize - pos. */
/*         while ( (pos < maxsize ) &&  (rd=ap_get_client_block(r,buffer+pos, maxsize - pos ))>0) { */
	    /*     printf( "Reading %d %d %d \n", posLen, pos, maxsize ); */
/* 	    pos=pos + rd; */
/*         } */
	/* 	printf( "End reading %d %d %d \n", posLen, pos, maxsize ); */
/* 	b_set_int( msg, posLen, pos - posLen -2 ); */
/* 	b_set_len( msg, pos ); */
	/* 	b_dump(msg, "Post ");  */
    /*     b_dump(msg, "Encode req"); */
}

/** 
    SetHeaders callback - all headers are added to headers->out, no 
    more parsing 
*/
static int setHeaders( MsgBuffer *msg, jk_ws_service_t *r, jk_logger_t *l) {
    int i;
    int count;
    char **names=NULL;
    char **values=NULL;
    int status=200;
    

    count= b_get_int( msg  );
    names=(char **) jk_pool_alloc( r->pool, ( count + 1 ) * sizeof( char * ));
    values=(char **) jk_pool_alloc( r->pool, ( count + 1 ) * sizeof( char * ));

    //    printf( "Header count: %x %x %x %x\n", count, pos, (int)msg[2], (int)msg[3] );
    for( i=0; i< count; i++ ) {
	char *n=b_get_string( msg );
	char *v=b_get_string( msg );
	names[i]=n;
	values[i]=v;

	if( 0==strcmp(n, "Status")) {
	    //	    printf("Setting status %s\n", v);
	    status=atoi(v);
	}
    }
    if( ! r->start_response( r, status, "Reason", 
			     (const char * const *)names, /* ??? */
			     (const char * const *)values, 
			     count )) {
	jk_log( l, JK_LOG_ERROR, "Error starting response " );
	return NO_RESPONSE;
    }
			     
    return NO_RESPONSE;
}

/** 
    Get Body Chunk
*/
static int getBodyChunk( MsgBuffer *msg, jk_ws_service_t *r) {
    int i;
    int count;

    /* No parameters, send body */
    b_reset( msg );
    b_append_int( msg, SEND_BODY_CHUNK );
    
/*     if ( ! ap_should_client_block(r)) { */
/* 	// no body, send 0 */
/* 	printf("No body\n"); */
/* 	b_append_int( msg, 0 ); */
/*     } else { */
/*         int maxsize=b_get_size( msg ); */
/* 	char *buffer=b_get_buff(msg); */
/* 	int posLen= b_get_len( msg ); */
/* 	int pos=posLen +2 ; */
/*         long rd; */
	
/* 	/* Read in buff, at pos + 2 ( let space for size ), up to  */
/* 	   maxsize - pos. */
/*         while ( (pos < maxsize ) &&  (rd=ap_get_client_block(r,buffer+pos, maxsize - pos ))>0) { */
/* 	    printf( "Reading %d %d %d \n", posLen, pos, maxsize ); */
/* 	    pos=pos + rd; */
/*         } */
/* 	printf( "End reading %d %d %d \n", posLen, pos, maxsize ); */
/* 	b_set_int( msg, posLen, pos - posLen -2 ); */
/* 	b_set_len( msg, pos ); */
/* 	b_dump(msg, "Post additional data");  */
/*     } */
    
    return HAS_RESPONSE;
}

/*
    Small methods inlined
    XXX add support for user-defined methods
 */
int process_callback( MsgBuffer *msg, jk_ws_service_t *r, jk_logger_t *l) {
    int len;

    /*     printf("Callback %x\n", b_getCode(msg)); */
    switch( b_getCode(msg) ) {
    case SET_HEADERS:
	setHeaders( msg , r, l);
	break;
    case SEND_BODY_CHUNK:
	len=b_get_int( msg );
	r->write( r, msg->buf + msg->pos, len);
	break;
    case GET_BODY_CHUNK:
	getBodyChunk( msg, r );
	return HAS_RESPONSE;
	break;
    case END_RESPONSE:
	break;
    default:
	b_dump( msg , "Invalid code");
	jk_log( l, JK_LOG_ERROR,
		"Invalid code: %d\n", b_getCode(msg));
	return -1;
    }
    
    return NO_RESPONSE;
}


// ---------------------------------------- START TCP ----------------------------------------

static int connection_tcp_send_message(  ajp23_endpoint_t *con, MsgBuffer *msg, jk_logger_t *l ) {
    int sent=0;
    int i;
    
    b_end( msg );
    /*     printf("Sending %x %x %x %x\n", msg->buf[0],msg->buf[1],msg->buf[2],msg->buf[3]) ;  */
    while( sent < msg->len ) {
	i=write( con->sd, msg->buf + sent , msg->len - sent );
	/* 	printf("i=%d\n", i); */
	if( i == 0 ) {
	    return -2;
	}
	if( i < 0 ) {
	    return -3;
	}
	sent += i;
    }

    /* ... */
    /*     flush( con->socket ); */
    return 0;
}


/** Read a full buffer, deal with partial reads
    XXX rewrite it - we should read as much as possible 
    and then interpret it and read more if needed, right now there
    are at least 2 reads per request 
 */
static int read_full( ajp23_endpoint_t *con, unsigned char *buff, int msglen, jk_logger_t *l ) {
    int rdlen=0;
    int i;

    while( rdlen < msglen ) {
	i=read( con->sd, buff + rdlen, msglen - rdlen );
	/* 	printf( "Read: %d %d %x %x %x\n", i, rdlen, i, rdlen, msglen ); */
	
	if(i==-1) {
	    if(errno==EAGAIN) {
		jk_log( l, JK_LOG_ERROR,
			      "EAGAIN, continue reading");
	    } 
	    else {
		jk_log( l, JK_LOG_ERROR,
			      "ERRNO=%s", strerror(errno));
		/* 		//		closeConnection( ses ); */
		return -6;
	    }
	}
	/* 	printf("."); */
	if(i==0) return -7; 
	rdlen += i;
    }
    return 0;
}


/** 
    Read a callback 
 */
static int connection_tcp_get_message( ajp23_endpoint_t *con, MsgBuffer *msg, jk_logger_t *l ) {
    char head[6];
    int rdlen;
    int i;
    int pos;
    int msglen;
    int off;
    char *message;
    int *imessage;

    /*     // mark[2] + len[2]  */
    i=read_full( con, head, 4, l );
    /*     printf( "XXX %d \n" , i ) ;  */
    if(i<0) return i;
    
    if( (head[0] != 'A') || (head[1] != 'B' )) {
	return ERR_BAD_PACKET ;
    }

    /*    sreq->msglen=get_I( head, &pos ); */
    msglen=((head[2]&0xff)<<8);
    msglen+= (head[3] & 0xFF);

    /* printf( "Packet len %d %x\n", msglen, msglen ); */

    if(msglen > b_get_size(msg) ) {
	printf("Message too long ");
	return -5; /* XXX */
	/* 	sreq->message=(char *)ap_palloc( p, sreq->msglen ); */
	/* 		      "Re-alocating msg buffer, %d %d\n", sreq->buffSize, sreq->msglen);
	/* 	sreq->buffSize = sreq->msglen; */
    }
    
    msg->len=msglen;
    msg->pos=2; /* After code */
    i=read_full(con, msg->buf, msglen, l );
    if( i<0) return i;
    
    /*     b_dump( msg, " RCV: " ); */
    return 0;
}



// ---------------------------------------- ----------------------------------------

static int JK_METHOD service(jk_endpoint_t *e, 
                             jk_ws_service_t *s,
                             jk_logger_t *l,
                             int *is_recoverable_error)
{
    jk_log(l, JK_LOG_DEBUG, "Into jk_endpoint_t::service\n");

    if(e && e->endpoint_private && s && is_recoverable_error) {
        ajp23_endpoint_t *p = e->endpoint_private;
        unsigned attempt;

        *is_recoverable_error = JK_TRUE;

        for(attempt = 0 ; attempt < p->worker->connect_retry_attempts ; attempt++) {
            p->sd = jk_open_socket(&p->worker->worker_inet_addr, 
                                   JK_TRUE, 
                                   l);

            jk_log(l, JK_LOG_DEBUG, "In jk_endpoint_t::service, sd = %d\n", p->sd);
            if(p->sd >= 0) {
                break;
            }
        }
        if(p->sd >= 0) {
	    MsgBuffer *msg;
	    int err;
            /*
             * After we are connected, each error that we are going to
             * have is probably unrecoverable
             */
            *is_recoverable_error = JK_FALSE;
            jk_sb_open(&p->sb, p->sd);

	    msg = b_new( s->pool );
	    b_set_buffer_size( msg, 2048); 

	    b_reset( msg );
	    encode_request( msg , s, l );
    
	    err= connection_tcp_send_message( p, msg, l );
    
	    if(err<0) {
		jk_log( l, JK_LOG_ERROR,
			"Error sending request %d\n", err);
		return JK_FALSE;
	    }
	    

	    while( 1 ) {
		int err=connection_tcp_get_message( p, msg, l );
		/* 	b_dump(msg, "Get Message: " ); */
		if( err < 0 ) {
		    jk_log( l, JK_LOG_ERROR,
				  "Error reading request %d\n", err);
		    // XXX cleanup, close connection if packet error
		    return JK_FALSE;
		}
		if( b_getCode( msg ) == END_RESPONSE )
		    break;
		err=process_callback( msg, s, l );
		if( err == HAS_RESPONSE ) {
		    err=connection_tcp_send_message( p, msg, l );
		    if( err < 0 ) {
			jk_log( l, JK_LOG_DEBUG,
				      "Error reading response1 %d\n", err);
			return JK_FALSE;
		    }
		}
		if( err < 0 ) break; /* XXX error */
	    }
	    return JK_TRUE;
	    
        }
        jk_log(l, JK_LOG_ERROR, "In jk_endpoint_t::service, Error sd = %d\n", p->sd);
    } else {
        jk_log(l, JK_LOG_ERROR, "In jk_endpoint_t::service, NULL parameters\n");
    }

    return JK_FALSE;
}

static int JK_METHOD get_endpoint(jk_worker_t *pThis,
                                  jk_endpoint_t **pend,
                                  jk_logger_t *l)
{
    jk_log(l, JK_LOG_DEBUG, "Into jk_worker_t::get_endpoint\n");

    if(pThis && pThis->worker_private && pend) {        
        ajp23_endpoint_t *p = (ajp23_endpoint_t *)malloc(sizeof(ajp23_endpoint_t));
        if(p) {
            p->sd = -1;         
            p->worker = pThis->worker_private;
            p->endpoint.endpoint_private = p;
            p->endpoint.service = service;
            p->endpoint.done = done;
            *pend = &p->endpoint;
            return JK_TRUE;
        }
        jk_log(l, JK_LOG_ERROR, "In jk_worker_t::get_endpoint, malloc failed\n");
    } else {
        jk_log(l, JK_LOG_ERROR, "In jk_worker_t::get_endpoint, NULL parameters\n");
    }

    return JK_FALSE;
}



/** Constructor for ajp23_worker_t
 */
int JK_METHOD ajp23_worker_factory(jk_worker_t **w,
                                   const char *name,
                                   jk_logger_t *l)
{
    ajp23_worker_t *private_data = (ajp23_worker_t *)malloc(sizeof(ajp23_worker_t));
    printf( "Into ajp23_worker_factory\n"); fflush(stdout);

    jk_log(l, JK_LOG_DEBUG, "Into ajp23_worker_factory\n");
    if(NULL == name || NULL == w) {
        jk_log(l, JK_LOG_ERROR, "In ajp23_worker_factory, NULL parameters\n");
	return JK_FALSE;
    }
    
    
    if(! private_data) {
        jk_log(l, JK_LOG_ERROR, "In ajp23_worker_factory, NULL parameters\n");
	return JK_FALSE;
    }

    private_data->name = strdup(name);          
    
    if( ! private_data->name) {
	free(private_data);
	jk_log(l, JK_LOG_ERROR, "In ajp23_worker_factory, malloc failed\n");
	return JK_FALSE;
    } 

    private_data->connect_retry_attempts= DEF_RETRY_ATTEMPTS;
    private_data->worker.worker_private = private_data;
    
    private_data->worker.validate       = validate;
    private_data->worker.init           = init;
    private_data->worker.get_endpoint   = get_endpoint;
    private_data->worker.destroy        = destroy;
    
    *w = &private_data->worker;
    return JK_TRUE;
}


