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
import org.apache.tomcat.util.compat.*;
import org.apache.tomcat.util.IntrospectionUtils;
import org.apache.tomcat.core.*;
import org.apache.tomcat.modules.server.*;
import org.apache.tomcat.util.log.*;
import org.xml.sax.*;

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
