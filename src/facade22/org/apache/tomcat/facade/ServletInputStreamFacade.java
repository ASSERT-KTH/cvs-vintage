/*
 *  Copyright 1999-2004 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language 
 */

package org.apache.tomcat.facade;

import java.io.IOException;

import javax.servlet.ServletInputStream;

import org.apache.tomcat.core.Request;

/**
 * This is the input stream returned by ServletRequest.getInputStream().
 * It is the adapter between the ServletInputStream interface expected
 * by webapps and Request.doRead() methods. 
 *
 * This will also deal with the "contentLength" limit.
 * <b>Important</b> Only the methods in ServletInputStream can be public.
 */
public final class ServletInputStreamFacade extends ServletInputStream {
    private int bytesRead = 0;
    // Stop after reading ContentLength bytes. 
    private int limit = -1;
    private boolean closed=false;

    private Request reqA;
    
    ServletInputStreamFacade() {
    }

    void prepare() {
	int contentLength = reqA.getContentLength();
	//System.out.println("ContentLength= " + contentLength);
	if (contentLength != -1) {
	    limit=contentLength;
	}
	bytesRead=0;
    }
    
    void setRequest(Request reqA ) {
	this.reqA=reqA;
    }

    void recycle() {
	limit=-1;
	closed=false;
    }

    // -------------------- ServletInputStream methods 

    public int read() throws IOException {
	if( dL>0) debug("read() " + limit + " " + bytesRead );
	if(closed)
	    throw new IOException("Stream closed");
	if (limit == -1) {
	    // Ask the adapter for more data. We are in the 'no content-length'
	    // case - i.e. chunked encoding ( acording to http spec CL is required
	    // for everything else.
	    int rd=reqA.doRead();
	    if( rd<0 ) {
		limit=0; // no more bytes can be read.
	    } else {
		bytesRead++; // for statistics
	    }
	    return rd;
	}

	// We have a limit
	if (bytesRead >= limit)
	    return -1;
	
	bytesRead++;
	int rd=reqA.doRead();
	if( rd<0 ) {
	    limit=0; // adapter detected EOF, before C-L finished.
	    // trust the adapter - if it returns EOF it's unlikely it'll give us
	    // any more data
	}
	return rd;
    }

    public int read(byte[] b) throws IOException {
	return read(b, 0, b.length);
    }

    public int read(byte[] b, int off, int len) throws IOException {
	if( dL>0) debug("read(" +  len + ") " + limit + " " + bytesRead );
	if(closed)
	    throw new IOException("Stream closed");
	if (limit == -1) {
	    int numRead = reqA.doRead(b, off, len);
	    if (numRead > 0) {
		bytesRead += numRead;
	    }
	    if( numRead< 0 ) {
		// EOF - stop reading
		limit=0;
	    }
	    return numRead;
	}

	if (bytesRead >= limit) {
	    return -1;
	}

	if (bytesRead + len > limit) {
	    len = limit - bytesRead;
	}
	int numRead = reqA.doRead(b, off, len);
	if (numRead > 0) {
	    bytesRead += numRead;
	}
	return numRead;
    }
    

    public int readLine(byte[] b, int off, int len) throws IOException {
	return super.readLine(b, off, len);
    }

    /** Close the stream
     *  Since we re-cycle, we can't allow the call to super.close()
     *  which would permantely disable us.
     */
    public void close() {
	closed=true;
    }

    private static int dL=0;
    private void debug( String s ) {
	System.out.println("ServletInputStreamFacade: " + s );
    }
}
