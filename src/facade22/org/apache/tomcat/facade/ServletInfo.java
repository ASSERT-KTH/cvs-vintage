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

import java.util.Enumeration;
import java.util.Hashtable;

import javax.servlet.ServletConfig;

import org.apache.tomcat.core.BaseInterceptor;
import org.apache.tomcat.core.Context;
import org.apache.tomcat.core.Handler;
import org.apache.tomcat.core.TomcatException;
import org.apache.tomcat.util.collections.EmptyEnumeration;
import org.apache.tomcat.util.depend.DependManager;
import org.apache.tomcat.util.depend.Dependency;

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
