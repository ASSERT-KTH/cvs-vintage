/*
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
 *  See the License for the specific language 
 */


package org.apache.tomcat.facade;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Enumeration;
import java.util.Vector;

import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.apache.tomcat.core.Context;
import org.apache.tomcat.core.ContextManager;
import org.apache.tomcat.core.Handler;
import org.apache.tomcat.util.compat.Jdk11Compat;
import org.apache.tomcat.util.io.FileUtil;
import org.apache.tomcat.util.res.StringManager;


/**
 * Implementation of the javax.servlet.ServletContext interface that
 * servlets see. Having this as a Facade class to the Context class
 * means that we can split up some of the work.
 *
 * @author James Duncan Davidson [duncan@eng.sun.com]
 * @author Jason Hunter [jch@eng.sun.com]
 * @author James Todd [gonzo@eng.sun.com]
 * @author Harish Prabandham
 */
public final class ServletContextFacade implements ServletContext {
    // Use the strings from core
    private StringManager sm = StringManager.getManager("org.apache.tomcat.resources");
    private ContextManager contextM;
    private Context context;
    Jdk11Compat jdk11Compat=Jdk11Compat.getJdkCompat();
    Object accessControlContext=null;
    
    ServletContextFacade(ContextManager server, Context context) {
        this.contextM = server;
        this.context = context;
	try {
	    accessControlContext=jdk11Compat.getAccessControlContext();
	} catch( Exception ex) {
	    ex.printStackTrace();
	}
    }

    Context getRealContext() {
	return context;
    }

    // -------------------- Public facade methods --------------------
    public ServletContext getContext(String path) {
        Context target=contextM.getContext(context, path);
	return (ServletContext)target.getFacade();
    }

    
    public Object getAttribute(String name) {
        return context.getAttribute(name);
    }

    public Enumeration getAttributeNames() {
        return context.getAttributeNames();
    }

    public void setAttribute(String name, Object object) {
        context.setAttribute(name, object);
    }

    public void removeAttribute(String name) {
        context.removeAttribute(name);
    } 
    
    public int getMajorVersion() {
	// hardcoded - this facade is only for 2.2
        return 2;
    }

    public int getMinorVersion() {
        return 2;
    }

    public String getMimeType(String filename) {
        return context.getMimeMap().getContentTypeFor(filename);
    }

    // Specific to servlet version and interpretation.
    public String getRealPath(String path) {
 	return FileUtil.safePath( context.getAbsolutePath(),
				  path);
    }

    public InputStream getResourceAsStream(String path) {
        InputStream is = null;
        try {
            URL url = getResource(path);
	    if( url==null ) return null;
            URLConnection con = url.openConnection();
            con.connect();
            is = con.getInputStream();
        } catch (MalformedURLException e) {
        } catch (IOException e) {
        }
	return is;
    }

    public URL getResource(String rpath) throws MalformedURLException {
	if (rpath == null) return null;

	String absPath=context.getAbsolutePath();
	String realPath=FileUtil.safePath( absPath, rpath);
	if( realPath==null ) {
	    log( "Unsafe path " + absPath + " " + rpath );
	    return null;
	}
	File f=new File( realPath );
	if( ! f.exists() ) {
	    return null;
	}
	try {
            return new URL("file", null, 0,realPath );
	} catch( IOException ex ) {
	    log("getting resource " + rpath, ex);
	    return null;
	}
    }

    public RequestDispatcher getRequestDispatcher(String path) {
	if ( path == null  || ! path.startsWith("/")) {
	    return null; // spec say "return null if we can't return a dispather
	}
	RequestDispatcherImpl rD=new RequestDispatcherImpl( context, accessControlContext);
	rD.setPath( path );
	
	return rD;
    }

    public RequestDispatcher getNamedDispatcher(String name) {
        if (name == null)
	    return null;

	// We need to do the checks
	Handler wrapper = context.getServletByName( name );
	if (wrapper == null)
	    return null;
	RequestDispatcherImpl rD=new RequestDispatcherImpl( context, accessControlContext );
	rD.setName( name );

	return rD;
    }

    public String getServerInfo() {
        return context.getEngineHeader();
    }

    public void log(String msg) {
	context.logServlet( msg, null );
    }

    public String getInitParameter(String name) {
        return context.getInitParameter(name);
    }

    public Enumeration getInitParameterNames() {
	return context.getInitParameterNames();
    }

    public void log(String msg, Throwable t) {
	context.logServlet(msg, t);
    }

    /**
     *
     * @deprecated This method is deprecated in the
     *             javax.servlet.ServletContext interface
     */
    public void log(Exception e, String msg) {
        log(msg, e);
    }

    /**
     *
     * @deprecated This method is deprecated in the
     *             javax.servlet.ServletContext interface
     */
    public Servlet getServlet(String name) throws ServletException {
        return null;
    }

    /**
     * This method has been deprecated in the public api and always
     * return an empty enumeration.
     *
     * @deprecated
     */
    public Enumeration getServlets() {
	// silly hack to get an empty enumeration
	Vector v = new Vector();
	return v.elements();
    }
    
    /**
     * This method has been deprecated in the public api and always
     * return an empty enumeration.
     *
     * @deprecated
     */
    public Enumeration getServletNames() {
	// silly hack to get an empty enumeration
	Vector v = new Vector();
	return v.elements();
    }

}
