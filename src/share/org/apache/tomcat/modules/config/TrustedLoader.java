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

package org.apache.tomcat.modules.config;

import java.io.File;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import org.apache.tomcat.core.BaseInterceptor;
import org.apache.tomcat.core.Context;
import org.apache.tomcat.core.ContextManager;
import org.apache.tomcat.core.Request;
import org.apache.tomcat.core.TomcatException;
import org.apache.tomcat.util.xml.SaxContext;
import org.apache.tomcat.util.xml.XmlAction;
import org.apache.tomcat.util.xml.XmlMapper;
import org.xml.sax.AttributeList;

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
    Hashtable allModules=new Hashtable();

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

            Vector ctxModules=new Vector();
            allModules.put( context, ctxModules );

	    // Now add all modules to cm
	    for( int i=0; i< modV.size(); i++ ) {
		BaseInterceptor bi=(BaseInterceptor)modV.elementAt( i );
		if(debug>0) log( "Add dummy module, for configuration " + context.getDocBase() + " " + context);
		cm.addInterceptor( bi );
                ctxModules.addElement( bi );
	    }
	    cm.setNote(  "trustedLoader.currentContext", null );
	    context.setClassLoader( null );
            context.removeAttribute("org.apache.tomcat.classloader");
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

        Vector ctxModules = (Vector)allModules.get( ctx );
        if( ctxModules != null ) {
            // remove modules
            for( int i=0; i< ctxModules.size(); i++ ) {
                BaseInterceptor bi=(BaseInterceptor)ctxModules.elementAt( i );
                cm.removeInterceptor( bi );
            }
            ctxModules.removeAllElements();
        } else {
            ctxModules = new Vector();
            allModules.put( ctx, ctxModules );
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
            ctxModules.addElement( bi );
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

