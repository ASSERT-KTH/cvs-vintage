package org.apache.tomcat.j2ee;

import java.net.URL;
import java.net.InetAddress;
import java.io.File;

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

    public TomcatJ2EEAdapter() {
	server=new EmbededTomcat();
    }

    public void setDebug( int debug ) {
	super.setDebug(debug);
	server.setDebug(debug);
    }

    public void setApplication( Object app ) {
	super.setApplication( app );
	server.setApplication( app );
    }
    
    public void addApplicationAdapter( Object adapter ) {
	server.addApplicationAdapter( adapter );
    }
    
    public void setWorkDir( String dir ) {
	server.setWorkDir( dir );
    }
    
    public void addEndpoint( int port, InetAddress addr ,
			     String hostname)
    {
	server.addEndpoint( port, addr, hostname);
    }
    public  void addSecureEndpoint( int port, InetAddress addr,
				    String hostname, String keyFile,
				    String keyPass ) {
	server.addSecureEndpoint( port, addr, hostname, keyFile, keyPass);
    }

    public  ServletContext addContext( String ctxPath, URL docRoot ) {
	return server.addContext( ctxPath, docRoot);
    }

    public  void initContext( ServletContext ctx ) {
	server.initContext( ctx );
    }

    public  void destroyContext( ServletContext ctx ) {
	server.destroyContext( ctx );
    }

    public  ServletContext getServletContext( String host,
					      String cpath ) {
	return server.getServletContext(host, cpath);
    }
    
    public  void removeContext( ServletContext ctx ) {
	server.removeContext( ctx);
    }

    public  void addClassPath( ServletContext ctx, String cpath ) {
	server.addClassPath( ctx, cpath);
    }

    public  void start() {
	server.start();
    }

    public  void stop() {
	server.stop();
    }
}






