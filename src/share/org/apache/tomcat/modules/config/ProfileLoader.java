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
import java.net.URL;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import org.apache.tomcat.core.BaseInterceptor;
import org.apache.tomcat.core.Context;
import org.apache.tomcat.core.ContextManager;
import org.apache.tomcat.core.TomcatException;
import org.apache.tomcat.util.IntrospectionUtils;
import org.apache.tomcat.util.compat.Jdk11Compat;
import org.apache.tomcat.util.xml.SaxContext;
import org.apache.tomcat.util.xml.XmlAction;
import org.apache.tomcat.util.xml.XmlMapper;
import org.xml.sax.AttributeList;

/**
 * This module can be used to specify groups of modules and
 * add them automcatically to all web applications declared as 
 * belonging to the profile.
 *
 * ( not implemented ) A profile can also declare a set of jars
 * that will be shared by all the apps belonging to that profile.
 * This allows apps to share objects and attributes.
 * 
 * @author Costin Manolache
 */
public class ProfileLoader extends BaseInterceptor {
    Hashtable profiles=new Hashtable();
    
    public ProfileLoader() {
    }

    // -------------------- Properties --------------------
    String configFile=null;
    static final String DEFAULT_CONFIG="conf/profile.xml";

    public void setConfig( String s ) {
	configFile=s;
    }

    public void addProfile( Profile p ) {
	String name=p.getName();
	if( debug > 0 ) log ( "Adding " + name );
	if( name==null ) return;
	profiles.put( name, p );
    }

    // -------------------- Contexts --------------------
    /** Adjust paths for a context - make the base and all loggers
     *  point to canonical paths.
     */
    public void addContext( ContextManager cm, Context ctx)
	throws TomcatException
    {
	String ctxProfile=ctx.getProperty("profile");
	if( ctxProfile==null ) ctxProfile="default";
	Profile p=(Profile)profiles.get( ctxProfile );
	if( p==null ) {
	    log( "Can't find profile " + ctxProfile );
	    p=(Profile)profiles.get("default");
	}

	
	if( p==null ) throw new TomcatException( "Can't load profile");

	URL[] cp=p.commonClassPath;
	for (int i=0; i<cp.length; i++ ) {
	    ctx.addClassPath( cp[i]);
	}
	cp=p.sharedClassPath;
	for (int i=0; i<cp.length; i++ ) {
	    ctx.addClassPath( cp[i]);
	}

	
	Enumeration en=p.getModules();
	while( en.hasMoreElements()) {
	    BaseInterceptor bi=(BaseInterceptor)en.nextElement();
	    if( debug > 0 ) log( ctx + " " + bi );
	    ctx.addInterceptor( bi );
	}
    }
    

    // -------------------- Reade config and init --------------------
    /**
     *  Read the profiles.
     */
    public void addInterceptor(ContextManager cm, Context ctx,
			       BaseInterceptor module)
	throws TomcatException
    {
	if( this != module ) return;

	XmlMapper xh=new XmlMapper();
	xh.setDebug( debug );

	addProfileRules( xh );
	addTagRules( cm, ctx, xh );
	
	if (configFile == null)
	    configFile=DEFAULT_CONFIG;
	
        File f=new File(configFile);
        if ( !f.isAbsolute())
	    f=new File( cm.getHome(), File.separator + configFile);

	if( f.exists() ){
	    try {
		xh.readXml(f, this );
	    } catch( Exception ex ) {
		ex.printStackTrace();
		throw new TomcatException(ex);
	    }
        }
    }

    // -------------------- Xml reading details --------------------

    public void addTagRules( ContextManager cm, Context ctx, XmlMapper xh )
	throws TomcatException
    {
	Hashtable modules=(Hashtable)cm.getNote("modules");
	if( modules==null) return;
	Enumeration keys=modules.keys();

	while( keys.hasMoreElements() ) {
	    String name=(String)keys.nextElement();
	    String classN=(String)modules.get( name );

	    String tag="Profile" + "/" + name;
	    xh.addRule( tag, new TagAction(classN));
	}
    }

    public  void addProfileRules( XmlMapper xh ) {
	xh.addRule( "Profile", new ProfileAction(this));
    }

    // -------------------- Utils --------------------

    static class ProfileAction extends XmlAction {
	ProfileLoader ploader;
	
	ProfileAction( ProfileLoader ploader ) {
	    this.ploader=ploader;
	}
	
	public void start(SaxContext ctx ) throws Exception {
	    Profile p=new Profile(ploader.getContextManager() );
	    AttributeList attributes = ctx.getCurrentAttributes();
	    p.setName( attributes.getValue("name"));
	    p.initClassLoaders();
	    ctx.pushObject( p );
	}
	public void end(SaxContext ctx ) {
	    Profile obj=(Profile)ctx.currentObject();
	    ploader.addProfile( obj );

	}
	public void cleanup( SaxContext ctx ) {
	    ctx.popObject();
	}
    }


    static class TagAction extends XmlAction {
	String className;
	
	TagAction( String classN ) {
	    this.className=classN;
	}

	public void start( SaxContext ctx) throws Exception {	
	    String tag=ctx.getCurrentElement();
	    Profile profile=(Profile)ctx.currentObject();
	    Class c=null;
	    ClassLoader cl=profile.containerLoader;	
	    try {
		c=cl.loadClass( className );
	    } catch( ClassNotFoundException ex2 ) {
		c=profile.commonLoader.loadClass(className);
	    }

	    Object o=c.newInstance();

	    AttributeList attributes = ctx.getCurrentAttributes();

	    for (int i = 0; i < attributes.getLength (); i++) {
		String type = attributes.getType (i);
		String name=attributes.getName(i);
		String value=attributes.getValue(i);
		
		IntrospectionUtils.setProperty( o, name, value );
	    }

	    profile.addModule( (BaseInterceptor)o );
	}
    }

}

/** Context profiles - set of modules, with separate class loaders used
    to simplify configuration
*/
class Profile {
    String name;
    URL[] sharedClassPath;
    URL[] commonClassPath;
    URL[] serverClassPath;
    
    ClassLoader commonLoader;
    ClassLoader containerLoader;
    ClassLoader appLoader;
    Vector modules=new Vector();

    ContextManager cm;
    
    public Profile(ContextManager cm) {
	this.cm=cm;
    }

    public String getName() {
	return name;
    }
	
    public void setName( String s ) {
	name=s;
    }

    public Enumeration getModules() {
	return modules.elements();
    }
    
    public void addModule(BaseInterceptor bi) {
	modules.addElement( bi );
    }

    ClassLoader getContainerLoader() {
	return containerLoader;
    }

    static final Jdk11Compat jdk11Compat=Jdk11Compat.getJdkCompat();
    /** init profile class loaders
     */
    public void initClassLoaders() {
	String home=cm.getHome();
	// Could check if no extra jars are added

	
	// Create common loader
	Vector commonClassPathV=new Vector();
	IntrospectionUtils.addToClassPath( commonClassPathV,
					   home + "/lib/common/" + name);
	//IntrospectionUtils.addToClassPath( commonClassPathV,
	// 		         	   home + "/lib/common/");
	commonClassPath=IntrospectionUtils.getClassPath(commonClassPathV);
	commonLoader=
	    jdk11Compat.newClassLoaderInstance(commonClassPath ,
					       cm.getCommonLoader());

	// Create app shared loader
	Vector sharedClassPathV=new Vector();
	IntrospectionUtils.addToClassPath( sharedClassPathV,
					   home + "/lib/apps/" + name );
	//IntrospectionUtils.addToClassPath( sharedClassPathV,
	// home + "/lib/apps/");
	sharedClassPath=IntrospectionUtils.getClassPath(sharedClassPathV);
	
	appLoader=jdk11Compat.newClassLoaderInstance(sharedClassPath ,
						     cm.getAppsLoader());

	// Create container loader
	Vector serverClassPathV=new Vector();
	IntrospectionUtils.addToClassPath( serverClassPathV,
					   home + "/lib/container/" + name);
	//IntrospectionUtils.addToClassPath( serverClassPathV,
	// home + "/lib/container/");
	IntrospectionUtils.addToolsJar( serverClassPathV );
	
	serverClassPath=IntrospectionUtils.getClassPath(serverClassPathV);
	containerLoader=jdk11Compat.newClassLoaderInstance(serverClassPath ,
					   cm.getContainerLoader());
	
    }

}
