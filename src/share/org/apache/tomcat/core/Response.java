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
import java.net.*;
import java.util.*;
import org.apache.tomcat.util.*;
import org.apache.tomcat.helper.*;

/**
 *
 * @author James Duncan Davidson [duncan@eng.sun.com]
 * @author Jason Hunter [jch@eng.sun.com]
 * @author James Todd [gonzo@eng.sun.com]
 * @author Harish Prabandham
 * @author Hans Bergsten <hans@gefionsoftware.com>
 */
public class Response {
    
    public static final String DEFAULT_CONTENT_TYPE = "text/plain";

    public static final String DEFAULT_CHAR_ENCODING = "8859_1";

    public static final String LOCALE_DEFAULT="en";

    public static final Locale DEFAULT_LOCALE=new Locale(LOCALE_DEFAULT, "");

    // -------------------- fields --------------------
    protected static StringManager sm =
        StringManager.getManager("org.apache.tomcat.resources");

    // associated request 
    protected Request request;

    // facade
    protected Object responseFacade;

    // Response components
    protected int status = 200;
    protected MimeHeaders headers = new MimeHeaders();
    protected OutputBuffer oBuffer;

    // state
    protected boolean commited = false;
    protected boolean usingStream = false;
    protected boolean usingWriter = false;
    protected boolean included=false;

    // 
    protected String contentType = DEFAULT_CONTENT_TYPE;
    protected String contentLanguage = null;
    protected String characterEncoding = DEFAULT_CHAR_ENCODING;
    protected int contentLength = -1;
    private Locale locale = DEFAULT_LOCALE;

    // -------------------- Constructor --------------------
    
    public Response() {
    }

    // --------------------  --------------------
    
    /**  Init is called from CM when the object is added
	 to tomcat.
     */
    void init() {
	oBuffer=new OutputBuffer( this );
    }
    
    public Object getFacade() {
	return responseFacade;
    }

    /** Higher-level layer
     */
    public void setFacade(Object facade ) {
	responseFacade=facade;
    }

    /** Associated request
     */
    public void setRequest(Request request) {
	this.request = request;
    }

    public Request getRequest() {
	return request;
    }

    public OutputBuffer getBuffer() {
	return oBuffer;
    }
    
    public MimeHeaders getMimeHeaders() {
	return headers;
    }

    // -------------------- State --------------------

    // Included response behavior
    public boolean isIncluded() {
	return included;
    }

    public void setIncluded( boolean incl ) {
	included= incl;
    }

    public int getStatus() {
        return status;
    }
    
    /** Set the response status 
     */ 
    public void setStatus( int status ) {
	if( included ) return;
	this.status=status;
    }

    public boolean isUsingStream() {
	return usingStream;
    }

    public void setUsingStream( boolean stream ) {
	usingStream=stream;
    }
    
    public boolean isUsingWriter() {
	return usingWriter;
    }

    public void setUsingWriter( boolean writer ) {
	usingWriter=writer;
    }

    public boolean isBufferCommitted() {
	return commited;
    }

    public void setBufferCommitted( boolean v ) {
	this.commited=v;
    }

    // -------------------- Methods --------------------
    
    
    public void reset() throws IllegalStateException {
	// Force the PrintWriter to flush its data to the output
        // stream before resetting the output stream
        //
	contentType = DEFAULT_CONTENT_TYPE;
        locale = DEFAULT_LOCALE;
	characterEncoding = DEFAULT_CHAR_ENCODING;
	contentLength = -1;
	status = 200;

	// Reset the stream
	if( commited ) {
	    String msg = sm.getString("servletOutputStreamImpl.reset.ise"); 
	    throw new IllegalStateException(msg);
	}
	oBuffer.reset();

        // Clear the headers
        if( ! included) headers.clear();
    }

    public void finish() throws IOException {
        oBuffer.close();
	ContextManager cm=request.getContextManager();
	BaseInterceptor reqI[]= cm.
	    getInterceptors(request, Container.H_afterBody);

	for( int i=0; i< reqI.length; i++ ) {
	    reqI[i].afterBody( request, this );
	}
    }


    // -------------------- Headers --------------------
    public boolean containsHeader(String name) {
	return headers.getHeader(name) != null;
    }

    public void setHeader(String name, String value) {
	if( included ) return; // we are in included sub-request
	char cc=name.charAt(0);
	if( cc=='C' || cc=='c' ) {
	    if( checkSpecialHeader(name, value) )
		return;
	}
	headers.setValue(name).setString( value);
    }

    public void addHeader(String name, String value) {
	if( included ) return; // we are in included sub-request
	char cc=name.charAt(0);
	if( cc=='C' || cc=='c' ) {
	    if( checkSpecialHeader(name, value) )
		return;
	}
	headers.addValue(name).setString( value );
    }

    
    /** Set internal fields for special header names. Called from set/addHeader.
	Return true if the header is special, no need to set the header.
     */
    private boolean checkSpecialHeader( String name, String value) {
	// XXX Eliminate redundant fields !!!
	// ( both header and in special fields )
	if( name.equalsIgnoreCase( "Content-Type" ) ) {
	    setContentType( value );
	    return true;
	}
	if( name.equalsIgnoreCase( "Content-Length" ) ) {
	    try {
		int cL=Integer.parseInt( value );
		setContentLength( cL );
		return true;
	    } catch( NumberFormatException ex ) {
		// Do nothing - the spec doesn't have any "throws" 
		// and the user might know what he's doing
		return false;
	    }
	}
	if( name.equalsIgnoreCase( "Content-Language" ) ) {
	    // XXX XXX Need to construct Locale or something else
	}
	return false;
    }

    /** Signal that we're done with the headers, and body will follow.
     *  Any implementation needs to notify ContextManager, to allow
     *  interceptors to fix headers.
     */
    public void endHeaders() throws IOException {
	notifyEndHeaders();
    }

    /** Signal that we're done with the headers, and body will follow.
     *  Any implementation needs to notify ContextManager, to allow
     *  interceptors to fix headers.
     */
    public void notifyEndHeaders() throws IOException {
	commited=true;
	if(request.getProtocol()==null) // HTTP/0.9 
	    return;

	// let CM notify interceptors and give a chance to fix
	// the headers
	if(request.getContext() != null && ! included ) {
	    // call before body hooks
	    ContextManager cm=request.getContext().getContextManager();

	    BaseInterceptor reqI[]= cm.
		getInterceptors(request, Container.H_beforeBody);

	    for( int i=0; i< reqI.length; i++ ) {
		reqI[i].beforeBody( request, this );
	    }
	}
	
	// No action.. 
    }

    // -------------------- Buffer --------------------
    
    public int getBufferSize() {
	return oBuffer.getBufferSize();
    }

    public void setBufferSize(int size) throws IllegalStateException {
	// Force the PrintWriter to flush the data to the OutputStream.
	//	if (usingWriter == true && writer != null ) writer.flush();
        try{
            oBuffer.flushChars();
        }catch(IOException ex){
                ;
        }
	if( oBuffer.getBytesWritten() >0) {
	    throw new IllegalStateException ( sm.getString("servletOutputStreamImpl.setbuffer.ise"));
	}
	oBuffer.setBufferSize( size );
    }


    // Reset the response buffer but not headers and cookies
    public void resetBuffer() throws IllegalStateException {

	if( commited ) {
	    String msg = sm.getString("servletOutputStreamImpl.reset.ise"); 
	    throw new IllegalStateException(msg);
	}
	oBuffer.reset();
    }

    public void flushBuffer() throws IOException {
      oBuffer.flush();
    }


    // -------------------- I18N --------------------
    
    public Locale getLocale() {
        return locale;
    }

    public void setLocale(Locale locale) {
        if (locale == null || included) {
            return;  // throw an exception?
        }

        // Save the locale for use by getLocale()
        this.locale = locale;

        // Set the contentLanguage for header output
        contentLanguage = locale.getLanguage();

        // Set the contentType for header output
        // Use the setContentType() method so encoding is set properly
        String newType = RequestUtil.constructLocalizedContentType(contentType,
								   locale);
        setContentType(newType);

	// only one header !
	headers.setValue("Content-Language").setString( contentLanguage);
    }

    public String getCharacterEncoding() {
	return characterEncoding;
    }

    public void setContentType(String contentType) {
        if( included ) return;
	this.contentType = contentType;
	String encoding = RequestUtil.getCharsetFromContentType(contentType);
        if (encoding != null) {
	    characterEncoding = encoding;
        }
	headers.setValue("Content-Type").setString( contentType);
    }

    public String getContentType() {
	return contentType;
    }
    
    public void setContentLength(int contentLength) {
        if( included ) return;
	this.contentLength = contentLength;
	headers.setValue("Content-Length").setInt(contentLength);
    }

    public int getContentLength() {
	return contentLength;
    }

    // -------------------- Extend --------------------
    
    /** Write a chunk of bytes. Should be called only from ServletOutputStream implementations,
     *	No need to implement it if your adapter implements ServletOutputStream.
     *  Headers and status will be written before this method is exceuted.
     */
    public void doWrite( byte buffer[], int pos, int count) throws IOException {
	// do nothing.
	// This method must be overriden ( in the current setup ).

	// This should call a hook and follow the same patterns with
	// the rest of tomcat ( I'll do that - costin )
    }


    // --------------------
    
    public void recycle() {
	contentType = DEFAULT_CONTENT_TYPE;
	contentLanguage = null;
        locale = DEFAULT_LOCALE;
	characterEncoding = DEFAULT_CHAR_ENCODING;
	contentLength = -1;
	status = 200;
	usingWriter = false;
	usingStream = false;
	commited = false;
	included=false;
	if ( oBuffer != null ) oBuffer.recycle();
	headers.clear();
    }

}
