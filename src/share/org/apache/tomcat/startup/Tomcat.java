package org.apache.tomcat.startup;

import java.beans.*;
import java.io.*;
import java.io.IOException;
import java.lang.reflect.*;
import java.util.Hashtable;
import java.util.*;
import java.net.*;
import org.apache.tomcat.util.*;
import org.apache.tomcat.helper.*;
import org.apache.tomcat.task.*;
import org.apache.tomcat.util.xml.*;
import org.apache.tomcat.core.*;
import org.apache.tomcat.util.log.*;
import org.xml.sax.*;

/**
 * Starter for Tomcat using XML.
 * Based on Ant.
 *
 * @author costin@dnt.ro
 */
public class Tomcat extends Log {

    private static StringManager sm =
	StringManager.getManager("org.apache.tomcat.resources");

    Tomcat() {
	super("tc_log");
    }

    public void execute(String args[] ) throws Exception {
	if( ! processArgs( args ) ) {
	    printUsage();
	    return;
	}
	
	if( doStop ) {
	    System.out.println(sm.getString("tomcat.stop"));
	    try {
		org.apache.tomcat.task.StopTomcat task=
		    new  org.apache.tomcat.task.StopTomcat();
		
		task.setConfig( configFile );
		task.execute();     
	    }
	    catch (TomcatException te) {
		if (te.getRootCause() instanceof java.net.ConnectException)
		    System.out.println(sm.getString("tomcat.connectexception"));
		else
		    throw te;
	    }
	    return;
	}

	StartTomcat st=new StartTomcat();
	if( doGenerate ) st.setGenerateConfigs( true );

	// load server.xml
	if( configFile!=null)
	    st.setConfig( configFile );
	
	st.execute();
    }
    
    public static void main(String args[] ) {
	try {
	    Tomcat tomcat=new Tomcat();
	    tomcat.execute( args );
	} catch(Exception ex ) {
	    System.out.println(sm.getString("tomcat.fatal"));
	    System.err.println(Logger.throwableToString(ex));
	    System.exit(1);
	}

    }

    // -------------------- Command-line args processing --------------------
    // null means user didn't set one
    String configFile=null;
    // relative to TOMCAT_HOME 
    static final String DEFAULT_CONFIG="conf/server.xml";

    boolean doStop=false;
    boolean doGenerate=false;
    
    public static void printUsage() {
	System.out.println(sm.getString("tomcat.usage"));
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
	    } else if (arg.equals("-g") || arg.equals("-generateConfigs")) {
		doGenerate=true;
	    } else if (arg.equals("-f") || arg.equals("-config")) {
		i++;
		if( i < args.length )
		    configFile = args[i];
	    } else if (arg.equals("-h") || arg.equals("-home")) {
		i++;
		if (i < args.length)
		    System.getProperties().put("tomcat.home", args[i]);
	    }
	}
	return true;
    }        

}
