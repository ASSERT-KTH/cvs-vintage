#include "door.h"
/** Connection using Door 
 */

/* How it works: 
   Apache send a message == a door invocation with the message as parameter.
                            Block waiting for result.
   Apache blocks reading a request == We already got the result, so no block here,
                            The incoming message is the result of the previous call.
   
   It is a bit dangerous and strange, but the alternative is too complex.
   Important: 
     - same msg_buffer is used allways.
     - never send/return more data than MAX
 */

#define DEFAULT_DOOR "apdoor"

/* The default size of a buffer
   XXX make it configurable
 */
#define DEFAULT_SIZE 0x1000


typedef struct DoorRemoteHost RemoteHost;
typedef struct DoorConnection Connection;

#define connection_get( rhost ) connection_door_get( rhost )
#define connection_release( c ) connection_door_release( c )
#define connection_destroy( c ) connection_door_destroy( c )

#define connection_get_message( c, msg ) connection_door_get_message( c, msg )
#define connection_send_message( c, msg ) connection_door_send_message( c, msg )

#define rhost_new( p, name, arg1, arg2 ) rhost_door_new( p, name, arg1, arg2)

/* 
*/
typedef struct DoorConnection {
    pool *pool;
    RemoteHost *host;
    
    char *fname;
    int size;
    int fd;
    char *buff;
    door_arg_t *args;
} DoorConnection;

/* 
 */
typedef struct DoorRemoteHost {
    pool *pool;
    char *name;
    DoorConnection *connection; // cached connection/host 

    char *door_file;
} DoorRemoteHost;

/* ---------------------------------------- */
static void connection_callback( void * cookie, char *data, size_t datasize, 
	       door_desc_t *desc, size_t ndesc) {

    printf("In door %d\n", (int) datasize);
    
    door_return( data, datasize, NULL, 0);
}

static RemoteHost *rhost_door_new(pool *p, char *name, char *arg1, char *arg2) {
    DoorRemoteHost *rh=ap_palloc( p, sizeof(DoorRemoteHost)); 
    pid_t pid;
    int fd, fd1;
    char fname[20];

    rh->pool=p;
    rh->connection=NULL;
    rh->name=name;
    if(arg1==NULL)
	rh->door_file=DEFAULT_DOOR;
    else
	rh->door_file=arg1;

    /* Create callback-door */
    printf("Creating door\n");
    fd= door_create( connection_callback, NULL, 0);
    pid=getpid();
    sprintf( fname, "door.%d", (int)pid );
    
    printf("Attaching door to %s\n", fname);
    unlink( fname );
    fd1=creat( fname, 0600 );
    close(fd1);
    
    fattach( fd, fname);

}


/* ---------------------------------------- */
static Connection *connection_door_create( RemoteHost *rhost ) {
    DoorConnection *conn;

    char *buff;
    int err;
    int fd_flags;

    conn=(DoorConnection *)ap_palloc( rhost->pool, sizeof ( DoorConnection ));
    conn->args=(door_arg_t *)ap_palloc( rhost->pool, sizeof( door_arg_t )); 
    conn->pool=rhost->pool;
    conn->size=DEFAULT_SIZE;

    conn->fd=open(rhost->door_file, O_RDONLY);
    if(conn->fd <0 ) {
	// free
	if(err<0) ap_log_error( APLOG_MARK, APLOG_EMERG, NULL, "Error connecting \n");
	return NULL;
    }
    conn->buff=(char *)ap_palloc( rhost->pool, conn->size );

    conn->args->data_ptr = NULL;
    conn->args->data_size = -1;

    conn->args->desc_ptr = NULL;
    conn->args->desc_num = 0;


    conn->args->rbuf = NULL;
    conn->args->rsize = 0;

    rhost->connection=conn;
    return conn;
}    


static Connection *connection_door_get( struct DoorRemoteHost *rhost ) {
    Connection *con;

    if( rhost->connection != NULL ) 
	return rhost->connection;
    
    con=connection_door_create( rhost );

    return con;
}

static int connection_door_release( struct DoorConnection *con ) {
    return 0;
}

static int connection_door_destroy( struct DoorConnection *conn ) {
    close( conn->fd );
    fdetach( conn->fname );
    conn->host->connection= NULL;
    unlink( conn->fname );
    /* XXX free ? */
    /* XXX leak - either create subpool, or reuse the connection */
    return 0;
}


static int connection_door_send_message(  struct DoorConnection *conn, MsgBuffer *msg ) {
    
    if( conn->args->data_ptr == NULL ) {
	/* one time init, MsgBuff MUST remain the same */
	conn->args->data_ptr = b_get_buff( msg );
	conn->args->data_size = b_get_size( msg );

	conn->args->rbuf = conn->args->data_ptr;
	conn->args->rsize = conn->args->data_size;
    }

    b_end( msg );
    printf("Calling \n");
    printf("Before %lx %lx %d %d %d\n", conn->args->data_ptr, conn->args->rbuf, conn->args->data_size, conn->args->rsize, (int)conn->args->data_ptr[0]);
    door_call( conn->fd, conn->args );
    printf("Done\n");
    printf("After %lx %lx %d %d %d\n", conn->args->data_ptr, conn->args->rbuf, conn->args->data_size, conn->args->rsize, (int)conn->args->data_ptr[0]);
    /* Now msg contains the response == the next incoming message */
    return 0;
}

#define GET_MESSAGE 3

/** 
    Read a callback.
 */
static int connection_door_get_message( struct DoorConnection *con, MsgBuffer *msg ) {
    /* Really nothing to get here. See protocol description, the input message is
       the door-function result.
     */
    /* get message */
    b_reset( msg );
    b_append_int( msg, GET_MESSAGE );
    b_end( msg );
    door_call( con->fd, con->args );
    return 0;
}


