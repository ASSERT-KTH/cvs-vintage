/*   
 *  Copyright 1999-2004 The Apache Sofware Foundation.
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

package org.apache.tomcat.modules.mappers;

import java.io.File;
import java.util.Enumeration;
import java.util.Vector;

import org.apache.tomcat.core.BaseInterceptor;
import org.apache.tomcat.core.Container;
import org.apache.tomcat.core.Context;
import org.apache.tomcat.core.ContextManager;
import org.apache.tomcat.core.Handler;
import org.apache.tomcat.core.Request;
import org.apache.tomcat.core.TomcatException;
import org.apache.tomcat.util.depend.DependClassLoader;
import org.apache.tomcat.util.depend.DependManager;
import org.apache.tomcat.util.depend.Dependency;

/**
 * This interceptor deals with context reloading.
 *  This should be "AT_END" - just after the context is mapped, it
 *  will determine if the context needs reload.
 *
 *  This interceptor supports multiple forms of reloading.
 *  Configuration. Must be set after LoaderInterceptor
 */
public class ReloadInterceptor extends  BaseInterceptor
{
    // Stop and start the context.
    boolean fullReload=true;
    int dependManagerNote=-1;
    
    public ReloadInterceptor() {
    }

    public void engineInit( ContextManager cm ) throws TomcatException {
	dependManagerNote=cm.getNoteId(ContextManager.CONTAINER_NOTE,
				       "DependManager");
    }
    
    /** A full reload will stop and start the context, without
     *  saving any state. It's the cleanest form of reload, equivalent
     *  with (partial) server restart.
     */
    public void setFullReload( boolean full ) {
	fullReload=full;
    }

    public void addContext( ContextManager cm, Context context)
	throws TomcatException
    {
	DependManager dm=(DependManager)context.getContainer().
	    getNote("DependManager");
	if( dm==null ) {
	    dm=new DependManager();
	    context.getContainer().setNote("DependManager", dm);
	}
	if( debug > 0 ) {
	    dm.setDebug( debug );
	}
    }
    
    /** Example of adding web.xml to the dependencies.
     *  JspInterceptor can add all taglib descriptors.
     */
    public void contextInit( Context context)
	throws TomcatException
    {
        ContextManager cm = context.getContextManager();
	DependManager dm=(DependManager)context.getContainer().
	    getNote("DependManager");

	File inf_xml = new File(context.getAbsolutePath() +
				"/WEB-INF/web.xml");
	if( inf_xml.exists() ) {
	    Dependency dep=new Dependency();
	    dep.setTarget("web.xml");
	    dep.setOrigin( inf_xml );
	    dep.setLastModified( inf_xml.lastModified() );
	    dm.addDependency( dep );
	}

	// Use a DependClassLoader to autmatically record class loader
	// deps
	loaderHook(dm, context);
    }
    
    public void reload( Request req, Context context) throws TomcatException {

	DependManager dm=(DependManager)context.getContainer().
	    getNote("DependManager");

	if( dm!=null ) {
	    // we are using a util.depend for reloading
	    dm.reset();
	}
	loaderHook(dm, context);
	log( "Reloading context " + context );
    }

    
    protected void  loaderHook( DependManager dm, Context context ) {
	// ReloadInterceptor must be configured _after_ LoaderInterceptor
	ClassLoader cl=context.getClassLoader();
	
	ClassLoader loader=DependClassLoader.getDependClassLoader( dm, cl,
		     context.getAttribute( Context.ATTRIB_PROTECTION_DOMAIN), debug);

	context.setClassLoader(loader);
	context.setAttribute( "org.apache.tomcat.classloader", loader);
    }

    public int contextMap( Request request ) {
	Context ctx=request.getContext();
	if( ctx==null) return 0;
	
	// XXX This interceptor will be added per/context.
	if( ! ctx.getReloadable() ) return 0;

	// We are remapping ?
	if( request.getAttribute("tomcat.ReloadInterceptor")!=null)
	    return 0;
	
	DependManager dm=(DependManager)ctx.getContainer().
	    getNote(dependManagerNote);
	if( ! dm.shouldReload() ) return 0;

	if( debug> 0 )
	    log( "Detected changes in " + ctx.toString());

	try {
	    // Reload context.	
	    ContextManager cm=ctx.getContextManager();
	    
	    if( fullReload ) {
		synchronized(ctx) {
		    if(ctx.getState() == Context.STATE_NEW)
			return 0; // Already reloaded.
		    Vector sI=new Vector();  // saved local interceptors
		    BaseInterceptor[] eI;    // all exisiting interceptors

		    // save the ones with the same context, they are local
		    eI=ctx.getContainer().getInterceptors();
		    for(int i=0; i < eI.length ; i++)
			if(ctx == eI[i].getContext()) sI.addElement(eI[i]);
		    
		    Enumeration e;
		    // Need to find all the "config" that
		    // was read from server.xml.
		    // So far we work as if the admin interface was
		    // used to remove/add the context.
		    // Or like the deploytool in J2EE.
		    Context ctx1=cm.createContext();
		    ctx1.setContextManager( cm );
		    ctx1.setPath(ctx.getPath());
		    ctx1.setDocBase(ctx.getDocBase());
		    ctx1.setReloadable( ctx.getReloadable());
		    ctx1.setDebug( ctx.getDebug());
		    ctx1.setHost( ctx.getHost());
		    ctx1.setTrusted( ctx.isTrusted());
		    e=ctx.getHostAliases();
		    while( e.hasMoreElements())
			ctx1.addHostAlias( (String)e.nextElement());

		    BaseInterceptor ri[] = 
			cm.getContainer().getInterceptors(Container.H_copyContext);
		    int i;
		    for( i=0; i < ri.length; i++) {
			ri[i].copyContext(request, ctx, ctx1);
		    }
		    cm.removeContext( ctx );
		    
		    cm.addContext( ctx1 );

		    // put back saved local interceptors
		    e=sI.elements();
		    while(e.hasMoreElements()){
			BaseInterceptor savedI=(BaseInterceptor)e.nextElement();
			
			ctx1.addInterceptor(savedI);
			savedI.setContext(ctx1);
			savedI.reload(request,ctx1);
		    }

		    ctx1.init();

		    // remap the request
		    request.setAttribute("tomcat.ReloadInterceptor", this);
		    ri = cm.getContainer().getInterceptors(Container.H_contextMap);
		    
		    for( i=0; i< ri.length; i++ ) {
			if( ri[i]==this ) break;
			int status=ri[i].contextMap( request );
			if( status!=0 ) return status;
		    }
		}
		    
	    } else {
		// This is the old ( buggy) behavior
		// ctx.reload() has some fixes - it removes most of the
		// user servlets, but still need work XXX.

		// we also need to save context attributes.

		Enumeration sE=ctx.getServletNames();
		while( sE.hasMoreElements() ) {
		    try {
			String sN=(String)sE.nextElement();
			Handler sw=ctx.getServletByName( sN );
			sw.reload();
		    } catch( Exception ex ) {
			log( "Reload exception: " + ex);
		    }
		}

		// send notification to all interceptors
		// They may try to save up the state or take
		// the right actions


		if( debug>0 ) log( "Reloading hooks for context " +
				   ctx.toString());

		// Call reload hook in context manager
		BaseInterceptor cI[]=ctx.getContainer().getInterceptors();
		for( int i=0; i< cI.length; i++ ) {
		    cI[i].reload(  request, ctx );
		    ctx.getContainer().setNote( "oldLoader", null);
		}
	    }
	} catch( TomcatException ex) {
	    log( "Error reloading " + ex );
	}
	return 0;
    }
}
