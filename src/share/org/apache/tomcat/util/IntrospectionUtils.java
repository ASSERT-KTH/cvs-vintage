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
	executeM=findMethod( c, method, params );
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
	if( proxy instanceof AttributeHolder ) {
	    ((AttributeHolder)proxy).setAttribute( n, v );
	    return;
	}
	
	Method executeM=null;
	Class c=proxy.getClass();
	Class params[]=new Class[2];
	params[0]= String.class;
	params[1]= Object.class;
	executeM=findMethod( c, "setAttribute", params );
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
	    Method m=findMethod( urlCL, "newInstance", paramT);
	    if( m==null ) return null;
	    
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


    public static String guessInstall(String installSysProp,
		String homeSysProp, String jarName) {
	return guessInstall( installSysProp, homeSysProp, jarName, null);
    }
    
    /** Guess a product install/home by analyzing the class path.
     *  It works for product using the pattern: lib/executable.jar
     *  or if executable.jar is included in classpath by a shell
     *  script. ( java -jar also works )
     *
     *  Insures both "install" and "home" System properties are set.
     *  If either or both System properties are unset, "install" and
     *  "home" will be set to the same value.  This value will be
     *  the other System  property that is set, or the guessed value
     *  if neither is set.
     */
    public static String guessInstall(String installSysProp, String homeSysProp,
			String jarName,	String classFile) {
	String install=null;
	String home=null;
	
	if ( installSysProp != null )
	    install=System.getProperty( installSysProp );

	if( homeSysProp != null )
	    home=System.getProperty( homeSysProp );

	if ( install != null ) {
	    if ( home == null )
		System.getProperties().put( homeSysProp, install );
	    return install;
	}

	// Find the directory where jarName.jar is located
	
	String cpath=System.getProperty( "java.class.path");
	String pathSep=System.getProperty( "path.separator");
	StringTokenizer st=new StringTokenizer( cpath, pathSep );
	while( st.hasMoreTokens() ) {
	    String path=st.nextToken();
	    //	    log( "path " + path );
	    if( path.endsWith( jarName ) ) {
		home=path.substring( 0, path.length() - jarName.length() );
		try {
                    if( "".equals(home) ) {
                        home=new File("./").getCanonicalPath();
                    }
                    File f=new File( home );
		    String parentDir = f.getParent();
		    if(parentDir == null)
			parentDir = home;  // unix style
		    File f1=new File ( parentDir );
		    install = f1.getCanonicalPath();
		    if( installSysProp != null )
			System.getProperties().put( installSysProp, install );
		    if( home == null && homeSysProp != null )
			System.getProperties().put( homeSysProp, install );
		    return install;
		} catch( Exception ex ) {
		    ex.printStackTrace();
		}
	    } else  {
		String fname=path + ( path.endsWith("/") ?"":"/" ) + classFile;
		if( new File( fname ).exists()) {
		    try {
			File f=new File( path );
			String parentDir = f.getParent();
			if( parentDir == null )
			    parentDir = path; // unix style
			File f1=new File ( parentDir );
			install = f1.getCanonicalPath();
			if( installSysProp != null )
			    System.getProperties().put( installSysProp,
							install );
			if( home == null && homeSysProp != null )
			    System.getProperties().put( homeSysProp, install );
			return install;
		    } catch( Exception ex ) {
			ex.printStackTrace();
		    }
		}
	    }
	}

        // if install directory can't be found, use home as the default
	if ( home != null ) {
	    System.getProperties().put( installSysProp, home );
	    return home;
	}

	return null;
    }

    /** Debug method, display the classpath
     */
    public static void displayClassPath( String msg, URL[] cp ) {
	System.out.println(msg);
	for( int i=0; i<cp.length; i++ ) {
	    System.out.println( cp[i].getFile() );
	}
    }

    public static String PATH_SEPARATOR = System.getProperty("path.separator");
    /**
     * Adds classpath entries from a vector of URL's to the
     * "tc_path_add" System property.  This System property lists
     * the classpath entries common to web applications. This System
     * property is currently used by Jasper when its JSP servlet
     * compiles the Java file for a JSP.
    */
    public static String classPathAdd(URL urls[], String cp )
    {
	if( urls==null ) return cp;

	for( int i=0; i<urls.length; i++ ) {
            if( cp != null)
                cp += PATH_SEPARATOR + urls[i].getFile();
            else
                cp = urls[i].getFile();
        }
        return cp;
    }

    /** Find a method with the right name
	If found, call the method ( if param is int or boolean we'll convert
	value to the right type before) - that means you can have setDebug(1).
    */
    public static void setProperty( Object o, String name, String value ) {
	if( dbg > 1 ) d("setProperty(" +
			o.getClass() + " " +  name + "="  +
			value  +")" );

	String setter= "set" +capitalize(name);

	try {
	    Method methods[]=findMethods( o.getClass() );
	    Method setPropertyMethod=null;

	    // First, the ideal case - a setFoo( String ) method
	    for( int i=0; i< methods.length; i++ ) {
		Class paramT[]=methods[i].getParameterTypes();
		if( setter.equals( methods[i].getName() ) &&
		    paramT.length == 1 &&
		    "java.lang.String".equals( paramT[0].getName())) {
		    
		    methods[i].invoke( o, new Object[] { value } );
		    return;
		}
	    }
	    
	    // Try a setFoo ( int ) or ( boolean )
	    for( int i=0; i< methods.length; i++ ) {
		boolean ok=true;
		if( setter.equals( methods[i].getName() ) &&
		    methods[i].getParameterTypes().length == 1) {

		    // match - find the type and invoke it
		    Class paramType=methods[i].getParameterTypes()[0];
		    Object params[]=new Object[1];

		    // Try a setFoo ( int )
		    if ("java.lang.Integer".equals( paramType.getName()) ||
			"int".equals( paramType.getName())) {
			try {
			    params[0]=new Integer(value);
			} catch( NumberFormatException ex ) {ok=false;}

		    // Try a setFoo ( boolean )
		    } else if ("java.lang.Boolean".
			       equals( paramType.getName()) ||
			"boolean".equals( paramType.getName())) {
			params[0]=new Boolean(value);

		    // Try a setFoo ( InetAddress )
		    } else if ("java.net.InetAddress".
				equals( paramType.getName())){
			try{
 			    params[0]= InetAddress.getByName(value);
 			}catch(UnknownHostException exc) {
 			    d("Unable to resolve host name:" + value);
 			    ok=false;
 			}
 
 		    // Unknown type
		    } else {
			d("Unknown type " + paramType.getName() );
		    }

		    if( ok ) {
			methods[i].invoke( o, params );
			return;
		    }
		}

		// save "setProperty" for later
		if( "setProperty".equals( methods[i].getName())) {
		    setPropertyMethod=methods[i];
		}
	    }

	    // Ok, no setXXX found, try a setProperty("name", "value")
	    if( setPropertyMethod != null ) {
		Object params[]=new Object[2];
		params[0]=name;
		params[1]=value;
		setPropertyMethod.invoke( o, params );
	    }

	} catch( SecurityException ex1 ) {
	    if( dbg > 0 )
		d("SecurityException for " + o.getClass() + " " +
			name + "="  + value  +")" );
	    if( dbg > 1 ) ex1.printStackTrace();
	} catch (IllegalAccessException iae) {
	    if( dbg > 0 )
		d("IllegalAccessException for " +
			o.getClass() + " " +  name + "="  + value  +")" );
	    if( dbg > 1 ) iae.printStackTrace();
	} catch (InvocationTargetException ie) {
	    if( dbg > 0 )
		d("InvocationTargetException for " + o.getClass() +
			" " +  name + "="  + value  +")" );
	    if( dbg > 1 ) ie.printStackTrace();
	}
    }

    /** 
     */
    public static void setProperty( Object o, String name ) {
	String setter= "set" +capitalize(name);
	try {
	    Method methods[]=findMethods( o.getClass() );
	    Method setPropertyMethod=null;
	    // find setFoo() method
	    for( int i=0; i< methods.length; i++ ) {
		Class paramT[]=methods[i].getParameterTypes();
		if( setter.equals( methods[i].getName() ) &&
		    paramT.length == 0 ) {
		    methods[i].invoke( o, new Object[] {} );
		    return;
		}
	    }
	} catch( Exception ex1 ) {
	    if( dbg > 0 )
		d("Exception for " + o.getClass() + " " + name);
	    if( dbg > 1 ) ex1.printStackTrace();
	} 
    }
    
    /** Replace ${NAME} with the property value
     */
    public static String replaceProperties(String value,
					   Object getter )
    {
        StringBuffer sb=new StringBuffer();
        int i=0;
        int prev=0;
        // assert value!=nil
        int pos;
        while( (pos=value.indexOf( "$", prev )) >= 0 ) {
            if(pos>0) {
                sb.append( value.substring( prev, pos ) );
            }
            if( pos == (value.length() - 1)) {
                sb.append('$');
                prev = pos + 1;
            }
            else if (value.charAt( pos + 1 ) != '{' ) {
                sb.append( value.charAt( pos + 1 ) );
                prev=pos+2; // XXX
            } else {
                int endName=value.indexOf( '}', pos );
                if( endName < 0 ) {
		    sb.append( value.substring( pos ));
		    prev=value.length();
		    continue;
                }
                String n=value.substring( pos+2, endName );
		String v= null;
		if( getter instanceof Hashtable ) {
		    v=(String)((Hashtable)getter).get(n);
		} else if ( getter instanceof PropertySource ) {
		    v=((PropertySource)getter).getProperty( n );
		}
		if( v== null )
		    v = "${"+n+"}"; 
                
                sb.append( v );
                prev=endName+1;
            }
        }
        if( prev < value.length() ) sb.append( value.substring( prev ) );
        return sb.toString();
    }
    
    /** Reverse of Introspector.decapitalize
     */
    public static String capitalize(String name) {
	if (name == null || name.length() == 0) {
	    return name;
	}
	char chars[] = name.toCharArray();
	chars[0] = Character.toUpperCase(chars[0]);
	return new String(chars);
    }

    public static String unCapitalize(String name) {
	if (name == null || name.length() == 0) {
	    return name;
	}
	char chars[] = name.toCharArray();
	chars[0] = Character.toLowerCase(chars[0]);
	return new String(chars);
    }

    // -------------------- Class path tools --------------------

    /** Add all the jar files in a dir to the classpath,
     *  represented as a Vector of URLs.
     */ 
    public static void addToClassPath( Vector cpV, String dir ) {
	try{
            String cpComp[]=getFilesByExt(dir, ".jar");
            if (cpComp != null){
                int jarCount=cpComp.length;
                for( int i=0; i< jarCount ; i++ ) {
		    URL url=getURL(  dir , cpComp[i] );
                    if( url!=null )
			cpV.addElement( url );
                }
            }
        }catch(Exception ex){
            ex.printStackTrace();
        }
    }


    public static void addToolsJar( Vector v )
    {
	try {
            // Add tools.jar in any case
            File f=new File( System.getProperty( "java.home" ) +
                             "/../lib/tools.jar");

            if( ! f.exists() ) {
                // On some systems java.home gets set to the root of jdk.
                // That's a bug, but we can work around and be nice.
                f=new File( System.getProperty( "java.home" ) +
                                 "/lib/tools.jar");
                if( f.exists() ) {
                    System.out.println("Detected strange java.home value " +
                                       System.getProperty( "java.home" ) +
                                       ", it should point to jre");
                }
            }
            URL url=new URL( "file", "" , f.getAbsolutePath() );

	    v.addElement( url );
	} catch ( MalformedURLException ex ) {
	    ex.printStackTrace();
	}
    }

    
    /** Return all files with a given extension in a dir
     */
    public static String[] getFilesByExt( String ld, String ext ) {
	File dir = new File(ld);
        String[] names=null;
	final String lext=ext;
        if (dir.isDirectory()){
            names = dir.list( new FilenameFilter(){
            public boolean accept(File d, String name) {
                if (name.endsWith(lext)){
                    return true;
                }
                return false;
            }
            });
        }
	return names;
    }


    /** Construct a file url from a file, using a base dir
     */
    public static URL getURL( String base, String file ) {
        try {
            File baseF = new File(base);
            File f = new File(baseF,file);
            String path = f.getCanonicalPath();
            if( f.isDirectory() ){
                    path +="/";
            }
	    if( ! f.exists() ) return null;
            return new URL( "file", "", path );
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    /**
     * add elements from the classpath <i>cp</i> to a Vector
     * <i>jars</i> as file URLs (We use Vector for JDK 1.1 compat).
     * <p>
     * @param <b>cp</b> a String classpath of directory or jar file
     *                    elements separated by path.separator delimiters.
     * @return a Vector of URLs.
     */
    public static void addJarsFromClassPath(Vector jars, String cp)
            throws IOException,MalformedURLException
    {
        String sep = System.getProperty("path.separator");
        String token;
        StringTokenizer st;
        if(cp!=null){
            st = new StringTokenizer(cp,sep);
            while(st.hasMoreTokens()){
                File f = new File(st.nextToken());
                String path = f.getCanonicalPath();
                if(f.isDirectory()){
                        path += "/";
                }
                URL url = new URL("file","",path);
                if(!jars.contains(url)){
                        jars.addElement(url);
                }
            }
        }
    }

    /** Return a URL[] that can be used to construct a class loader
     */
    public static URL[] getClassPath(Vector v){
        URL[] urls=new URL[ v.size() ];
        for( int i=0; i<v.size(); i++ ) {
            urls[i]=(URL)v.elementAt( i );
        }
        return urls;
    }

    /** Construct a URL classpath from files in a directory,
     *  a cpath property, and tools.jar.
     */
    public static URL[] getClassPath( String dir, String cpath,
				      String cpathProp, boolean addTools )
	throws IOException, MalformedURLException
    {
	Vector jarsV = new Vector();
	if( dir!=null ) {
	    // Add dir/classes first, if it exists
	    URL url=getURL( dir, "classes");
	    if( url!=null )
		jarsV.addElement(url);
	    addToClassPath( jarsV, dir );
	}
	
	if( cpath != null )
	    addJarsFromClassPath(jarsV,cpath);
	
	if( cpathProp!=null ) {
	    String cpath1=System.getProperty( cpathProp );
	    addJarsFromClassPath(jarsV,cpath1);
	}

	if(addTools)
	    addToolsJar( jarsV );
	
	return getClassPath(jarsV);
    }
    
    // -------------------- Mapping command line params to setters

    public static boolean processArgs(Object proxy, String args[] ) 
	throws Exception
    {
	String args0[]=null;
	if( null != findMethod( proxy.getClass(),
				"getOptions1", new Class[] {} )) {
	    args0=(String[])callMethod0( proxy, "getOptions1");
	}
	if( args0==null ) {
	    //args0=findVoidSetters(proxy.getClass());
	    args0=findBooleanSetters(proxy.getClass());
	}
	Hashtable h=null;
	if( null != findMethod( proxy.getClass(),
				"getOptionAliases", new Class[] {} )) {
	    h=(Hashtable)callMethod0( proxy, "getOptionAliases");
	}
	return processArgs( proxy, args, args0, null, h );
    }

    public static boolean processArgs(Object proxy, String args[],
				      String args0[], String args1[],
				      Hashtable aliases )
	throws Exception
    {
	for( int i=0; i< args.length; i++ ) {
	    String arg=args[i];
	    if( arg.startsWith("-"))
		arg=arg.substring(1);
	    if( aliases != null && aliases.get( arg ) != null)
		arg=(String)aliases.get(arg);

	    if( args0!=null ) {
		boolean set=false;
		for( int j=0; j< args0.length ; j++ ) {
		    if( args0[j].equalsIgnoreCase( arg )) {
			setProperty( proxy, args0[j], "true");
			set=true;
			break;
		    }
		}
		if( set ) continue;
	    }
	    if( args1!=null ) {
		for( int j=0; j< args1.length ; j++ ) {
		    if( args1[j].equalsIgnoreCase( arg )) {
			i++;
			if( i >= args.length )
			    return false;
			setProperty( proxy, arg, args[i]);
			break;
		    }
		}
	    } else {
		// if args1 is not specified,assume all other options have param
		i++;
		if( i >= args.length )
		    return false;
		setProperty( proxy,arg, args[i]);
	    }

	}
	return true;
    }

    // -------------------- other utils  --------------------
    public static String[] findVoidSetters( Class c ) {
	Method m[]=findMethods( c );
	if( m==null ) return null;
	Vector v=new Vector();
	for( int i=0; i<m.length; i++ ) {
	    if( m[i].getName().startsWith("set") &&
		m[i].getParameterTypes().length == 0 ) {
		String arg=m[i].getName().substring( 3 );
		v.addElement( unCapitalize( arg ));
	    }
	}
	String s[]=new String[v.size()];
	for( int i=0; i<s.length; i++ ) {
	    s[i]=(String)v.elementAt( i );
	}
	return s;
    }

    public static String[] findBooleanSetters( Class c ) {
	Method m[]=findMethods( c );
	if( m==null ) return null;
	Vector v=new Vector();
	for( int i=0; i<m.length; i++ ) {
	    if( m[i].getName().startsWith("set") &&
	    	m[i].getParameterTypes().length == 1 &&
	    	"boolean".equals( m[i].getParameterTypes()[0].getName()) ) {
	    	String arg=m[i].getName().substring( 3 );
	    	v.addElement( unCapitalize( arg ));
	    }
	}
	String s[]=new String[v.size()];
	for( int i=0; i<s.length; i++ ) {
	    s[i]=(String)v.elementAt( i );
	}
	return s;
    }
    
    static Hashtable objectMethods=new Hashtable();

    public static Method[] findMethods( Class c ) {
	Method methods[]= (Method [])objectMethods.get( c );
	if( methods != null ) return methods;
	
	methods=c.getMethods();
	objectMethods.put( c, methods );
	return methods;
    }

    public static Method findMethod( Class c, String name, Class params[] ) {
	Method methods[] = findMethods( c );
	if( methods==null ) return null;
	for (int i = 0; i < methods.length; i++) {
	    if (methods[i].getName().equals(name) ) {
		Class methodParams[]=methods[i].getParameterTypes();
		if( methodParams==null )
		    if( params==null || params.length==0 )
			return methods[i];
		if( params==null )
		    if( methodParams==null || methodParams.length==0 )
			return methods[i];
		if( params.length != methodParams.length )
		    continue;
		boolean found=true;
		for( int j=0; j< params.length; j++ ) {
		    if( params[j] != methodParams[j] ) {
			found=false;
			break;
		    }
		}
		if( found ) return methods[i];
	    }
	}
	return null;
    }
    
    /** Test if the object implements a particular
     *  method
     */
    public static boolean hasHook( Object obj, String methodN ) {
	try {
	    Method myMethods[]=findMethods( obj.getClass() );
	    for( int i=0; i< myMethods.length; i++ ) {
		if( methodN.equals ( myMethods[i].getName() )) {
		    // check if it's overriden
		    Class declaring=myMethods[i].getDeclaringClass();
		    Class parentOfDeclaring=declaring.getSuperclass();
		    // this works only if the base class doesn't extend
		    // another class.

		    // if the method is declared in a top level class
		    // like BaseInterceptor parent is Object, otherwise
		    // parent is BaseInterceptor or an intermediate class
		    if( ! "java.lang.Object".
			equals(parentOfDeclaring.getName() )) {
			return true;
		    }
		}
	    }
	} catch ( Exception ex ) {
	    ex.printStackTrace();
	}
	return false;
    }

    public static void callMain( Class c, String args[] )
	throws Exception
    {
	Class p[]=new Class[1];
	p[0]=args.getClass();
	Method m=c.getMethod( "main", p);
	m.invoke( c, new Object[] {args} );
    }
    
    public static Object callMethod1( Object target,
				    String methodN,
				    Object param1,
				    String typeParam1,
				    ClassLoader cl)
	throws Exception
    {
	if( target==null || param1==null ) {
	    d("Assert: Illegal params " + target + " " + param1 );
	}
	if( dbg > 0 ) d("callMethod1 " + target.getClass().getName() +
			" " + param1.getClass().getName() +
			" " + typeParam1 );
	
	Class params[]=new Class[1];
	if( typeParam1==null )
	    params[0]=param1.getClass();
	else
	    params[0]=cl.loadClass( typeParam1 );
	Method m=findMethod( target.getClass(), methodN, params);
	if( m==null )
	    throw new NoSuchMethodException(target.getClass().getName() +
					    " " + methodN);
	return m.invoke(target,  new Object[] {param1 } );
    }

    public static Object callMethod0( Object target,
				    String methodN)
	throws Exception
    {
	if( target==null ) {
	    d("Assert: Illegal params " + target );
	    return null;
	}
	if( dbg > 0 )
	    d("callMethod0 " + target.getClass().getName() + "." + methodN);
	
	Class params[]=new Class[0];
	Method m=findMethod( target.getClass(), methodN, params);
	if( m==null )
	    throw new NoSuchMethodException(target.getClass().getName() +
					    " " + methodN);
	return m.invoke(target,  emptyArray );
    }
    
    static Object[] emptyArray=new Object[] {};
    
    public static Object callMethodN( Object target,  String methodN,
				    Object params[],  Class typeParams[] )
	throws Exception
    {
	Method m=null;
	m=findMethod( target.getClass(), methodN, typeParams );
        if( m== null ) {
	    d("Can't find method " + methodN + " in " +
	      target + " CLASS " + target.getClass());
	    return null;
	}
	Object o=m.invoke( target, params );

	if(dbg > 0 ) {
	    // debug
	    StringBuffer sb=new StringBuffer();
	    sb.append("" + target.getClass().getName() + "." + methodN + "( " );
	    for(int i=0; i<params.length; i++ ) {
		if(i>0) sb.append( ", ");
		sb.append(params[i]);
	    }
	    sb.append(")");
	    d(sb.toString());
	}
	return o;
    }
    
    // -------------------- Get property --------------------
    // This provides a layer of abstraction

    public static interface PropertySource {

	public String getProperty( String key );
	
    }

    public static interface AttributeHolder {

	public void setAttribute( String key, Object o );
	
    }

    
    // debug --------------------
    static final int dbg=0;
    static void d(String s ) {
	System.out.println("IntrospectionUtils: " + s );
    }
}
