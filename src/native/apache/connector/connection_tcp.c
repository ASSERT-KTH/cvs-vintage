#include "httpd.h"

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
	rh->port=8007;/* XXX */
	
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

