/*
 * Copyright (c) 1997-1999 The Java Apache Project.  All rights reserved.
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
 * 3. All advertising materials mentioning features or use of this
 *    software must display the following acknowledgment:
 *    "This product includes software developed by the Java Apache 
 *    Project for use in the Apache JServ servlet engine project
 *    <http://java.apache.org/>."
 *
 * 4. The names "Apache JServ", "Apache JServ Servlet Engine" and 
 *    "Java Apache Project" must not be used to endorse or promote products 
 *    derived from this software without prior written permission.
 *
 * 5. Products derived from this software may not be called "Apache JServ"
 *    nor may "Apache" nor "Apache JServ" appear in their names without 
 *    prior written permission of the Java Apache Project.
 *
 * 6. Redistributions of any form whatsoever must retain the following
 *    acknowledgment:
 *    "This product includes software developed by the Java Apache 
 *    Project for use in the Apache JServ servlet engine project
 *    <http://java.apache.org/>."
 *    
 * THIS SOFTWARE IS PROVIDED BY THE JAVA APACHE PROJECT "AS IS" AND ANY
 * EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE JAVA APACHE PROJECT OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Java Apache Group. For more information
 * on the Java Apache Project and the Apache JServ Servlet Engine project,
 * please see <http://java.apache.org/>.
 *
 */

package org.apache.tomcat.util.depend;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.security.ProtectionDomain;
import java.util.Enumeration;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import org.apache.tomcat.util.compat.SimpleClassLoader;

public class DependClassLoader12 implements DependClassLoader.DCLFactory {

    public ClassLoader createDependLoader(DependManager depM, ClassLoader parent, Object pd, int debug ) {
        return new DependClassLoader12Impl( depM, parent, pd, debug );
    }
}

/** 
 * 1.2 support for DependClassLoader
 * 
 */
class DependClassLoader12Impl extends URLClassLoader {

    static org.apache.commons.logging.Log logger =
	org.apache.commons.logging.LogFactory.getLog(DependClassLoader12Impl.class);

    private final static String FILE_PROTOCOL = "file:";
    private final static String BANG = "!";

    protected ClassLoader parent;
    protected ClassLoader parent2;
    
    private static int debug=0;
    DependManager dependM;
    protected Object pd;

    public DependClassLoader12Impl( DependManager depM, ClassLoader parent, Object pd, int debug ) {
        super( new URL[0], parent );
	this.parent=parent;
	this.parent2=parent.getParent();
	dependM=depM;
	this.pd=pd;
        this.debug=debug;
    }

    protected synchronized Class loadClass(String name, boolean resolve)
        throws ClassNotFoundException
    {
	final String lname=name;
	final boolean lresolve=resolve;
	try {
	    return (Class)AccessController.doPrivileged( new PrivilegedExceptionAction() {
		    public Object run() throws Exception {
			return loadClassInternal1( lname, lresolve );
		    }
		});
	} catch( PrivilegedActionException pex ) {
	    Exception ex=pex.getException();
	    if( ex instanceof ClassNotFoundException )
		throw (ClassNotFoundException)ex;
	    // unknown - better display it 
	    ex.printStackTrace();
	    throw new ClassNotFoundException( name );
	}
    }

    protected Class defineClassCompat( String name, byte data[], int s, int end, URL res )
	throws ClassNotFoundException
    {
	// JDK1.2 - XXX need to fix for JDK1.1 compat 
	// 	CodeSource cs=new CodeSource( res , null);
	// 	PermissionCollection perms=Policy.getPolicy().getPermissions(cs);
	// 	ProtectionDomain pd=new ProtectionDomain( cs,perms);
	// 	System.out.println("XXX " + name + ": " + cs + "\n" + perms );
        int idx = name.lastIndexOf(".");
        String pkgname = idx != -1 ? name.substring(0, idx) : null;
        if ( pkgname != null ) {
          Package p = getPackage(pkgname);
          if ( p == null ) {
            if ( "jar".equals(res.getProtocol()) ) {
              try {
		  String JarN = res.getFile();
		  if (JarN.startsWith(FILE_PROTOCOL)) 
		      JarN = JarN.substring(FILE_PROTOCOL.length());
		  int bang = JarN.indexOf(BANG);
		  if (bang != -1) JarN = JarN.substring(0, bang);
 		  JarFile JarF = new JarFile(JarN);
		  Manifest mf = JarF.getManifest();
		  if(mf == null) // Jar may not be Java2
		      throw new IOException("No Manifest");
		  Attributes main = mf.getMainAttributes();
		  Attributes pkg = mf.getAttributes(
			 pkgname.replace('.', '/').concat("/")
		  );
		  boolean sealed = Boolean.valueOf(
			    getAttribute(Attributes.Name.SEALED, main, pkg)
                  ).booleanValue();
		  definePackage(
                  pkgname, 
                  getAttribute(Attributes.Name.SPECIFICATION_TITLE, main, pkg),
                  getAttribute(Attributes.Name.SPECIFICATION_VERSION, main, pkg),
                  getAttribute(Attributes.Name.SPECIFICATION_VENDOR, main, pkg),
                  getAttribute(Attributes.Name.IMPLEMENTATION_TITLE, main, pkg),
                  getAttribute(Attributes.Name.IMPLEMENTATION_VERSION, main, pkg),
                  getAttribute(Attributes.Name.IMPLEMENTATION_VENDOR, main, pkg),
                  sealed ? res : null
                );
		  JarF.close();
              } catch ( IOException e ) {
                definePackage(pkgname, null, null, null, null, null, null, null);
              }
            } else {
              definePackage(pkgname, null, null, null, null, null, null, null);
            }
          }
        }
 	return defineClass(name, data, s, end, (ProtectionDomain)pd);
    }

    public URL[] getURLs() {
        if( parent instanceof URLClassLoader )
            return ((URLClassLoader)parent).getURLs();
        if( parent instanceof SimpleClassLoader ) 
            return ((SimpleClassLoader)parent).getURLs();
        return super.getURLs();
    }
    
    private String getAttribute(Attributes.Name key, Attributes main, Attributes pkg)
    {
      String value = null;
      if ( pkg != null ) {
        value = (String)pkg.get(key);
      }
      if ( value == null ) {
        value = (String)main.get(key);
      }
      return value;
     }

    public Enumeration findResources(String name) 
	throws IOException {
	return parent.getResources(name);
    }

    /** Actual class loading. The name 'loadClassInternal' generates a warning,
     *  as a private method with the same name exists int ClassLoader in JDK1.1 ( Sun impl ).
     */
    protected Class loadClassInternal1( String name, boolean resolve )
	throws ClassNotFoundException
    {
	if( logger.isTraceEnabled() ) 
	    logger.trace( "loadClass() " + name + " " + resolve);
	// The class object that will be returned.
        Class c = null;

	// check if  we already loaded this class
	c = findLoadedClass( name );
	if (c!= null ) {
	    if(resolve) resolveClass(c);
	    return c;
        }

        String classFileName = name.replace('.', '/' ) + ".class";

	URL res=getResource( classFileName );

	// If it's in parent2, load it ( we'll not track sub-dependencies ).
	try {
	    c = parent2.loadClass(name);
	    if (c != null) {
		if (resolve) resolveClass(c);
		// No need, we can't reload anyway
		// dependency( c, res );
		return c;
	    }
	} catch (Exception e) {
	    c = null;
	}

	if( res==null ) 
	    throw new ClassNotFoundException(name);

	// This should work - SimpleClassLoader should be able to get
	// resources from jar files. 
	InputStream is=getResourceAsStream( classFileName );
	if( is==null ) 
	    throw new ClassNotFoundException(name);


	// It's in our parent. Our task is to track all class loads, the parent
	// should load anything ( otherwise the deps are lost ), but just resolve
	// resources.
	byte data[]=null;
	try {
	    if( is.available() > 0 ) {
		data=readFully( is );
		if( data.length==0 ) data=null;
	    }
	    is.close();
	} catch(IOException ex ) {
	    if( logger.isDebugEnabled() ) 
		logger.debug("error reading " + name, ex);
	    data=null;
	    throw new ClassNotFoundException( name + " error reading " + ex.toString());
	}
	if( data==null ) 
	    throw new ClassNotFoundException( name + " lenght==0");

	c=defineClassCompat( name, data, 0, data.length, res );
	dependency( c, res );
	
	if (resolve) resolveClass(c);

	return c;
    }

    public URL getResource(String name) {
	return parent.getResource(name);
    }

    public InputStream getResourceAsStream(String name) {
	return parent.getResourceAsStream( name );
    }

    private void dependency( Class c, URL res ) {
	if( res==null) return;
	File f=null;
	if( "file".equals( res.getProtocol() )) {
	    f=new File( res.getFile());
	    if( logger.isTraceEnabled() ) 
		logger.trace( "File dep "  +f );
	    if( ! f.exists()) f=null;
	}
	if( "jar".equals( res.getProtocol() )) {
	    String fileN=res.getFile();
	    int idx=fileN.indexOf( "!" );
	    if( idx>=0 )
		fileN=fileN.substring( 0, idx) ;
	    // Bojan Smojver <bojan@binarix.com>: remove jar:
	    if( fileN.startsWith( "file:" ))
		fileN=fileN.substring( 5 );
	    // If the standard URL parser is used ( jdk1.1 compat )
	    if( fileN.startsWith( "/file:" ))
		fileN=fileN.substring( 6 );
	    f=new File(fileN);
	    if( logger.isTraceEnabled() ) 
		logger.trace( "Jar dep "  +f + " " + f.exists() );
	    if( ! f.exists()) f=null;
	}

	if( f==null ) return;
	Dependency dep=new Dependency();
	dep.setLastModified( f.lastModified() );
	dep.setTarget( c );
	dep.setOrigin( f );

	dependM.addDependency( dep );
    }

    private byte[] readFully( InputStream is )
	throws IOException
    {
	byte b[]=new byte[1024];
	int count=0;

	int available=1024;
	
	while (true) {
	    int nRead = is.read(b,count,available);
	    if( nRead== -1 ) {
		// we're done reading
		byte result[]=new byte[count];
		System.arraycopy( b, 0, result, 0, count );
		return result;
	    }
	    // got a chunk
	    count += nRead;
            available -= nRead;
	    if( available == 0 ) {
		// buffer full
		byte b1[]=new byte[ b.length * 2 ];
		available=b.length;
		System.arraycopy( b, 0, b1, 0, b.length );
		b=b1;
	    }
        }
    }
}
