#include "httpd.h"
#include "http_config.h"
#include "http_log.h"
#include "http_main.h"
#include "http_protocol.h"
#include "http_request.h"
#include "util_script.h"
#include "util_md5.h"

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
static void encode_request( MsgBuffer *msg, request_rec *r );
static int process_callback(  MsgBuffer *msg, request_rec *r);

/* XXX what's above this line should go to .h XXX */


/** Method codes */
/* XXX all method codes should start with 0xFF,
   For methods not starting with 0xFF type is a length, and 
   the method _name_ follows 
*/
static void encode_env(  MsgBuffer *msg, request_rec *r ) {
    //    b_append_table( msg, r->subprocess_env );
    /* XXX use r instead of env */
    b_append_int( msg, 6 );
    b_append_string( msg, "REQUEST_METHOD" );
    b_append_string( msg, (char *)ap_table_get( r->subprocess_env , "REQUEST_METHOD") );

    b_append_string( msg, "SERVER_PROTOCOL");
    b_append_string( msg, (char *)ap_table_get( r->subprocess_env , "SERVER_PROTOCOL" ) );

    b_append_string( msg, "REQUEST_URI" );
    b_append_string( msg, (char *)ap_table_get( r->subprocess_env , "REQUEST_URI" ) );

    b_append_string( msg, "QUERY_STRING" );
    b_append_string( msg, (char *)ap_table_get( r->subprocess_env , "QUERY_STRING") );

    b_append_string( msg, "SERVER_PORT" );
    b_append_string( msg, (char *)ap_table_get( r->subprocess_env , "SERVER_PORT") );

    b_append_string( msg, "REMOTE_ADDR");
    b_append_string( msg, (char *)ap_table_get( r->subprocess_env , "REMOTE_ADDR") );

}

static char *headers[]={ "content-length", "content-type", "cookie" };

static void encode_headers(  MsgBuffer *msg, request_rec *r ) {
    int i;
    b_append_table( msg, r->headers_in );
    if( 0 ) {
	b_append_int( msg, 3  );
	
	for( i=0; i<3 ; i++ ) {
	    b_append_string( msg, headers[i] );
	    b_append_string( msg, (char *)ap_table_get( r->headers_in , headers[i]) );
	}
    }

}

/** Forward request info to the remote engine.
    XXX do not forward everything, only what's used
    XXX configure what is sent per request, fine tune
    XXX integrate with mod_session (notes)
    XXX integrate with special module to find the context (notes)
*/
static void encode_request( MsgBuffer *msg, request_rec *r ) {

    ap_setup_client_block(r, REQUEST_CHUNKED_ERROR);

    b_append_int( msg, REQUEST_FORWARD ); 
    encode_env( msg, r );
    encode_headers( msg, r );

    // Append first chunk of request body ( up to the buffer size )
    /*     printf("Encode request \n"); */
    if ( ! ap_should_client_block(r)) {
	// no body, send 0
	/* printf("No body\n"); */
	b_append_int( msg, 0 );
    } else {
        int maxsize=b_get_size( msg );
	char *buffer=b_get_buff(msg);
	int posLen= b_get_len( msg );
	int pos=posLen +2 ;
        long rd;

	/* Read in buff, at pos + 2 ( let space for size ), up to 
	   maxsize - pos.
	*/
        while ( (pos < maxsize ) &&  (rd=ap_get_client_block(r,buffer+pos, maxsize - pos ))>0) {
	    /*     printf( "Reading %d %d %d \n", posLen, pos, maxsize ); */
	    pos=pos + rd;
        }
	/* 	printf( "End reading %d %d %d \n", posLen, pos, maxsize ); */
	b_set_int( msg, posLen, pos - posLen -2 );
	b_set_len( msg, pos );
	/* 	b_dump(msg, "Post ");  */
    }
    /*     b_dump(msg, "Encode req"); */
}

/** 
    SetHeaders callback - all headers are added to headers->out, no 
    more parsing 
*/
static int setHeaders( MsgBuffer *msg, request_rec *r) {
    int i;
    int count;

    count= b_get_int( msg  );
    //    printf( "Header count: %x %x %x %x\n", count, pos, (int)msg[2], (int)msg[3] );
    for( i=0; i< count; i++ ) {
	char *n=b_get_string( msg );
	char *v=b_get_string( msg );
	ap_table_add( r->headers_out, n, v);
	/* 	printf( "Setting header: %s=%s\n", n , v ); */
	if( 0==strcmp(n,"Content-Type") ) {
	    // XXX Todo Content-encoding!!!
	    // XXX special function to send "special" headers
	    //	    printf("Setting content-type %s\n", v);
	    r->content_type=v;
	}
	if( 0==strcmp(n, "Status")) {
	    //	    printf("Setting status %s\n", v);
	    r->status=atoi(v);
	}
    }
    return NO_RESPONSE;
}

/** 
    Get Body Chunk
*/
static int getBodyChunk( MsgBuffer *msg, request_rec *r) {
    int i;
    int count;

    /* No parameters, send body */
    b_reset( msg );
    b_append_int( msg, SEND_BODY_CHUNK );
    
    if ( ! ap_should_client_block(r)) {
	// no body, send 0
	printf("No body\n");
	b_append_int( msg, 0 );
    } else {
        int maxsize=b_get_size( msg );
	char *buffer=b_get_buff(msg);
	int posLen= b_get_len( msg );
	int pos=posLen +2 ;
        long rd;
	
	/* Read in buff, at pos + 2 ( let space for size ), up to 
	   maxsize - pos.
	*/
        while ( (pos < maxsize ) &&  (rd=ap_get_client_block(r,buffer+pos, maxsize - pos ))>0) {
	    printf( "Reading %d %d %d \n", posLen, pos, maxsize );
	    pos=pos + rd;
        }
	printf( "End reading %d %d %d \n", posLen, pos, maxsize );
	b_set_int( msg, posLen, pos - posLen -2 );
	b_set_len( msg, pos );
	b_dump(msg, "Post additional data"); 
    }
    
    return HAS_RESPONSE;
}

/*
    Small methods inlined
    XXX add support for user-defined methods
 */
int process_callback( MsgBuffer *msg, request_rec *r) {
    int len;

    //printf("Callback %x\n", (int)sreq->type);
    switch( b_getCode(msg) ) {
    case SET_HEADERS:
	setHeaders( msg , r);
	ap_send_http_header(r);
	break;
    case SEND_HEADERS:
	ap_send_http_header(r);
	break;
    case SEND_BODY_CHUNK:
	len=b_get_int( msg );
	ap_rwrite( msg->buf + msg->pos, len, r);
	break;
    case GET_BODY_CHUNK:
	getBodyChunk( msg, r );
	return HAS_RESPONSE;
	break;
    case END_RESPONSE:
	break;
    default:
	b_dump( msg , "Invalid code");
	ap_log_error( APLOG_MARK, APLOG_EMERG, NULL,
		      "Invalid code: %d\n", b_getCode(msg));
	return -1;
    }
    
    return NO_RESPONSE;
}



