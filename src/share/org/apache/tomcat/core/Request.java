/*
 * $Header: /tmp/cvs-vintage/tomcat/src/share/org/apache/tomcat/core/Request.java,v 1.1 1999/10/09 00:30:15 duncan Exp $
 * $Revision: 1.1 $
 * $Date: 1999/10/09 00:30:15 $
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


/**
 * Server neutral form of the request from a client. Subclasses can
 * be specialized for socket or plug-in use.
 *
 * @author James Duncan Davidson [duncan@eng.sun.com]
 * @author Jason Hunter [jch@eng.sun.com]
 * @author James Todd [gonzo@eng.sun.com]
 * @author Harish Prabandham
 */

public abstract class Request {

    protected Response response;
    protected HttpServletRequestFacade requestFacade;
    protected String scheme = Constants.Request.HTTP;
    protected Context context;
    protected Hashtable attributes = new Hashtable();
    protected Hashtable parameters = new Hashtable();
    protected Vector cookies = new Vector();
    protected String protocol;

    protected String requestURI;
    protected String contextPath;
    protected String lookupPath;
    protected String servletPath;
    protected String pathInfo;
    protected String queryString;
    
    protected String method;
    protected int contentLength = -1;
    protected String contentType = "";
    protected String charEncoding = null;
    protected String authType;
    protected String remoteUser;
    protected String reqSessionId;
    protected ServerSession serverSession;
    protected boolean didReadFormData;
    
    public Request() {
        requestFacade = new HttpServletRequestFacade(this);
    }

    public HttpServletRequestFacade getFacade() {
	return requestFacade;
    }

    void setURI(String requestURI) {
        this.requestURI = requestURI;
    }

    void setContext(Context context) {
	this.context = context;
	contextPath = context.getPath();
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

    public String getLookupPath() {
	return lookupPath;
    }
    
    public Context getContext() {
	return context;
    }

    // XXX - changed from package protected to public so
    //       that we can call this from ConnectionHandler.java

    public void setResponse(Response response) {
	this.response = response;
    }
    
    protected void recycle() {
	response = null;
	scheme = Constants.Request.HTTP;
	context = null;
        attributes.clear();
        parameters.clear();
        cookies.removeAllElements();
        method = null;
	protocol = null;
        requestURI = null;
        queryString = null;
        contentLength = -1;
        contentType = "";
        charEncoding = null;
        authType = null;
        remoteUser = null;
        reqSessionId = null;
	serverSession = null;
	didReadFormData = false;
    }
    
    public Object getAttribute(String name) {
        return attributes.get(name);
    }

    public void setAttribute(String name, Object value) {
        attributes.put(name, value);
    }

    public void removeAttribute(String name) {
	attributes.remove(name);
    }
    
    public Enumeration getAttributeNames() {
        return attributes.keys();
    }
    
    public String[] getParameterValues(String name) {
	if (!didReadFormData) {
	    readFormData();
	}

        return (String[])parameters.get(name);
    }
    
    public Enumeration getParameterNames() {
	if (!didReadFormData) {
	    readFormData();
	}

        return parameters.keys();
    }
    
    private void readFormData() {
	didReadFormData = true;

	if (contentType != null &&
            contentType.equals("application/x-www-form-urlencoded")) {

	    try {
		ServletInputStream is=getInputStream();
                Hashtable postParameters =
		    HttpUtils.parsePostData(contentLength, is);
		parameters = mergeParameters(parameters, postParameters);
	    }
	    catch (IOException e) {
		// nothing
	    }
        }
    }

    private Hashtable mergeParameters(Hashtable one, Hashtable two) {
	// Try some shortcuts
	if (one.size() == 0) {
	    return two;
	}

	if (two.size() == 0) {
	    return one;
	}

	Hashtable combined = (Hashtable) one.clone();

        Enumeration e = two.keys();

	while (e.hasMoreElements()) {
	    String name = (String) e.nextElement();
	    String[] oneValue = (String[]) one.get(name);
	    String[] twoValue = (String[]) two.get(name);
	    String[] combinedValue;

	    if (oneValue == null) {
		combinedValue = twoValue;
	    }

	    else {
		combinedValue = new String[oneValue.length + twoValue.length];

	        System.arraycopy(oneValue, 0, combinedValue, 0,
                    oneValue.length);
	        System.arraycopy(twoValue, 0, combinedValue,
                    oneValue.length, twoValue.length);
	    }

	    combined.put(name, combinedValue);
	}

	return combined;
    }

    public ApplicationSession getSession() {
        return getSession(true);
    }

    ServerSession getServerSession(boolean create) {
	if (context == null) {
	    System.out.println("CONTEXT WAS NEVER SET");
	    return null;
	}

	if (serverSession == null && create) {
            serverSession =
		ServerSessionManager.getManager()
		    .getServerSession(this, response, create);
            serverSession.accessed();
	}

	return serverSession;
    }
    
    public ApplicationSession getSession(boolean create) {
	getServerSession(create);
	ApplicationSession appSession = null;
	if (serverSession != null) {
	    appSession = serverSession.getApplicationSession(context, create);
	}

	return appSession;

	//  if (reqSessionId != null) {
//  	    //Session session = context.getSession(reqSessionId);
//  	    //if (session == null) {
//  	    //session = context.createSession(reqSessionId);
//  	    //}
//  	    //return session;
//  	    System.out.println("DANGER, SESSIONS ARE NOT WORKING");
//  	} else {
//  	    if (create) {
//  		Session session = serverSession.createSession(response);
//  		return session;
//  	    } else {
//  		return null;
//  	    }
//  	}
    }

    public String getRequestURI() {
        return requestURI;
    }
    
    public String getAuthType() {
    	return authType;    
    }
    
    public void setAuthType(String authType) {
        this.authType = authType;
    }

    public String getCharacterEncoding() {
        return charEncoding;
    }

    public void setCharacterEncoding(String charEncoding) {
	this.charEncoding = charEncoding;
    }
    
    public int getContentLength() {
        return contentLength;
    }
    
    public String getContentType() {
    	return contentType;   
    }
    
    Vector getCookies() {
        return cookies;
    }
    
    public abstract long getDateHeader(String name);
    
    public abstract String getHeader(String name);

    public abstract Enumeration getHeaders(String name);
    
    public abstract int getIntHeader(String name);
    
    public abstract Enumeration getHeaderNames();
    
    public abstract ServletInputStream getInputStream()
    throws IOException;
    
    public abstract BufferedReader getReader()
    throws IOException;
        
    public String getMethod() {
        return method;
    }
    
    public String getPathInfo() {
        return pathInfo;
    }
    
    public void setPathInfo(String pathInfo) {
        this.pathInfo = pathInfo;
    }
    
    public String getProtocol() {
        return protocol;
    }
    
    public String getQueryString() {
        return queryString;
    }

    public void setQueryString(String queryString) {
        this.queryString = queryString;
    }
    
    public String getRemoteUser() {
        return remoteUser;
    }
    
    public String getScheme() {
        return scheme;
    }
    
    public void setScheme(String scheme) {
        this.scheme = scheme;
    }
    
    public abstract String getServerName();
    
    public abstract int getServerPort();

    public abstract String getRemoteAddr();
    
    public abstract String getRemoteHost();

    void setRequestedSessionId(String reqSessionId) {
	this.reqSessionId = reqSessionId;
    }
    
    public String getRequestedSessionId() {
        return reqSessionId;
    }

    void setServerSession(ServerSession serverSession) {
	this.serverSession = serverSession;
    }
    
    void setServletPath(String servletPath) {
	this.servletPath = servletPath;
    }
    
    public String getServletPath() {
        return servletPath;
    }
}
