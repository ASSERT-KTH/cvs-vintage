package org.apache.tomcat.j2ee;

import java.net.*;
import java.io.*;
import java.util.*;

import com.sun.web.server.*;
import org.apache.tomcat.core.*;
import org.apache.tomcat.request.*;
import org.apache.tomcat.context.*;
import java.security.*;
import javax.servlet.*;
import org.apache.tomcat.startup.EmbededTomcat;
/**
 * Simple adapter EmbededTomcat, workaround to avoid
 * using com.sun.web.servet.WebService inside tomcat.
 * 
 * @author costin@eng.sun.com
 */
public class TomcatJ2EEAdapter extends WebService {
    EmbededTomcat server;
    ContextManager cm;
    int debug=0;

    public TomcatJ2EEAdapter() {
	server=new EmbededTomcat();
	cm=server.getContextManager();
    }

    public void setDebug( int debug ) {
	super.setDebug(debug);
	server.setDebug(debug);
	this.debug=debug;
    }

    public void setApplication( Object app ) {
	super.setApplication( app );
	server.setApplication( app );
    }
    
    public void addApplicationAdapter( Object adapter ) {
	try {
	    server.addApplicationAdapter( adapter );
	} catch(TomcatException ex ) {
	    ex.printStackTrace();
	}
    }
    
    public void setWorkDir( String dir ) {
	server.setWorkDir( dir );
    }
    
    public void addEndpoint( int port, InetAddress addr ,
			     String hostname)
    {
	try {
	    server.addEndpoint( port, addr, hostname);
	} catch(TomcatException ex ) {
	    ex.printStackTrace();
	}

    }
    public  void addSecureEndpoint( int port, InetAddress addr,
				    String hostname, String keyFile,
				    String keyPass ) {
	try {
	    server.addSecureEndpoint( port, addr, hostname, keyFile, keyPass);
	} catch(TomcatException ex ) {
	    ex.printStackTrace();
	}

    }

    Hashtable contexts=new Hashtable();
    
    public  ServletContext addContext( String ctxPath, URL docRoot ) {
	try {
	    Context ctx=(Context)server.addContext( ctxPath, docRoot);
	    contexts.put( ctx.getFacade(), ctx );
	    return (ServletContext)ctx.getFacade();
	} catch(TomcatException ex ) {
	    ex.printStackTrace();
	}
	return null;
    }

    public  void initContext( ServletContext sctx ) {
	Context ctx=(Context)contexts.get( sctx );

	Vector cp=(Vector)extraClassPaths.get( sctx );
	if( cp!=null ) {
	    for( int i=0; i<cp.size(); i++ ) {
		String cpath=(String)cp.elementAt(i);
		File f=new File( cpath );
		String absPath=f.getAbsolutePath();
		if( ! absPath.endsWith("/" ) && f.isDirectory() ) {
		    absPath+="/";
		}
		try {
		    ctx.addClassPath( new URL( "file", null,
					       absPath ));
		} catch( MalformedURLException ex ) {
		}
	    }
	}
	try {
	    ctx.init();
	} catch( TomcatException ex ) {
	    ex.printStackTrace();
	}
    }

    public  void destroyContext( ServletContext sctx ) {
	Context ctx=(Context)contexts.get( sctx );
	try {
	    ctx.shutdown();
	} catch( TomcatException ex ) {
	    ex.printStackTrace();
	}
    }

    public  ServletContext getServletContext( String host,
					      String cpath ) {
	Context ctx=(Context)server.getServletContext(host, cpath);
	return (ServletContext)ctx.getFacade();
    }
    
    public  void removeContext( ServletContext sctx ) {
	Context ctx=(Context)contexts.get( sctx );
	if(debug>0) cm.log( "remove context " + ctx );
	try {
	    server.getContextManager().removeContext( ctx );
	} catch( Exception ex ) {
	    cm.log("exception removing context " + sctx, ex);
	}
    }

    Hashtable extraClassPaths=new Hashtable();

    public  void addClassPath( ServletContext sctx, String cpath ) {
	Context ctx=(Context)contexts.get( sctx );
	if(debug>0) cm.log( "addClassPath " + ctx + " " +
			 cpath );
	try {
	    Vector cp=(Vector)extraClassPaths.get(ctx);
	    if( cp == null ) {
		cp=new Vector();
		extraClassPaths.put( ctx, cp );
	    }
	    cp.addElement( cpath );
	} catch( Exception ex ) {
	    cm.log("exception adding classpath " + cpath +
		" to context " + ctx, ex);
	}
    }

    public  void start() {
	try {
	    server.getContextManager().start();
	} catch( Exception ex ) {
	    cm.log("Error starting EmbededTomcat", ex);
	}
	if(debug>0) cm.log( "Started" );
    }

    public  void stop() {
	try {
	    cm.shutdown();
	} catch( Exception ex ) {
	    cm.log("Error starting EmbededTomcat", ex);
	}
	
    }
}






