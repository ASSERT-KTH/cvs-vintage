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


/**
 * Starter for Tomcat using XML.
 * Based on Ant.
 *
 * @author costin@dnt.ro
 */
public class Tomcat {

    Tomcat() {
    }

    // Set the mappings
    void setHelper( XmlHelper xmlHelper ) {
	xmlHelper.addMap( "module", "org.apache.tomcat.service.startup.Module", true);
	xmlHelper.addMap( "contextManager", "org.apache.tomcat.core.ContextManager");
	xmlHelper.addMap( "context", "org.apache.tomcat.core.Context");
	xmlHelper.addMap( "adapter" , "httpAdapter", "org.apache.tomcat.service.http.HttpAdapter");
	xmlHelper.addMap( "requestInterceptor" , "mapper", "org.apache.tomcat.request.MapperInterceptor");
	xmlHelper.addMap( "requestInterceptor", "contextMapper", "org.apache.tomcat.request.ContextMapperInterceptor");
	xmlHelper.addMap( "requestInterceptor", "session", "org.apache.tomcat.request.SessionInterceptor");
	xmlHelper.addMap( "requestInterceptor", "simpleMapper", "org.apache.tomcat.request.SimpleMapper");
    }
    
    void startTomcat() throws Exception {
	File f=new File(configFile);

	XmlHelper xmlHelper=new XmlHelper();
	setHelper( xmlHelper );
	
	ContextManager cm=(ContextManager)xmlHelper.readXml(f);
	
	cm.start();
	System.out.println("Done with  " + cm);
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
    
    void startTomcatOld() throws Exception {
	File f=new File(configFile);

	XmlHelper xmlHelper=new XmlHelper();
	xmlHelper.setDebug( 0 );
	setHelperOld( xmlHelper );
	
	org.apache.tomcat.server.HttpServer cm=(org.apache.tomcat.server.HttpServer)xmlHelper.readXml(f);

	// XXX use invocation to do start!
	cm.start();
	System.out.println("Done with  " + cm);
    }

    
    public static void main(String args[] ) {
	try {
	    Tomcat tomcat=new Tomcat();
	    
	    if( ! tomcat.processArgs( args ) ) {
		System.out.println("Wrong arguments");
		printUsage();
		return;
	    }

	    if( tomcat.stopPort != null ) {
		tomcat.stopTomcat();
		return;
	    }
	    
	    if( ! "server.xml".equals(tomcat.configFile) )
		tomcat.startTomcat();
	    else
		tomcat.startTomcatOld();

	    

	} catch(Exception ex ) {
	    ex.printStackTrace();
	}

    }

    void stopTomcat( ) {
	// use Ajp12 to stop the server...
	int portInt=8007;
	try {
	    portInt = Integer.valueOf(stopPort).intValue();
	} catch (NumberFormatException nfe) {
	}
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
    String configFile="server.xml";
    String stopPort=null;
    
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
		i++;
		if( i < args.length )
		    stopPort = args[i]; 
	    } else if (arg.equals("-f")) {
		i++;
		if( i < args.length )
		    configFile = args[i]; 
	    }
	}
	return true;
    }        

}

