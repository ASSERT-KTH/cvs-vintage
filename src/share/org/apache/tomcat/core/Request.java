/*
 * $Header: /tmp/cvs-vintage/tomcat/src/share/org/apache/tomcat/core/Request.java,v 1.3 1999/10/24 16:53:18 costin Exp $
 * $Revision: 1.3 $
 * $Date: 1999/10/24 16:53:18 $
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
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;

/* XXX
   - add comments
   - more clean-up
   - make it minimal!
*/

/**
 * Server neutral form of the request from a client. Subclasses can
 * be specialized for socket or plug-in use.
 *
 * @author James Duncan Davidson [duncan@eng.sun.com]
 * @author Jason Hunter [jch@eng.sun.com]
 * @author James Todd [gonzo@eng.sun.com]
 * @author Harish Prabandham
 */

public interface Request {
    // Core parts of the request
    public String getMethod();
    
    public String getRequestURI();

    public String getProtocol();
    
    public String getQueryString();

    public String getScheme();
    
    public String getServerName();
    
    public int getServerPort();

    public String getRemoteAddr();
    
    public String getRemoteHost();

    public  ServletInputStream getInputStream() throws IOException;
    
    public  BufferedReader getReader() throws IOException;


    
    // Attributes - we can have the attribute stuff in a
    // separate class
    public Object getAttribute(String name);

    public void setAttribute(String name, Object value);

    public void removeAttribute(String name);

    public Enumeration getAttributeNames();


    // Parameters
    public String[] getParameterValues(String name);

    public Enumeration getParameterNames();

    
    /** Return the parsed Cookies
     */
    public Cookie[] getCookies();
    // Original:    public Vector getCookies();

    // Context
    // after it is found, allow as to recalculate all other "gets"
    public void setContext(Context context); 
    
    public String getLookupPath();

    public Context getContext();

    public String getPathInfo();
    
    public void setPathInfo(String pathInfo);

    public String getServletPath();


    // Session
    // XXX right now the code assume ServerSessionManager will be
    // used, need to figure out how to use the session Id passed from Apache
    public ApplicationSession getSession();

    public ApplicationSession getSession(boolean create);
    
    public String getRequestedSessionId();

    public ServerSession getServerSession(boolean create);
    
    /** Hook required to support getRequestSessionId using ServerSession
	manager.
	If getSession use the default ServerSessionManager, it will call back
	setting requestedSessionId.
    */
    public void setRequestedSessionId(String reqSessionId);
    


    // Authentication
    public String getAuthType();
    
    public void setAuthType(String authType);


    // Headers and special Headers
    public String getCharacterEncoding();

    public int getContentLength();
    
    public String getContentType();

    public String getRemoteUser();

    public  long getDateHeader(String name);
    
    public  String getHeader(String name);

    public  Enumeration getHeaders(String name);
    
    public  int getIntHeader(String name);
    
    public  Enumeration getHeaderNames();
    

    // Setters - should go away
//     public void setCharacterEncoding(String charEncoding);
    
    public void setQueryString(String queryString);// used in ForwardRequest
    
//     public void setScheme(String scheme);
    

//     // ????
    public void setServerSession(ServerSession serverSession);// Used by Context.handleRequest ?
    
    public void setServletPath(String servletPath);// Used by Context.handleRequest ?
    
//     // Internal 
//     // Does Request needs to know the Response?
    public void setResponse(Response response);// XXX used only in ServletWrapper - shouldn't
    public  void setURI(String requestURI); // XXX used in ServletWrapper, need to clean up 

    // One to one Request - Facade
    public HttpServletRequestFacade getFacade();


    // Removed methods:
    
    // Should go to RequestUtil
    //public  void readFormData();
    //public Hashtable mergeParameters(Hashtable one, Hashtable two);

    // Connector-specific, maybe in a separate interface
    public  void recycle(); // used in strange places - ServletWrapper
}
