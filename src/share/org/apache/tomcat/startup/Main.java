/* $Id: Main.java,v 1.31 2001/03/25 21:53:15 larryi Exp $
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
	@version $Revision: 1.31 $ $Date: 2001/03/25 21:53:15 $
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
	System.err.println("TomcatStartup: " + s );
    }

    // -------------------- Utils --------------------

    public static String checkDir( String base ) {
        String r=null;
        try {
            File f = new File(base);
            r = f.getCanonicalPath();
            if( ! r.endsWith("/") ) r+="/";
        } catch (IOException ioe) {
            ioe.printStackTrace();
            r=base;
        }
        return r;
    }

    public static URL getURL( String base, String file ) {
        try {
            File baseF = new File(base);
            File f = new File(baseF,file);
            String path = f.getCanonicalPath();
            if( f.isDirectory() ){
                    path +="/";
            }
            return new URL( "file", "", path );
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public String getServerDir() {
        if( libBase!=null ){
            return libBase;
        }
        if( installDir!=null ){
            libBase=checkDir( installDir + "/lib/container");
        }else{
            libBase=checkDir("./lib/container");
        }
        return libBase;
    }

    public String getAppsDir() {
        if( serverBase!=null ){
            return serverBase;
        }
        if( installDir!=null ){
            serverBase=checkDir( installDir + "/lib/apps");
        }else{
            serverBase=checkDir("./lib/apps");
        }
        return serverBase;
    }

    public String getCommonDir() {
        if( commonBase!=null ){
            return commonBase;
        }
        if( installDir!=null ){
            commonBase=checkDir( installDir + "/lib/common");
        }else{
            commonBase=checkDir("./lib/common");
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

            // the server classloader loads from classes dir too and from tools.jar

            Vector serverJars=new Vector();
            //serverJars.addElement( getURL(  getServerDir() ,"../classes/" ));
            Vector serverUrlV =getClassPathV(getServerDir());
            for(int i=0; i < serverUrlV.size();i++){
                serverJars.addElement(serverUrlV.elementAt(i));
            }
            serverJars.addElement( new URL( "file", "" ,
                System.getProperty( "java.home" ) + "/../lib/tools.jar"));

            Vector commonDirJars = getClassPathV(getCommonDir());
            Vector commonJars = getJarsFromClassPath(TOMCAT_COMMON_CLASSPATH);
            Enumeration jars = commonDirJars.elements();
            while(jars.hasMoreElements()){
                URL url = (URL)jars.nextElement();
                if(!commonJars.contains(url)){
                    commonJars.addElement(url);
                }
            }
            Vector appsDirJars = getClassPathV(getAppsDir());
            Vector appsJars = getJarsFromClassPath(TOMCAT_APPS_CLASSPATH);
            jars = appsDirJars.elements();
            while(jars.hasMoreElements()){
                URL url = (URL)jars.nextElement();
                if(!appsJars.contains(url)){
                    appsJars.addElement(url);
                }
            }
            URL[] commonClassPath=getURLs(commonJars);
            ClassLoader commonCl=
                    jdk11Compat.newClassLoaderInstance(commonClassPath ,parentL);
            URL[] appsClassPath=getURLs(appsJars);
            ClassLoader appsCl=
                    jdk11Compat.newClassLoaderInstance(appsClassPath ,commonCl);
            URL[] serverClassPath=getURLs(serverJars);
            ClassLoader serverCl=
                    jdk11Compat.newClassLoaderInstance(serverClassPath ,commonCl);


            Class cls=serverCl.loadClass("org.apache.tomcat.startup.Tomcat");
            Object proxy=cls.newInstance();

            IntrospectionUtils.setAttribute(proxy,"args", args );
            IntrospectionUtils.setAttribute(proxy,"home", homeDir );
            IntrospectionUtils.setAttribute(proxy,"install", installDir );
            IntrospectionUtils.setAttribute(proxy,"parentClassLoader",appsCl);
            IntrospectionUtils.execute(  proxy, "execute" );
            return;
        } catch( Exception ex ) {
            System.out.println("Guessed home=" + homeDir);
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
/*
    String args0[]= { "help", "stop", "g", "generateConfigs" };
    String args1[]= { "f", "config", "h", "home" };

     Read command line arguments and set properties in proxy,
	using ant-like patterns
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
*/
    /**
            add elements from the classpath <i>cp</i> to a Vector
            <i>jars</i> as file URLs (We use Vector for JDK 1.1 compat).
            <p>
            @param <b>cp</b> a String classpath of directory or jar file
                            elements separated by path.separator delimiters.
            @return a Vector of URLs.
    */
    public static Vector getJarsFromClassPath(String cp)
            throws IOException,MalformedURLException{
        Vector jars = new Vector();
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
        return jars;
    }

    public String[] getJarFiles(String ld) {
	File dir = new File(ld);
        String[] names=null;
        if (dir.isDirectory()){
            names = dir.list( new FilenameFilter(){
            public boolean accept(File d, String name) {
                if (name.endsWith(".jar")){
                    return true;
                }
                return false;
            }
            });
        }

	return names;
    }

    Vector getClassPathV(String p0) throws Exception {
        Vector urlV=new Vector();
        try{
            String cpComp[]=getJarFiles(p0);
            if (cpComp != null){
                int jarCount=cpComp.length;
                for( int i=0; i< jarCount ; i++ ) {
                    urlV.addElement( getURL(  p0 , cpComp[i] ));
                }
            }
        }catch(Exception ex){
            ex.printStackTrace();
        }
        return urlV;
    }

    private URL[] getURLs(Vector v){
        URL[] urls=new URL[ v.size() ];
        for( int i=0; i<v.size(); i++ ) {
            urls[i]=(URL)v.elementAt( i );
        }
        return urls;
    }

}


