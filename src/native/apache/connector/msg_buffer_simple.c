#include "httpd.h"

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


