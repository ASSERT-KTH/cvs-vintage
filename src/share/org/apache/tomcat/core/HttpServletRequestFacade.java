/*
 * $Header: /tmp/cvs-vintage/tomcat/src/share/org/apache/tomcat/core/Attic/HttpServletRequestFacade.java,v 1.1 1999/10/09 00:30:05 duncan Exp $
 * $Revision: 1.1 $
 * $Date: 1999/10/09 00:30:05 $
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
 * The facade to the request that a servlet will see.
 *
 * @author James Duncan Davidson [duncan@eng.sun.com]
 * @author Jason Hunter [jch@eng.sun.com]
 * @author James Todd [gonzo@eng.sun.com]
 * @author Harish Prabandham
 */

public class HttpServletRequestFacade
implements HttpServletRequest {

    private StringManager sm =
        StringManager.getManager(Constants.Package);
    private Request request;
    private boolean usingStream = false;
    private boolean usingReader = false;
    
    public Request getRealRequest() {
	return request;
    }
    
    public HttpServletRequestFacade(Request request) {
        this.request = request;
    }

    public Object getAttribute(String name) {
	return request.getAttribute(name);
    }

    public Enumeration getAttributeNames() {
	return request.getAttributeNames();
    }

    public void setAttribute(String name, Object value) {
        request.setAttribute(name, value);
    }

    public void removeAttribute(String name) {
	request.removeAttribute(name);
    }
    
    public String getAuthType() {
	return request.getAuthType();
    }
    
    public String getCharacterEncoding() {
	return request.getCharacterEncoding();
    }

    public int getContentLength() {
        return request.getContentLength();
    }

    public String getContentType() {
	return request.getContentType();
    }

    public Cookie[] getCookies() {
	Vector cookies = request.getCookies();
	Cookie[] cookieArray = new Cookie[cookies.size()];

	for (int i = 0; i < cookies.size(); i ++) {
	    cookieArray[i] = (Cookie)cookies.elementAt(i);    
	}

        return cookieArray;
    }

    public long getDateHeader(String name) {
        return request.getDateHeader(name);
    }
    
    public String getHeader(String name) {
        return request.getHeader(name);
    }

    public Enumeration getHeaders(String name) {
        return request.getHeaders(name);
    }

    public Enumeration getHeaderNames() {
        return request.getHeaderNames();
    }
    
    public ServletInputStream getInputStream() throws IOException {
	if (usingReader) {
	    String msg = sm.getString("reqfac.getinstream.ise");

	    throw new IllegalStateException(msg);
	}

	usingStream = true;

	return request.getInputStream();
    }

    public int getIntHeader(String name) {
        return request.getIntHeader(name);
    }
    
    public String getMethod() {
        return request.getMethod();
    }

    public String getParameter(String name) {
        String[] values = getParameterValues(name);
        if (values != null) {
            return values[0];
        } else {
	    return null;
        }
    }

    public String[] getParameterValues(String name) {
        return request.getParameterValues(name);
    }

    public Enumeration getParameterNames() {
        return request.getParameterNames();
    }
    
    public String getPathInfo() {
        return request.getPathInfo();
    }

    public String getPathTranslated() {
        String pathTranslated = null;
	String pathInfo = getPathInfo();

	if (pathInfo != null) {
            if (pathInfo.equals("")) {
                pathInfo = "/";
            }

    	    try {
                URL url = 
		    request.getContext().getFacade().getResource(pathInfo);

                if (url != null &&
                    url.getProtocol().equals("file")) {
                    pathTranslated = FilePathUtil.patch(url.getFile());
                }
            } catch (MalformedURLException e) {
            }
        }
	
	// XXX
	// resolve this against the context

        return pathTranslated;
    }
    
    public String getProtocol() {
        return request.getProtocol();
    }

    public String getQueryString() {
        return request.getQueryString();
    }

    public String getRemoteUser() {
	// Using the Servlet 2.2 semantics ...
	//  return request.getRemoteUser();
	java.security.Principal p = getUserPrincipal();

	if (p != null) {
	    return p.getName();
	}

	return null;
    }

    public String getScheme() {
        return request.getScheme();
    }

    public String getServerName() {
	return request.getServerName();
    }

    public int getServerPort() {
        return request.getServerPort();
    }

    public HttpSession getSession() {
        return request.getSession();
    }
    
    public HttpSession getSession(boolean create) {
        return request.getSession(create);
    }

    public ServerSession getServerSession(boolean create) {
        return request.getServerSession(create);
    }

    public BufferedReader getReader() throws IOException {
	if (usingStream) {
	    String msg = sm.getString("reqfac.getreader.ise");

	    throw new IllegalStateException(msg);
	}

	usingReader = true;

	return request.getReader();
    }
    
    public String getRemoteAddr() {
        return request.getRemoteAddr();
    }

    public String getRemoteHost() {
        return request.getRemoteHost();
    }

    public String getRequestedSessionId() {
        return request.getRequestedSessionId();
    }
    
    public String getRequestURI() {
        return request.getRequestURI();
    }

    private Context getContext() {
	return getRealRequest().getContext();
    }

    public RequestDispatcher getRequestDispatcher(String path) {
        if (path == null) {
	    String msg = sm.getString("hsrf.dispatcher.iae", path);

	    throw new IllegalArgumentException(msg);
	}

	if (! path.startsWith("/")) {
	    String lookupPath = request.getLookupPath();

            // Cut off the last slash and everything beyond
	    int index = lookupPath.lastIndexOf("/");
	    lookupPath = lookupPath.substring(0, index);

            // Deal with .. by chopping dirs off the lookup path
	    while (path.startsWith("../")) { 
		if (lookupPath.length() > 0) {
		    index = lookupPath.lastIndexOf("/");
		    lookupPath = lookupPath.substring(0, index);
		} 
                else {
                    // More ..'s than dirs, return null
                    return null;
                }

		index = path.indexOf("../") + 3;
		path = path.substring(index);
	    }

	    path = lookupPath + "/" + path;
	}

	RequestDispatcher requestDispatcher =
	    getContext().getFacade().getRequestDispatcher(path);

        return requestDispatcher;
    }

    public boolean isSecure() {
	Context ctx = getContext();

	return ctx.getRequestSecurityProvider().isSecure(ctx, this);
    }

    public Locale getLocale() {
	return (Locale)getLocales().nextElement();
    }

    public Enumeration getLocales() {
        String acceptLanguage = getHeader(Constants.Header.AcceptLanguage);

        return getLocales(acceptLanguage);
    }

    public String getContextPath() {
        return getContext().getPath();
    }

    public boolean isUserInRole(String role) {
	Context ctx = getContext();

	return ctx.getRequestSecurityProvider().isUserInRole(ctx, this, role);
    }

    public Principal getUserPrincipal() {
	Context ctx = getContext();

	return ctx.getRequestSecurityProvider().getUserPrincipal(ctx, this);
    }

    public String getServletPath() {
        return request.getServletPath();
    }

    /**
     * @deprecated
     */
    
    public String getRealPath(String name) {
        return request.getContext().getFacade().getRealPath(name);
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

    public boolean isRequestedSessionIdFromCookie() {
        // XXX
	// yes, this is always true for now as cookies
	// are all we use....
	return true;
    }

    /**
     * @deprecated
     */
    
    public boolean isRequestedSessionIdFromUrl() {
	return isRequestedSessionIdFromURL();
    }

    public boolean isRequestedSessionIdFromURL() {
        // XXX
        return false;
    }

    private Enumeration getLocales(String acceptLanguage) {
        // Short circuit with an empty enumeration if null header
        if (acceptLanguage == null) {
            Vector def = new Vector();
            def.addElement(Locale.getDefault());
            return def.elements();
        }

        Hashtable languages = new Hashtable();

        StringTokenizer languageTokenizer =
            new StringTokenizer(acceptLanguage, ",");

        while (languageTokenizer.hasMoreTokens()) {
            String language = languageTokenizer.nextToken().trim();
            int qValueIndex = language.indexOf(';');
            int qIndex = language.indexOf('q');
            int equalIndex = language.indexOf('=');
            Double qValue = new Double(1);

            if (qValueIndex > -1 &&
                qValueIndex < qIndex &&
                qIndex < equalIndex) {
	        String qValueStr = language.substring(qValueIndex + 1);

                language = language.substring(0, qValueIndex);
                qValueStr = qValueStr.trim().toLowerCase();
                qValueIndex = qValueStr.indexOf('=');
                qValue = new Double(0);

                if (qValueStr.startsWith("q") &&
                    qValueIndex > -1) {
                    qValueStr = qValueStr.substring(qValueIndex + 1);

                    try {
                        qValue = new Double(qValueStr.trim());
                    } catch (NumberFormatException nfe) {
                    }
                }
            }

	    // XXX
	    // may need to handle "*" at some point in time

	    if (! language.equals("*")) {
	        String key = qValue.toString();
		Vector v = (Vector)((languages.containsKey(key)) ?
		    languages.get(key) : new Vector());

		v.addElement(language);
		languages.put(key, v);
	    }
        }

        if (languages.size() == 0) {
            Vector v = new Vector();

            v.addElement(Constants.Locale.Default);
            languages.put("1.0", v);
        }

        Vector l = new Vector();
        Enumeration e = languages.keys();

        while (e.hasMoreElements()) {
            String key = (String)e.nextElement();
            Vector v = (Vector)languages.get(key);
            Enumeration le = v.elements();

            while (le.hasMoreElements()) {
	        String language = (String)le.nextElement();
		String country = "";
		int countryIndex = language.indexOf("-");

		if (countryIndex > -1) {
		    country = language.substring(countryIndex + 1).trim();
		    language = language.substring(0, countryIndex).trim();
		}

                l.addElement(new Locale(language, country));
            }
        }

        return l.elements();
    }
}
