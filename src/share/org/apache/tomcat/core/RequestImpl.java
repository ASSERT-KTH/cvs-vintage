/*
 * $Header: /tmp/cvs-vintage/tomcat/src/share/org/apache/tomcat/core/Attic/RequestImpl.java,v 1.11 2000/02/01 21:39:38 costin Exp $
 * $Revision: 1.11 $
 * $Date: 2000/02/01 21:39:38 $
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

import org.apache.tomcat.util.*;
import java.io.*;
import java.net.*;
import java.security.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;


/**
 *
 * @author James Duncan Davidson [duncan@eng.sun.com]
 * @author James Todd [gonzo@eng.sun.com]
 * @author Jason Hunter [jch@eng.sun.com]
 * @author Harish Prabandham
 * @author Alex Cruikshank [alex@epitonic.com]
 */
public class RequestImpl  implements Request {
    // XXX used by forward to override, need a better
    // mechanism
    protected String requestURI;
    protected String queryString;

   //  RequestAdapterImpl Hints
    protected String serverName;
    protected Vector cookies = new Vector();

    protected String contextPath;
    protected String lookupPath; // everything after contextPath before ?
    protected String servletPath;
    protected String pathInfo;

    protected Hashtable parameters = new Hashtable();
    protected int contentLength = -1;
    protected String contentType = null;
    protected String charEncoding = null;
    protected String authType;
    protected String remoteUser;

    // Request
    protected Response response;
    protected HttpServletRequestFacade requestFacade;
    protected Context context;
    protected Hashtable attributes = new Hashtable();

    protected boolean didReadFormData;
    protected boolean didParameters;
    protected boolean didCookies;
    // end "Request" variables

    // Session
    // set by interceptors - the session id
    protected String reqSessionId;
    boolean sessionIdFromCookie=true;
    boolean sessionIdFromURL=false;
    // cache- avoid calling SessionManager for each getSession()
    protected HttpSession serverSession;


    // LookupResult - used by sub-requests and
    // set by interceptors
    ServletWrapper handler = null;
    String mappedPath = null;
    String resolvedServlet = null;
    String resouceName=null;

    protected String scheme;
    protected String method;
    protected String protocol;
    protected MimeHeaders headers;
    protected ServletInputStream in;
    
    protected int serverPort;
    protected String remoteAddr;
    protected String remoteHost;


    protected static StringManager sm =
        StringManager.getManager("org.apache.tomcat.core");

    public RequestImpl() {
 	headers = new MimeHeaders();
 	recycle(); // XXX need better placement-super()
    }

    public String getScheme() {
        return scheme; 
    }

    public String getMethod() {
        return method; 
    }

    public String getRequestURI() {
        if( requestURI!=null) return requestURI;
	return requestURI; 
    }

    // XXX used by forward
    public String getQueryString() {
	if( queryString != null ) return queryString;
        return queryString; 
    }

    public String getProtocol() {
        return protocol; 
    }

    // XXX server IP and/or Host:
    public String getServerName() {
	if(serverName!=null) return serverName;

	// XXX Move to interceptor!!!
	String hostHeader = this.getHeader("host");
	if (hostHeader != null) {
	    int i = hostHeader.indexOf(':');
	    if (i > -1) {
		hostHeader = hostHeader.substring(0,i);
	    }
	    serverName=hostHeader;
	    return serverName;
	}
	// default to localhost - and warn
	System.out.println("No server name, defaulting to localhost");
	serverName="localhost";
	return serverName;
    }

    public String getLookupPath() {
	return lookupPath;
    }

    public void setLookupPath( String l ) {
	lookupPath=l;
    }

    public String[] getParameterValues(String name) {
	handleParameters();
        return (String[])parameters.get(name);
    }

    public Enumeration getParameterNames() {
	handleParameters();
        return parameters.keys();
    }

    /**
     * Used by the RequestDispatcherImpl to get a copy of the
     * original parameters before adding parameters from the
     * query string, if any.
     */
    public Hashtable getParametersCopy() {
	handleParameters();
	return (Hashtable) parameters.clone();
    }

    public String getAuthType() {
    	return authType;
    }

    public String getCharacterEncoding() {
        if(charEncoding!=null) return charEncoding;
        charEncoding = RequestUtil.getCharsetFromContentType( getContentType());
	return charEncoding;
    }

    public int getContentLength() {
        if( contentLength > -1 ) return contentLength;
	contentLength = getIntHeader("content-length");
	return contentLength;
    }

    public String getContentType() {
	if(contentType != null) return contentType;
	contentType = getHeader("content-type");
	if(contentType != null) return contentType;
	// can be null!! -
	return contentType;
    }
    
    public String getPathTranslated() {
	try {
	    URL url = context.getResourceURL(this);
	    
	    if (url != null &&	url.getProtocol().equals("file")) {
		return FileUtil.patch(url.getFile());
	    }
	} catch (MalformedURLException e) {
	}
	return null;
    }


    public String getPathInfo() {
        return pathInfo;
    }

    public String getRemoteUser() {
	// Using the Servlet 2.2 semantics ...
	//  return request.getRemoteUser();
	java.security.Principal p = getUserPrincipal();

	if (p != null) {
	    return p.getName();
	}

	return null;

        //return remoteUser;
    }

    public boolean isSecure() {
	if( context.getRequestSecurityProvider() == null )
	    return false;
	return context.getRequestSecurityProvider().isSecure(context, getFacade());
    }

    public RequestDispatcher getRequestDispatcher(String path) {
        if (path == null)
	    return null;

	if (! path.startsWith("/")) {
	    path= FileUtil.catPath( getLookupPath(), path );
	    if( path==null) return null;
	}

	return context.getRequestDispatcher(path);
    }


    public Principal getUserPrincipal() {
	if( context.getRequestSecurityProvider() == null )
	    return null;
	return context.getRequestSecurityProvider().getUserPrincipal(context, getFacade());
    }

    public boolean isUserInRole(String role) {
	if( context.getRequestSecurityProvider() == null )
	    return false;
	return context.getRequestSecurityProvider().isUserInRole(context, getFacade(), role);
    }


    public String getRequestedSessionId() {
        return reqSessionId;
    }

    public void setRequestedSessionId(String reqSessionId) {
	this.reqSessionId = reqSessionId;
    }

    public String getServletPath() {
        return servletPath;
    }

    // End hints

    // -------------------- Request methods ( high level )
    public HttpServletRequestFacade getFacade() {
	// some requests are internal, and will never need a
	// facade - no need to create a new object unless needed.
        if( requestFacade==null )
	    requestFacade = new HttpServletRequestFacade(this);
	return requestFacade;
    }

    public Context getContext() {
	return context;
    }

    public void setResponse(Response response) {
	this.response = response;
    }

    public Response getResponse() {
	return response;
    }

    public boolean isRequestedSessionIdFromCookie() {
	return sessionIdFromCookie;
    }

    public boolean isRequestedSessionIdFromURL() {
	return sessionIdFromURL;
    }

    
    public void setContext(Context context) {
	this.context = context;
    }

    // Called after a Context is found, adjust all other paths.
    // XXX XXX XXX
    public void updatePaths() {
	contextPath = context.getPath();
	String requestURI = getRequestURI();
	// do not set it if it is already set or we have no
	// URI - the case of a sub-request generated internally
	if( requestURI!=null && lookupPath==null ) 
	    lookupPath = requestURI.substring(contextPath.length(),
					      requestURI.length());

	// check for ? string on lookuppath
	int qindex = lookupPath.indexOf("?");

	if (qindex > -1) {
	    lookupPath = lookupPath.substring(0, qindex);
	}

	if (lookupPath.length() < 1) {
	    lookupPath = "/";
	}
    }


    public Cookie[] getCookies() {
	// XXX need to use Cookie[], Vector is not needed
	if( ! didCookies ) {
	    // XXX need a better test
	    // XXX need to use adapter for hings
	    didCookies=true;
	    RequestUtil.processCookies( this, cookies );
	}

	Cookie[] cookieArray = new Cookie[cookies.size()];

	for (int i = 0; i < cookies.size(); i ++) {
	    cookieArray[i] = (Cookie)cookies.elementAt(i);
	}

	return cookieArray;
	//        return cookies;
    }

    public HttpSession getSession(boolean create) {
	// use the cached value 
	if( serverSession!=null )
	    return serverSession;

	SessionManager sM=context.getSessionManager();
	
	// if the interceptors found a request id, use it
	if( reqSessionId != null ) {
	    // we have a session !
	    serverSession=sM.findSession( context, reqSessionId );
	    if( serverSession!=null) return serverSession;
	}
	
	if( ! create )
	    return null;

	// no session exists, create flag
	serverSession =sM.createSession( context );
	reqSessionId = serverSession.getId();

	// XXX XXX will be changed - post-request Interceptors
	// ( to be defined) will set the session id in response,
	// SessionManager is just a repository and doesn't deal with
	// request internals.
	// hardcoded - will change!
	response.setSessionId( reqSessionId );
	
	return serverSession;
    }

    public boolean isRequestedSessionIdValid() {
	// so here we just assume that if we have a session it's,
	// all good, else not.
	HttpSession session = (HttpSession)getSession(false);

	if (session != null) {
	    return true;
	} else {
	    return false;
	}
    }

    // -------------------- LookupResult 
    public String getResolvedServlet() {
	return resolvedServlet;
    }

    public void setResolvedServlet(String rs ) {
	resolvedServlet=rs;
    }

    public ServletWrapper getWrapper() {
	return handler;
    }
    
    public void setWrapper(ServletWrapper handler) {
	this.handler=handler;
    }

    /** The file - result of mapping the request ( using aliases and other
     *  mapping rules. Usefull only for static resources.
     */
    public String getMappedPath() {
	return mappedPath;
    }

    public void setMappedPath( String m ) {
	mappedPath=m;
    }

    public String getResourceName() {
	return resouceName;
    }

    public void setResourceName( String m ) {
	resouceName=m;
    }

    public void setRequestURI( String r ) {
 	this.requestURI=r;
    }

    public void setParameters( Hashtable h ) {
	if(h!=null)
	    this.parameters=h;
	// XXX Should we override query parameters ??
    }

    public Hashtable getParameters() {
	return parameters;
    }

    public void setContentLength( int  len ) {
	this.contentLength=len;
    }

    public void setContentType( String type ) {
	this.contentType=type;
    }

    public void setCharEncoding( String enc ) {
	this.charEncoding=enc;
    }

    public void setAuthType(String authType) {
        this.authType = authType;
    }


    public void setPathInfo(String pathInfo) {
	///*DEBUG*/ try {throw new Exception(); } catch(Exception ex) {ex.printStackTrace();}
        this.pathInfo = pathInfo;
    }

    /** Set query string - will be called by forward
     */
    public void setQueryString(String queryString) {
	// the query will be processed when getParameter() will be called.
	// Or - if you alredy have it parsed, call setParameters()
	this.queryString = queryString;
    }
    
    public void setSession(HttpSession serverSession) {
	this.serverSession = serverSession;
    }

    public void setServletPath(String servletPath) {
	this.servletPath = servletPath;
    }


    // XXX
    // the server name should be pulled from a server object of some
    // sort, not just set and got.

    /** Virtual host */
    public void setServerName(String serverName) {
	this.serverName = serverName;
    }

    // -------------------- Attributes
    public Object getAttribute(String name) {
        return attributes.get(name);
    }

    public void setAttribute(String name, Object value) {
	if(name!=null && value!=null)
	    attributes.put(name, value);
    }

    public void removeAttribute(String name) {
	attributes.remove(name);
    }

    public Enumeration getAttributeNames() {
        return attributes.keys();
    }
    // End Attributes

    // -------------------- Facade for MimeHeaders
    public long getDateHeader(String name) {
	//return reqA.getMimeHeaders().getDateHeader(name);
	return getMimeHeaders().getDateHeader(name);
    }

    public Enumeration getHeaders(String name) {
	//	Vector v = reqA.getMimeHeaders().getHeadersVector(name);
	Vector v = getMimeHeaders().getHeadersVector(name);
	return v.elements();
    }

    public int getIntHeader(String name)  {
	//        return reqA.getMimeHeaders().getIntHeader(name);
        return getMimeHeaders().getIntHeader(name);
    }

    // -------------------- Utils - facade for RequestUtil
    public BufferedReader getReader()
	throws IOException {
	return RequestUtil.getReader( this );
    }

    private void handleParameters() {
   	if(!didParameters) {
	    String qString=getQueryString();
	    if(qString!=null) {
		didParameters=true;
		RequestUtil.processFormData( qString, parameters );
	    }
	}
	if (!didReadFormData) {
	    didReadFormData = true;
	    Hashtable postParameters=RequestUtil.readFormData( this );
	    if(postParameters!=null)
		parameters = RequestUtil.mergeParameters(parameters, postParameters);
	}
    }

    // -------------------- End utils
    public void recycle() {
	response = null;
	context = null;
        attributes.clear();
        parameters.clear();
        cookies.removeAllElements();
	//        requestURI = null;
	//        queryString = null;
        contentLength = -1;
        contentType = null;
        charEncoding = null;
        authType = null;
        remoteUser = null;
        reqSessionId = null;
	serverSession = null;
	didParameters = false;
	didReadFormData = false;
	didCookies = false;

	scheme = "http";// no need to use Constants
	method = "GET";
	requestURI="/";
	queryString=null;
	protocol="HTTP/1.0";
	headers.clear(); // XXX use recycle pattern
	serverName="localhost";
	serverPort=8080;

	// XXX a request need to override those if it cares
	// about security
	remoteAddr="127.0.0.1";
	remoteHost="localhost";

    }

    public MimeHeaders getMimeHeaders() {
	return headers;
    }
    
    public String getHeader(String name) {
        return headers.getHeader(name);
    }

    public Enumeration getHeaderNames() {
        return headers.names();
    }
    
    public ServletInputStream getInputStream() throws IOException {
    	return in;    
    }

    public int getServerPort() {
        return serverPort;
    }
    
    public String getRemoteAddr() {
        return remoteAddr;
    }
    
    public String getRemoteHost() {
	return remoteHost;
    }    

    // you need to override this method if you want non-empty InputStream
    public  int doRead( byte b[], int off, int len ) throws IOException {
	return -1; // not implemented - implement getInputStream 
    }

    // you need to override this method if you want non-empty InputStream
    public int doRead() throws IOException {
	return -1;
    }
    
    // -------------------- "cooked" info --------------------
    // Hints = return null if you don't know,
    // and Tom will find the value. You can also use the static
    // methods in RequestImpl

    /** Return the parsed Cookies
     */
    public String[] getCookieHeaders() {
	return null;
    }

    // server may have it pre-calculated - return null if
    // it doesn't
    public String getContextPath() {
	return null;
    }

    // What's between context path and servlet name ( /servlet )
    // A smart server may use arbitrary prefixes and rewriting
    public String getServletPrefix() {
	return null;
    }

    // Servlet name ( a smart server may use aliases and rewriting !!! )
    public String getServletName() {
	return null;
    }

    public void setScheme( String scheme ) {
	this.scheme=scheme;
    }

    public void setMethod( String method ) {
	this.method=method;
    }

    public void setProtocol( String protocol ) {
	this.protocol=protocol;
    }

    public void setMimeHeaders( MimeHeaders headers ) {
	this.headers=headers;
    }

    public void setBody( StringBuffer body ) {
	// ??? 
    }

    public void setServerPort(int serverPort ) {
	this.serverPort=serverPort;
    }

    public void setRemoteAddr( String remoteAddr ) {
	this.remoteAddr=remoteAddr;
    }

    public void setRemoteHost(String remoteHost) {
	this.remoteHost=remoteHost;
    }

    public String toString() {
	StringBuffer sb=new StringBuffer();
	sb.append( "Request( " + context ).append("\n");
	sb.append( "    URI:" + getRequestURI()  ).append("\n");
	sb.append( "    SP:" + getServletPath() );
	sb.append( ",PI:" + getPathInfo() );
	sb.append( ",LP:" + getLookupPath() );
	sb.append( ",MP:" + getMappedPath() );
	sb.append( "," + getWrapper() +") ");
	return sb.toString();
    }


    // utility method - should be in a different class
    public static String getMessage( int status ) {
	return sm.getString("sc."+ status);
    }
}
