package org.apache.tomcat.startup;

import java.beans.*;
import java.io.*;
import java.io.IOException;
import java.lang.reflect.*;
import java.util.Hashtable;
import java.util.*;
import java.net.*;
import org.apache.tomcat.util.res.StringManager;
import org.apache.tomcat.modules.config.*;
import org.apache.tomcat.util.xml.*;
import org.apache.tomcat.core.*;
import org.apache.tomcat.util.log.*;
import org.xml.sax.*;
import org.apache.tomcat.util.collections.*;

/**
 * Main entry point to several Tomcat functions. Uses EmbededTomcat to
 * start and init tomcat, and special functions to stop, configure, etc.
 * 
 * It is intended as a replacement for the shell command - EmbededTomcat
 * is the "real" tomcat-specific object that deals with tomcat internals,
 * this is just a wrapper.
 * 
 * It can be used in association with Main.java - in order to set the
 * CLASSPATH.
 * 
 * @author costin@dnt.ro
 */
public class Tomcat {

    private static StringManager sm =
	StringManager.getManager("org.apache.tomcat.resources");

    private String action="start";

    EmbededTomcat tomcat=new EmbededTomcat();

    String home=null;
    
    String args[];

    // null means user didn't set one
    String configFile=null;
    boolean fastStart=false;
    
    // relative to TOMCAT_HOME
    static final String DEFAULT_CONFIG="conf/server.xml";
    SimpleHashtable attributes=new SimpleHashtable();
    static Log log=Log.getLog( "tc_log", "Tomcat" );
    
    public Tomcat() {
    }
    //-------------------- Properties --------------------
    
    public void setHome(String home) {
	this.home=home;
	tomcat.setHome( home );
    }
    
    public void setInstall(String install) {
	tomcat.setInstall(install);
    }
    
    public void setArgs(String args[]) {
	this.args=args;
    }
    

    public void setAction(String s ) {
	action=s;
    }

    public void setSandbox( boolean b ) {
	tomcat.setSandbox( b );
    }
    
    public void setParentClassLoader( ClassLoader cl ) {
	tomcat.setParentClassLoader(cl);
    }

    public void setCommonClassLoader( ClassLoader cl ) {
	tomcat.setCommonClassLoader( cl );
    }

    public void setAppsClassLoader( ClassLoader cl ) {
	tomcat.setAppsClassLoader( cl );
    }

    public void setContainerClassLoader( ClassLoader cl ) {
	tomcat.setContainerClassLoader( cl );
    }
    
    // -------------------- main/execute --------------------
    
    public static void main(String args[] ) {
	try {
	    Tomcat tomcat=new Tomcat();
	    tomcat.setArgs( args );
            tomcat.execute();
	} catch(Exception ex ) {
	    log.log(sm.getString("tomcat.fatal"), ex);
	    System.exit(1);
	}
    }

    public void execute() throws Exception {
	//	String[] args=(String[])attributes.get("args");
        if ( args == null || ! processArgs( args )) {
	    setAction("help");
	}
	if( "stop".equals( action )){
	    stopTomcat();
	} else if( "enableAdmin".equals( action )){
	    enableAdmin();
	} else if( "help".equals( action )) {
	    printUsage();
	} else if( "start".equals( action )) {
	    startTomcat();
	}
    }

    // -------------------- Actions --------------------

    public void enableAdmin() throws IOException
    {
	System.out.println("Overriding apps-admin settings ");
	FileWriter fw=new FileWriter( home + File.separator +
				      "conf" + File.separator +
				      "apps-admin.xml" );
	PrintWriter pw=new PrintWriter( fw );
        pw.println( "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>");
	pw.println( "<webapps>" );
        pw.println( "    <!-- Special rules for the admin webapplication -->");
	pw.println( "    <Context path=\"/admin\"");
	pw.println( "             docBase=\"webapps/admin\"");
	pw.println( "             trusted=\"true\">");
	pw.println( "            <SimpleRealm filename=\"conf/users/admin-users.xml\" />");
	pw.println( "    </Context>");
	pw.println( "</webapps>" );
	pw.close();
    }
	
    public void stopTomcat() throws TomcatException {
	try {
	    StopTomcat task=
		new  StopTomcat();

	    task.execute();     
	}
	catch (Exception te) {
	    if( te instanceof TomcatException ) {
		if (((TomcatException)te).getRootCause() instanceof java.net.ConnectException)
		    System.out.println(sm.getString("tomcat.connectexception"));
		else
		    throw (TomcatException)te;
	    } else
		throw new TomcatException( te );
	}
	return;
    }

    public void startTomcat() throws TomcatException {
	if( tomcat==null ) tomcat=new EmbededTomcat();
	
	if( ! tomcat.isInitialized() ) {
	    long time1=System.currentTimeMillis();
	    PathSetter pS=new PathSetter();
	    tomcat.addInterceptor( pS );

	    ServerXmlReader sxmlConf=new ServerXmlReader();
	    sxmlConf.setConfig( configFile );
	    tomcat.addInterceptor( sxmlConf );

	    tomcat.initContextManager();

	    long time2=System.currentTimeMillis();
	    tomcat.log("Init time "  + (time2-time1));
	}

	long time3=System.currentTimeMillis();
	tomcat.start();
	long time4=System.currentTimeMillis();
	tomcat.log("Startup " + ( time4-time3 ));
    }

    
    // -------------------- Command-line args processing --------------------


    public static void printUsage() {
	//System.out.println(sm.getString("tomcat.usage"));
	System.out.println("Usage: java org.apache.tomcat.startup.Tomcat {options}");
	System.out.println("  Options are:");
        System.out.println("    -ajpid file                Use this file instead of conf/ajp12.id");
        System.out.println("                                 Use with -stop option");
	System.out.println("    -config file (or -f file)  Use this file instead of server.xml");
        System.out.println("    -enableAdmin               Updates admin webapp config to \"trusted\"");
	System.out.println("    -help (or help)            Show this usage report");
	System.out.println("    -home dir (or -h dir)      Use this directory as tomcat.home");
	System.out.println("    -install dir (or -i dir)   Use this directory as tomcat.install");
        System.out.println("    -sandbox                   Enable security manager (includes java.policy)");
	System.out.println("    -stop                      Shut down currently running Tomcat");
        System.out.println();
        System.out.println("In the absence of \"-enableAdmin\" and \"-stop\", Tomcat will be started");
    }

    /** Process arguments - set object properties from the list of args.
     */
    public  boolean processArgs(String[] args) {
	for (int i = 0; i < args.length; i++) {
	    String arg = args[i];

	    if (arg.equals("-help") || arg.equals("help")) {
		action="help";
		return false;
	    } else if (arg.equals("-stop")) {
		action="stop";
	    } else if (arg.equals("-sandbox")) {
		setSandbox(true);
	    } else if (arg.equals("-security")) {
		setSandbox(true);
	    } else if (arg.equals("-fastStart")) {
		fastStart=true;
	    } else if (arg.equals("-enableAdmin")) {
		action="enableAdmin";
	    } else if (arg.equals("-f") || arg.equals("-config")) {
		i++;
		if( i < args.length )
		    configFile = args[i];
		else
		    return false;
	    } else if (arg.equals("-h") || arg.equals("-home")) {
		i++;
		if (i < args.length)
		    setHome( args[i] );
		else
		    return false;
	    } else if (arg.equals("-i") || arg.equals("-install")) {
		i++;
		if (i < args.length)
		    setInstall( args[i] );
		else
		    return false;
	    } else if (arg.equalsIgnoreCase("-ajpid") ) {
                // accept this argument so it can pass through to StopTomcat
		i++;
		if (i >= args.length) 
		    return false;
            }
	}
	return true;
    }

    // Hack for Main.java, will be replaced with calling the setters directly
    public void setAttribute(String s,Object o) {
	if( "home".equals( s ) )
	    setHome( (String)o);
	if( "install".equals( s ) )
	    setInstall( (String)o);
	else if("args".equals( s ) ) 
	    setArgs((String[])o);
	else if( "parentClassLoader".equals( s ) ) 
	    setParentClassLoader((ClassLoader)o);
	else if( "appsClassLoader".equals( s ) ) 
	    setAppsClassLoader((ClassLoader)o);
	else if( "commonClassLoader".equals( s ) ) 
	    setCommonClassLoader((ClassLoader)o);
	else if( "containerClassLoader".equals( s ) ) 
	    setContainerClassLoader((ClassLoader)o);
	else {
	    System.out.println("Tomcat: setAttribute " + s + "=" + o);
	    attributes.put(s,o);
	}
    }
}
