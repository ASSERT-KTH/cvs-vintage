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
import org.apache.tomcat.util.IntrospectionUtils;

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

    EmbededTomcat tomcat=new EmbededTomcat();

    // relative to TOMCAT_HOME
    static final String DEFAULT_CONFIG="conf/server.xml";

    Hashtable attributes=new Hashtable();
    static Log log=Log.getLog( "tc_log", "Tomcat" );
    
    public Tomcat() {
    }
    //-------------------- Properties --------------------
    
    public void setHome(String home) {
	if( dL > 0 ) debug( "setHome " + home );
	attributes.put( "home", home );
    }
    public void setH(String home) {
	setHome( home );
    }

    public void setInstall(String install) {
	attributes.put( "install", install );
    }
    
    public void setI(String install) {
	setInstall( install );
    }
    
    public void setArgs(String args[]) {
	attributes.put("args", args);
    }

    public void setConfig( String s ) {
	attributes.put( "config" , s );
    }

    public void setF( String s ) {
	setConfig( s );
    }

    public void setAction(String s ) {
	attributes.put("action",s);
	attributes.put(s, "true" );
    }

    public void setSandbox( boolean b ) {
	if( b ) attributes.put( "sandbox", "true" );
    }
    
    public void setStop( boolean b ) {
	if( b ) attributes.put( "stop", "true" );
    }
    
    public void setEnableAdmin( boolean b ) {
	if( b ) attributes.put( "enableAdmin", "true" );
    }
    
    public void setParentClassLoader( ClassLoader cl ) {
	attributes.put( "parentClassLoader", cl );
    }

    public void setCommonClassLoader( ClassLoader cl ) {
	attributes.put( "commonClassLoader", cl );
    }

    public void setAppsClassLoader( ClassLoader cl ) {
	attributes.put( "appsClassLoader", cl );
    }

    public void setContainerClassLoader( ClassLoader cl ) {
    	attributes.put( "containerClassLoader", cl );
    }
    
    // -------------------- execute --------------------
    
    public void execute() throws Exception {
	if( attributes.get("stop") != null ) {
	    stopTomcat();
	} else if( attributes.get("enableAdmin") != null ){
	    enableAdmin();
	} else if( attributes.get("help") != null ) {
	    printUsage();
	} else {
	    startTomcat();
	}
    }

    // -------------------- Actions --------------------

    public void enableAdmin() throws TomcatException
    {
	try {
	    EnableAdmin task= new EnableAdmin();
	    task.setHome( (String)attributes.get("home") );
	    task.processArgs( (String[])attributes.get("args"));
	    task.execute();     
	} catch (Exception te) {
	    te.printStackTrace();
	    throw new TomcatException( te );
	}
    }
	
    public void stopTomcat() throws TomcatException {
	try {
	    StopTomcat task= new  StopTomcat();
	    task.setHome( (String)attributes.get("home") );
	    task.processArgs( (String[])attributes.get("args"));
	    task.execute();     
	} catch (Exception te) {
	    throw new TomcatException( te );
	}
    }

    public void startTomcat() throws TomcatException {
	if( tomcat==null ) tomcat=new EmbededTomcat();
	setTomcatProperties();
	
	if( ! tomcat.isInitialized() ) {
	    long time1=System.currentTimeMillis();
	    PathSetter pS=new PathSetter();
	    tomcat.addInterceptor( pS );

	    ServerXmlReader sxmlConf=new ServerXmlReader();
	    if( null!=attributes.get( "config" ) )
		sxmlConf.setConfig( (String)attributes.get("config") );
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

    private void setTomcatProperties() {
	if( attributes.get("home") != null )
	    tomcat.setHome( (String)attributes.get("home"));
	if( attributes.get("install") != null )
	    tomcat.setInstall( (String)attributes.get("install"));
	if( attributes.get("parentClassLoader") != null )
	    tomcat.setParentClassLoader((ClassLoader)attributes.get("parentClassLoader"));
	if( attributes.get("commonClassLoader") != null )
	    tomcat.setCommonClassLoader((ClassLoader)attributes.get("commonClassLoader"));
	if( attributes.get("appsClassLoader") != null )
	    tomcat.setAppsClassLoader( (ClassLoader)attributes.get("appsClassLoader"));
	if( attributes.get("containerClassLoader") != null )
	    tomcat.setContainerClassLoader( (ClassLoader)attributes.get("containerClassLoader"));
	if( null!= attributes.get("sandbox"))
	    tomcat.setSandbox( true );
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
	System.out.println("    -home dir                  Use this directory as tomcat.home");
	System.out.println("    -install dir (or -i dir)   Use this directory as tomcat.install");
        System.out.println("    -sandbox                   Enable security manager (includes java.policy)");
	System.out.println("    -stop                      Shut down currently running Tomcat");
        System.out.println();
        System.out.println("In the absence of \"-enableAdmin\" and \"-stop\", Tomcat will be started");
    }


    static String options1[]= { "help", "stop", "sandbox", "security",  "enableAdmin" };
    static Hashtable optionAliases=new Hashtable();
    static Hashtable optionDescription=new Hashtable();
    static {
	optionAliases.put("h", "home");
	optionAliases.put("i", "install");
	optionAliases.put("f", "config");
	optionAliases.put("security", "sandbox");
	optionAliases.put("?", "help");
    }

//     public String[] getOptions1() {
// 	return options1;
//     }
//     public Hashtable getOptionAliases() {
// 	return optionAliases;
//     }
	
    
    /** Process arguments - set object properties from the list of args.
     */
    public  boolean processArgs(String[] args) {
	setArgs(args);
	try {
	    return IntrospectionUtils.processArgs( this, args );
	    //, args,getOptions1(),
	    //			    null, getOptionAliases());
	} catch( Exception ex ) {
	    ex.printStackTrace();
	    return false;
	}
    }

    /** Callback from argument processing
     */
    public void setProperty(String s,Object v) {
	if ( dL > 0 ) debug( "Generic property " + s );
	attributes.put(s,v);
    }

    /** Called by Main to set non-string properties
     */
    public void setAttribute(String s,Object o) {
	if( optionAliases.get( s ) !=null )
	    s=(String)optionAliases.get( s );

	if ( "args".equals(s) ) {
	    String args[]=(String[])o;
	    boolean ok=processArgs( args );
	    if ( ! ok ) {
		printUsage();
		return;
	    }
	}

	attributes.put(s,o);
    }

    // -------------------- Main --------------------

    public static void main(String args[] ) {
	try {
	    Tomcat tomcat=new Tomcat();
	    tomcat.processArgs( args );
            tomcat.execute();
	} catch(Exception ex ) {
	    log.log(sm.getString("tomcat.fatal"), ex);
	    System.exit(1);
	}
    }

    private static int dL=0;
    private void debug( String s ) {
	System.out.println("Tomcat: " + s );
    }
}
