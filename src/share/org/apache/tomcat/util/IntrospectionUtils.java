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


package org.apache.tomcat.util;
import java.lang.reflect.*;
import java.net.*;
import java.io.*;
import java.util.*;

// Depends: JDK1.1

/**
 *  Utils for introspection and reflection
 */
public final class IntrospectionUtils {

    /** Call execute() - any ant-like task should work
     */
    public static void execute( Object proxy, String method  )
	throws Exception
    {
	Method executeM=null;
	Class c=proxy.getClass();
	Class params[]=new Class[0];
	//	params[0]=args.getClass();
	executeM=c.getMethod( method, params );
	if( executeM == null ) {
	    throw new RuntimeException("No execute in " + proxy.getClass() );
	}
	executeM.invoke(proxy, null );//new Object[] { args });
    }

    /** 
     *  Call void setAttribute( String ,Object )
     */
    public static void setAttribute( Object proxy, String n, Object v)
	throws Exception
    {
	Method executeM=null;
	Class c=proxy.getClass();
	Class params[]=new Class[2];
	params[0]= String.class;
	params[1]= Object.class;
	executeM=c.getMethod( "setAttribute", params );
	if( executeM == null ) {
	    System.out.println("No setAttribute in " + proxy.getClass() );
	    return;
	}
	if( false )
	    System.out.println("Setting " + n + "=" + v + "  in " + proxy);
	executeM.invoke(proxy, new Object[] { n, v });
	return; 
    }

    /** Construct a URLClassLoader. Will compile and work in JDK1.1 too.
     */
    public static ClassLoader getURLClassLoader( URL urls[],
						 ClassLoader parent )
    {
	try {
	    Class urlCL=Class.forName( "java.net.URLClassLoader");
	    Class paramT[]=new Class[2];
	    paramT[0]= urls.getClass();
	    paramT[1]=ClassLoader.class;
	    Method m=urlCL.getMethod( "newInstance", paramT);
	    
	    ClassLoader cl=(ClassLoader)m.invoke( urlCL,
						  new Object[] { urls,
								 parent } );
	    return cl;
	} catch(ClassNotFoundException ex ) {
	    // jdk1.1
	    return null;
	} catch(Exception ex ) {
	    ex.printStackTrace();
	    return null;
	}
    }


    public static String guessHome(String systemProperty, String jarName) {
	return guessHome( systemProperty, jarName, null);
    }
    
    /** Guess a product home by analyzing the class path.
     *  It works for product using the pattern: lib/executable.jar
     *  or if executable.jar is included in classpath by a shell
     *  script. ( java -jar also works )
     */
    public static String guessHome(String systemProperty, String jarName,
				   String classFile) {
	String h=null;
	
	if( systemProperty != null )
	    h=System.getProperty( systemProperty );
	
	if( h!=null ) return h;

	// Find the directory where jarName.jar is located
	
	String cpath=System.getProperty( "java.class.path");
	String pathSep=System.getProperty( "path.separator");
	StringTokenizer st=new StringTokenizer( cpath, pathSep );
	while( st.hasMoreTokens() ) {
	    String path=st.nextToken();
	    //	    log( "path " + path );
	    if( path.endsWith( jarName ) ) {
		h=path.substring( 0, path.length() - jarName.length() );
		try {
		    File f=new File( h );
		    File f1=new File ( h, "..");
		    h = f1.getCanonicalPath();
		    if( systemProperty != null )
			System.getProperties().put( systemProperty, h );
		    return h;
		} catch( Exception ex ) {
		    ex.printStackTrace();
		}
	    } else  {
		String fname=path + ( path.endsWith("/") ?"":"/" ) + classFile;
		if( new File( fname ).exists()) {
		    try {
			File f=new File( path );
			File f1=new File ( h, "..");
			h = f1.getCanonicalPath();
			if( systemProperty != null )
			    System.getProperties().put( systemProperty, h );
			return h;
		    } catch( Exception ex ) {
			ex.printStackTrace();
		    }
		}
	    }
	}
	return null;
    }
}
