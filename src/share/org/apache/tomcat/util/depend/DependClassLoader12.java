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

import java.io.*;
import java.lang.*;
import java.net.*;
import java.text.*;
import java.util.*;
import java.util.jar.*;
import java.util.zip.*;
import java.security.*;

import org.apache.tomcat.util.compat.*;
/** 
 * 1.2 support for DependClassLoader
 * 
 */
public class DependClassLoader12 extends DependClassLoader {
	
    private final static String FILE_PROTOCOL = "file:";
    private final static String BANG = "!";
	
    DependClassLoader12() {
    }
    
    public DependClassLoader12( DependManager depM, ClassLoader parent, Object pd ) {
	super(depM, parent, pd);
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

    protected Enumeration findResources(String name) 
	throws IOException {
	return parent.getResources(name);
    }
}
