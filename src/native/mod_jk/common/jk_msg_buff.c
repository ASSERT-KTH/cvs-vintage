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

/***************************************************************************
 * Description: Data marshaling. XDR like                                  *
 * Author:      Costin <costin@costin.dnt.ro>                              *
 * Author:      Gal Shachor <shachor@il.ibm.com>                           *
 * Version:     $Revision: 1.1 $                                           *
 ***************************************************************************/

#include "jk_pool.h"
#include "jk_connect.h"
#include "jk_util.h"
#include "jk_sockbuf.h"
#include "jk_msg_buff.h"

struct jk_msg_buf {
    jk_pool_t *pool;

    unsigned char *buf;
    int pos; 
    int len;
    int maxlen;
};


/*
 * Simple marshaling code.
 */

/* XXX what's above this line can go to .h XXX */
void jk_b_dump(jk_msg_buf_t *msg, 
               char *err) 
{
    int i=0;
	printf("%s %d/%d/%d %x %x %x %x - %x %x %x %x - %x %x %x %x - %x %x %x %x\n", err, msg->pos, msg->len, msg->maxlen,  
	       msg->buf[i++],msg->buf[i++],msg->buf[i++],msg->buf[i++],
	       msg->buf[i++],msg->buf[i++],msg->buf[i++],msg->buf[i++],
	       msg->buf[i++],msg->buf[i++],msg->buf[i++],msg->buf[i++],
	       msg->buf[i++],msg->buf[i++],msg->buf[i++],msg->buf[i++]);

	i = msg->pos - 4;
    if(i < 0) {
        i=0;
    }
	
    printf("        %x %x %x %x - %x %x %x %x --- %x %x %x %x - %x %x %x %x\n", 
	       msg->buf[i++],msg->buf[i++],msg->buf[i++],msg->buf[i++],
	       msg->buf[i++],msg->buf[i++],msg->buf[i++],msg->buf[i++],
	       msg->buf[i++],msg->buf[i++],msg->buf[i++],msg->buf[i++],
	       msg->buf[i++],msg->buf[i++],msg->buf[i++],msg->buf[i++]);

}

void jk_b_reset(jk_msg_buf_t *msg) 
{
    msg->len = 4;
    msg->pos = 4;
}

void jk_b_set_int(jk_msg_buf_t *msg, 
                  int pos, 
                  unsigned short val) 
{
    msg->buf[pos]       = (unsigned char)((val >> 8) & 0xff);
    msg->buf[pos + 1]   = (unsigned char)(val & 0xff);
}


int jk_b_append_int(jk_msg_buf_t *msg, 
                    unsigned short val) 
{
    if(msg->len + 2 > msg->maxlen) {
	    return -1;
    }

    jk_b_set_int(msg, msg->len, val);

    msg->len += 2;

    return 0;
}


void jk_b_set_byte(jk_msg_buf_t *msg, 
                   int pos, 
                   unsigned char val) 
{
    msg->buf[pos]= val;
}

int jk_b_append_byte(jk_msg_buf_t *msg, 
                     unsigned char val)
{
    if(msg->len + 1 > msg->maxlen) {
	    return -1;
    }

    jk_b_set_byte(msg, msg->len, val);

    msg->len += 1;

    return 0;
}


void jk_b_end(jk_msg_buf_t *msg) 
{
    /* 
     * Ugly way to set the size in the right position 
     */
    jk_b_set_int(msg, 2, (unsigned short )(msg->len - 4)); /* see protocol */
    jk_b_set_int(msg, 0, 0x1234);
}


jk_msg_buf_t *jk_b_new(jk_pool_t *p) 
{
    jk_msg_buf_t *msg = 
            (jk_msg_buf_t *)jk_pool_alloc(p, sizeof(jk_msg_buf_t));

    if(!msg) {
        return NULL;
    }

    msg->pool = p;
    
    return msg;
}

int jk_b_set_buffer(jk_msg_buf_t *msg, 
                    char *data, 
                    int buffSize) 
{
    if(!msg) {
        return -1;
    }

    msg->len = 0;
    msg->buf = data;
    msg->maxlen = buffSize;
    
    return 0;
}


int jk_b_set_buffer_size(jk_msg_buf_t *msg, 
                         int buffSize) 
{
    unsigned char *data = (unsigned char *)jk_pool_alloc(msg->pool, buffSize);
    
    if(!data) {
	    return -1;
    }

    jk_b_set_buffer(msg, data, buffSize);
    return 0;
}

unsigned char *jk_b_get_buff(jk_msg_buf_t *msg) 
{
    return msg->buf;
}

unsigned int jk_b_get_pos(jk_msg_buf_t *msg) 
{
    return msg->pos;
}

void jk_b_set_pos(jk_msg_buf_t *msg,
                          int pos) 
{
    msg->pos = pos;
}

unsigned int jk_b_get_len(jk_msg_buf_t *msg) 
{
    return msg->len;
}

void jk_b_set_len(jk_msg_buf_t *msg, 
                  int len) 
{
    msg->len=len;
}

int jk_b_get_size(jk_msg_buf_t *msg) 
{
    return msg->maxlen;
}

int jk_b_append_string(jk_msg_buf_t *msg, 
                       const char *param) 
{
    int len;

    if(!param) {
	    jk_b_append_int( msg, 0xFFFF );
	    return 0; 
    }

    len = strlen(param);
    if(msg->len + len + 2  > msg->maxlen) {
	    return -1;
    }

    /* ignore error - we checked once */
    jk_b_append_int(msg, (unsigned short )len);

    /* We checked for space !!  */
    strncpy(msg->buf + msg->len , param, len+1); /* including \0 */
    msg->len += len + 1;

    return 0;
}

unsigned short jk_b_get_int(jk_msg_buf_t *msg) 
{
    int i;
    if(msg->pos + 1 > msg->len) {
	    printf( "Read after end \n");
	    return -1;
    }
    i  = ((msg->buf[msg->pos++]&0xff)<<8);
    i += (msg->buf[(msg->pos++)] & 0xFF);
    return i;
}

unsigned short jk_b_pget_int(jk_msg_buf_t *msg, 
                             int pos) 
{
    int i= ((msg->buf[pos++]&0xff)<<8);
    i+= (msg->buf[pos] & 0xFF);
    return i;
}

unsigned char jk_b_get_byte(jk_msg_buf_t *msg) 
{
    unsigned char rc;
    if(msg->pos > msg->len) {
	    printf("Read after end \n");
	    return -1;
    }
    rc = msg->buf[msg->pos++];
    
    return rc;
}

unsigned char jk_b_pget_byte(jk_msg_buf_t *msg, 
                             int pos) 
{
    return msg->buf[pos];
}


unsigned char *jk_b_get_string(jk_msg_buf_t *msg) 
{
    int size = jk_b_get_int(msg);
    int start = msg->pos;

    if((size < 0 ) || (size + start > msg->maxlen)) { 
	    jk_b_dump(msg, "After get int"); 
	    printf("ERROR\n" );
	    return "ERROR"; /* XXX */
    }

    msg->pos += size;
    msg->pos++;  /* terminating NULL */
    
    return (unsigned char *)(msg->buf + start); 
}

/** Shame-less copy from somewhere.
    assert (src != dst)
 */
static void swap_16(unsigned char *src, unsigned char *dst) 
{
    *dst++ = *(src + 1 );
    *dst= *src;
}
