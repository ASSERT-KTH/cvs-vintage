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
import java.net.*;
import java.util.*;

/**
 * 
 * @author James Duncan Davidson [duncan@eng.sun.com]
 * @author James Todd [gonzo@eng.sun.com]
 * @author Jason Hunter [jch@eng.sun.com]
 */

public class Container {

    private Context context;
    private ServletClassLoader servletLoader;
    private Hashtable servlets = new Hashtable();
    private Hashtable prefixMappedServlets = new Hashtable();
    private Hashtable extensionMappedServlets = new Hashtable();
    private Hashtable pathMappedServlets = new Hashtable();
    private ServletWrapper defaultServlet = null;
    private URL servletBase = null;
    private Vector classPaths = new Vector();
    private Vector libPaths = new Vector();
    
    Container(Context context) {
	this.context = context;
    }

    Context getContext() {
	return context;
    }

    ServletClassLoader getLoader() {
	if(servletLoader == null) {
	    servletLoader = new ServletClassLoader(this);
	}

	return servletLoader;
    }

    void shutdown() {
	Enumeration enum = servlets.keys();

	while (enum.hasMoreElements()) {
	    String key = (String)enum.nextElement();
	    ServletWrapper wrapper = (ServletWrapper)servlets.get(key);

	    servlets.remove(key);
	    wrapper.destroy();
	}
    }

    public URL getServletBase() {
        return this.servletBase;
    }

    public void setServletBase(URL servletBase) {
        this.servletBase = servletBase;
    }

    public Enumeration getClassPaths() {
        return this.classPaths.elements();
    }

    public void addClassPath(String path) {
        this.classPaths.addElement(path);
    }

    public Enumeration getLibPaths() {
        return this.libPaths.elements();
    }

    public void addLibPath(String path) {
        this.libPaths.addElement(path);
    }

    /**
     * Add a servlet with the given name to the container. The
     * servlet will be loaded by the container's class loader
     * and instantiated using the given class name.
     */
    
    public void addServlet(String name, String className) {
        addServlet(name, null, className, null);
    }
 
    public void addServlet(String name, String className,
        String description) {
        addServlet(name, description, className, null);
    }

    public void addServlet(String name, Class clazz) {
        addServlet(name, null, null, clazz);
    }

    public void addServlet(String name, Class clazz,
	String description) {
        addServlet(name, description, null, clazz);
    }

    public void addJSP(String name, String path) {
        addJSP(name, null, path);
    }

    public void addJSP(String name, String path, String description) {
        // XXX
        // check for duplicates!

        ServletWrapper wrapper = new ServletWrapper(this);

	wrapper.setServletName(name);
	wrapper.setServletDescription(description);
	wrapper.setPath(path);

	servlets.put(name, wrapper);
    }

    /** True if we have a servlet with className.
     */
    boolean containsServlet(String className) {
        ServletWrapper[] sw = getServlets(className);

        return (sw != null &&
	    sw.length > 0);
    }

    /** Check if we have a servlet with the specified name
     */
    boolean containsServletByName(String name) {
	return (servlets.containsKey(name));
    }

    /** Remove all servlets with a specific class name
     */
    void removeServlet(String className) {
        removeServlets(getServlets(className));
    }

    /** Remove the servlet with a specific name
     */
    void removeServletByName(String servletName) {
	ServletWrapper wrapper=(ServletWrapper)servlets.get(servletName);
	if( wrapper != null ) {
	    ServletWrapper wa[]={wrapper};
	    removeServlets( wa );
	}
    }

    boolean containsJSP(String path) {
        ServletWrapper[] sw = getServletsByPath(path);

        return (sw != null &&
	    sw.length > 0);
    }

    void removeJSP(String path) {
        removeServlets(getServletsByPath(path));
    }

    public void setServletInitParams(String name, Hashtable initParams) {
	ServletWrapper wrapper = (ServletWrapper)servlets.get(name);

	if (wrapper != null) {
	    wrapper.setInitArgs(initParams);
	}
    }
    
    /**
     * Maps a named servlet to a particular path or extension.
     * If the named servlet is unregistered, it will be added
     * and subsequently mapped.
     *
     * Note that the order of resolution to handle a request is:
     *
     *    exact mapped servlet (eg /catalog)
     *    prefix mapped servlets (eg /foo/bar/*)
     *    extension mapped servlets (eg *jsp)
     *    default servlet
     *
     */

    public void addMapping(String servletName, String path) {
        ServletWrapper sw = (ServletWrapper)servlets.get(servletName);

	if (sw == null) {
	    // XXX
	    // this might be a bit aggressive

	    if (! servletName.startsWith("/")) {
	        addServlet(servletName, null, servletName, null);
	    } else {
	        addJSP(servletName, servletName);
	    }

	    sw = (ServletWrapper)servlets.get(servletName);
	}

	path = path.trim();

	if (sw != null &&
	    (path.length() > 0)) {
	    if (path.startsWith("/") &&
                path.endsWith("/*")){
	        prefixMappedServlets.put(path, sw);
	    } else if (path.startsWith("*.")) {
	        extensionMappedServlets.put(path, sw);
	    } else if (! path.equals("/")) {
	        pathMappedServlets.put(path, sw);
	    } else {
	        defaultServlet = sw;
	    }
	}
    }

    boolean containsMapping(String mapping) {
        mapping = mapping.trim();

        return (prefixMappedServlets.containsKey(mapping) ||
	    extensionMappedServlets.containsKey(mapping) ||
	    pathMappedServlets.containsKey(mapping));
    }

    void removeMapping(String mapping) {
        mapping = mapping.trim();

	prefixMappedServlets.remove(mapping);
	extensionMappedServlets.remove(mapping);
	pathMappedServlets.remove(mapping);
    }

    Request lookupServlet(String lookupPath) {
        RequestMapper requestMapper = new RequestMapper(this);

	requestMapper.setPathMaps(pathMappedServlets);
	requestMapper.setPrefixMaps(prefixMappedServlets);
	requestMapper.setExtensionMaps(extensionMappedServlets);

	Request lookupResult =
	    requestMapper.lookupServlet(lookupPath);

        if (lookupResult == null) {
	    ServletWrapper wrapper = null;

	    if (defaultServlet != null) {
	        wrapper = defaultServlet;
	    } else {
	        wrapper = (ServletWrapper)servlets.get(
		    Constants.Servlet.Default.Name);
	    }

	    String servletPath = Constants.Servlet.Default.Map;
            String pathInfo = lookupPath;

	    lookupResult = new Request();
	    lookupResult.setWrapper( wrapper );
	    lookupResult.setServletPath( servletPath );
	    lookupResult.setPathInfo( pathInfo );
	}

	return lookupResult;
    }

    Request lookupServletByName(String servletName) {
        Request lookupResult = null;

	ServletWrapper wrapper = (ServletWrapper)servlets.get(servletName);

	if (wrapper != null) {
	    lookupResult = new Request();
	    lookupResult.setWrapper( wrapper );
	    lookupResult.setPathInfo("");
	}

        return lookupResult;
    }

    ServletWrapper getServletByName(String servletName) {
	return (ServletWrapper)servlets.get(servletName);
    }

    ServletWrapper getServletAndLoadByName(String servletName) {
	// XXX
	// make sure that we aren't tramping over ourselves!
	ServletWrapper wrapper = new ServletWrapper(this);

	wrapper.setServletClass(servletName);

	servlets.put(servletName, wrapper);

	return wrapper;
    }

    ServletWrapper loadServlet(String servletClassName) {
        // XXX
        // check for duplicates!

        // XXX
        // maybe dispatch to addServlet?
        
        ServletWrapper wrapper = new ServletWrapper(this);

        wrapper.setServletClass(servletClassName);

        servlets.put(servletClassName, wrapper);

        return wrapper;
    }

    private void addServlet(String name, String description,
        String className, Class clazz) {
        // XXX
        // check for duplicates!

        if (servlets.get(name) != null) {
            removeServlet(name);
            removeServletByName(name);
        }

        ServletWrapper wrapper = new ServletWrapper(this);

	wrapper.setServletName(name);
	wrapper.setServletDescription(description);

	if (className != null) {
	    wrapper.setServletClass(className);
	}

	if (clazz != null) {
	    wrapper.setServletClass(clazz);
	}

	servlets.put(name, wrapper);
    }

    private void removeServlets(ServletWrapper[] sw) {
	if (sw != null) {
	    for (int i = 0; i < sw.length; i++) {
	        if (prefixMappedServlets.contains(sw[i])) {
		    Enumeration enum = prefixMappedServlets.keys();

		    while (enum.hasMoreElements()) {
		        String key = (String)enum.nextElement();

			if (prefixMappedServlets.get(key).equals(sw[i])) {
			    prefixMappedServlets.remove(key);
			}
		    }
		}

		if (extensionMappedServlets.contains(sw[i])) {
		    Enumeration enum = extensionMappedServlets.keys();

		    while (enum.hasMoreElements()) {
		        String key = (String)enum.nextElement();

			if (extensionMappedServlets.get(key).equals(sw[i])) {
			    extensionMappedServlets.remove(key);
			}
		    }
		}

		if (pathMappedServlets.contains(sw[i])) {
		    Enumeration enum = pathMappedServlets.keys();

		    while (enum.hasMoreElements()) {
		        String key = (String)enum.nextElement();

			if (pathMappedServlets.get(key).equals(sw[i])) {
			    pathMappedServlets.remove(key);
			}
		    }
		}

	        servlets.remove(sw[i].getServletName());
	    }
	}
    }

    /** Return servlets with a specified class name
     */
    private ServletWrapper[] getServlets(String name) {
        Vector servletWrappers = new Vector();
	Enumeration enum = servlets.keys();

	while (enum.hasMoreElements()) {
	    String key = (String)enum.nextElement();
	    ServletWrapper sw = (ServletWrapper)servlets.get(key);


            if (sw.getServletClass() != null &&
                sw.getServletClass().equals(name)) {
	        servletWrappers.addElement(sw);
	    }
	}

	ServletWrapper[] wrappers =
	    new ServletWrapper[servletWrappers.size()];

	servletWrappers.copyInto((ServletWrapper[])wrappers);

        return wrappers;
    }

    // XXX
    // made package protected so that RequestMapper can have access

    ServletWrapper[] getServletsByPath(String path) {
        Vector servletWrappers = new Vector();
	Enumeration enum = servlets.keys();

	while (enum.hasMoreElements()) {
	    String key = (String)enum.nextElement();
	    ServletWrapper sw = (ServletWrapper)servlets.get(key);

	    if (sw.getPath() != null &&
	        sw.getPath().equals(path)) {
	        servletWrappers.addElement(sw);
	    }
	}

	ServletWrapper[] wrappers =
	    new ServletWrapper[servletWrappers.size()];

	servletWrappers.copyInto((ServletWrapper[])wrappers);

        return wrappers;
    }
}
