/*   
 *  Copyright 1999-2004 The Apache Sofware Foundation.
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

package org.apache.tomcat.core;

import java.io.IOException;
import java.util.Locale;

import org.apache.tomcat.util.res.StringManager;

import org.apache.tomcat.util.http.MimeHeaders;
import org.apache.tomcat.util.http.ContentType;

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

    public static final String DEFAULT_CHAR_ENCODING = "ISO-8859-1";

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

    // holds request error exception
    // set this just once during request processing
    protected Exception errorException=null;
    // holds request error URI
    protected String errorURI=null;

    // content type set by user, not including encoding
    protected String contentType = DEFAULT_CONTENT_TYPE;
    protected String contentLanguage = null;
    protected String characterEncoding = DEFAULT_CHAR_ENCODING;
    protected boolean haveCharacterEncoding = false;
    protected Locale locale = DEFAULT_LOCALE;

    // -------------------- Constructor --------------------
    
    public Response() {
    }

    // --------------------  --------------------
    
    /**  Init is called from CM when the object is added
	 to tomcat.
     */
    protected void init() {
	oBuffer=request.getContextManager().createOutputBuffer();
	oBuffer.setResponse( this );
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

    // -----------------Error State --------------------

    /** Set the error Exception that occurred during
	request processing.
     */
    public void setErrorException(Exception ex) {
	errorException = ex;
    }

    /** Get the Exception that occurred during request
	processing.
     */
    public Exception getErrorException() {
	return errorException;
    }

    public boolean isExceptionPresent() {
	return ( errorException != null );
    }

    /** Set request URI that caused an error during
	request processing.
     */
    public void setErrorURI(String uri) {
	errorURI = uri;
    }

    /** Get the request URI that caused the original error.
     */
    public String getErrorURI() {
	return errorURI;
    }

    // -------------------- Methods --------------------
    
    
    public void reset() throws IllegalStateException {
	if( ! included ) {
	    // Reset the headers only if this is the main request,
	    // not for included
	    contentType = DEFAULT_CONTENT_TYPE;
	    locale = DEFAULT_LOCALE;
	    characterEncoding = DEFAULT_CHAR_ENCODING;
	    haveCharacterEncoding = false;
	    status = 200;
	    headers.clear();
	}
	
	// Force the PrintWriter to flush its data to the output
	// stream before resetting the output stream
	//
	// Reset the stream
	if( commited ) {
	    String msg = sm.getString("servletOutputStreamImpl.reset.ise"); 
	    throw new IllegalStateException(msg);
	}
	oBuffer.reset();

    }

    public void finish() throws IOException {
	oBuffer.close();
	ContextManager cm=request.getContextManager();
	BaseInterceptor reqI[]= request.getContainer().
	    getInterceptors(Container.H_afterBody);

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
// 	if( name.equalsIgnoreCase( "Content-Length" ) ) {
// 	    try {
// 		int cL=Integer.parseInt( value );
// 		setContentLength( cL );
// 		return true;
// 	    } catch( NumberFormatException ex ) {
// 		// Do nothing - the spec doesn't have any "throws" 
// 		// and the user might know what he's doing
// 		return false;
// 	    }
// 	}
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

    // XXX XXX 
    /** Signal that we're done with the headers, and body will follow.
     *  Any implementation needs to notify ContextManager, to allow
     *  interceptors to fix headers.
     *  Note: This can be called during an included request.
     */
    public void notifyEndHeaders() throws IOException {
	commited=true;

	// let CM notify interceptors and give a chance to fix
	// the headers
	if(request.getContext() != null ) {
	    // call before body hooks
	    ContextManager cm=request.getContext().getContextManager();

	    BaseInterceptor reqI[]= request.getContainer().
		getInterceptors(Container.H_beforeBody);

	    // Since this can occur during an include, temporarily
	    // force included false and use top level request.
	    boolean saveIncluded = included;
	    included=false;

	    for( int i=0; i< reqI.length; i++ ) {
		reqI[i].beforeBody( request.getTop(), this );
	    }

	    // restore included state
	    included = saveIncluded;
	}
	
	// No action.. 
    }

    // -------------------- Buffer --------------------
    
    public int getBufferSize() {
	return oBuffer.getBufferSize();
    }

    public void setBufferSize(int size) throws IllegalStateException {
	if( !oBuffer.isNew() ) {
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

    /** Client-originated flush
     */
    public void clientFlush() throws IOException {
        // Nothing. Can be overriden.
    }

    // -------------------- I18N --------------------
    
    public Locale getLocale() {
        return locale;
    }

    /** Called explicitely by user to set the Content-Language and
     *  the default encoding
     */
    public void setLocale(Locale locale) {
        if (locale == null || included) {
            return;  // throw an exception?
        }

        // Save the locale for use by getLocale()
        this.locale = locale;

        // Set the contentLanguage for header output
        contentLanguage = locale.getLanguage();

	// Wrong: if setLocale is called after setContentType, it'll override
	// the user-value with the default value.
        // String newType = ContentType.constructLocalizedContentType(
	// contentType, locale);
	//        setContentType(newType);

	// Guessing charset from language is inexact - it's better to
	// not do it.
	// setContentType must take priority
	//	characterEncoding = LocaleToCharsetMap.getCharset( locale );

	// only one header !
	headers.setValue("Content-Language").setString( contentLanguage);
    }

    public String getCharacterEncoding(boolean defaultOK) {
	if(defaultOK || haveCharacterEncoding)
	    return characterEncoding;
	return null;
    }

    public String getCharacterEncoding() {
	return getCharacterEncoding(true);
    }

    public void setContentType(String contentType) {
        if( included ) return;
	this.contentType = contentType;
	String encoding = ContentType.getCharsetFromContentType(contentType);
        if (encoding != null) {
	    characterEncoding = encoding;
	    haveCharacterEncoding = true;
        }
	headers.setValue("Content-Type").setString( contentType);
    }

    public String getContentType() {
	return contentType;
    }
    
    public void setContentLength(int contentLength) {
        if( included ) return;
	headers.setValue("Content-Length").setInt(contentLength);
    }

    /** @deprecated. Not used in any piece of code, will fail for long values,
	it's not an acurate value of the length ( just the header ).
    */
    public int getContentLength() {
	String value=headers.getHeader( "Content-Length" );
	if( value == null )
	    return -1;
	try {
	    int cL=Integer.parseInt( value );
	    return cL;
	} catch( Exception ex ) {
	    return -1;
	}
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
	haveCharacterEncoding = false;
	status = 200;
	usingWriter = false;
	usingStream = false;
	commited = false;
	included=false;
	errorException=null;
	errorURI=null;
	if ( oBuffer != null ) oBuffer.recycle();
	headers.clear();
    }

}
