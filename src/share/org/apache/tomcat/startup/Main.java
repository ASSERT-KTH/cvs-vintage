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
import org.apache.tomcat.util.IntrospectionUtils;

// Depends:
// JDK1.1
// tomcat.util.IntrospectionUtils, SimpleClassLoader

/**
 * Starter for Tomcat.
 *
 * This is a replacement/enhancement for the .sh and .bat files - you can
 * use JDK1.2 "java -jar tomcat.jar", or ( for jdk 1.1 ) you just need to
 * include a single jar file in the classpath.
 *
 * @author Costin Manolache
 */
public class Main {
    String installDir;
    String libBase;
    String homeDir;
    static final String DEFAULT_CONFIG="conf/server.xml";
    boolean doStop=false;
    // if needed
    // null means user didn't set one
    String configFile;
    
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

    // -------------------- Guess tomcat.home --------------------

    
    // -------------------- Utils --------------------
    
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
	    if( f.isDirectory() )
		path +="/";
	    return new URL( "file", null, path );
        } catch (Exception ex) {
	    ex.printStackTrace();
	    return null;
        }
    }

    public String getLibDir() {
	if( libBase!=null ) return libBase;

	String pkg=IntrospectionUtils.guessHome("tomcat.home", "tomcat.jar");
	System.out.println("Guessed home=" + pkg);
	if( pkg!=null ) setLibDir( pkg + "/lib");
	else setLibDir("./lib");
	return libBase;
    }

    
    void execute( String args[] ) throws Exception {

	try {
	    Vector urlV=new Vector();
            String cpComp[]=getJarFiles(getLibDir());
	    int jarCount=cpComp.length;
            urlV.addElement( getURL(  getLibDir() ,"../classes/" ));
	    for( int i=0; i< jarCount ; i++ ) {
		urlV.addElement( getURL(  getLibDir() , cpComp[i] ));
	    }

	    // add CLASSPATH
	    String cpath=System.getProperty( "tomcat.cp");
	    if( cpath!=null ) {
		System.out.println("Extra CLASSPATH: " + cpath);
		String pathSep=System.getProperty( "path.separator");
		StringTokenizer st=new StringTokenizer( cpath, pathSep );
		while( st.hasMoreTokens() ) {
		    String path=st.nextToken();
		    urlV.addElement( getURL( path, "" ));
		}
	    }

	    // Add tools.jar if JDK1.2
	    String java_home=System.getProperty( "java.home" );
	    urlV.addElement( new URL( "file", null , java_home +
				       "/../lib/tools.jar"));
	    
	    URL urls[]=new URL[ urlV.size() ];
	    System.out.println("CLASSPATH: " );
	    for( int i=0; i<urlV.size(); i++ ) {
		urls[i]=(URL)urlV.elementAt( i );
		System.out.print(":" + urls[i] );
	    }
	    System.out.println();
	    System.out.println();
	    
	    ClassLoader parentL=this.getClass().getClassLoader();
	    System.out.println("ParentL " + parentL );

	    ClassLoader cl=null;
	    cl= IntrospectionUtils.getURLClassLoader( urls, parentL );
	    if( cl==null )
		cl=new SimpleClassLoader(urls, parentL);

	    
	    Class cls=cl.loadClass("org.apache.tomcat.startup.Tomcat");
	    Object proxy=cls.newInstance();
	    
	    processArgs( proxy, args );
	    // 	    IntrospectionUtils.setAttribute( proxy,
	    // 		     "parentClassLoader", parentL );
	    //	    setAttribute( proxy, "serverClassPath", urls );
	    IntrospectionUtils.execute(  proxy, "execute" );
	    return;
	} catch( Exception ex ) {
	    ex.printStackTrace();
	}
    }

    // -------------------- Command-line args processing --------------------
    /* Later
       static class Arg {
       String name;
       String aliases[];
       int args;
       
       boolean task;
       }
    */
    String args0[]= { "help", "stop", "g", "generateConfigs" };
    String args1[]= { "f", "config", "h", "home" };

    /** Read command line arguments and set properties in proxy,
	using ant-like patterns
    */
    void processArgs(Object proxy, String args[] )
	throws Exception
    {

	for( int i=0; i< args.length; i++ ) {
	    String arg=args[i];
	    if( arg.startsWith("-"))
		arg=arg.substring(1);

	    for( int j=0; j< args0.length ; j++ ) {
		if( args0[j].equalsIgnoreCase( arg )) {
		    IntrospectionUtils.setAttribute( proxy, args0[j], "true");
		    break;
		}
	    }
	    for( int j=0; j< args1.length ; j++ ) {
		if( args1[j].equalsIgnoreCase( arg )) {
		    i++;
		    if( i < args.length )
			IntrospectionUtils.setAttribute( proxy,
							 args1[j], args[i]);
		    break;
		}
	    }
	}
    }

    public String[] getJarFiles(String ld) {
	File dir = new File(ld);
	String[] names = dir.list( new FilenameFilter(){
            public boolean accept(File d, String name) {
                if (name.endsWith(".jar"))
                {
                    return true;
                }
                return false;
            }
        });
	return names;
    }


}


