package org.apache.tomcat.startup;

import java.beans.*;
import java.io.*;
import java.io.IOException;
import java.lang.reflect.*;
import java.util.Hashtable;
import java.util.*;
import java.net.*;
import org.apache.tomcat.util.*;
import org.apache.tomcat.util.xml.*;
import org.apache.tomcat.core.*;

// Used to stop tomcat
import org.apache.tomcat.service.TcpEndpointConnector;
import org.apache.tomcat.service.connector.Ajp12ConnectionHandler;

/**
 * Starter for Tomcat using XML.
 * Based on Ant.
 *
 * @author costin@dnt.ro
 */
public class Tomcat {

    static {
	// XXX temp fix for wars
	// Register our protocols XXX
	String warPackage = "org.apache.tomcat.protocol";
	String protocolKey = "java.protocol.handler.pkgs";
	String protocolHandlers = System.getProperties().getProperty(protocolKey);
	System.getProperties().put(protocolKey,
				   (protocolHandlers == null) ?
				   warPackage : protocolHandlers + "|" + warPackage);
    };

    Tomcat() {
    }

    // Set the mappings
    void setHelper( XmlMapper xh ) {
 	// xh.addRule( "ContextManager", xh.objectCreate("org.apache.tomcat.core.ContextManager") );
	xh.addRule( "ContextManager", xh.setProperties() );
	//	xh.addRule( "ContextManager", xh.setParent( "setServer" ) );
	//	xh.addRule( "ContextManager", xh.addChild( "setContextManager", null) );

	xh.addRule( "ContextManager/ContextInterceptor", xh.objectCreate(null, "className"));
	xh.addRule( "ContextManager/ContextInterceptor", xh.setProperties() );
	xh.addRule( "ContextManager/ContextInterceptor", xh.setParent("setContextManager") );
	xh.addRule( "ContextManager/ContextInterceptor", xh.addChild( "addContextInterceptor",
								      "org.apache.tomcat.core.ContextInterceptor" ) );
	
	xh.addRule( "ContextManager/RequestInterceptor", xh.objectCreate(null, "className"));
	xh.addRule( "ContextManager/RequestInterceptor", xh.setProperties() );
	xh.addRule( "ContextManager/RequestInterceptor", xh.setParent("setContextManager") );
	xh.addRule( "ContextManager/RequestInterceptor", xh.addChild( "addRequestInterceptor",
								      "org.apache.tomcat.core.RequestInterceptor" ) );

 	xh.addRule( "ContextManager/Context", xh.objectCreate("org.apache.tomcat.core.Context"));
	xh.addRule( "ContextManager/Context", xh.setParent( "setContextManager") );
	xh.addRule( "ContextManager/Context", xh.setProperties() );
	xh.addRule( "ContextManager/Context", xh.addChild( "addContext", null ) );

    }

    void setConnectorHelper( XmlMapper xh ) {

	xh.addRule( "ContextManager/Connector", xh.objectCreate(null, "className"));
	xh.addRule( "ContextManager/Connector", xh.setParent( "setContextManager") );
	xh.addRule( "ContextManager/Connector", xh.addChild( "addServerConnector", "org.apache.tomcat.core.ServerConnector") );

	xh.addRule( "ContextManager/Connector/Parameter", xh.methodSetter("setProperty",2) );
	xh.addRule( "ContextManager/Connector/Parameter", xh.methodParam(0, "name") );
	xh.addRule( "ContextManager/Connector/Parameter", xh.methodParam(1, "value") );
    }

    	
    void setLogHelper( XmlMapper xh ) {
	xh.addRule("Server/Logger",
		   xh.objectCreate("org.apache.tomcat.logging.TomcatLogger"));
	xh.addRule("Server/Logger", xh.setProperties());
	xh.addRule("Server/Logger", 
		   xh.addChild("addLogger", "org.apache.tomcat.logging.Logger") );
    }
    

    /**
     * Return the configuration file we are processing.  If the
     * <code>-config filename</code> command line argument is not
     * used, the default configuration filename will be loaded from
     * the TOMCAT_HOME directory.
     *
     * @param cm The ContextManager we are configuring
     **/
    File getConfigFile(ContextManager cm) {

	// If configFile is already set, use it
	if (configFile != null)
	    return (new File(configFile));

	// Use the "tomcat.home" property to resolve the default filename
	String tchome = System.getProperty("tomcat.home");
	if (tchome == null) {
	    System.out.println("No tomcat.home property, you need to set TOMCAT_HOME or add -Dtomcat.home");
	    tchome = ".";	// Assume current working directory
	}
	cm.setHome(tchome);
	return (new File(tchome, DEFAULT_CONFIG));

    }


    public void execute(String args[] ) throws Exception {
	if( ! processArgs( args ) ) {
	    System.out.println("Wrong arguments");
	    printUsage();
	    return;
	}

	if( doStop ) {
	    System.out.println("Stop tomcat");
	    stopTomcat(); // stop serving
	    return;
	}

	XmlMapper xh=new XmlMapper();
	xh.setDebug( 0 );
	ContextManager cm=new ContextManager();
	setHelper( xh );
	setConnectorHelper( xh );
	setLogHelper( xh );

	File f = getConfigFile(cm);
	try {
	    xh.readXml(f,cm);
	} catch( Exception ex ) {
	    System.out.println("FATAL: configuration error" );
	    ex.printStackTrace();
	    System.exit(1);
	}

	// Generate Apache configs
	//
	org.apache.tomcat.task.ApacheConfig apacheConfig=new  org.apache.tomcat.task.ApacheConfig();
	apacheConfig.execute( cm );     

	System.out.println("Starting tomcat. Check logs/tomcat.log for error messages ");
	cm.init(); // set up contexts
	cm.start(); // start serving
    }
    
    public static void main(String args[] ) {
	try {
	    Tomcat tomcat=new Tomcat();
	    tomcat.execute( args );
	} catch(Exception ex ) {
	    System.out.println("FATAL: " + ex );
	    ex.printStackTrace();
	}

    }

    /** Stop tomcat using the configured cm
     *  The manager is set up using the same configuration file, so
     *  it will have the same port as the original instance ( no need
     *  for a "log" file).
     *  It uses the Ajp12 connector, which has a built-in "stop" method,
     *  that will change when we add real callbacks ( it's equivalent
     *  with the previous RMI method from almost all points of view )
     */
    void stopTomcat() {
	XmlMapper xh=new XmlMapper();
	xh.setDebug( 0 );
	ContextManager cm=new ContextManager();
	setConnectorHelper( xh );

	// read only connector information out of server.xml
	File f = getConfigFile(cm);
	try {
	    xh.readXml(f,cm);
	} catch( Exception ex ) {
	    System.out.println("FATAL: configuration error" );
	    ex.printStackTrace();
	    System.exit(1);
	}

	// Find Ajp12 connector
	int portInt=8007;
	Enumeration enum=cm.getConnectors();
	while( enum.hasMoreElements() ) {
	    Object con=enum.nextElement();
	    if( con instanceof  TcpEndpointConnector ) {
		TcpEndpointConnector tcpCon=(TcpEndpointConnector) con;
		if( tcpCon.getTcpConnectionHandler()  instanceof Ajp12ConnectionHandler ) {
		    portInt=tcpCon.getPort();
		}
	    }
	}

	// use Ajp12 to stop the server...
	try {
	    Socket socket = new Socket("localhost", portInt);
	    OutputStream os=socket.getOutputStream();
	    byte stopMessage[]=new byte[2];
	    stopMessage[0]=(byte)254;
	    stopMessage[1]=(byte)15;
	    os.write( stopMessage );
	    socket.close();
	} catch(Exception ex ) {
	    ex.printStackTrace();
	}
    }
    
    // -------------------- Command-line args processing --------------------
    // null means user didn't set one
    String configFile=null;
    // relative to TOMCAT_HOME 
    static final String DEFAULT_CONFIG="conf/server.xml";
    boolean doStop=false;
    
    public static void printUsage() {
	System.out.println("usage: ");
    }

    /** Process arguments - set object properties from the list of args.
     */
    public  boolean processArgs(String[] args) {
	for (int i = 0; i < args.length; i++) {
	    String arg = args[i];
            
	    if (arg.equals("-help") || arg.equals("help")) {
		printUsage();
		return false;
		
	    } else if (arg.equals("-stop")) {
		doStop=true;
	    } else if (arg.equals("-f") || arg.equals("-config")) {
		i++;
		if( i < args.length )
		    configFile = args[i]; 
	    }
	}
	return true;
    }        

}

