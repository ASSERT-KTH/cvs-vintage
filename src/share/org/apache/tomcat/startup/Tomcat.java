package org.apache.tomcat.startup;

import java.beans.*;
import java.io.*;
import java.io.IOException;
import java.lang.reflect.*;
import java.util.Hashtable;
import java.util.*;
import java.net.*;
import org.apache.tomcat.util.*;
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
    void setHelper( XmlHelper xmlHelper ) {
	xmlHelper.addMap( "module", "org.apache.tomcat.service.startup.Module", true);
	xmlHelper.addMap( "contextManager", "org.apache.tomcat.core.ContextManager");
	xmlHelper.addMap( "context", "org.apache.tomcat.core.Context");
	xmlHelper.addMap( "adapter" , "httpAdapter", "org.apache.tomcat.service.http.HttpAdapter");
	xmlHelper.addMap( "adapter" , "ajp12Adapter", "org.apache.tomcat.service.connector.Ajp12Adapter");
	xmlHelper.addMap( "requestInterceptor" , "mapper", "org.apache.tomcat.request.MapperInterceptor");
	xmlHelper.addMap( "requestInterceptor", "contextMapper", "org.apache.tomcat.request.ContextMapperInterceptor");
	xmlHelper.addMap( "requestInterceptor", "session", "org.apache.tomcat.request.SessionInterceptor");
	xmlHelper.addMap( "requestInterceptor", "simpleMapper", "org.apache.tomcat.request.SimpleMapper");
    }
    
    // Set the mappings
    void setHelperOld( XmlHelper xmlHelper ) {
	xmlHelper.addMap( "Server", "org.apache.tomcat.server.HttpServer");
	xmlHelper.addMap( "ContextManager", "org.apache.tomcat.core.ContextManager");
	xmlHelper.addMap( "Context", "org.apache.tomcat.core.Context");
	xmlHelper.addMap( "Connector", "org.apache.tomcat.core.Context");

	// special treatement - use name attribute as a property name in parent
	xmlHelper.addPropertyTag( "Parameter" );

	xmlHelper.addAttributeMap( "org.apache.tomcat.server.HttpServer",
				   "ContextManager",
				   "contextManager");
	xmlHelper.addAttributeMap( "org.apache.tomcat.core.Context",
				   "ContextManager",
				   "contextManager");
    }
    
    public void execute(String args[] ) throws Exception {
	if( ! processArgs( args ) ) {
	    System.out.println("Wrong arguments");
	    printUsage();
	    return;
	}

	File f=new File(configFile);

	XmlHelper xmlHelper=new XmlHelper();
	xmlHelper.setDebug( 0 );
	ContextManager cm=null;
	if( configFile.indexOf("server.xml") <0 ) {
	    // new config format
	    setHelper( xmlHelper );
	    cm=(ContextManager)xmlHelper.readXml(f);
	} else {
	    // old config format
	    setHelperOld( xmlHelper );
	    org.apache.tomcat.server.HttpServer server=(org.apache.tomcat.server.HttpServer)xmlHelper.readXml(f);
	    // XXX use invocation to do start!
	    cm=server.getContextManager();
	}

	if( doStop ) {
	    stopTomcat(cm);
	    return;
	}
	    
	cm.start();
    }
    
    public static void main(String args[] ) {
	try {
	    Tomcat tomcat=new Tomcat();
	    tomcat.execute( args );
	} catch(Exception ex ) {
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
    void stopTomcat( ContextManager cm ) {
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
    String configFile="conf/server.xml";
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

