/*
 * $Header: /tmp/cvs-vintage/tomcat/src/share/org/apache/tomcat/core/Attic/ResponseImpl.java,v 1.11 2000/02/01 21:39:39 costin Exp $
 * $Revision: 1.11 $
 * $Date: 2000/02/01 21:39:39 $
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

import java.io.*;
import java.net.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;
import org.apache.tomcat.util.*;
/**
 *
 * @author James Duncan Davidson [duncan@eng.sun.com]
 * @author Jason Hunter [jch@eng.sun.com]
 * @author James Todd [gonzo@eng.sun.com]
 * @author Harish Prabandham
 * @author Hans Bergsten <hans@gefionsoftware.com>
 */
public class ResponseImpl implements Response {
    protected static StringManager sm =
        StringManager.getManager("org.apache.tomcat.core");

    protected Request request;
    protected HttpServletResponseFacade responseFacade;
    protected Vector userCookies = new Vector();
    protected String contentType = Constants.ContentType.Default;
    protected String contentLanguage = null;
    protected String characterEncoding = Constants.CharacterEncoding.Default;
    protected String sessionId;
    protected int contentLength = -1;
    protected int status = 200;
    private Locale locale = new Locale(Constants.LOCALE_DEFAULT, "");

    protected MimeHeaders headers = new MimeHeaders();
    protected BufferedServletOutputStream out;
    protected PrintWriter writer;

    protected boolean usingStream = false;
    protected boolean usingWriter = false;
    protected boolean started = false;
    protected boolean committed = false;

    StringBuffer body=new StringBuffer();

    public ResponseImpl() {
        responseFacade = new HttpServletResponseFacade(this);
	out=new BufferedServletOutputStream();
	out.setResponse(this);
    }

    public HttpServletResponseFacade getFacade() {
	return responseFacade;
    }

    public void setRequest(Request request) {
	this.request = request;
    }

    public Request getRequest() {
	return request;
    }

    /* -------------------- */
    
    public boolean isStarted() {
	return started;
    }

    public void recycle() {
	userCookies.removeAllElements();
	contentType = Constants.ContentType.Default;
        locale = new Locale(Constants.LOCALE_DEFAULT, "");
	characterEncoding = Constants.CharacterEncoding.Default;
	contentLength = -1;
	status = 200;
	headers.clear();
	usingWriter = false;
	usingStream = false;
	sessionId=null;
	writer=null;
	out.recycle();
	started = false;
	committed = false;

	// adapter
	status=-1;
	body.setLength(0);
    }

    public void finish() throws IOException {
	try {
	    if (usingWriter && (writer != null)) {
	        writer.flush();
		writer.close();
	    }
	    out.reallyFlush();
	    out.close();
	} catch (SocketException e) {
	    return;  // munch
	} catch (IOException e) {
	    if("Broken pipe".equals(e.getMessage()))
		return;
	    throw e;
	}
    }

    public boolean containsHeader(String name) {
	return headers.containsHeader(name);
    }

    // XXX
    // mark whether or not we are being used as a stream our writer

    public boolean isUsingStream() {
	return usingStream;
    }
    
    public PrintWriter getWriter() throws IOException {
	if(writer!=null) return writer;
	// it already did all the checkings
	
	if (usingStream) {
	    String msg = sm.getString("serverResponse.writer.ise");
	    throw new IllegalStateException(msg);
	}

	started = true;
	usingWriter = true;

	String encoding = getCharacterEncoding();

	// XXX - EBCDIC issue here?

	if ((encoding == null) || "Default".equals(encoding) )
	    writer = new PrintWriter(new OutputStreamWriter(out));
	else
	    try {
		writer = new PrintWriter(new OutputStreamWriter(out, encoding));
	    } catch (java.io.UnsupportedEncodingException ex) {
		// if we don't do that, the runtime exception will propagate
		// and we'll try to send an error page - but surprise, we
		// still can't get the Writer to send the error page...
		writer = new PrintWriter( new OutputStreamWriter(out));
		
		// Deal with strange encodings - webmaster should see a message
		// and install encoding classes - n new, unknown language was discovered,
		// and they read our site!
		System.out.println("Unsuported encoding: " + encoding );
	    }
	out.setUsingWriter (true);

	return writer;
    }

    public void setHeader(String name, String value) {
	headers.putHeader(name, value);
    }

    public void addHeader(String name, String value) {
        headers.addHeader(name, value);
    }

    public int getBufferSize() {
	return out.getBufferSize();
    }

    public void setBufferSize(int size) throws IllegalStateException {

	// Force the PrintWriter to flush the data to the OutputStream.
	if (usingWriter == true && writer != null ) writer.flush();

	if (out.isContentWritten() == true) {
	    String msg = sm.getString("servletOutputStreamImpl.setbuffer.ise");
	    throw new IllegalStateException (msg);
	}
	out.setBufferSize(size);
    }

    /*
     * Methodname "isCommitted" already taken by Response class.
     */
    public boolean isBufferCommitted() {
	return out.isCommitted();
    }

    public void reset() throws IllegalStateException {
	// Force the PrintWriter to flush its data to the output
        // stream before resetting the output stream
        //
	userCookies.removeAllElements();  // keep system (session) cookies
	contentType = Constants.ContentType.Default;
        locale = new Locale(Constants.LOCALE_DEFAULT, "");
	characterEncoding = Constants.CharacterEncoding.Default;
	contentLength = -1;
	status = 200;

	if (usingWriter == true && writer != null)
	    writer.flush();

	// Reset the stream
	out.reset();

        // Clear the cookies and such

        // Clear the headers
        headers.clear();
    }

    public void flushBuffer() throws IOException {
	if (usingWriter == true && writer != null)
	    writer.flush();

	out.reallyFlush();
    }


    /** Signal that we're done with the headers, and body will follow.
     *  Any implementation needs to notify ContextManager, to allow
     *  interceptors to fix headers.
     */
    public void endHeaders() throws IOException {
	if(request.getProtocol()==null) // HTTP/0.9 
	    return;

	// let CM notify interceptors and give a chance to fix
	// the headers
	if(request.getContext() != null) 
	    request.getContext().getContextManager().doBeforeBody(request, this);

	// No action.. 
    }

    public void addCookie(Cookie cookie) {
	userCookies.addElement(cookie);
    }

    public Enumeration getCookies() {
	return userCookies.elements();
    }

    public void setSessionId( String id ) {
	sessionId=id;
    }

    public String getSessionId() {
	return sessionId;
    }
    
    public Locale getLocale() {
        return locale;
    }

    public void setLocale(Locale locale) {
        if (locale == null) {
            return;  // throw an exception?
        }

        // Save the locale for use by getLocale()
        this.locale = locale;

        // Set the contentLanguage for header output
        contentLanguage = locale.getLanguage();

        // Set the contentType for header output
        // Use the setContentType() method so encoding is set properly
        String newType = constructLocalizedContentType(contentType, locale);
        setContentType(newType);
    }

    /** Utility method for parsing the mime type and setting
     *  the encoding to locale. Also, convert from java Locale to mime encodings
    */
    private static String constructLocalizedContentType(String type, Locale loc) {
        // Cut off everything after the semicolon
        int semi = type.indexOf(";");
        if (semi != -1) {
            type = type.substring(0, semi);
        }

        // Append the appropriate charset, based on the locale
        String charset = LocaleToCharsetMap.getCharset(loc);
        if (charset != null) {
            type = type + "; charset=" + charset;
        }

        return type;
    }

    public String getCharacterEncoding() {
	return characterEncoding;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
	String encoding = RequestUtil.getCharsetFromContentType(contentType);
        if (encoding != null) {
	    characterEncoding = encoding;
        }
    }

    public String getContentType() {
	return contentType;
    }
    
    public void setContentLength(int contentLength) {
	this.contentLength = contentLength;
    }

    public int getContentLength() {
	return contentLength;
    }

    public int getStatus() {
        return status;
    }

    
    /** Set the response status 
     */ 
    public void setStatus( int status ) {
	this.status=status;
    }

    /** Either implement ServletOutputStream or return BufferedServletOutputStream(this)
	and implement doWrite();
     */
    public ServletOutputStream getOutputStream() {
	started = true;

	if (usingWriter) {
	    String msg = sm.getString("serverResponse.outputStream.ise");
	    throw new IllegalStateException(msg);
	}

	usingStream = true;

	return out;
    }
    

    /** Write a chunk of bytes. Should be called only from ServletOutputStream implementations,
     *	No need to implement it if your adapter implements ServletOutputStream.
     *  Headers and status will be written before this method is exceuted.
     */
    public void doWrite( byte buffer[], int pos, int count) throws IOException {
        // XXX fix if charset is other than default.
        body.append(new String(buffer, pos, count, 
                    Constants.CharacterEncoding.Default) );
    }

    public StringBuffer getBody() {
	return body;
    }

    // utility method - should be in a different class
    public static String getMessage( int status ) {
	return sm.getString("sc."+ status);
    }
    
}
