/*
 * ====================================================================
 *
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 1999 The Apache Software Foundation.  All rights 
 * reserved.
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
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:  
 *       "This product includes software developed by the 
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "The Jakarta Project", "Tomcat", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written 
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Group.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 * [Additional notices, if required by prior licensing conditions]
 *
 */ 


package org.apache.tomcat.util.buf;

import java.io.*;

/**
 *  Wrapper for a resizable byte[].
 *
 *  The buffer can grow to a limit, then will flush to an output channel.
 * 
 *  This is different from ByteChunk - ByteBuffer "owns" the buffer, and can
 *  reallocate it. ByteChunk is just a pointer in an existing buffer, and is
 *  intended for various operations on the buffer ( conversion, search, etc ).
 *
 *  This class is not thred safe ( caller must insure it's used only in a single
 *  thread or synchronize the calls.
 *
 *  You can also use the class in "write through" mode, with everything going
 *  to the output channel ( to avoid double buffering )
 *
 *  Derived from OutputBuffer, JspWriterImpl, BodyContentImpl, StringBuffer with
 *  extra hacks.
 *
 *  @author Costin Manolache
 *  @author Anil K. Vijendran ( JspWriterImpl )
 *  @author Rajiv Mordani ( BodyContentImpl )
 */
public final class ByteBuffer {
    /**
     *  When we need more space we'll either
     *  grow the buffer ( up to the limit ) or send it to a channel.
     */
    public static interface ByteOutputChannel {
	/** Send the bytes ( usually the internal conversion buffer ).
	 *  Expect 8k output if the buffer is full.
	 */
	public void realWriteBytes( byte cbuf[], int off, int len) throws IOException;
    }
    
    public static final int DEFAULT_INITIAL=1024;
    private byte buf[];
    // -1: grow undefinitely
    // maximum amount to be cached
    private int limit=-1;
    private boolean directOutput=false;
    
    // next character to be written
    private int pos=0;
    
    private ByteOutputChannel out=null;
    
    public ByteBuffer() {
	this( DEFAULT_INITIAL );
    }
    
    public ByteBuffer( int initial ) {
	if( initial > 0 ) {
	    buf=new byte[ initial ];
	} else {
	    // assert out!=null ( will be set later )
	    buf=new byte[1];
	    directOutput=true;
	}
    }

    // -------------------- Setup buffer --------------------
    
    /** Maximum amount of data in this buffer.
     *
     *  If -1 or not set, the buffer will grow undefinitely.
     *  Can be smaller than the current buffer size ( which will not shrink ).
     *  When the limit is reached, the buffer will be flushed ( if out is set )
     *  or throw exception.
     */
    public void setLimit(int limit) {
	this.limit=limit;
    }
    
    public int getLimit() {
	return limit;
    }

    /** When the buffer is full, write the data to the output channel.
     * 	Also used when large amount of data is appended.
     *
     *  If not set, the buffer will grow to the limit.
     */
    public void setByteOutputChannel(ByteOutputChannel out) {
	this.out=out;
    }

    public void recycle() {
	pos=0;
    }

    /** Go directly to the output channel, don't buffer
     */
    public void setDirectOutput(boolean b) {
	directOutput=b;
    }
    
    // -------------------- Adding data to the buffer --------------------
    
    public void append( char c )
	throws IOException
    {
	append( (byte)c);
    }
    
    public void append( byte b )
	throws IOException
    {
	// no buffering 
	if( directOutput ) {
	    buf[0]=b;
	    out.realWriteBytes(buf, 0, 1 );
	    return;
	}

	makeSpace( 1 );

	// couldn't make space
	if( pos >= limit ) {
	    flushBuffer();
	}
	buf[pos++]=b;
    }
    
    /** Add data to the buffer
     */
    public void append( byte src[], int off, int len )
	throws IOException
    {
	// no buffering 
	if( directOutput ) {
	    out.realWriteBytes( src, off, len );
	    return;
	}

	// will grow, up to limit
	makeSpace( len );

	// if we don't have limit: makeSpace can grow as it wants
	if( limit < 0 ) {
	    // assert: makeSpace made enough space
	    System.arraycopy( src, off, buf, pos, len );
	    pos+=len;
	    return;
	}
	
	// if we have limit and we're below
	if( len <= limit - pos ) {
	    // makeSpace will grow the buffer to the limit,
	    // so we have space
	    System.arraycopy( src, off, buf, pos, len );
	    pos+=len;
	    return;
	}

	// need more space than we can afford, need to flush
	// buffer

	// the buffer is already at ( or bigger than ) limit
	
	// Optimization:
	// If len-avail < length ( i.e. after we fill the buffer with
	// what we can, the remaining will fit in the buffer ) we'll just
	// copy the first part, flush, then copy the second part - 1 write
	// and still have some space for more. We'll still have 2 writes, but
	// we write more on the first.

	if( len + pos < 2 * limit ) {
	    /* If the request length exceeds the size of the output buffer,
	       flush the output buffer and then write the data directly.
	       We can't avoid 2 writes, but we can write more on the second
	    */
	    int avail=limit-pos;
	    System.arraycopy(src, off, buf, pos, avail);
	    pos += avail;
	    
	    flushBuffer();
	    
	    System.arraycopy(src, off+avail, buf, pos, len - avail);
	    pos+= len - avail;
	    
	} else {	// len > buf.length + avail
	    // long write - flush the buffer and write the rest
	    // directly from source
	    flushBuffer();
	    
	    out.realWriteBytes( src, off, len );
	}
    }

    public void flushBuffer()
	throws IOException
    {
	//assert out!=null
	if( out==null ) {
	    throw new IOException( "Buffer overflow, no sink " + limit + " " +
				   buf.length  );
	}
	out.realWriteBytes( buf, 0, pos );
	pos=0;
    }

    /** Make space for len chars. If len is small, allocate
     *	a reserve space too. Never grow bigger than limit.
     */
    private void makeSpace(int count)
    {
	byte[] tmp = null;

	int newSize;
	int desiredSize=pos + count;

	// Can't grow above the limit
	if( limit > 0 &&
	    desiredSize > limit ) {
	    desiredSize=limit;
	}

	// limit < buf.length ( the buffer is already big )
	// or we already have space
	if( desiredSize < buf.length ) {
	    return;
	}
	// grow in larger chunks
	if( desiredSize < 2 * buf.length ) {
	    newSize= buf.length * 2;
	    if( limit >0 &&
		newSize > limit ) newSize=limit;
	    tmp=new byte[newSize];
	} else {
	    newSize= buf.length * 2 + count ;
	    if( limit > 0 &&
		newSize > limit ) newSize=limit;
	    tmp=new byte[newSize];
	}
	
	System.arraycopy(buf, 0, tmp, 0, pos);
	buf = tmp;
	tmp = null;
    }
    
    // -------------------- Getting data from the buffer --------------------

    /** Return the ( current ) buffer. An append operation may
     *  realocate the buffer ( but this is not a thread-safe class anyway.
     */ 
    public byte[] getBuffer() {
	return buf;
    }

    /** Get the current length of the buffer
     */
    public int getLen() {
	return pos;
    }

    /** The start of the buffer
     */
    public int getOff() {
	return 0; // no tricks right now - we start from 0
    }
    
    public int getPos() {
	return pos;
    }
    
    public void setPos( int off ) {
	this.pos=off;
    }
}
