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
package org.apache.tomcat.modules.config;

import java.beans.*;
import java.io.*;
import java.lang.reflect.*;
import java.util.Hashtable;
import java.util.*;
import java.net.*;
import org.apache.tomcat.util.res.StringManager;
import org.apache.tomcat.util.io.FileUtil;
import org.apache.tomcat.util.xml.*;
import org.apache.tomcat.core.*;
import org.apache.tomcat.modules.server.*;
import org.apache.tomcat.util.log.*;
import org.apache.tomcat.util.hooks.*;
import org.apache.tomcat.util.IntrospectionUtils;
import org.xml.sax.*;

/**
 * Special configuration for trusted applications.
 * Need to be loaded _after_ LoaderInterceptor.
 *
 * @author Costin Manolache
 */
public class TrustedLoader extends BaseInterceptor {

    public TrustedLoader() {
    }

    // -------------------- Properties --------------------
    
    // -------------------- Hooks --------------------
    Vector allModules=new Vector();
    
    /** Called when the server is configured - all base modules are added,
	some contexts are added ( explicitely or by AutoDeploy/AutoAdd ).
	No addContext callback has been called.

	We assume all modules are loaded from config or AutoDeploy ( so
	they are trusted ). We check for trusted contexts, and load
	any eventual module.

	Note that the loader used to load the module will be different
	from the "real" one, used on reloading or init ( XXX all
	modules must be prepared to handle reloading !!! )
    */
    public void engineState( ContextManager cm , int state )
	throws TomcatException
    {
	if( state!=ContextManager.STATE_CONFIG ) return;

	if( debug>0 ) log("TrustedLoader: " + state );
	Enumeration ctxsE= cm.getContexts();
	while( ctxsE.hasMoreElements() ) {
	    Context context=(Context)ctxsE.nextElement();
	    if( ! context.isTrusted() ) continue;

	    File modules=getModuleFile( context );
	    if( modules==null ) continue;
	    
	    /*  We'll create a temporary loader for this context, and use it
	     *  to create a module. The module will be notified for all
	     *  contexts that were added so far, as with any normal module.
	     * 
	     *  What's special is that at init stage, the module will be
	     *  removed and loaded again, with the real class loader.
	     *  Same thing will happen when the application is reloaded.
	     *
	     *  BTW, modules are supposed to be reloadable, but there is
	     *  a lot of work still needed ( mostly in modules, to make them
	     *  aware )
	     */
	    LoaderInterceptor11 loaderHelper=new LoaderInterceptor11();
	    loaderHelper.setContextManager( cm );
	    loaderHelper.addContext( cm, context );
	    loaderHelper.contextInit( context );

	    Vector modV=new Vector();
	    if(debug>0) log("loadInterceptors in a dummy classloader for setup " + context + " " +
			    context.isTrusted() + " " + context.getDocBase());
	    loadInterceptors( context, modules, modV );
	    cm.setNote( "trustedLoader.currentContext", context );

	    // Now add all modules to cm
	    for( int i=0; i< modV.size(); i++ ) {
		BaseInterceptor bi=(BaseInterceptor)modV.elementAt( i );
		if(debug>0) log( "Add dummy module, for configuration " + context.getDocBase() + " " + context);
		cm.addInterceptor( bi );
		allModules.addElement( bi );
	    }	
	    cm.setNote(  "trustedLoader.currentContext", null );
	    context.setClassLoader( null );
	}
    }


    public void contextInit( Context ctx )
	throws TomcatException
    {
	// like a reload, the modules will be removed and added back
	if( ! ctx.isTrusted() ) return;

	if(debug>0) log("contextInit " + ctx + " " + cm.getState());

	File modules=getModuleFile( ctx );
	if( modules==null ) return;

	reInitModules( ctx, modules );
    }

    private  void reInitModules( Context ctx, File modules )
	throws TomcatException
    {
	if(debug>0) log("reInit " + modules );
	// remove modules
	for( int i=0; i< allModules.size(); i++ ) {
	    BaseInterceptor bi=(BaseInterceptor)allModules.elementAt( i );
	    cm.removeInterceptor( bi );
	}
	

	// The real loader is set. 
	Vector modV=new Vector();
	if( debug > 0 ) log( "Loading the real module " + ctx + " " + modules);
	loadInterceptors( ctx, modules, modV );
	cm.setNote( "trustedLoader.currentContext", ctx );

	// Now add all modules to cm
	for( int i=0; i< modV.size(); i++ ) {
	    BaseInterceptor bi=(BaseInterceptor)modV.elementAt( i );
	    cm.addInterceptor( bi );
	}	
	cm.setNote(  "trustedLoader.currentContext", null );
    }

    /** Again, remove and add back
     */
    public void reload( Request req, Context context) throws TomcatException {
	if( ! context.isTrusted() ) return;

	File modules=getModuleFile( context );
	if( modules==null ) return;

	if( debug > 0 )
	    log( "Reload modules " + context.getPath() );

	reInitModules( context , modules);
    }


    
    public void loadInterceptors( Context ctx, File modulesF, Vector modulesV )
	throws TomcatException
    {
	
	XmlMapper xh=new XmlMapper();
	xh.setClassLoader( ctx.getClassLoader());
	//xh.setDebug( debug );

	// no backward compat rules. The file must be self-contained,
	// with <module> definition and the module itself
	setTagRules( xh );

	// then load the actual config 
	ServerXmlReader.loadConfigFile(xh,modulesF,modulesV);

    }

    public static void addTagRule( XmlMapper xh, String tag, String classN ) {
	xh.addRule( tag ,
		    xh.objectCreate( classN, null ));
	xh.addRule( tag ,
		    xh.setProperties());
	xh.addRule( tag,
		    new XmlAction() {
			public void end( SaxContext ctx) throws Exception {
			    Vector modules=(Vector)ctx.getRoot();
			    Object obj=ctx.currentObject();
			    modules.addElement( obj );
			}
		    });
    }

    
    public static void setTagRules( XmlMapper xh ) {
	xh.addRule( "module",  new XmlAction() {
		public void start(SaxContext ctx ) throws Exception {
		    Object elem=ctx.currentObject();
		    AttributeList attributes = ctx.getCurrentAttributes();
		    String name=attributes.getValue("name");
		    String classN=attributes.getValue("javaClass");
		    if( name==null || classN==null ) return;
		    addTagRule( ctx.getMapper(), name, classN );
		    if( ctx.getDebug() > 0 ) ctx.log("Adding " + name + " " + classN );
		}
	    });
    }

    
    
    private File getModuleFile(Context ctx ) {
	// PathSetter is the first module in the chain, we shuld have
	// a valid path by now 
	String dir=ctx.getAbsolutePath();

	File f=new File(dir);
	File modules=new File( f, "WEB-INF" + File.separator +
			       "interceptors.xml" );
	if( modules.exists() ) {
	    ctx.log( "Loading modules from webapp " + modules );
	    return modules;
	} else {
	    if( debug > 0 )
		ctx.log( "Can't find " + modules );
	    return null;
	}
    }
    

}

