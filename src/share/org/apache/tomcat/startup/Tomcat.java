package org.apache.tomcat.startup;

import java.beans.*;
import java.io.*;
import java.io.IOException;
import java.lang.reflect.*;
import java.util.Hashtable;
import java.util.*;
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

    void startTomcat() throws Exception {
	File f=new File(configFile);
	
	TagMap mapper=new TagMap( "org.apache.tomcat.core" );
	mapper.addMap( "httpAdapter", "org.apache.tomcat.service.http.HttpAdapter");
	mapper.addMap( "contextManager", "org.apache.tomcat.core.ContextManager");
	mapper.addMap( "tomcat", "org.apache.tomcat.startup.Tomcat");
	mapper.addMap( "mapperInterceptor", "org.apache.tomcat.request.MapperInterceptor");
	mapper.addMap( "contextMapperInterceptor", "org.apache.tomcat.request.ContextMapperInterceptor");
	mapper.addMap( "sessionInterceptor", "org.apache.tomcat.request.SessionInterceptor");
	mapper.addMap( "simpleMapper", "org.apache.tomcat.request.SimpleMapper");
	
	//	    mapper.addMap( "project", "org.apache.tools.tomcat.Project");

	Properties props=new Properties();
	ContextManager cm=(ContextManager)XmlHelper.readXml(f, props, mapper, null);
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

	    tomcat.startTomcat();

	} catch(Exception ex ) {
	    ex.printStackTrace();
	}

    }

    // -------------------- Command-line args processing --------------------
    String configFile="tomcat.xml";
    
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
		
	    } else if (arg.equals("-f")) {
		i++;
		if( i < args.length )
		    configFile = args[i]; 
	    }
	}
	return true;
    }        

}

/** Maps a tag name using a package prefix and a number of static mappings.
 *  That means <contextManager> will be mapped to
 *              org.apache.core.tomcat.ContextManager
 *  and all tags will use the given package, unless overriden with addMap
 */
class TagMap   extends Hashtable {
    String defaultPackage;
    Hashtable maps=new Hashtable();
    
    
    public TagMap(String defP) {
	defaultPackage=defP + ".";
    }

    public void addMap( String tname, String jname ) {
	maps.put( tname, jname );
    }

    public Object get( Object key ) {
	String tname=(String)key;
	String cName=(String)maps.get(tname);
	if(cName!=null) return InvocationHelper.getInstance( cName );
	// default
	cName= defaultPackage + InvocationHelper.capitalize( tname );
	return InvocationHelper.getInstance( cName); 
    }
}
