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
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.tomcat.facade;

import java.io.IOException;

import javax.servlet.ServletOutputStream;

import org.apache.tomcat.core.OutputBuffer;
import org.apache.tomcat.core.Response;

/**
 * 
 */
public final class ServletOutputStreamFacade extends ServletOutputStream {
    protected boolean closed = false;

    Response resA;
    OutputBuffer ob;
    
    /** Encoding - first time print() is used.
	IMPORTANT: print() is _bad_, if you want to write Strings and mix
	bytes you'd better use a real writer ( it's faster ).
	But _don't_ use the servlet writer - since you'll not be able to write
	bytes.
    */
    String enc;
    /** True if we already called getEncoding() - cache result */
    boolean gotEnc=false;
    
    protected ServletOutputStreamFacade( Response resA) {
	this.resA=resA;
	ob=resA.getBuffer();
    }

    // -------------------- Write methods --------------------
    
    public void write(int i) throws IOException {
	ob.writeByte(i);
    }

    public void write(byte[] b) throws IOException {
	write(b,0,b.length);
    }
    
    public void write(byte[] b, int off, int len) throws IOException {
	ob.write( b, off, len );
    }

    // -------------------- Servlet Output Stream methods --------------------
    
    /** Alternate implementation for print, using String.getBytes(enc).
	It seems to be a bit faster for small strings, but it's 10..20% slower
	for larger texts ( nor very large - 5..10k )

	That seems to be mostly because of byte b[] - the writer has an
	internal ( and fixed ) buffer.

	Please use getWriter() if you want to send strings.
    */
    public void print(String s) throws IOException {
// 	if (s==null) s="null";
// 	byte b[]=null;
// 	if( !gotEnc ) {
// 	    enc = resA.getCharacterEncoding();
// 	    gotEnc=true;
// 	    if ( Constants.DEFAULT_CHAR_ENCODING.equals(enc) )
// 		enc=null;
// 	}
// 	if( enc==null) 
// 	    b=s.getBytes();
// 	else 
// 	    try {
// 		b=s.getBytes( enc );
// 	    } catch (java.io.UnsupportedEncodingException ex) {
// 		b=s.getBytes();
// 		enc=null;
// 	    } 
	
// 	write( b );
	ob.write(s);
    } 

    /** Will send the buffer to the client.
     */
    public void flush() throws IOException {
	if( ob.flushCharsNeeded() )
	    ob.flushChars();
        ob.flushBytes();
        resA.clientFlush();
    }

    public void close() throws IOException {
	if( ob.flushCharsNeeded() )
	    ob.flushChars();
	ob.flushBytes();
	closed = true;
    }

    /** Reuse the object instance, avoid GC
     *  Called from BSOS
     */
    void recycle() {
	closed = false;
	enc=null;
	gotEnc=false;
    }
}

