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
package org.apache.tomcat.task;

import java.beans.*;
import java.io.*;
import java.io.IOException;
import java.lang.reflect.*;
import java.util.Hashtable;
import java.util.*;
import java.net.*;
import org.apache.tomcat.util.*;
import org.apache.tomcat.helper.*;
import org.apache.tomcat.util.xml.*;
import org.apache.tomcat.core.*;
import org.apache.tomcat.logging.*;
import org.xml.sax.*;

/**
 * Starter for Tomcat using server.XML.
 * Based on Ant.
 *
 * @author costin@dnt.ro
 */
public class StartTomcat {
    // Passed to ContxtManager - it have no other way
    // to find out. ( since java.class.path represent what
    // the VM knows, not what is used to load tomcat if
    // embeded )
    ClassLoader parentLoader;
    URL serverClassPath[];
    
    private static StringManager sm =
	StringManager.getManager("org.apache.tomcat.resources");
    Logger.Helper loghelper = new Logger.Helper("tc_log", "StartTomcat");

    public StartTomcat() {
    }

    public void execute() throws Exception {
	if( doHelp ) {
	    printUsage();
	    return;
	}
	if( doStop ) {
	    org.apache.tomcat.task.StopTomcat task=
		new  org.apache.tomcat.task.StopTomcat();

	    task.setConfig( configFile );
	    task.execute();     
	    return;
	}

	ContextManager cm=prepareContextManager();
	
	// XXX Make this optional, and make sure it doesn't require
	// a full start. It is called after init to make sure
	// auto-configured contexts are initialized.
	if( doGenerateConfigs ) {
	    generateServerConfig( cm );
	    return;
	}

	try {
	    cm.start(); // start serving
	}
	catch (java.net.BindException be) {
	    loghelper.log("Starting Tomcat: " + be.getMessage(), Logger.ERROR);
	    System.out.println(sm.getString("tomcat.bindexception"));
	    try {
		cm.stop();
	    }
	    catch (Exception e) {
		loghelper.log("Stopping ContextManager", e);
	    }
	}
    }

    public ContextManager prepareContextManager() throws Exception {
	XmlMapper xh=new XmlMapper();
	xh.setDebug( 0 );
	ContextManager cm=new ContextManager();
	cm.setParentLoader( parentLoader );
	cm.setServerClassPath( serverClassPath );
	
	ServerXmlHelper sxml=new ServerXmlHelper();
	sxml.setHelper( xh );
	sxml.setConnectorHelper( xh );
	sxml.setLogHelper( xh );

	String tchome=sxml.getTomcatInstall();
	cm.setInstallDir( tchome);

	// load server.xml
	File f = null;
	if (configFile != null)
	    f=new File(configFile);
	else
	    f=new File(tchome, DEFAULT_CONFIG);

	loadConfigFile(xh,f,cm);

	// load server-*.xml
	Vector v = sxml.getUserConfigFiles(f);
	for (Enumeration e = v.elements();
	     e.hasMoreElements() ; ) {
	    f = (File)e.nextElement();
	    loadConfigFile(xh,f,cm);
	}
	
	// by now, we should know where the log file is
	String path = cm.getLogger().getPath();
	if (path == null)
	    path = "console";
	else
	    path = new File(path).getAbsolutePath();
	System.out.println(sm.getString("tomcat.start", new Object[] { path }));
	
	cm.init(); // set up contexts
	loghelper.log(ContextManager.TOMCAT_NAME + " " +
		      ContextManager.TOMCAT_VERSION);
	return cm;
    }

    /** Special call to support a multiple class paths
     */
    public void setParentClassLoader( ClassLoader cl ) {
	parentLoader=cl;
    }

    public void setServerClassPath( URL urls[] ) {
	serverClassPath=urls;
    }
    
    /** This method will generate Server config files that
	reflect the existing cm settings. It is called
	at startup, and may be called when a new context is
	added ( at runtime for example ).
    */
    public static void generateServerConfig( ContextManager cm )
	throws TomcatException
    {
	// Generate Apache configs
	//
	org.apache.tomcat.task.ApacheConfig apacheConfig=
	    new  org.apache.tomcat.task.ApacheConfig();
	apacheConfig.execute( cm );     

	// Generate IIS configs
	//
	org.apache.tomcat.task.IISConfig iisConfig=
	    new  org.apache.tomcat.task.IISConfig();
	iisConfig.execute( cm );     

	// Generate Netscape configs
	//
	org.apache.tomcat.task.NSConfig nsConfig=
	    new  org.apache.tomcat.task.NSConfig();
	nsConfig.execute( cm );     
    }
    
    public static void printUsage() {
	System.out.println(sm.getString("tomcat.usage"));
    }

    public void loadConfigFile(XmlMapper xh, File f, ContextManager cm)
	throws Exception
    {
	loghelper.log(sm.getString("tomcat.loading") + " " + f);
	try {
	    xh.readXml(f,cm);
	} catch( Exception ex ) {
	    loghelper.log( sm.getString("tomcat.fatalconfigerror"), ex );
	    throw ex;
	}
	loghelper.log(sm.getString("tomcat.loaded") + " " + f);
    }

    // -------------------- setAttribute() --------------------

    /** The normal configuration pattern in tomcat and for
	ant tasks is using bean setters. In order to simplify the
	launcher ( that should be minimal ) we do provide the
	alternate setAttribute pattern.

	XXX in future we may use a util/ that will do proper
	introspection, etc

	Attribute names are identical with the bean setter name.
    */
    public void setAttribute( String n, Object v ) {
	if( "help".equals( n ) ) {
	    setHelp( true );
	} else if( "stop".equals( n ) ) {
	    setHelp( true );
	} else if( "parentClassLoader".equals( n ) ) {
	    setParentClassLoader( (ClassLoader)v);
	} else if( "serverClassPath".equals( n ) ) {
	    setServerClassPath( (URL[])v);
	} else if( "config".equals( n ) ||
		   "f".equals(n) ) {
	    setConfig( (String)v);
	} else if( "generateConfigs".equals( n ) ||
		   "g".equals(n) ) {
	    setGenerateConfigs( true );
	} else if( "home".equals( n ) ||
		   "h".equals(n)) {
	    setHome( (String)v);
	}
    }

    // -------------------- Command-line args processing --------------------
    // null means user didn't set one
    String configFile=null;
    boolean doHelp=false;
    boolean doStop=false;
    boolean doGenerateConfigs=false;
    // relative to TOMCAT_HOME 
    static final String DEFAULT_CONFIG="conf/server.xml";

    public void setGenerateConfigs( boolean b ) {
	doGenerateConfigs=b;
    }

    public void setG( boolean b ) {
	doGenerateConfigs=b;
    }

    /** Print help message
     */
    public void setHelp(boolean b) {
	doHelp=true;
    }

    public void setStop( boolean b ) {
	doStop=true;
    }

    public void setConfig( String s ) {
	configFile=s;
    }

    public void setF( String s ) {
	configFile=s;
    }

    public void setH( String h ) {
	System.out.println("home : " + h);
	System.getProperties().put("tomcat.home", h);
    }

    public void setHome( String h ) {
	System.getProperties().put("tomcat.home", h);
    }

    // -------------------- Special method called to get help
    public void help() {
	System.out.println(sm.getString("tomcat.usage"));
    }


    // -------------------- Command-line based startup
    /** Originally part of JNI endpoint
     */
    public int startup(String cmdLine,
		       String stdout,
		       String stderr)
    {
        try {
            if(null != stdout) {
                System.setOut(new PrintStream(new FileOutputStream(stdout)));
            }
            if(null != stderr) {
                System.setErr(new PrintStream(new FileOutputStream(stderr)));
            }
        } catch(Throwable t) {
        }
	
	// We need to make sure tomcat did start successfully and
	// report this back.
        try {
            StartupThread startup = new StartupThread(cmdLine);
            startup.start();
	    System.out.println("Starting up StartupThread");
            synchronized (this) {
                wait(60*1000);
            }
	    System.out.println("End waiting");
        } catch(Throwable t) {
        }

        if(running) {
	    System.out.println("Running fine ");
            return 1;
        }
	System.out.println("Error - why doesn't run ??");
        return 0;
    }

    boolean running = false;
    
    // Called back when the server is initializing the handler
    public void startNotify(Object handler) {
	// the handler is no longer useable
    	if( handler==null ) {
	    running=false;
	    notify();
	    return;
	}

	System.out.println("Running ...");
	running=true;
        notify();
    }
}

/** Tomcat is started in a separate thread. It may be loaded on demand,
    and we can't take up the request thread, as it may be affect the server.

    During startup the JNIConnectionHandler will be initialized and
    will configure JNIEndpoint ( static - need better idea )
 */
class StartupThread extends Thread {
    String []cmdLine = null;

    public StartupThread(String cmdLine) {

        if(null == cmdLine) {
        	this.cmdLine = new String[0];
        } else {
            Vector v = new Vector();
            StringTokenizer st = new StringTokenizer(cmdLine);
            while (st.hasMoreTokens()) {
                v.addElement(st.nextToken());
            }
            this.cmdLine = new String[v.size()];
            v.copyInto(this.cmdLine);
        }
    }

    public void run() {
        boolean failed = true;
        try {
	    System.out.println("Calling main" );
            org.apache.tomcat.startup.Tomcat.main(cmdLine);
	    System.out.println("Main returned" );
            failed = false;
        } catch(Throwable t) {
            t.printStackTrace(); // OK
        } finally {
            if(failed) {
		System.out.println("Failed ??");
            }
        }
    }
}
