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
package org.apache.tomcat.startup;

import java.beans.*;
import java.io.*;
import java.io.IOException;
import java.lang.reflect.*;
import java.util.Hashtable;
import java.util.*;
import java.net.*;

import org.apache.tomcat.util.SimpleClassLoader;

// XXX there is a nice trick to "guess" TOMCAT_HOME from
// classpath - you open each component of cp and check if
// it contains this file. When you find it just take the
// path and use it.

// Since the .sh will need to include it in CP probably it
// already know where it is.


// <b> Thanks ANT </b>

/**
 * Starter for Tomcat. This is the standalone started - the only component that is
 * part of the system CLASSPATH. It will process command line options and
 * load the right jars ( like JAXP and anything else required to start tomcat).
 *
 * This is a replacement for all the scripting we use to do in tomcat.sh and tomcat.bat.
 *
 * This class have (only) the following dependencies( that need to be included in the
 * same jar): 
 *  - org.apache.tomcat.util.SimpleClassLoader - for JDK1.1
 *
 *
 * <b>Starting tomcat</b>. 
 * Add tcstarter.jar to CLASSPATH
 * Launch org.apache.tomcat.startup.Tomcat with TOMCAT_HOME parameter
 * pointing to the tomcat install directory.
 * 
 * 
 * @author Costin
 */
public class Main {
    String installDir;
    String homeDir;
    static final String DEFAULT_CONFIG="conf/server.xml";
    boolean doStop=false;
    // if needed
    // null means user didn't set one
    String configFile;
    
    static boolean jdk12=false;
    static {
	try {
	    Class.forName( "java.security.PrivilegedAction" );
	    jdk12=true;
	} catch(Throwable ex ) {
	}
    }

    public Main() {
    }

    public static void main(String args[] ) {
	try {
	    Main tomcat=new Main();
	    tomcat.execute( args );
	} catch(Exception ex ) {
	    System.out.println("Fatal error");
	    ex.printStackTrace();
	}
    }

    void log( String s ) {
	System.out.println("TomcatStartup: " + s );
    }
    
    // -------------------- Command-line args processing --------------------
    String libBase;
    
    public void setLibDir( String base ) {
        try {
	    File f = new File(base);
	    this.libBase = f.getCanonicalPath();
	    if( ! libBase.endsWith("/") ) libBase+="/";
        } catch (IOException ioe) {
	    ioe.printStackTrace();
	    libBase=base;
        }
    }

    URL getURL( String base, String file ) {
        try {
	    if( ! base.endsWith( "/" ) )
		base=base + "/";
	    
	    File f = new File(base + file);
	    String path = f.getCanonicalPath();
	    return new URL( "file", null, path );
        } catch (Exception ex) {
	    ex.printStackTrace();
	    return null;
        }
    }
	

    public String getPackageDir() {
	String tcHome=System.getProperty("tomcat.home");
	// XXX process args, find if -install is specified
	
	if( tcHome==null ) {
	    log( "No tomcat.home specified, can't run");
	    return null;
	}
	return tcHome;
    }
    
    public String getLibDir() {
	if( libBase!=null ) return libBase;
	String pkg=getPackageDir();
	if( pkg!=null ) setLibDir( pkg + "/lib");
	else setLibDir("./lib");
	return libBase;
    }

    ClassLoader getURLClassLoader( URL urls[], ClassLoader parent )
	throws Exception
    {
	Class urlCL=Class.forName( "java.net.URLClassLoader");
	Class paramT[]=new Class[2];
	paramT[0]= urls.getClass();
	paramT[1]=ClassLoader.class;
	Method m=urlCL.getMethod( "newInstance", paramT);

	ClassLoader cl=(ClassLoader)m.invoke( urlCL,
					      new Object[] { urls, parent } );
	return cl;
    }
	
    
    String cpComp[]=new String[] { "../classes/", "jaxp.jar",
				   "parser.jar", "jasper.jar",
				   "servlet.jar", "webserver.jar" };
    
    void execute( String args[] ) throws Exception {

	try {


	    int jarCount=cpComp.length;
	    URL urls[]=new URL[jarCount + 1 ];
	    for( int i=0; i< jarCount ; i++ ) {
		urls[i]=getURL(  getLibDir() , cpComp[i] );
		System.out.println( "Add to CP: " + urls[i] );
	    }

	    // Add tools.jar if JDK1.2
	    String java_home=System.getProperty( "java.home" );
	    urls[jarCount]= new URL( "file", null , java_home + "/../lib/tools.jar");
	    System.out.println( "Add to CP: " + urls[jarCount] );
	    
	    ClassLoader parentL=this.getClass().getClassLoader();

	    ClassLoader cl=null;
	    if( jdk12 )
		cl= getURLClassLoader( urls, parentL );
	    else
		cl=new SimpleClassLoader(urls, parentL);

	    
	    Object proxy=instantiate( cl, 
				      "org.apache.tomcat.task.StartTomcat");
	    processArgs( proxy, args );
	    setParentLoader( proxy, parentL );
	    execute(  proxy );
	    return; 
	} catch( Exception ex ) {
	    ex.printStackTrace();
	}
    }

    /** Create an instance of the target task
     */
    Object instantiate( ClassLoader cl, String classN  ) throws Exception {
	Class sXmlC=cl.loadClass(classN );
	return sXmlC.newInstance();
    }

    /** If the proxy has a setParentClassLoader() method, use it
	to allow it to access this class' loader
    */
    void setParentLoader( Object proxy, ClassLoader cl) throws Exception {
	Method executeM=null;
	Class c=proxy.getClass();
	Class params[]=new Class[1];
	params[0]= ClassLoader.class;
	executeM=c.getMethod( "setParentClassLoader", params );
	if( executeM == null ) {
	    log("No setParentClassLoader in " + proxy.getClass() );
	    return;
	}
	log( "Setting parent loader " + cl);
	executeM.invoke(proxy, new Object[] { cl });
	return; 
    }

    /** Call execute() - any ant-like task should work
     */
    void execute( Object proxy  ) throws Exception {
	Method executeM=null;
	Class c=proxy.getClass();
	Class params[]=new Class[0];
	//	params[0]=args.getClass();
	executeM=c.getMethod( "execute", params );
	if( executeM == null ) {
	    log("No execute in " + proxy.getClass() );
	    return;
	}
	log( "Calling proxy ");
	executeM.invoke(proxy, null );//new Object[] { args });
	return; 
    }

    /** Read command line arguments and set properties in proxy,
	using ant-like patterns
    */
    void processArgs(Object proxy, String args[] ) {

	for( int i=0; i< args.length; i++ ) {
	    
	    
	}
    }
 

}
