/*
 * $Header: /tmp/cvs-vintage/tomcat/src/share/org/apache/tomcat/core/Attic/ResponseImpl.java,v 1.7 2000/01/15 23:30:21 costin Exp $
 * $Revision: 1.7 $
 * $Date: 2000/01/15 23:30:21 $
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
    protected StringManager sm =
        StringManager.getManager(Constants.Package);

    protected Request request;
    protected HttpServletResponseFacade responseFacade;
    protected Vector userCookies = new Vector();
    protected Vector systemCookies = new Vector();
    protected String contentType = Constants.ContentType.Default;
    protected String contentLanguage = null;
    protected String characterEncoding = Constants.CharacterEncoding.Default;
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
    protected boolean omitHeaders = false;
    protected String serverHeader = null;

    String message;
    BufferedServletOutputStream sos=new BufferedServletOutputStream(this);
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

    public boolean isStarted() {
	return started;
    }

    public boolean isCommitted() {
	return committed;
    }

    public String getServerHeader() {
        return serverHeader;
    }

    public void setServerHeader(String serverHeader) {
        this.serverHeader = serverHeader;
    }

    public void setOmitHeaders(boolean omitHeaders) {
	this.omitHeaders = omitHeaders;
    }

    public void recycle() {
	userCookies.removeAllElements();
	systemCookies.removeAllElements();
	contentType = Constants.ContentType.Default;
        locale = new Locale(Constants.LOCALE_DEFAULT, "");
	characterEncoding = Constants.CharacterEncoding.Default;
	contentLength = -1;
	status = 200;
	headers.clear();
	usingWriter = false;
	usingStream = false;
	writer=null;
	out.recycle();
	started = false;
	committed = false;
	omitHeaders=false;

	// adapter
	sos.recycle();
	headers.clear();
	status=-1;
	message=null;
	body.setLength(0);
    }

    public void finish() throws IOException {
	try {
	    if (usingWriter && (writer != null)) {
	        writer.flush();
	    }
	    out.reallyFlush();
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

    public ServletOutputStream getOutputStream() {
	started = true;

	if (usingWriter) {
	    String msg = sm.getString("serverResponse.outputStream.ise");

	    throw new IllegalStateException(msg);
	}

	usingStream = true;

	return out;
    }

    public PrintWriter getWriter() throws IOException {
	started = true;

	if (usingStream) {
	    String msg = sm.getString("serverResponse.writer.ise");

	    throw new IllegalStateException(msg);
	}

	usingWriter = true;

	if (writer == null) {
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
	}

	out.setUsingWriter (usingWriter);

	return writer;
    }

    public void setDateHeader(String name, long date) {
	headers.putDateHeader(name, date);
    }

    public void addDateHeader(String name, long date) {
        headers.addDateHeader(name, date);
    }

    public void setHeader(String name, String value) {
	headers.putHeader(name, value);
    }

    public void addHeader(String name, String value) {
        headers.addHeader(name, value);
    }

    public void setIntHeader(String name, int value) {
	headers.putIntHeader(name, value);
    }

    public void addIntHeader(String name, int value) {
        headers.addIntHeader(name, value);
    }

    public int getBufferSize() {
	return out.getBufferSize();
    }

    public void setBufferSize(int size) throws IllegalStateException {

	// Force the PrintWriter to flush the data to the OutputStream.
	if (usingWriter == true) writer.flush();

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

	if (usingWriter == true)
	    writer.flush();

	// Reset the stream
	out.reset();

        // Clear the cookies and such

        // Clear the headers
        headers.clear();
    }

    public void flushBuffer() throws IOException {
	if (usingWriter == true)
	    writer.flush();

	out.reallyFlush();
    }


    /** Set server-specific headers */
    public void fixHeaders() throws IOException {
	//	System.out.println( "Fixing headers" );
	HttpDate date = new HttpDate(System.currentTimeMillis());
	headers.putHeader("Date", date.toString());

	headers.putIntHeader("Status", status);
        headers.putHeader("Content-Type", contentType);

	// Generated by Server!!!
	//headers.putDateHeader("Date",System.currentTimeMillis());
	if( getServerHeader()!=null)
	    headers.putHeader("Server",getServerHeader());
	if (contentLanguage != null) {
            headers.putHeader("Content-Language",contentLanguage);
        }

	// context is null if we are in a error handler before the context is
	// set ( i.e. 414, wrong request )
	if( request.getContext() != null)
	    headers.putHeader("Servlet-Engine", request.getContext().getEngineHeader());


        if (contentLength != -1) {
            headers.putIntHeader("Content-Length", contentLength);
        }

        // write cookies
        Enumeration cookieEnum = null;
        cookieEnum = systemCookies.elements();
        while (cookieEnum.hasMoreElements()) {
            Cookie c  = (Cookie)cookieEnum.nextElement();
            addHeader( CookieTools.getCookieHeaderName(c),
			       CookieTools.getCookieHeaderValue(c));
	    if( c.getVersion() == 1 ) {
		// add a version 0 header too.
		// XXX what if the user set both headers??
		Cookie c0 = (Cookie)c.clone();
		c0.setVersion(0);
		addHeader( CookieTools.getCookieHeaderName(c0),
				   CookieTools.getCookieHeaderValue(c0));
	    }
        }
	// XXX duplicated code, ugly
        cookieEnum = userCookies.elements();
        while (cookieEnum.hasMoreElements()) {
            Cookie c  = (Cookie)cookieEnum.nextElement();
            addHeader( CookieTools.getCookieHeaderName(c),
			       CookieTools.getCookieHeaderValue(c));
	    if( c.getVersion() == 1 ) {
		// add a version 0 header too.
		// XXX what if the user set both headers??
		Cookie c0 = (Cookie)c.clone();
		c0.setVersion(0);
		addHeader( CookieTools.getCookieHeaderName(c0),
				   CookieTools.getCookieHeaderValue(c0));
	    }
        }
	// XXX
        // do something with content encoding here
    }

    // XXX should be abstract
    public void endResponse() throws IOException {
	//	resA.endResponse();
    }

    // XXX should be abstract
    public void writeHeaders() throws IOException {
	if(omitHeaders)
	    return;

	setStatus( status, sm.getString("sc."+ status ));
	fixHeaders();
	addMimeHeaders( headers );
    }

    public void addCookie(Cookie cookie) {
	userCookies.addElement(cookie);
    }

    public void addSystemCookie(Cookie cookie) {
	systemCookies.addElement(cookie);
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

    public String constructLocalizedContentType(String type, Locale loc) {
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

    public void setContentLength(int contentLength) {
	this.contentLength = contentLength;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public void sendError(int sc) throws IOException {
	//	System.out.println("Send error " + sc );
	/*XXX*/ try {throw new Exception(); } catch(Exception ex) {ex.printStackTrace();}
	sendError(sc, "No detailed message");
    }

    public void sendError(int sc, String msg) throws IOException {
	// 	System.out.println("Send error " + sc + " " + msg);
	// 	System.out.println("Original request " + request.getRequestURI());
	// 	System.out.println(request.getContext().getClassPath());
	//	/*XXX*/ try {throw new Exception(); } catch(Exception ex) {ex.printStackTrace();}
	this.status = sc;

	Context context = request.getContext();

	if (context == null) {
	    sendPrivateError(sc, msg);

	    return;
	}

	ServletContextFacade contextFacade = context.getFacade();
	String path = context.getErrorPage(String.valueOf(sc));

	if (path != null) {
	    RequestDispatcher rd = contextFacade.getRequestDispatcher(path);
	    request.setAttribute("javax.servlet.error.status_code",
                String.valueOf(sc));
	    request.setAttribute("javax.servlet.error.message", msg);

	    try {
		reset();
		rd.forward(request.getFacade(), this.getFacade());
	    } catch (IllegalStateException ise) {
		// too late for a forward
		try {
		    rd.include(request.getFacade(), this.getFacade());
		} catch (ServletException se) {
		    sendPrivateError(sc, msg);
		}
	    } catch (ServletException se) {
		sendPrivateError(sc, msg);
	    }
	} else {
	    sendPrivateError(sc, msg);
	}

	// XXX
	// we only should set this if we are the head, not in an include

	close();
    }

    private void sendPrivateError(int sc, String msg) throws IOException {
	setContentType("text/html");

	StringBuffer buf = new StringBuffer();
	buf.append("<h1>Error: " + sc + "</h1>\r\n");
	buf.append(msg + "\r\n");

	// XXX
	// need to figure out if we are in an include better. The subclass
	// knows whether or not we are in an include!

	sendBodyText(buf.toString());
    }

    public void sendRedirect(String location) throws IOException {
	setStatus(HttpServletResponse.SC_MOVED_TEMPORARILY);
	//mimeType = null;
	setContentType(Constants.ContentType.HTML);	// ISO-8859-1 default

        location = makeAbsolute(location);

	setHeader("Location", location);

	StringBuffer buf = new StringBuffer();
	buf.append("<head><title>Document moved</title></head>\r\n");
	buf.append("<body><h1>Document moved</h1>\r\n");
	buf.append("This document has moved <a href=\"");
	buf.append(location);
	buf.append("\">here</a>.<p>\r\n");
	buf.append("</body>\r\n");

	String body = buf.toString();

	setContentLength(body.length());

	sendBodyText(body);

	close();
    }

    private String makeAbsolute(String location) {
        URL url = null;
        try {
	    // Try making a URL out of the location
	    // Throws an exception if the location is relative
            url = new URL(location);
	}
	catch (MalformedURLException e) {
	    String requrl = HttpUtils.getRequestURL(
                                request.getFacade()).toString();
	    try {
	        url = new URL(new URL(requrl), location);
	    }
	    catch (MalformedURLException ignored) {
	        // Give up
	        return location;
	    }
	}
        return url.toString();
    }

    public void sendBodyText(String s) throws IOException {
	try {
	    PrintWriter out = getWriter();
	    out.print(s);
	} catch (IllegalStateException ise) {
	    ServletOutputStream out = getOutputStream();
	    out.print(s);
	}
    }

    private void close() throws IOException {
	try {
	    PrintWriter out = getWriter();
	    out.close();
	} catch (IllegalStateException ise) {
	    ServletOutputStream out = getOutputStream();
	    out.close();
	}
    }


    
    /** Set the response status and message. 
     *	@param message null will set the "default" message, "" will send no message
     */ 
    public void setStatus( int status, String message) throws IOException {
	this.status=status;
	this.message=message;
    }

    // XXX This one or multiple addHeader?
    // Probably not a big deal - but an adapter may have
    // an optimized version for this one ( one round-trip only )
    public void addMimeHeaders(MimeHeaders headers) throws IOException {
	int size = headers.size();
        for (int i = 0; i < size; i++) {
            MimeHeaderField h = headers.getField(i);
            addHeader( h.getName(), h.getValue());
        }
    }

    /** Signal that we're done with the headers, and body will follow.
	The adapter doesn't have to maintain state, it's done inside the engine
    */
    public void endHeaders() throws IOException {

    }

    /** Either implement ServletOutputStream or return BufferedServletOutputStream(this)
	and implement doWrite();
     */
    public ServletOutputStream getServletOutputStream() throws IOException {
	return sos;
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

    public String getMessage() {
	return message;
    }

    public StringBuffer getBody() {
	return body;
    }

    
}
