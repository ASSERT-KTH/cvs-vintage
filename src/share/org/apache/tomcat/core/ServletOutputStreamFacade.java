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

import org.apache.tomcat.util.StringManager;
import java.io.*;
import javax.servlet.ServletOutputStream;

/**
 *  Facade to BufferedServletOutputStream - we need that to prevent any
 *  uncontroled access to public methods internal to tomcat and
 *  to isolate the servlet semantics from the internal implementation.
 *  ( and make sure the API is respected even if the internal objects
 *    have different behavior - for performance or integration )
 *
 * @author James Duncan Davidson [duncan@eng.sun.com]
 * @author Jason Hunter [jch@eng.sun.com]
 * @author James Todd [gonzo@eng.sun.com]
 * @author Mandar Raje [mandar@eng.sun.com]
 * @author Costin Manolache [costin@eng.sun.com]
 */
public class ServletOutputStreamFacade extends ServletOutputStream {
    protected StringManager sm = StringManager.getManager("org.apache.tomcat.core");
    // encoding
    private Writer writer=null;
    
    protected boolean closed = false;

    Response resA;
    BufferedServletOutputStream bos;

    /** Encoding - first time print() is used.
	IMPORTANT: print() is _bad_, if you want to write Strings and mix
	bytes you'd better use a real writer ( it's faster ).
	But _don't_ use the servlet writer - since you'll not be able to write
	bytes.
    */
    String enc;
    /** True if we already called getEncoding() - cache result */
    boolean gotEnc=false;
    
    protected ServletOutputStreamFacade( BufferedServletOutputStream bos, Response resA) {
	this.resA=resA;
	this.bos=bos;
    }

    // -------------------- Write methods --------------------
    
    public void write(int i) throws IOException {
	bos.write(i);
    }

    public void write(byte[] b) throws IOException {
	bos.write(b,0,b.length);
    }
    
    public void write(byte[] b, int off, int len) throws IOException {
	bos.write( b, off, len );
    }

    /** Alternate implementation for print, using String.getBytes(enc).
	It seems to be a bit faster for small strings, but it's 10..20% slower
	for larger texts ( nor very large - 5..10k )

	That seems to be mostly because of byte b[] - the writer has an
	internal ( and fixed ) buffer.

	Please use getWriter() if you want to send strings.
    */
    public void print(String s) throws IOException {
	if (s==null) s="null";
	byte b[]=null;
	if( !gotEnc ) {
	    enc = resA.getCharacterEncoding();
	    gotEnc=true;
	    if ( Constants.DEFAULT_CHAR_ENCODING.equals(enc) )
		enc=null;
	}
	if( enc==null) 
	    b=s.getBytes();
	else 
	    try {
		b=s.getBytes( enc );
	    } catch (java.io.UnsupportedEncodingException ex) {
		b=s.getBytes();
		enc=null;
	    } 
	
	write( b );
    } 
    
    /** We can use the writer to do the actual writing - that mean tomcat will
	have a writer and an OutputStream and will use both of them.
	Externaly ( i.e. at servlet level ) this is forbiden, but
	internaly it's the best solution to avoid the mess we had in
	I18N.

	This method is faster than print - but it's harder ( mixing
	writer/stream, flush issues. We may use it after we figure out the
	rest of the buffering issues.
    */
    private void printFast(String s) throws IOException {
	if (s==null) s="null";

	if( writer== null ) {
	    writer=resA.getWriter();
	    //	 XXX XXX   bos.setDisableFlush( true );
	    bos.setUsingWriter( true );
	}
	
	writer.write( s );
	writer.flush(); // the data will be in this buffer -

	// We need to flush so we can mix chars and bytes,
	// all will end up in our buffer in the right order ).

	// The main problem is that Writer.flushBuffer is not public
	// nor protected - so we can't use the same trick as in PrintStream.
	// This method seems to be 10..20% faster than calling
	// String.getBytes(enc) - but if it'll create problems we can use the
	// slower method.

	// It works because the writer will call flush on BSOS,
	// and that is disabled.
	//
	// The flush will work fine - if you are using the stream and
	//  call flush -> realFlush will be called.
	// If you are using the writer - you don't get here.
    }


    /** Will send the buffer to the client.
     */
    public void flush() throws IOException {
	bos.reallyFlush();
    }

    public void close() throws IOException {
	bos.reallyFlush();
	closed = true;
    }

    /** Reuse the object instance, avoid GC
     *  Called from BSOS
     */
    void recycle() {
	writer=null;
	closed = false;
	enc=null;
	gotEnc=false;
    }

}

