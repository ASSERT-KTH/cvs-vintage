#include "door.h"
#include "stdio.h"
#include "stdlib.h"
#include "fcntl.h"
#include "signal.h"

#define DOOR_FILE "test2"

int door=-1;
char *door_file;

void destroy_door() {
    if (door >= 0) {
	printf("Closing door\n");
	close(door);
	printf("Detaching door %s\n", door_file);
	fdetach(door_file);
	printf("Unlinking %s\n", door_file);
	unlink(door_file);
	door = -1;
    }
}
void sigcleanup(int signo)
{
	destroy_door();
	printf("Done\n");
	exit(0);
}

void testProc( void * cookie, char *data, size_t datasize, 
	       door_desc_t *desc, size_t ndesc) {

    char arg;

    printf("In door %d\n", (int) datasize);
    arg= *((char *)data);
    printf("Arg %d\n", (int) arg);
    data[0] = 2 * arg + 100;
    
    door_return( data, datasize, NULL, 0);
    printf("Door return\n");
}


void testCall() {
    int fd;
    char *buff;
    door_arg_t *args;

    fd=open(door_file, O_RDONLY);
    if( fd < 0 ) {
	printf("No door\n");
	return;
    }
    
    args=(door_arg_t *)malloc( sizeof( door_arg_t )); 
    buff=(char *)malloc( 2048 );
    buff[0]=3;

    args->data_ptr = (char*)buff;
    args->data_size = 2048;
    args->desc_ptr = NULL;
    args->desc_num = 0;
    args->rbuf = buff;
    args->rsize = 2048;

    printf("Calling \n");
    printf("Before %lx %lx %d %d %d\n", args->data_ptr, args->rbuf, args->data_size, args->rsize, (int)args->data_ptr[0]);
    door_call( fd, args);
    printf("Done\n");
    printf("After %lx %lx %d %d %d\n", args->data_ptr, args->rbuf, args->data_size, args->rsize, (int)args->data_ptr[0]);
    printf( "Result %d\n", args->rbuf[0]);
}

int main( int argc, char **argv) {

    int fd, fd1;
    door_file=argv[1];
    
    if( argc > 2 ) {
	printf("Door create \n");
	fd=door_create( testProc, NULL, 0);
	
	signal(SIGINT, sigcleanup);
	signal(SIGTERM, sigcleanup);
	signal(SIGHUP, sigcleanup);

	unlink(door_file); 
	fd1=creat(door_file,  0600 ); 
	close(fd1); 
	
	printf("Created file\n");
	fattach( fd, door_file);
	door=fd;
	printf("Waiting\n");
	for( ; ; ) 
	    pause();
    } else {
	testCall();
    }
}
