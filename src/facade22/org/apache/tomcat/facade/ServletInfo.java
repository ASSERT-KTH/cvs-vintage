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
package org.apache.tomcat.facade;

import org.apache.tomcat.core.*;
import org.apache.tomcat.util.*;
import org.apache.tomcat.util.depend.*;
import org.apache.tomcat.util.collections.*;
import java.io.*;
import java.net.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;

/**
 * Class used to represent a servlet inside a Context.
 *
 * It will deal with all servlet-specific issues:
 * - load on startup
 * - servlet class name ( dynamic loading )
 * - init parameters
 * - security roles/mappings ( per servlet )
 * - jsp that acts like a servlet ( web.xml )
 * - reloading
 * 
 * @author James Duncan Davidson [duncan@eng.sun.com]
 * @author Jason Hunter [jch@eng.sun.com]
 * @author James Todd [gonzo@eng.sun.com]
 * @author Harish Prabandham
 * @author costin@dnt.ro
 */
public final class ServletInfo {

    // the actual tomcat handler associated with this servlet
    private ServletHandler handler;

    // facade
    private ServletConfig configF;

    // optional informations
    private String description = null;

    private Hashtable securityRoleRefs=new Hashtable();

    private Hashtable initArgs=null;

    // should be removed from handler
    private String jspFile = null;
    private int loadOnStartup=-1;
    private boolean loadingOnStartup=false;

    public ServletInfo() {
	handler=new ServletHandler();
	handler.setServletInfo( this );
    }

    public ServletInfo( ServletHandler handler ) {
	this.handler=handler;
	handler.setServletInfo( this );
    }

    public String toString() {
	return "SW (" + jspFile + " CN=" +
	    handler.getServletClassName() +  ")";
    }

    // -------------------- Configuration hook
    /** This method can called to add the servlet to the web application.
     * ( typlically used from the config - WebXmlReader ).
     */
    public void addServlet(Context ctx, WebXmlReader wxR)
	throws TomcatException
    {
	// set the owner module for the servlet.
	// Even if the servlets are defined in WebXmlReader, they should
	// belong to Servlet22Interceptor. ( it's easy to set WebXmlReader,
	// but it's more intuitive to set debug and options on Servlet22 )
	BaseInterceptor mods[]=ctx.getContainer().getInterceptors();
	for( int i=0; i<mods.length; i++ ) {
	    if( mods[i] instanceof Servlet22Interceptor ) {
		handler.setModule( mods[i] );
		break;
	    }
	}
	// if not found - then we don't have a Servlet22Interceptor.
	// That means a configuration problem.
	if( handler.getModule() == null )
	    throw new TomcatException("Can't find Servlet22Interceptor");
	ctx.addServlet( handler );
	handler.setContext( ctx );
    }

    public void setContext(Context ctx ) {
	handler.setContext( ctx );
    }
    
    public Context getContext() {
	return handler.getContext();
    }

    Handler getHandler() {
	return handler;
    }

    // -------------------- Init parameters --------------------

    /** Add configuration properties associated with this handler.
     *  This is a non-final method, handler may override it with an
     *  improved/specialized version.
     */
    public void addInitParam( String name, String value ) {
	if( initArgs==null) {
	    initArgs=new Hashtable();
	}
	initArgs.put( name, value );
    }

    public String getInitParameter(String name) {
	if (initArgs != null) {
            return (String)initArgs.get(name);
        } else {
            return null;
        }
    }
    
    public Enumeration getInitParameterNames() {
        if (initArgs != null) {
            return initArgs.keys();
        } else {
	    return EmptyEnumeration.getEmptyEnumeration();
	}
    }

    // -------------------- Servlet specific properties 
    public void setLoadOnStartUp( int level ) {
	loadOnStartup=level;
	// here setting a level implies loading
	loadingOnStartup=true;
    }

    public void setLoadOnStartUp( String level ) {
	if (level.length() > 0)
	    loadOnStartup=new Integer(level).intValue();
	else
	    loadOnStartup=-1;
	// here setting a level implies loading
	loadingOnStartup=true;
    }

    public int getLoadOnStartUp() {
	return loadOnStartup;
    }

    public boolean getLoadingOnStartUp() {
	return loadingOnStartup;
    }

    public String getServletName() {
	return handler.getName();
    }

    public void setServletName(String servletName) {
	handler.setName(servletName);
    }

    public String getServletDescription() {
        return this.description;
    }

    public void setServletDescription(String description) {
        this.description = description;
    }

    public String getServletClassName() {
	return handler.getServletClassName();
    }

    public void setServletClassName(String servletClassName) {
	handler.setServletClassName( servletClassName );
    }
    
    /** Security Role Ref represent a mapping between servlet role names and
     *  server roles
     */
    public void addSecurityMapping( String name, String role,
				    String description ) {
	securityRoleRefs.put( name, role );
    }

    public String getSecurityRole( String name ) {
	return (String)securityRoleRefs.get( name );
    }

    // -------------------- Jsp specific code
    
    public String getJspFile() {
        return this.jspFile;
    }

    public void setJspFile(String path) {
        this.jspFile = path;
	if( handler.getName() == null ) 
	    handler.setName(jspFile);
	// the path will serve as servlet name if not set
    }
    
    DependManager dependM;

    public DependManager getDependManager() {
	return dependM;
    }

    public void setDependManager(DependManager dep ) {
	dependM=dep;
    }

    Dependency dependency;

    /** @deprecated this supports only one depend per
	jsp/servlet. Wrong.
    */
    public Dependency getDependency() {
	return dependency;
    }

    /** @deprecated this supports only one depend per
	jsp/servlet. Wrong.
    */
    public void setDependency(Dependency dep ) {
	dependency=dep;
    }

    public void setDescription(String s ) {
	
    }

    // -------------------- 

    public ServletConfig getServletConfig() {
	if( configF==null )
	    configF=new ServletConfigImpl( this );
	return configF;
    }
    
}
