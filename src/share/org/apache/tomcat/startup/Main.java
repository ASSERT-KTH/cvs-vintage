/* $Id: Main.java,v 1.35 2001/06/13 21:28:28 mmanders Exp $
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

import org.apache.tomcat.util.IntrospectionUtils;
import org.apache.tomcat.util.compat.Jdk11Compat;

// The main idea is to have a starter with minimal class loader deps,
// and use it to create the initial environment

/**
	Starter class for Tomcat.
	<p>
	This is a replacement/enhancement for the .sh and .bat files - you can
	use JDK1.2 "java -jar tomcat.jar", or ( for jdk 1.1 ) you just need to
	include a single jar file in the classpath.
	<p>
	This class creates three class loader instances: 
	<ol>
	<li>a 'common' loader to be the parent of both the server
	    container and also webapp loaders.</li>
	<li>an 'applications' loader to load classes used by all webapps, but
	    not the servlet engine.</i>
	<li>a 'container' loader exclusively for the tomcat servlet engine.</li>
	</ol>
	Both the 'apps' loader and 'container' loader have the common loader as
	the parent class loader.  The class path for each is assembled like so:
	<ul>
	<li>common - all elements of the 
	      <code>org.apache.tomcat.common.classpath</code>
	      property plus all *.jar files found in ${TOMCAT_HOME}/lib/common/.
	      </li>
	<li>apps - all elements of the 
	      <code>org.apache.tomcat.apps.classpath</code>
	      property plus all *.jar files found in ${TOMCAT_HOME}/lib/apps/.
	      In addition, all classes loaded via the 'common' loader.</i>
	<li>container - all jar files found in ${TOMCAT_HOME}/lib/container/ plus 
	      the class folder ${TOMCAT_HOME}/classes and finally also the utility 
	      jar file ${JAVA_HOME}/lib/tools.jar.  In addition, all classes loaded
	      via the common loader.</li>
	</ul>
	After creating the above class loaders, this class instantiates, initializes
	and starts an instance of the class <code>org.apache.tomcat.startup.Tomcat</code>.
	<p>
	@author Costin Manolache
	@author Ignacio J. Ortega
	@author Mel Martinez mmartinez@g1440.com
	@version $Revision: 1.35 $ $Date: 2001/06/13 21:28:28 $
 */
public class Main{

    /**
            name of configuration property to set (using the -D option at
            startup or via .properties file) to specify the classpath
            to be used by the ClassLoader shared amongst all web applications
            (but not by the servlet container).  Specify this string as
            normal file paths separated by the path.seperator delimiter for
            the host platform.  Example (unix):
            <pre><code>
            * org.apache.tomcat.apps.classpath = /home/mypath/lib/mylib.jar: \
            *                                      /home/mypath/classes/
            </code></pre>
    */
    public static final String TOMCAT_APPS_CLASSPATH_PROPERTY =
            "org.apache.tomcat.apps.classpath";

    /**
            the classpath shared among all web apps (in addition to any
            jar files placed directly in $TOMCAT_HOME/lib/apps/).
    */
    public static final String TOMCAT_APPS_CLASSPATH;

    /**
            name of configuration property to set (using the -D option at
            startup or via .properties file) to specify the classpath
            to be used by the ClassLoader common to both the servlet engine
            and all web applications.  Specify this string as
            normal file paths separated by the path.seperator delimiter for
            the host platform.  Example (unix):
            <pre><code>
            * org.apache.tomcat.common.classpath = /home/mypath/lib/mylib.jar: \
            *                                      /home/mypath/classes/
            </code></pre>
    */
    public static final String TOMCAT_COMMON_CLASSPATH_PROPERTY =
            "org.apache.tomcat.common.classpath";

    /**
            the classpath common to both the servlet engine and also to
            any web applications served by it (in addition to any
            jar files placed directly in $TOMCAT_HOME/lib/common/).
    */
    public static final String TOMCAT_COMMON_CLASSPATH;

    static{
        String s=null;
        s = System.getProperty(TOMCAT_APPS_CLASSPATH_PROPERTY);
        if(s==null){
            s="";
        }
        TOMCAT_APPS_CLASSPATH=s;
        s=null;
        s = System.getProperty(TOMCAT_COMMON_CLASSPATH_PROPERTY);
        if(s==null){
            s="";
        }
        TOMCAT_COMMON_CLASSPATH=s;
    }

    String installDir;
    String libBase;
    String serverBase;
    String commonBase;
    String homeDir;
    static final String DEFAULT_CONFIG="conf" + File.separator + "server.xml";
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
	System.err.println("TomcatStartup: " + s );
    }

    // -------------------- Utils --------------------

    public String getInstallDir() {
	if( installDir==null )
	    installDir=".";
	return installDir;
    }

    public String getServerDir() {
        if( libBase==null ){
	    libBase=getInstallDir() + File.separator + "lib" +
		File.separator + "container" + File.separator;
        }
	return libBase;
    }

    public String getAppsDir() {
        if( serverBase==null ){
	    serverBase=getInstallDir() + File.separator + "lib" +
		File.separator + "apps" + File.separator;
        }
	return serverBase;
    }

    public String getCommonDir() {
        if( commonBase==null ){
	    commonBase=getInstallDir() + File.separator + "lib" +
		File.separator+ "common" + File.separator;
			
        }
	return commonBase;
    }


    static final Jdk11Compat jdk11Compat=Jdk11Compat.getJdkCompat();

    protected void execute( String args[] ) throws Exception {

        try {
            installDir=IntrospectionUtils.guessInstall("tomcat.install",
                                "tomcat.home", "tomcat.jar");

            homeDir = System.getProperty("tomcat.home");

            ClassLoader parentL=this.getClass().getClassLoader();

            // the server classloader loads from classes dir too and
	    // from tools.jar

	    // Create the common class loader --------------------
	    Vector commonJars = new Vector();
	    IntrospectionUtils.addToClassPath( commonJars,
					       getCommonDir());
	    IntrospectionUtils.addJarsFromClassPath(commonJars,
						    TOMCAT_COMMON_CLASSPATH);
            addToTomcatClasspathSysProp(commonJars);

            URL[] commonClassPath=IntrospectionUtils.getClassPath(commonJars);
	    //            displayClassPath( "common ", commonClassPath );
	    ClassLoader commonCl=
                    jdk11Compat.newClassLoaderInstance(commonClassPath ,
						       parentL);


	    // Create the container class loader --------------------
            Vector serverJars=new Vector();
	    IntrospectionUtils.addToClassPath( serverJars, getServerDir());
	    IntrospectionUtils.addToolsJar( serverJars );


	    URL[] serverClassPath=IntrospectionUtils.getClassPath(serverJars);
	    //displayClassPath( "server ", serverClassPath );
            ClassLoader serverCl=
                    jdk11Compat.newClassLoaderInstance(serverClassPath ,
						       commonCl);

	    // Create the webapps class loader --------------------
            Vector appsJars = new Vector();
	    IntrospectionUtils.addToClassPath(appsJars, getAppsDir());
	    IntrospectionUtils.addJarsFromClassPath( appsJars, 
						     TOMCAT_APPS_CLASSPATH);
            addToTomcatClasspathSysProp(appsJars);

            URL[] appsClassPath=IntrospectionUtils.getClassPath(appsJars);
            ClassLoader appsCl=
		jdk11Compat.newClassLoaderInstance(appsClassPath ,
						       commonCl);

	    // Instantiate tomcat ( using container class loader )
            Class cls=serverCl.loadClass("org.apache.tomcat.startup.Tomcat");
            Object proxy=cls.newInstance();

            IntrospectionUtils.setAttribute(proxy,"args", args );
            IntrospectionUtils.setAttribute(proxy,"home", homeDir );
            IntrospectionUtils.setAttribute(proxy,"install", installDir );
            IntrospectionUtils.setAttribute(proxy,"parentClassLoader",appsCl);
	    IntrospectionUtils.setAttribute(proxy,"commonClassLoader",
					    commonCl);
	    IntrospectionUtils.setAttribute(proxy,"containerClassLoader",
					    serverCl);
	    IntrospectionUtils.setAttribute(proxy,"appsClassLoader",
					    appsCl);
            IntrospectionUtils.execute(  proxy, "execute" );
            return;
        } catch( Exception ex ) {
            System.out.println("Guessed home=" + homeDir);
            ex.printStackTrace();
        }
    }

    public void displayClassPath( String msg, URL[] cp ) {
	System.out.println(msg);
	for( int i=0; i<cp.length; i++ ) {
	    System.out.println( cp[i].getFile() );
	}
    }

    /**
     * Adds classpath entries from a vector of URL's to the
     * "tc_path_add" System property.  This System property lists
     * the classpath entries common to web applications. This System
     * property is currently used by Jasper when its JSP servlet
     * compiles the Java file for a JSP.
    */

    private void addToTomcatClasspathSysProp(Vector v)
    {
        String sep = System.getProperty("path.separator");
        String cp = System.getProperty("tc_path_add");

        Enumeration e = v.elements();
        while( e.hasMoreElements() ) {
            URL url = (URL)e.nextElement();
            if( cp != null)
                cp += sep + url.getFile();
            else
                cp = url.getFile();
        }
        if( cp != null)
            System.getProperties().put("tc_path_add",cp);
    }

}


