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

import org.apache.tomcat.util.*;
import java.io.*;
import java.net.*;
import java.security.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;


/**
 *
 */
public interface Request  {
    // Required fields

    public String getScheme() ;

    public String getMethod() ;

    public String getRequestURI() ;

    public String getQueryString() ;

    public String getProtocol() ;

    public String getHeader(String name) ;

    public Enumeration getHeaderNames() ;

    
    public ServletInputStream getInputStream() 	throws IOException;

    public String getServerName() ;

    public int getServerPort() ;
        
    public String getRemoteAddr() ;

    public String getRemoteHost() ;

    // Hints - will be set by Interceptors if not set
    // by adapter
    public String getLookupPath() ;

    public void setLookupPath( String l ) ;

    public String getAuthType() ;


    String getPathTranslated() ;

    public String getPathInfo() ;

    String getRemoteUser() ;

    boolean isSecure() ;
	
    Principal getUserPrincipal() ;

    boolean isUserInRole(String role) ;

    public String getRequestedSessionId() ;

    public void setRequestedSessionId(String reqSessionId) ;

    public String getServletPath() ;

    boolean isRequestedSessionIdFromCookie() ;

    boolean isRequestedSessionIdFromURL() ;

    
    public void updatePaths() ;//XXX - not to be used, use RD

    public void setContext(Context context) ;

    public HttpSession getSession(boolean create) ;

    boolean isRequestedSessionIdValid() ;

    public String getResolvedServlet() ;

    public void setResolvedServlet(String rs ) ;
    public ServletWrapper getWrapper() ;
    
    public void setWrapper(ServletWrapper handler) ;

    /** The file - result of mapping the request ( using aliases and other
     *  mapping rules. Usefull only for static resources.
     */
    public String getMappedPath() ;

    public void setMappedPath( String m ) ;

    public String getResourceName() ;

    public void setResourceName( String m ) ;

    public void setRequestURI( String r ) ;


    public void setParameters( Hashtable h ) ;

    public Hashtable getParameters() ;

    public void setContentLength( int  len ) ;

    public void setContentType( String type ) ;

    public void setCharEncoding( String enc ) ;

    public void setAuthType(String authType) ;


    public void setPathInfo(String pathInfo) ;

    /** Set query string - will be called by forward
     */
    public void setQueryString(String queryString) ;

    
    // -------------------- Computed fields - will be computed if not set
    // ( computed at invocation time ! )
    public Cookie[] getCookies() ;

    public Context getContext() ;

    public String[] getParameterValues(String name) ;

    public Enumeration getParameterNames() ;

    Hashtable getParametersCopy() ;

    public String getCharacterEncoding() ;

    public int getContentLength() ;

    public String getContentType() ;
    
    public HttpServletRequestFacade getFacade() ;

    public void setResponse(Response response) ;

    public Response getResponse() ;
    public RequestDispatcher getRequestDispatcher(String path);
    
    // -------------------- LookupResult 
    public void setSession(HttpSession serverSession) ;
    public void setServletPath(String servletPath) ;

    public void setServerName(String serverName) ;

    public Object getAttribute(String name) ;
    public void setAttribute(String name, Object value) ;

    public void removeAttribute(String name) ;

    public Enumeration getAttributeNames() ;

    // -------------------- Facade for MimeHeaders
    public long getDateHeader(String name) ;

    public Enumeration getHeaders(String name) ;

    public int getIntHeader(String name)  ;

    // -------------------- Utils - facade for RequestUtil
    public BufferedReader getReader() 	throws IOException;


    // -------------------- End utils
    public void recycle() ;

    public MimeHeaders getMimeHeaders();

    /** Return the cookies
     */
    public String[] getCookieHeaders();

    // server may have it pre-calculated - return null if
    // it doesn't
    public String getContextPath();

    // Servlet name ( a smart server may use aliases and rewriting !!! )
    public String getServletName();

    /** Fill in the buffer. This method is probably easier to implement than
	previous.
	This method should only be called from SerlvetInputStream implementations.
	No need to implement it if your adapter implements ServletInputStream.
     */
    public  int doRead( byte b[], int off, int len ) throws IOException;

    // XXX I hate this - but the only way to remove this method from the
    // inteface is to implement it on top of doRead(b[]).
    // Don't use this method if you can ( it is bad for performance !!)
    public int doRead() throws IOException;
    


}
