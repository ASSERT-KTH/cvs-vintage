/*
 * $Header: /tmp/cvs-vintage/tomcat/src/share/org/apache/tomcat/core/Attic/BufferedServletOutputStream.java,v 1.6 1999/12/13 21:07:23 costin Exp $
 * $Revision: 1.6 $
 * $Date: 1999/12/13 21:07:23 $
 *
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

import org.apache.tomcat.util.StringManager;
import java.io.*;
import javax.servlet.ServletOutputStream;

/* Was: server.ServletOutputStreamImpl.
   - replaced private with protected
   - removed OutputStram out
   - replaced ServerResponse with Response
   - replaced out.write() with abstract doWrite that will be implemented by protocols
   - replaced out.close() with abstract endResponse()
   - response.writeHeaders() take no parameters ( response knows where to write anyway )
   - replaced response.sendHeaders with abstract sendHeaders( response )
 */

/**
 *
 *
 * @author James Duncan Davidson [duncan@eng.sun.com]
 * @author Jason Hunter [jch@eng.sun.com]
 * @author James Todd [gonzo@eng.sun.com]
 * @author Mandar Raje [mandar@eng.sun.com]
 */
public class BufferedServletOutputStream extends ServletOutputStream {

    protected StringManager sm =
        StringManager.getManager(Constants.Package);
    protected boolean usingWriter = false;
    
    // XXX
    // need to make buffer a configurable property
    protected static final int DEFAULT_BUFFER_SIZE = 8*1024;
    protected byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
    protected int bufferSize = DEFAULT_BUFFER_SIZE;
    protected int bufferCount = 0;
    protected int totalCount = 0;
    protected boolean committed = false;
    protected boolean closed = false;
    Response response;
    ResponseAdapter resA;
    
    protected BufferedServletOutputStream() {
	//	System.out.println("new BOS " + closed);
    }

    protected BufferedServletOutputStream(ResponseAdapter resA) {
	//	System.out.println("new BOS " + closed);
	this.resA=resA;
    }

    public void setResponseAdapter( ResponseAdapter resA ) {
	this.resA=resA;
    }
    
    protected void doWrite( byte buffer[], int pos, int count) throws IOException {
	resA.doWrite( buffer, pos, count);
    }

    protected void endResponse() throws IOException {
	response.getResponseAdapter().endResponse();
    }

    protected void sendHeaders() throws IOException {
	if(response!=null)
	    response.writeHeaders();
	if(resA!=null)
	    resA.endHeaders();
    }

    public void setResponse( Response response ) {
	this.response=response;
    }

    // Hack for the buffering issue.
    public void setUsingWriter(boolean uwrt) {
	this.usingWriter = uwrt;
    }
    
    // XXX
    // write all the other write methods here so that we dont
    // have this 1 byte bottleneck here.
    
    public void write(int i) throws IOException {
	if (closed) {
	    //	    System.out.println("CLOSED");
	    return;
	}
	//	System.out.print(".");
        buffer[bufferCount] = (byte)i;
	bufferCount++;
	totalCount++;
	
	if (bufferCount >= buffer.length) {
	    reallyFlush();
	}     
    }

    public void write(byte[] b) throws IOException {
	write(b,0,b.length);
    }
    
    public void write(byte[] b, int off, int len) throws IOException {

	if (closed) {
	    return;
	}
	
	if (len < 0) {
            String msg = sm.getString("servletOutputStreamImpl.write.iae");
	    throw new IllegalArgumentException(msg);
	}

        // If the whole thing fits in the buffer, then just put it there.
        if (len < (buffer.length - bufferCount)) {
            System.arraycopy(b, off, buffer, bufferCount, len);
            bufferCount += len;
            totalCount += len;
        }
        else {
            // Otherwise, we might as well flush and then write out full
            // buffers of data directly to the underlying stream.  Whatever
            // is left over, we simply put in the buffer.
            // This code was adapted (and cleaned up) from Aaron Renn's 
            // BufferedOutputStream implementation from the Classpath project.

            reallyFlush();

            int iters = len / buffer.length;
            int leftoverStart = iters * buffer.length;
            int leftoverLen = len - leftoverStart;

            for (int i = 0; i < iters; i++)
                doWrite(b, off + (i * buffer.length), buffer.length);

            totalCount += leftoverStart;

            if ((len % buffer.length) != 0) {
                System.arraycopy(b, off + leftoverStart, buffer, bufferCount,
                                 leftoverLen);
                bufferCount += leftoverLen;
                totalCount += leftoverLen;
            }
        }
    }

    public void print(String s) throws IOException {
	if (s==null) s="null";
	int len = s.length();
	for (int i = 0; i < len; i++) {
	    char c = s.charAt (i);
	    
	    //
	    // XXX NOTE:  This is clearly incorrect for many strings,
	    // but is the only consistent approach within the current
	    // servlet framework.  It must suffice until servlet output
	    // streams properly encode their output.
	    //
	    if ((c & 0xff00) != 0) {	// high order byte must be zero
		String errMsg = sm.getString(
                    "servletOutputStream.fmt.not_iso8859_1", 
                     new Object[] {new Character(c)});
		throw new IOException(errMsg);
	    }
	    write(c);
	}	
    }

    // If a servlet is using PrintWriter then this method is NO-OP.
    public void flush() throws IOException {
	if (this.usingWriter == false)
	    reallyFlush();
    }

    public void reallyFlush() throws IOException {
	// 	System.out.println("x " + bufferCount+ " " + closed);
	try {
	    if (!committed) {
		//	        response.writeHeaders(out);
		sendHeaders();
	        committed = true;
	    }
    
	    if (bufferCount > 0) {
		//	        out.write(buffer, 0, bufferCount);
		doWrite( buffer, 0, bufferCount );
	    }
	}
	finally {    
	    bufferCount = 0;
	}
    }    

    public void close() throws IOException {
	reallyFlush();
	closed = true;
	//	out.close();
	endResponse();
    }

    public boolean isContentWritten() {
	return totalCount > 0 ? true : false;
    }

    public boolean isCommitted() {
	return this.committed;
    }
    
    public int getBufferSize() {
	return buffer.length;
    }
    
    public void setBufferSize(int size) throws IllegalStateException {
	
	// If requested size is less than current, this is a no-op.
	if (size <= bufferSize) {
            return;
        }
	
        // Make sure size is evenly divisible by 8K
        // This helps our buffer align with the PrintWriter buffer
        int eightK = 8*1024;
        if (size % eightK != 0) {
            size = ((size / eightK) + 1) * eightK;  // round up
        }

	// Allocate buffer of the requested size.
	bufferSize = size;
	buffer = new byte[size];
    }

    public void reset() throws IllegalStateException {

	// If buffer is already commited, throw IllegalStateException.
	if (isCommitted()) {
	    String msg = sm.getString("servletOutputStreamImpl.reset.ise"); 
	    throw new IllegalStateException(msg);
	}

	// Reset the buffer.
	bufferCount = 0;
	totalCount = 0;
    }

    /** Reuse the object instance, avoid GC
     */
    public void recycle() {
	// 	System.out.println("Recycle BOS " );
	bufferCount = 0;
	totalCount = 0;
	committed = false;
	closed = false;
    }

}

