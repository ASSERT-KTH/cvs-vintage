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
#include "jk_util.h"


/* // XXX replace all return values with error codes */
#define ERR_BAD_PACKET -5

/*
RPC details:

  - one parameter  - use a structure for more. The method
    is encoded as part of the request
  - one or no result
  - 



 */

/* Data marshaling. Like XDR *
 */
struct MsgBuffer_Simple {
    jk_pool_t *pool;

    unsigned char *buf;
    int pos; /* XXX MT */
    int len;
    int maxlen;
};


typedef struct MsgBuffer_Simple MsgBuffer;

/* -------------------- Setup routines -------------------- */

/** Allocate a buffer.
 */
MsgBuffer *jk_b_new(jk_pool_t *p); 

/** Set up a buffer with an existing buffer
 */
int jk_b_set_buffer( MsgBuffer *msg, char *data, int buffSize );

/** Set up a buffer with a new buffer of buffSize
 */
int jk_b_set_buffer_size( MsgBuffer *msg, int buffSize );

/** Finalize the buffer before sending - set length fields, etc
 */
void jk_b_end(MsgBuffer *msg);

/** Recycle the buffer - prepare for a new invocation 
 */
void jk_b_reset( MsgBuffer *msg );

/** Return the buffer body 
 */ 
unsigned char *jk_b_get_buff( MsgBuffer *msg );

/** Return the current reading position
 */
unsigned int jk_b_get_pos( MsgBuffer *msg );

/** Buffer size 
*/
int jk_b_get_size( MsgBuffer *msg );

void jk_b_set_len( MsgBuffer *msg, int len );

/** Get the  message length for incomming buffers
    or the current length for outgoing
*/
unsigned int jk_b_get_len( MsgBuffer *msg );

/** Dump the buffer header
    @param err Message text
*/
void jk_b_dump( MsgBuffer *msg, char *err ); 

/* -------------------- Real encoding -------------------- */


void jk_b_set_int( MsgBuffer *msg, int pos, unsigned int val );

int jk_b_append_int( MsgBuffer *msg, unsigned int val );

int jk_b_append_string( MsgBuffer *msg, char *param );


/* -------------------- Decoding -------------------- */

unsigned char *jk_b_get_string( MsgBuffer *msg);

/** Get an int from an arbitrary position 
 */
int jk_b_pget_int( MsgBuffer *msg, int pos);

int jk_b_get_int( MsgBuffer *msg);

