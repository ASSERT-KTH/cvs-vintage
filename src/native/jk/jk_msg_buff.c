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
    Simple marshaling code.
*/

#include "jk_pool.h"
#include "jk_connect.h"
#include "jk_util.h"
#include "jk_sockbuf.h"
#include "jk_msg_buff.h"



/* XXX what's above this line can go to .h XXX */
void jk_b_dump( MsgBuffer *msg, char *err ) {
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

void jk_b_reset( MsgBuffer *msg ) {
    msg->len =4;
    msg->pos =4;
}

void jk_b_set_int( MsgBuffer *msg, int pos, unsigned int val ) {
    /* XXX optimize - swap if needed or just copyb */
    /* #if SWAP */
    /*     swap_16( (unsigned char *)&val, msg->buf ) */
    /* #else */
    /*     ???	 */
    /* #endif  */
    msg->buf[pos++]=(unsigned char) ( (val >> 8) & 0xff );
    msg->buf[pos]= (unsigned char) ( val & 0xff );
}

int jk_b_append_int( MsgBuffer *msg, unsigned int val ) {
    if( msg->len + 2 > msg->maxlen ) 
	return -1;

    jk_b_set_int( msg, msg->len, val );
    msg->len +=2;
    return 0;
}


void jk_b_end(MsgBuffer *msg) {
    /* Ugly way to set the size in the right position */
    jk_b_set_int( msg, 2, msg->len - 4 ); /* see protocol */
    jk_b_set_int( msg, 0, 0x1234 );
}


/* XXX optimize it ( less function calls, macros )
   Ugly pointer arithmetic code
 */
/* XXX io_vec ? XXX just send/map the pool !!! */

MsgBuffer *jk_b_new(jk_pool_t *p) {
    MsgBuffer *msg=(MsgBuffer *)jk_pool_alloc( p, sizeof ( MsgBuffer ));
    msg->pool=p;
    if(msg==NULL) return NULL;
}

int jk_b_set_buffer( MsgBuffer *msg, char *data, int buffSize ) {
    if(msg==NULL) return -1;

    msg->len=0;
    msg->buf=data;
    msg->maxlen=buffSize;
    /* XXX error checking !!! */
    
    return 0;
}


int jk_b_set_buffer_size( MsgBuffer *msg, int buffSize ) {

    unsigned char *data=(unsigned char *)jk_pool_alloc( msg->pool, buffSize );
    if( data==NULL ) {
	/* Free - sub-pools */
	return -1;
    }

    jk_b_set_buffer( msg, data, buffSize );
}

unsigned char *jk_b_get_buff( MsgBuffer *msg ) {
    return msg->buf;
}

unsigned int jk_b_get_pos( MsgBuffer *msg ) {
    return msg->pos;
}

unsigned int jk_b_get_len( MsgBuffer *msg ) {
    return msg->len;
}

void jk_b_set_len( MsgBuffer *msg, int len ) {
    msg->len=len;
}

int jk_b_get_size( MsgBuffer *msg ) {
    return msg->maxlen;
}

/** Shame-less copy from somewhere.
    assert (src != dst)
 */
static void swap_16( unsigned char *src, unsigned char *dst) {
    *dst++ = *(src + 1 );
    *dst= *src;
}

int jk_b_append_string( MsgBuffer *msg, char *param ) {
    int len;

    if( param==NULL ) {
	jk_b_append_int( msg, 0xFFFF );
	return 0; 
    }

    len=strlen(param);
    if( msg->len + len + 2  > msg->maxlen )
	return -1;

    // ignore error - we checked once
    jk_b_append_int( msg, len );

    // We checked for space !! 
    strncpy( msg->buf + msg->len , param, len+1 ); // including \0
    msg->len += len + 1;
    return 0;
}

int jk_b_get_int( MsgBuffer *msg) {
    int i;
    if( msg->pos + 1 > msg->len ) {
	printf( "Read after end \n");
	return 0;
    }
    i= ((msg->buf[msg->pos++]&0xff)<<8);
    i+= (msg->buf[(msg->pos++)] & 0xFF);
    return i;
}

int jk_b_pget_int( MsgBuffer *msg, int pos) {
    int i= ((msg->buf[pos++]&0xff)<<8);
    i+= (msg->buf[pos] & 0xFF);
    return i;
}


/* int jk_b_getCode( MsgBuffer *msg ) { */
/*     return jk_b_pget_int( msg, 0 ); */
/* } */

unsigned char *jk_b_get_string( MsgBuffer *msg) {
    int size, start;
    char *str;

    /*     jk_b_dump(msg, "Before GS: "); */
    
    size=jk_b_get_int(msg);
    start=msg->pos;
    if(( size < 0 ) || ( size + start > msg->maxlen ) ) { 
	jk_b_dump(msg, "After get int"); 
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




