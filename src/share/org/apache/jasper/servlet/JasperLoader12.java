/*
 *  Copyright 1999-2004 The Apache Software Foundation
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
 *  See the License for the specific language 
 */

package org.apache.jasper.servlet;

import java.io.FileNotFoundException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.security.ProtectionDomain;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.jasper.JasperException;

/**
 * 1.2 version of the JspLoader
 * 
 * @author Anil K. Vijendran
 * @author Harish Prabandham
 * @author Costin Manolache 
 */
public class JasperLoader12 extends JasperLoader {

    JasperLoader12()
    {
	super();
    }

    /**
     */
    protected  Class defClass(String className, byte[] classData) {
        // If a SecurityManager is being used, set the ProtectionDomain
        // for this clas when it is defined.
	// 	System.out.println("JspLoader12: " + className + " " +
	// 			   ((ProtectionDomain)pd).getCodeSource() );
        if( pd != null ) {
	    return defineClass(className, classData, 0,
			       classData.length,
			       (ProtectionDomain)pd);
	}
        return defineClass(className, classData, 0, classData.length);
    }

    protected byte[] loadClassDataFromFile( String fileName ) {
	/**
	 * When using a SecurityManager and a JSP page itself triggers
	 * another JSP due to an errorPage or from a jsp:include,
	 * the loadClass must be performed with the Permissions of
	 * this class using doPriviledged because the parent JSP
	 * may not have sufficient Permissions.
	 */
	if( System.getSecurityManager() != null ) {
	    class doInit implements PrivilegedAction {
		private String fileName;
		public doInit(String file) {
		    fileName = file;
		}
		public Object run() {
		    return doLoadClassDataFromFile(fileName);
		}
	    }
	    doInit di = new doInit(fileName);
	    return (byte [])AccessController.doPrivileged(di);
	} else {
	    return doLoadClassDataFromFile( fileName );
	}
    }

    // Hack - we want to keep JDK1.2 dependencies in fewer places,
    // and same for doPriviledged.
    boolean loadJSP(JspServlet jspS, String name, String classpath, 
		    boolean isErrorPage, HttpServletRequest req,
		    HttpServletResponse res) 
	throws JasperException, FileNotFoundException 
    {
        if( System.getSecurityManager() == null ) {
	    return jspS.doLoadJSP( name, classpath, isErrorPage, req, res );
	}

	final JspServlet jspServlet=jspS;
	final String nameF=name;
	final String classpathF=classpath;
	final boolean isErrorPageF=isErrorPage;
	final HttpServletRequest reqF=req;
	final HttpServletResponse resF=res;
	try {
	    Boolean b = (Boolean)AccessController.doPrivileged(new
		PrivilegedExceptionAction() {
		    public Object run() throws Exception
		    {
			return new Boolean(jspServlet.doLoadJSP( nameF,
								 classpathF,
								 isErrorPageF,
								 reqF, resF ));
		    } 
		});
	    return b.booleanValue();
	} catch( Exception ex ) {
	    if( ex instanceof PrivilegedActionException ) 
		ex=((PrivilegedActionException)ex).getException();
	    
	    if( ex instanceof JasperException )
		throw (JasperException)ex;
	    if( ex instanceof FileNotFoundException )
		throw (FileNotFoundException) ex;
	    throw new JasperException( ex );
	}
    }

}
