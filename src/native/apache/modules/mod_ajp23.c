/*
 * XXX copyright
 */

#include "httpd.h"
#include "http_config.h"
#include "http_log.h"
#include "http_main.h"
#include "http_protocol.h"
#include "http_request.h"
#include "util_script.h"
#include "util_md5.h"

/** 
    XXX Add high level description
    XXX Add configuration documentation
*/

module ajp23_module;

#define MAX_HOSTS 20
/* 16 k */
#define MAX_BUFF_SIZE 0x4000
#define DEFAULT_ARG1 "localhost"
#define DEFAULT_ARG2 "8008"

#define CONNECTOR "Ajp23"
#define CONNECTOR_MODULE ajp23_module
#define CONNECTOR_HANDLE_NAME "ajp23_handler"


/* -------------------- #include "msg_buffer_simple.c" -------------------- */ 

/* Data marshaling.
   Uses a Buffer ( iovect later ), with 1 copy.
   Simple marshaling, based on Ajp21.
 */
typedef struct MsgBuffer_Simple MsgBuffer;

MsgBuffer *new_MsgBuffer();

/* strbuf ? */
struct MsgBuffer_Simple {
    pool *pool;

    unsigned char *buf;
    int pos; /* XXX MT */
    int len;
    int maxlen;
};

/* XXX what's above this line should go to .h XXX */

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

static MsgBuffer *b_new(pool *p) {
    MsgBuffer *msg=(MsgBuffer *)ap_palloc( p, sizeof ( MsgBuffer ));
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

    unsigned char *data=(unsigned char *)ap_palloc( msg->pool, buffSize );
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

static int b_append_table( MsgBuffer *msg, table *tbl) {
    array_header *env_arr = ap_table_elts(tbl);
    
    table_entry *elts = (table_entry *) env_arr->elts;
    int i, err;

    err=b_append_int( msg, env_arr->nelts);
    if(err<0) return err;

    for (i = 0; i < env_arr->nelts; ++i) {
	//if (!elts[i].key) continue;
	// XXX do not send headers as environment variables
	//if ( type== ENV && !strncmp(elts[i].key, "HTTP_", 5)) continue;
	
	err=b_append_string( msg, elts[i].key );
	if (err<0)  return err;

	err=b_append_string( msg, elts[i].val );
	if (err<0)  return err;
    }
    return 0;
}

static int table_size( MsgBuffer *msg, table *tbl ) {
    int size=0;
    int i;

    array_header *env_arr = ap_table_elts(tbl);
    
    table_entry *elts = (table_entry *) env_arr->elts;

    size+=2; // NV count

    for (i = 0; i < env_arr->nelts; ++i) {
	if (!elts[i].key) continue;
	size += strlen( elts[i].key );
	size += strlen( elts[i].val );
    }
    size+=6*env_arr->nelts; // 2 byte for each string length + 1 ending 0
    return size;
}

/* -------------------- #include "rmethods.c" -------------------- */ 
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

/* -------------------- #include "connection_tcp.c" -------------------- */
/** Connection using TCP sockets 
 */

/* The default size of a buffer
   XXX make it configurable
 */
#define DEFAULT_SIZE 0x1000

#define connection_get( rhost ) connection_tcp_get( rhost )
#define connection_release( c ) connection_tcp_release( c )
#define connection_destroy( c ) connection_tcp_destroy( c )

#define connection_get_message( c, msg ) connection_tcp_get_message( c, msg )
#define connection_send_message( c, msg ) connection_tcp_send_message( c, msg )

#define rhost_new( p, name, arg1, arg2 ) rhost_tcp_new( p, name, arg1, arg2)

typedef struct TcpConnection Connection;
typedef struct TcpRemoteHost RemoteHost;

typedef struct TcpConnection  {
    pool *pool;
    RemoteHost *host;

    int socket; /* XXX sock_t */
    BUFF *bsocket;
} TcpConnection;

/* Informations about a remote TCP host
 */
typedef struct TcpRemoteHost{
    pool *pool;
    char *name;
    TcpConnection *connection; /* cache */
    
    char *host;
    int port;
    struct hostent *hinfo; /* in pool */
    struct sockaddr_in addr;
}  TcpRemoteHost;

/* // XXX replace all return values with error codes */
#define ERR_BAD_PACKET -5

/* ---------------------------------------- */

static RemoteHost *rhost_tcp_new(pool *p, char *name, char *arg1, char *arg2) {
    TcpRemoteHost *rh=ap_palloc( p, sizeof(TcpRemoteHost)); 

    rh->pool=p;
    rh->connection=NULL;
    rh->name=name;

    if(arg2!=NULL)
	rh->port=atoi(arg2);
    else
	rh->port=DEFAULT_ARG2;/* XXX */
	
    rh->host=arg1;
    rh->hinfo=ap_pgethostbyname( rh->pool, rh->host ); 
    /* // palloc a new hostent, unlike gethostbyname */

    rh->addr.sin_family = AF_INET;
    rh->addr.sin_addr.s_addr = ((struct in_addr *)rh->hinfo->h_addr_list[0])->s_addr;
    rh->addr.sin_port = htons(rh->port);
    
    return (RemoteHost *)rh;
}

/* ---------------------------------------- */

static Connection *connection_tcp_create( RemoteHost *rhost ) {
    TcpConnection *con;

    int err;
    int fd_flags;

    con=(TcpConnection *)ap_palloc( rhost->pool, sizeof ( TcpConnection ));

    ap_log_error( APLOG_MARK, APLOG_EMERG, NULL, "Open connection to %s", rhost->host);

    con->pool=rhost->pool;
    con->host=rhost;

    con->bsocket= ap_bcreate( rhost->pool, B_SOCKET | B_RDWR );
    if( con->bsocket == NULL ) {
	/* XXX  clean the subpool */
	return NULL;
    }

    con->socket=  ap_psocket(con->pool, AF_INET, SOCK_STREAM, IPPROTO_TCP);
    
    err=connect(con->socket, &rhost->addr ,sizeof(struct sockaddr_in));
    if( err==-1 ) {
	ap_log_error( APLOG_MARK, APLOG_EMERG, NULL,
			  "Error connecting %d\n", err);
	return NULL; 
	/* XXX clean subpool
	// XXX add WIN stuff
	*/
    }
    
    /* XXX add TCP_NODELAY sfuff */

    ap_bpushfd( con->bsocket, con->socket, con->socket );
    
    fd_flags = fcntl(con->socket, F_GETFL, 0);
    fd_flags &= ~O_NONBLOCK;
    fcntl(con->socket, F_SETFL, fd_flags);

    rhost->connection=con; 
    return con;
}


/* Remote Host connection handling 
   If one connection/request - optimize it
   If connection reuse - everything is ok.
 */
static Connection *connection_tcp_get( TcpRemoteHost *rhost ) {
    Connection *con;

    if( rhost->connection != NULL ) 
	return (Connection *)rhost->connection;
    
    con=connection_tcp_create( rhost );

    return con;
}

static int connection_tcp_release( struct TcpConnection *con ) {

}


static int connection_tcp_destroy( struct TcpConnection *con ) {

    ap_log_error( APLOG_MARK, APLOG_EMERG, NULL,
		  "Closing connection");

    if(con==NULL) return 0;

    if( con->bsocket!=NULL ) {
	ap_bclose( con->bsocket );
	ap_pclosesocket( con->pool, con->socket );
    }

    con->bsocket=NULL;
    con->socket=0;
    con->host->connection=NULL;
    /* XXX free ? */
    /* XXX leak - either create subpool, or reuse the connection */
    return 0;
}


static int connection_tcp_send_message(  TcpConnection *con, MsgBuffer *msg ) {
    int sent=0;
    int i;
    
    b_end( msg );
    /*     printf("Sending %x %x %x %x\n", msg->buf[0],msg->buf[1],msg->buf[2],msg->buf[3]) ; */
    while( sent < msg->len ) {
	i=write( con->socket, msg->buf + sent , msg->len - sent );
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
static int read_full( TcpConnection *con, unsigned char *buff, int msglen ) {
    int rdlen=0;
    int i;

    while( rdlen < msglen ) {
	i=read( con->socket, buff + rdlen, msglen - rdlen );
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
	/* 	printf("."); */
	if(i==0) return -7; 
	rdlen += i;
    }
    return 0;
}


/** 
    Read a callback 
 */
static int connection_tcp_get_message( struct TcpConnection *con, MsgBuffer *msg ) {
    char head[6];
    int rdlen;
    int i;
    int pos;
    int msglen;
    int off;
    char *message;
    int *imessage;

    /*     // mark[2] + len[2]  */
    i=read_full( con, head, 4 );
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
	/* 	ap_log_error( APLOG_MARK, APLOG_EMERG, NULL,
	/* 		      "Re-alocating msg buffer, %d %d\n", sreq->buffSize, sreq->msglen);
	/* 	sreq->buffSize = sreq->msglen; */
    }
    
    msg->len=msglen;
    msg->pos=2; /* After code */
    i=read_full(con, msg->buf, msglen );
    if( i<0) return i;
    
    /*     b_dump( msg, " RCV: " ); */
    return 0;
}

/* -------------------- #include "module_c.c"  -------------------- */
/* Apache-specific code, to be included in modules 
   A better aproach to OO in C is needed ( instead of include and define )...
*/

/*
  module doors_connector_module;
*/


typedef struct {
    /**  Methods */

    /* "public" fields, in all protocol modules */
    int debug;
    pool *config_pool;
    server_rec *server_rec;

    int hostc;

    RemoteHost **hosts; 
} Connector;


/* ---------------------------------------- */
static int connector_fixups(request_rec *r) {
    if( (r->content_type != NULL ) ||
	(r->handler != NULL ) ) 
	return DECLINED;

    /* File not found - it may be a servlet */
    /* XXX Doing that means File not found will be handled by tomcat */
    printf("FIXUP: %s %s \n", r->filename , r->unparsed_uri );

    r->handler = CONNECTOR_HANDLE_NAME ; 
    
    return DECLINED;
}

static int connector_handler(request_rec *r) {

    void *sess;
    MsgBuffer *msg;
    Connector *rpm;
    RemoteHost *rhost;
    Connection *cn;
    int err;

    rpm=(Connector *)ap_get_module_config(r->server->module_config,
						     & CONNECTOR_MODULE );
    /* A load-balancing module will set a note with the rhost to use,
       we'll search it in hosts */
    rhost=rpm->hosts[0];

    /* XXX Send only the relevant information  */
    ap_add_cgi_vars(r);
    ap_add_common_vars(r);
    
    msg = b_new( r->pool );
    b_set_buffer_size( msg, MAX_BUFF_SIZE); 

    b_reset( msg );
    encode_request( msg , r );
    
    cn=connection_get( rhost );
    if(cn==NULL) {
	return NOT_FOUND;
    }
    err= connection_send_message( cn, msg );
    
    if(err<0) {
	/* Disconect */
	connection_destroy( cn );
	ap_log_error( APLOG_MARK, APLOG_EMERG, NULL,
			  "Error sending request %d\n", err);
	/* XXX retry once !!! */
	
	return NOT_FOUND;
    }

    while( 1 ) {
	int err=connection_get_message( cn, msg );
	/* 	b_dump(msg, "Get Message: " ); */
	if( err < 0 ) {
	    ap_log_error( APLOG_MARK, APLOG_EMERG, NULL,
			  "Error reading request %d\n", err);
	    // XXX cleanup, close connection if packet error
	    connection_destroy( cn );
	    return NOT_FOUND;
	}
	if( b_getCode( msg ) == END_RESPONSE )
	    break;
	err=process_callback( msg, r );
	if( err == HAS_RESPONSE ) {
	    err=connection_send_message( cn, msg );
	    if( err < 0 ) {
		ap_log_error( APLOG_MARK, APLOG_EMERG, NULL,
			      "Error reading response1 %d\n", err);
		connection_destroy( cn );
		return NOT_FOUND;
	    }
	}
	if( err < 0 ) break; /* XXX error */
    }

    connection_release( cn );

    return OK;
}

/* ---------------------------------------- */

/**
   Standard Apache configuration handling - create and maintain config struct
   Configuration directives
*/
static const char *add_host(cmd_parms *cmd, void *module_c, char *name, char *arg1, char *arg2);

static void *create_connector_config(pool *p, server_rec *server)
{
    Connector *rpm = (Connector *) ap_palloc(p, sizeof(Connector));
    
    rpm->config_pool=p;
    rpm->debug=1;
    rpm->server_rec=server;
    rpm->hosts = (RemoteHost **)ap_palloc( rpm->config_pool, MAX_HOSTS * sizeof( void *) );

    add_host( NULL, rpm, "default", DEFAULT_ARG1, DEFAULT_ARG2 );

    /* XXX check alloc error, return NULL if any */
    return rpm;
}

/* XXX Not fully implemented */
static const char *add_host(cmd_parms *cmd, void *module_c, char *name, char *arg1, char *arg2)
{
    /* XXX !!! Error checking - return real message since it's a config problem*/    
    Connector *rpm=(Connector *)module_c;
    
    rpm->hosts[0]= rhost_new( rpm->config_pool, name , arg1, arg2);

    if( rpm->debug ) ap_log_error( APLOG_MARK, APLOG_EMERG, rpm->server_rec,
				   "Add host %s %s", name, arg1);
	
    return NULL;
}

static const char *set_debug(cmd_parms *cmd, void *module_c, char *arg1)
{
    Connector *rpm=(Connector *)module_c;
    rpm->debug=atoi(arg1);
    return NULL;
}

/* --------------------  #include "connection_tcp.c -------------------- */
/**
   Module registration data ( sort of Module Interface ) 
*/

/* List of handlers */

static handler_rec connector_handlers[] = {
    {CONNECTOR_HANDLE_NAME , connector_handler}, // XXX remove, separate module
    {NULL}
};

/* List of configuration directives  */

static command_rec connector_cmds[] = {
    {CONNECTOR "Connector", add_host , NULL, RSRC_CONF, TAKE23,
     "Set a connector, default is apconnector "},
    {CONNECTOR "ConnectorDebug", set_debug, NULL, RSRC_CONF, TAKE1,
     "Debug level for Connector"},
    {NULL}
};

/* Apache module descriptor */

/* Assume at least Apache 1.3 - no check for >19970622 ( it was removed from 
  most Apache modules I saw) */
module MODULE_VAR_EXPORT  ajp23_module = {
    STANDARD_MODULE_STUFF,
    NULL,                       /* module initializer */
    NULL,                       /* per-directory config creator */
    NULL,                       /* dir config merger */
    create_connector_config,       /* server config creator */
    NULL,                       /* server config merger */
    connector_cmds,                /* command table */
    connector_handlers,            /* [7] list of handlers */
    NULL,                       /* [2] filename-to-URI translation */
    NULL,                       /* [5] check/validate user_id */
    NULL,                       /* [6] check user_id is valid *here* */
    NULL,                       /* [4] check access by host address */
    NULL,                       /* [7] MIME type checker/setter */
    connector_fixups,                       /* [8] fixups */
    NULL,                       /* [10] logger */
    NULL,                       /* [3] header parser */
    NULL,                       /* apache child process initializer */
    NULL,                       /* apache child process exit/cleanup */
    NULL                        /* [1] post read_request handling */
};


