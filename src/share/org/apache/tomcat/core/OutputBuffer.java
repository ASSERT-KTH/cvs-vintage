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


package org.apache.tomcat.core;

import java.io.*;

/**
 * The buffer used by tomcat response. It allows writting chars and
 * bytes. It does the mixing in order to implement ServletOutputStream
 * ( which has both byte and char methods ) and to allow a number of
 * optimizations (like a jsp pre-computing the byte[], but using char for
 * non-static content).
 *
 * @author Costin Manolache
 */
public final class OutputBuffer {
    public static final int DEFAULT_BUFFER_SIZE = 8*1024;
    int defaultBufferSize = DEFAULT_BUFFER_SIZE;
    int bytesWritten = 0;

    /** The buffer
     */
    public byte buf[];

    public int start;
    public int end;
    
    /**
     * The index one greater than the index of the last valid byte in 
     * the buffer. count==-1 for end of stream
     */
    public int count;

    final static int debug=0;

    Response resp;
    Request req;
    ContextManager cm;
    
    public OutputBuffer(Response resp) {
	buf=new byte[defaultBufferSize];
	this.resp=resp;
	req=resp.getRequest();
	cm=req.getContextManager();
    }

    public void recycle() {
	bytesWritten=0;
	count=0;
    }

    /** This method will call the interceptors and then write the buf[]
     *  to the adapter's doWrite.
     */
    void doWrite( byte buf[], int off, int count ) throws IOException {
	cm.doWrite( req, resp, buf, off, count );
    }

    // -------------------- Adding to the buffer -------------------- 
    // Like BufferedOutputStream, without sync

    public void write(int b) throws IOException {
	if( debug>1 )System.out.write( b );
	if (count >= buf.length) {
	    flush();
	}
	buf[count++] = (byte)b;
	bytesWritten++;
    }

    public void write(byte b[], int off, int len) throws IOException {
	if( debug>1 ) System.out.write( b, off, len );
	int avail=buf.length - count;

	bytesWritten += len;

	// fit in buffer, great.
	if( len <= avail ) {
	    System.arraycopy(b, off, buf, count, len);
	    count += len;
	    return;
	}

	// Optimization:
	// If len-avail < length ( i.e. after we fill the buffer with
	// what we can, the remaining will fit in the buffer ) we'll just
	// copy the first part, flush, then copy the second part - 1 write
	// and still have some space for more. We'll still have 2 writes, but
	// we write more on the first.

	if (len - avail < buf.length) {
	    /* If the request length exceeds the size of the output buffer,
    	       flush the output buffer and then write the data directly.
	       We can't avoid 2 writes, but we can write more on the second
	    */
	    System.arraycopy(b, off, buf, count, avail);
	    count += avail;
	    flush();
	    
	    System.arraycopy(b, off+avail, buf, count, len - avail);
	    count+= len - avail;
	    bytesWritten += len - avail;
	    return;
	}

	// len > buf.length + avail
	flush();
	doWrite( b, off, len );

	return;
    }

    // --------------------  BufferedOutputStream compatibility

    public void flush() throws IOException {
	if( count > 0) {
	    doWrite( buf, 0, count );
	    count=0;
	}
    }
    
    public int getBytesWritten() {
	return bytesWritten;
    }

    public void setBufferSize(int size) {
	if( size > buf.length ) {
	    buf=new byte[size];
	}
    }

    public void reset() {
	count=0;
	bytesWritten=0;
    }

    public int getBufferSize() {
	return buf.length;
    }


    // -------------------- Utils

    void log( String s ) {
	System.out.println("OutputBuffer: " + s );
    }
    
}
