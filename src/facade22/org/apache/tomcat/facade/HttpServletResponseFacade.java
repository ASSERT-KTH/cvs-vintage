/*
 * $Header: /tmp/cvs-vintage/tomcat/src/facade22/org/apache/tomcat/facade/HttpServletResponseFacade.java,v 1.33 2004/02/23 06:06:13 billbarker Exp $
 * $Revision: 1.33 $
 * $Date: 2004/02/23 06:06:13 $
 *
 *
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
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.util.Locale;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import org.apache.tomcat.core.Context;
import org.apache.tomcat.core.ContextManager;
import org.apache.tomcat.core.OutputBuffer;
import org.apache.tomcat.core.Request;
import org.apache.tomcat.core.Response;
import org.apache.tomcat.core.ServerSession;
import org.apache.tomcat.util.http.MimeHeaders;
import org.apache.tomcat.util.http.ServerCookie;
import org.apache.tomcat.util.net.URL;
import org.apache.tomcat.util.res.StringManager;

/**
 *
 * @author James Duncan Davidson [duncan@eng.sun.com]
 * @author Jason Hunter [jch@eng.sun.com]
 * @author James Todd [gonzo@eng.sun.com]
 * @author Costin Manolache
 * @author Hans Bergsten [hans@gefionsoftware.com]
 */
public final class HttpServletResponseFacade  implements HttpServletResponse
{
    // Use the strings from core
    private static StringManager sm =  StringManager.getManager("org.apache.tomcat.resources");

    private Response response;
    private boolean usingStream = false;
    private boolean usingWriter = false;
    ServletOutputStreamFacade osFacade=null;
    ServletWriterFacade writer;

    /** Package
     */
    HttpServletResponseFacade(Response response) {
        this.response = response;
	OutputBuffer oBuffer= response.getBuffer();
	writer = new ServletWriterFacade( oBuffer, response);
    }

    void recycle() {
	usingStream = false;
	usingWriter= false;
	if( writer.checkError() ) {
	    OutputBuffer oBuffer= response.getBuffer();
	    writer = new ServletWriterFacade( oBuffer, response);
	}
	if( osFacade != null ) osFacade.recycle();
    }

    // -------------------- Public methods --------------------

    public void addCookie(Cookie cookie) {
	if( response.isIncluded() ) return;
	// layer costs - this can be avoided, but it's not a
	// frequent operation ( for example sc can be reused )

	// XXX reuse
	StringBuffer sb=new StringBuffer();
	ServerCookie.appendCookieValue( sb, cookie.getVersion(),
				       cookie.getName(), cookie.getValue(),
				       cookie.getPath(), cookie.getDomain(),
				       cookie.getComment(), cookie.getMaxAge(),
				       cookie.getSecure());
	// the header name is Set-Cookie for both "old" and v.1 ( RFC2109 )
	// RFC2965 is not supported by browsers and the Servlet spec
	// asks for 2109.
	addHeader( "Set-Cookie", 
		   sb.toString());
    }

    public boolean containsHeader(String name) {
	return response.containsHeader(name);
    }

    /** Delegate to various components of tomcat. This is not
     *  part of response, but session code.
     */
    public String encodeRedirectURL(String location) {
	String absolute = toAbsolute(location);
	if (isEncodeable(absolute)) {
	    if( "".equals(location) ) {
		location = absolute;
	    }
	    return (toEncoded(location, response.getRequest().getSession(false)));
	} else {
	    return (location);
	}
    }

    /**
     * @deprecated
     */
    public String encodeRedirectUrl(String location) {
	return encodeRedirectURL(location);
    }

    public String encodeURL(String url) {
	String absolute = toAbsolute(url); 
	if (isEncodeable(absolute)) {
	    if( "".equals(url) ) {
		url = absolute;
	    }
	    return (toEncoded(url, response.getRequest().getSession(false)));
	} else {
	    return (url);
	}
    }

    /**
     * @deprecated
     */
    public String encodeUrl(String url) {
	return encodeURL(url);
    }

    public String getCharacterEncoding() {
	return response.getCharacterEncoding();
    }

    public ServletOutputStream getOutputStream() throws IOException {
	if ( usingWriter ) {
	    String msg = sm.getString("serverResponse.outputStream.ise");
	    throw new IllegalStateException(msg);
	}
	usingStream=true;
	// 	response.setUsingStream( true );

	if( osFacade!=null) return osFacade;
	//if( response.getOutputBuffer() != null ) {
	osFacade=new ServletOutputStreamFacade(response);
	// response.setServletOutputStream( osFacade );
	//}
	return osFacade;

// 	// old mechanism
// 	return response.getOutputStream();
// 	// response.getBufferedOutputStream().getServletOutputStreamFacade();
    }

    public PrintWriter getWriter() throws IOException {
	if (usingStream) {
	    String msg = sm.getString("serverResponse.writer.ise");
	    throw new IllegalStateException(msg);
	}
	usingWriter= true ;

	return writer;
    }

    public void sendError(int sc) throws IOException {
	sendError(sc, "No detailed message");
    }

    public void sendError(int sc, String msg) throws IOException {
	if (isCommitted()) {
	    Context ctx=response.getRequest().getContext();
	    ctx.log( "Servlet API error: sendError with commited buffer ", new Throwable("Trace"));
	    throw new IllegalStateException(sm.
					    getString("hsrf.error.ise"));
	}

	// 	if (sc != HttpServletResponse.SC_UNAUTHORIZED)	// CRM: FIXME
	// 	    response.resetBuffer();
	// Keep headers and cookies that are set

	setStatus( sc );
	Request request=response.getRequest();
	request.setAttribute("javax.servlet.error.message", msg);
	ContextManager cm=request.getContextManager();
	cm.handleStatus( request, response, sc );
    }

    public void sendRedirect(String location)
	throws IOException, IllegalArgumentException
    {
        if (location == null) {
            String msg = sm.getString("hsrf.redirect.iae");
            throw new IllegalArgumentException(msg);
	}

	// Even though DefaultErrorServlet will convert this
	// location to absolute (if required) we should do so
	// here in case the app has a non-default handler
	sendError(HttpServletResponse.SC_MOVED_TEMPORARILY,
		  toAbsolute(location));
    }

    public void setContentLength(int len) {
	response.setContentLength(len);
    }

    public void setContentType(String type) {
	response.setContentType(type);
    }

    public void setDateHeader(String name, long date) {
	if( ! response.isIncluded() ) {
	    MimeHeaders headers=response.getMimeHeaders();
	    headers.setValue( name ).setTime( date );
	}
    }

    public void addDateHeader(String name, long value) {
	if ( ! response.isIncluded() ) {
	    MimeHeaders headers=response.getMimeHeaders();
	    headers.addValue( name ).setTime( value );
	}
    }

    public void setHeader(String name, String value) {
	response.setHeader(name, value);
    }

    public void addHeader(String name, String value) {
	response.addHeader(name, value);
    }

    public void setIntHeader(String name, int value) {
	response.setHeader(name, Integer.toString(value));
    }

    public void addIntHeader(String name, int value) {
        response.addHeader(name, Integer.toString(value));
    }

    public void setStatus(int sc) {
	response.setStatus(sc);
    }

    public void setBufferSize(int size) throws IllegalStateException {
	response.setBufferSize(size);
    }

    public int getBufferSize() {
	return response.getBufferSize();
    }

    public void reset() throws IllegalStateException {
	response.reset();
    }

    public boolean isCommitted() {
	return response.isBufferCommitted();
    }

    public void flushBuffer() throws IOException {
	response.flushBuffer();
    }

    public void setLocale(Locale loc) {
        response.setLocale(loc);
    }

    public Locale getLocale() {
	return response.getLocale();
    }

    /**
     *
     * @deprecated
     */
    public void setStatus(int sc, String msg) {
	response.setStatus(sc);
    }


    // -------------------- Private methods --------------------

    /**
     * Return <code>true</code> if the specified URL should be encoded with
     * a session identifier.  This will be true if all of the following
     * conditions are met:
     * <ul>
     * <li>The request we are responding to asked for a valid session
     * <li>The requested session ID was not received via a cookie
     * <li>The specified URL points back to somewhere within the web
     *     application that is responding to this request
     * </ul>
     *
     * @param location Absolute URL to be validated
     **/
    private boolean isEncodeable(String location) {
	// Is this an intra-document reference?
	if (location.startsWith("#"))
	    return (false);

        // Are we in a valid session that is not using cookies?
	Request request = response.getRequest();
	ServerSession session = request.getSession(false);
	if(session == null || !session.isValid())
	    return false;
	// If the session is new, encode the URL
	if(!session.getTimeStamp().isNew() &&
		((HttpServletRequestFacade)request.getFacade()).
			isRequestedSessionIdFromCookie())
	    return false;

	// Is this a valid absolute URL?
	URL url = null;
	try {
	    url = new URL(location);
	} catch (MalformedURLException e) {
	    return (false);
	}
	// Does this URL match down to (and including) the context path?
	if (!request.scheme().equalsIgnoreCase(url.getProtocol()))
	    return (false);
	if (!request.serverName().equalsIgnoreCase(url.getHost()))
	    return (false);
        // Set the URL port to HTTP default if not available before comparing
        int urlPort = url.getPort();
        if (urlPort == -1) {
	    if("http".equalsIgnoreCase(url.getProtocol())) {
		urlPort = 80;
	    } else if ("https".equalsIgnoreCase(url.getProtocol())) {
		urlPort = 443;
            }
        }
	int serverPort = request.getServerPort();
	if (serverPort == -1)	// Work around bug in java.net.URL.getHost()
	    serverPort = 80;
	if (serverPort != urlPort)
	    return (false);
	String contextPath = request.getContext().getPath();
	if ((contextPath != null) && (contextPath.length() > 0)) {
	    String file = url.getFile();
	    if ((file == null) || !file.startsWith(contextPath))
		return (false);
	    // XXX endsWith() ? However, that confilicts with
	    // the ;charset= attribute.
	    if(file.indexOf(";jsessionid=" + session.getId()) >= 0)
		return (false); // Already encoded
	}

	// This URL belongs to our web application, so it is encodeable
	return (true);

    }


    /**
     * Convert (if necessary) and return the absolute URL that represents the
     * resource referenced by this possibly relative URL.  If this URL is
     * already absolute, return it unchanged.
     *
     * @param location URL to be (possibly) converted and then returned
     */
    private String toAbsolute(String location) {

	if (location == null)
	    return (location);

	// Construct a new absolute URL if possible (cribbed from
	// the DefaultErrorPage servlet)
	URL url = null;
	try {
	    url = new URL(location);
	} catch (MalformedURLException e1) {
	    Request request = response.getRequest();
	    String requrl = getRequestURL(request);
	    try {
		url = new URL(new URL(requrl), location);
	    } catch (MalformedURLException e2) {
		return (location);	// Give up
	    }
	}
	return (url.toExternalForm());

    }

    /**
     * Return the requested URL.
     * Should be moved to util.
     */
    private String getRequestURL(Request req) {
	StringBuffer sb = new StringBuffer();
	int port = req.getServerPort();
	String scheme = req.scheme().toString();
	sb.append(scheme).append("://");
	sb.append(req.serverName().toString());
	if(("http".equalsIgnoreCase(scheme) && port != 80) ||
	   ("https".equalsIgnoreCase(scheme) && port != 443)) {
	    sb.append(':').append(port);
	}
	sb.append(req.requestURI().toString());
	return sb.toString();
    }

	


    /**
     * Return the specified URL with the specified session identifier
     * suitably encoded.
     *
     * @param url URL to be encoded with the session id
     * @param session Session whose id is to be included in the encoded URL
     */
    private String toEncoded(String url, ServerSession session) {

	if ((url == null) || (session == null))
	    return (url);

	String sessionId = session.getId().toString();
	String path = null;
	String query = null;
	int question = url.indexOf("?");
	if (question < 0)
	    path = url;
	else {
	    path = url.substring(0, question);
	    query = url.substring(question);
	}
	String anchor = null;
	int hashP = path.indexOf("#");
	if(hashP >= 0) {
	    anchor = path.substring(hashP);
	    path = path.substring(0,hashP);
	}
	StringBuffer sb = new StringBuffer(path);
	if( sb.length() > 0) { // Can't have jsessionid first.
	    sb.append(";jsessionid=");
	    sb.append(sessionId);
	}
	if(anchor != null) 
	    sb.append(anchor);
	if (query != null)
	    sb.append(query);
	return (sb.toString());

    }
}
