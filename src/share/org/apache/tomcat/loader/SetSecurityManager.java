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
package org.apache.tomcat.loader;

import java.lang.*;
import java.util.*;
import java.lang.reflect.*;
import java.security.*;

/*
 * Installs a SecurityManager if one was configured in server.xml
 */
public class SetSecurityManager {
    // Default Security Permissions for webapps and jsp
    private Permissions perms = new Permissions();

    public void setClassName(String className) {
        if( System.getSecurityManager() == null ) {
            System.out.println("Not started with a SecurityManager, ignoring SecurityManager and Permissions");
            return;
        }
	// Use of a SecurityManager requires java 1.2 or greater
	String jv = System.getProperty("java.version");
	if( jv.startsWith("1.0") || jv.startsWith("1.1") ) {
	    // For now just print an error, then exit.
	    // org.apache.tomcat.util.xml.XmlMapper.SetProperty() is
	    // catching Exceptions but not rethrowing them.
	    System.out.println("Use of a SecurityManager requires Java version 1.2 or greater");
	    System.exit(1);
	}
	try {
	    Class c=Class.forName(className);
	    Object o=c.newInstance();
	    System.setSecurityManager((SecurityManager)o);
	    System.out.println("Security Manager set to " + className);
	} catch( ClassNotFoundException ex ) {
	    System.out.println("SecurityManager Class not found: " + className);
	    System.exit(1);
	} catch( Exception ex ) {
            System.out.println("SecurityManager Class could not be loaded: " + className);
            System.exit(1);
	}
	
	SecurityManager sm = System.getSecurityManager();
	if (sm == null) {
	    System.out.println("Could not install SecurityManager: " + className);
	    System.exit(1);
	}
	System.out.println("Security Manager set to: " + className);
    }

    public void setPermission(String className, String attr, String value) {
        try {
            Class c=Class.forName(className);
            Constructor con=c.getConstructor(new Class[]{String.class,String.class});
	    Object [] args=new Object[2];
	    args[0] = attr;
	    args[1] = value;
	    Permission p = (Permission)con.newInstance(args);
	    perms.add(p);
        } catch( ClassNotFoundException ex ) {           
            System.out.println("SecurityManager Class not found: " + className);
            System.exit(1);                                    
        } catch( Exception ex ) {                              
            System.out.println("SecurityManager Class could not be loaded: " + className);
	    ex.printStackTrace();
            System.exit(1);                                    
        }
//	System.out.println("SecurityManager, " + className + ", \"" + attr + "\", \"" + value + "\" added");
    }

    public Permissions getPermissions() {
	return perms;
    }
}
