/* $Id: Main.java,v 1.46 2004/10/02 20:29:21 billbarker Exp $
 *   
 *  Copyright 1999-2004 The Apache Sofware Foundation.
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
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.tomcat.startup;

import java.io.File;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Hashtable;

import org.apache.tomcat.util.IntrospectionUtils;
import org.apache.tomcat.util.compat.Jdk11Compat;

// The main idea is to have a starter with minimal class loader deps,
// and use it to create the initial environment. This class is pretty generic,
// deps on tomcat are minimal ( depends only on tomcat.util ).

/**
 * Launcher capable of setting class loader and guessing locations.
 * <p>
 * This is a replacement/enhancement for the .sh and .bat files - you can
 * use JDK1.2 "java -jar [PROGRAM].jar", or ( for jdk 1.1 ) you just need to
 * include a single jar file in the classpath.
 * <p>
 * The class will first guess it's own location by looking in each classpath
 * location. It'll then process the command line parameters and based on
 * a properties file, locate the actual class that will be started.
 * <p>
 * It'll then construct a class loader ( common ) from the content of a
 * specified directory and/or additionl system property. Based on the first
 * argument, it'll instantiate a class ( in the created class loader ), set the
 * parameters, and call it's execute() method.
 *
 * @author Costin Manolache
 * @author Ignacio J. Ortega
 * @author Mel Martinez mmartinez@g1440.com
 */
public class Main{
    /** System property that can be used to pass additional classpath
	to the 'common' loader, used to load EmbededTomcat and the
	core. EmbededTomcat will load the container int a separate
	loader, and each applications will be set up int its own loader.
	The loader configurator module may use additional properties
    */
    public static final String PROPERTY_COMMON_LOADER =
	"org.apache.tomcat.common.classpath";

    String installDir;
    String libDir;
    String serverBase;
    String commonBase;
    String homeDir;
    ClassLoader parentL;
    
    public Main() {
    }

    // -------------------- Properties --------------------

    public void setLibDir( String dir ) {
	libDir=dir;
    }

    public void setLoaderProperty( String prop ) {

    }

    public void setInstallDir( String dir ) {

    }

    public void setParentLoader( ClassLoader p ) {
	parentL=p;
    }

    // -------------------- Main --------------------
    
    public static void main(String args[] ) {
	try {
	    Main m=new Main();
	    m.processArgs( args );
	    m.execute();
	} catch(Exception ex ) {
	    System.out.println("Fatal error");
	    ex.printStackTrace();
	}
    }
 
    void log( String s ) {
	System.err.println("TomcatStartup: " + s );
    }

    // -------------------- Utils --------------------

    static final Jdk11Compat jdk11Compat=Jdk11Compat.getJdkCompat();
    String args[];
    URL commonCP[];
    ClassLoader commonCL;
    
    public void processArgs( String args[] ) {
	this.args=args;
    }

    private void initDirs()
	throws Exception
    {
	if( installDir==null ) {
	    installDir=	IntrospectionUtils.
		guessInstall("tomcat.install", "tomcat.home","tomcat.jar");
	}
	if( installDir==null )
	    installDir=".";
	    
	if( libDir==null ){
	    libDir=installDir + File.separator + "lib" + File.separator +
		"common";
	}
    }
    
    public void initClassLoader() {
	if( parentL==null )
	    parentL=this.getClass().getClassLoader();
	commonCL=
	    jdk11Compat.newClassLoaderInstance(commonCP, parentL);
	if( dL > 0 )
	    IntrospectionUtils.displayClassPath("Main classpath: ", commonCP );
    }

    // initSecurityFile is intended to simplify sandbox config, the shell
    // script can't normalize the path. We also want java -jar to behave the same,
    // without requiring anything difficult.
    
    /** If "-sandbox" parameter is found ( the first after the action ), we'll
     *  load a sandbox with the policy in install/conf/tomcat.policy. This
     *  has to happen before loading any class or constructing the loader, or
     *  some VMs will have wrong permissions.
     *  
     *  We do that here, instead of the shell script, in order to support java -jar
     *  and to minimize the ammount of platform-dependent code.
     *
     *  Note that we are not setting a security manager - just adding permissions
     *  so that all "system" classes have permissions.
     */
    public void initSecurityFile() {
	if( args.length > 1 &&
	    "-sandbox".equals( args[1] ) ) {
	    String oldPolicy=System.getProperty("java.security.policy");
	    if( oldPolicy != null ) {
		if( oldPolicy.startsWith("=") )
		    oldPolicy=oldPolicy.substring(1);
		File f=new File( oldPolicy );
		if( ! f.exists() ) {
		    debug( "Can't find old policy " + oldPolicy );
		    oldPolicy=null;
		}
	    }
	    if( null == oldPolicy ) {
		File f=null;
		String policyFile=installDir + File.separator + "conf" +
		    File.separator + "tomcat.policy";
		
		debug("Setting policy " + policyFile );
		System.getProperties().put( "tomcat.home", installDir );
		System.getProperties().put("java.security.policy",  policyFile);
		jdk11Compat.refreshPolicy();
	    }
	}
    }
    
    // -------------------- Tasks --------------------
    
    static Hashtable tasks=new Hashtable();
    static {
	tasks.put("stop", "org.apache.tomcat.startup.StopTomcat");
	tasks.put("enableAdmin", "org.apache.tomcat.startup.EnableAdmin");
	tasks.put("start", "org.apache.tomcat.startup.EmbededTomcat");
        tasks.put("startb", "org.apache.tomcat.startup.EmbededTomcat"); 
	tasks.put("run", "org.apache.tomcat.startup.EmbededTomcat");
	tasks.put("jspc", "org.apache.tomcat.startup.Jspc");
	tasks.put("estart", "org.apache.tomcat.startup.EmbededTomcat");
	tasks.put("", "org.apache.tomcat.startup.EmbededTomcat");
    }

    String task;

    public void setTask( String s ) {
	task=s;
    }
    
    String findTask( String args[] ) {
	// XXX We should display a help with all actions !
	if( args.length == 0 ) return null;
	String arg=args[0];
	if( arg.startsWith( "-" ) )
	    arg="";
	if( tasks.get( arg ) == null ) {
	    return null;
	}
	if( dL>0)
	    debug("Task: " + arg + " " + tasks.get( arg ));
	return arg;
    }

    public void printUsage() {
	PrintStream out=System.out;
	out.println( "Usage: java " + this.getClass().getName() +
		     " [task] [options]");
	out.println();
	out.println("Tasks: " );
	Enumeration keys=tasks.keys();
	while( keys.hasMoreElements() ) {
	    String t=(String)keys.nextElement();
	    String classN=(String)tasks.get(t);
	    out.println("    " + t );
	    printOptions( classN );
	}
	out.println();
    }

    private void printOptions( String classN ) {
    }
    
    // -------------------- Execute --------------------
    
    public void execute() throws Exception {

        try {
	    if( task==null )
		task=findTask( args );
	    if(  null == task) {
		printUsage();
		return;
	    }

	    initDirs();
	    commonCP=
		IntrospectionUtils.getClassPath( libDir, null,
						 PROPERTY_COMMON_LOADER,
						 false);
	    initSecurityFile();
	    initClassLoader();

	    jdk11Compat.setContextClassLoader(commonCL);
            Class cls=commonCL.loadClass((String)tasks.get(task));
	    
            Object proxy=cls.newInstance();

            IntrospectionUtils.setAttribute(proxy,"install", installDir );

	    IntrospectionUtils.setAttribute(proxy,"parentClassLoader",parentL);
	    IntrospectionUtils.setAttribute(proxy,"commonClassPath",
					    commonCP);
	    IntrospectionUtils.setAttribute(proxy,"commonClassLoader",
					    commonCL);
            IntrospectionUtils.setAttribute(proxy,"args", args );

            IntrospectionUtils.execute(  proxy, "execute" );
        } catch( Exception ex ) {
            System.out.println("Guessed home=" + installDir);
            System.out.println("Exception: " + ex);
            ex.printStackTrace();
            if( ex instanceof InvocationTargetException ) {
                Throwable t = ((InvocationTargetException)ex).getTargetException();
                System.out.println("Root Exception: " + t );
                t.printStackTrace();
            }
        }
    }

    private static int dL=0;
    private void debug( String s ) {
	System.out.println("Main: " +s );
    }
}


