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

package org.apache.tomcat.modules.config;

import org.apache.tomcat.core.*;
import org.apache.tomcat.util.*;
import java.io.*;
import java.net.*;
import java.util.*;

import org.apache.tomcat.util.xml.*;

/**
 *
 * @author cmanolache@yahoo.com
 */
public class WebAppsConfig extends BaseInterceptor {
    int debug=0;
    Hashtable hosts=new Hashtable();
    String hostsD="hosts";

    public WebAppsConfig() {
    }

    //-------------------- Config --------------------
    
    /** Use this directory for auto configuration
     */
    public void setDir( String d ) {
	hostsD=d;
    }

    /** Use this directory for auto configuration
     */
    public void setHostXmlDir( String d ) {
	hostsD=d;
    }

    //-------------------- Implementation --------------------

    /**
     */
    public void engineInit(ContextManager cm) throws TomcatException {
	Enumeration loadedCtx=cm.getContexts();
	while( loadedCtx.hasMoreElements() ) {
	    addExistingCtx( (Context)loadedCtx.nextElement());
	}

	String home=cm.getHome();
	File webappD=new File(hostsD);

	if( ! webappD.isAbsolute() )
	    webappD=new File(home + File.separator + hostsD);
	
	if (! webappD.exists() || ! webappD.isDirectory()) {
	    log("No autoconf directory " + webappD );
	    return ; // nothing to set up
	}
	
	String[] list = webappD.list();
	if( list.length==0 ) {
	    log("No hosts in " + webappD );
	}

	for (int i = 0; i < list.length; i++) {
	    String name = list[i];
	    if( name.endsWith(".xml") ) {
		configureVhost( cm, webappD, name );
	    }
	}

	for (int i = 0; i < list.length; i++) {
	    String name = list[i];
	    File f=new File( webappD, name );
	    if( f.isDirectory() ) {
		addApps( cm, webappD, name );
	    }
	}
    }

    private void addExistingCtx( Context ctx ) {
	String host=ctx.getHost();
	if(host==null) host="DEFAULT";
	
	Hashtable loaded=(Hashtable)hosts.get( host );
	if( loaded==null ) {
	    loaded=new Hashtable();
	    hosts.put(host, loaded );
	}
	loaded.put( ctx.getPath(), ctx );
    }
    
    /** Load a configuration file and set parameters for a virtual host
     */
    private void configureVhost( ContextManager cm, File dir, String name )
	throws TomcatException
    {
	XmlMapper xh=new XmlMapper();
	xh.setDebug( 0 );
	String host=name.substring(0, name.length()-4);// no extension
	host=unEscapeHost( host );
	if( host!=null) 
	    xh.setVariable( "current_host", host );
 	    
	ServerXmlInterceptor.ServerXmlHelper.setContextRules( xh );
	ServerXmlInterceptor.ServerXmlHelper.setLogRules( xh );

	File f=new File( dir, name );
	try {
	    xh.readXml(f,cm);
	} catch( Exception ex ) {
	    log( "Error reading virtual host config " + name );
	}

	xh.setVariable( "current_host", null );
    }

    private String unEscapeHost( String hostName ) {
	return unEscapeString( hostName, '_' , '.' );
    }

    private String unEscapePath( String pathDir ) {
	return unEscapeString( pathDir, '_', '/' );
    }

    /** Replace 'esc' with 'repl', and 'esc''esc' with 'esc'
     */
    private String unEscapeString( String s, char esc, char repl ) {
	StringBuffer sb=new StringBuffer();
	int len=s.length();
	for( int i=0; i< len; i++ ) {
	    char c=s.charAt( i );
	    if( c== esc ) {
		if( len > i + 1 && s.charAt( i+1 ) == esc ) {
		    // _ _
		    i++;
		    sb.append( esc );
		} else {
		    sb.append( repl );
		}
	    } else {
		sb.append( c );
	    }
	}
	return sb.toString();
    }

    
    /** Add all the contexts for a virtual host
     */
    private void addApps( ContextManager cm, File dir, String host )
	throws TomcatException
    {
	File webappD=new File( dir, host );
	
	String[] list = webappD.list();
	if( list.length==0 ) {
	    log("No contexts in " + webappD );
	}

	for (int i = 0; i < list.length; i++) {
	    String name = list[i];
	    if( name.endsWith(".war") ) {
		expandWar( webappD, name );
	    }
	}

	for (int i = 0; i < list.length; i++) {
	    String name = list[i];
	    File f=new File(webappD, name );
	    if( f.isDirectory() ) {
		addWebApp( cm, webappD,host,  name );
	    }
	}
	
    }

    /** Add one application
     */
    private void addWebApp( ContextManager cm, File dir, String host,
			    String name)
	throws TomcatException
    {
	host= unEscapeHost( host );
	if(host==null) host="DEFAULT";

	String path="/" + unEscapePath( name );
	if( path.equals("/ROOT") )
	    path="";

	Hashtable loaded=(Hashtable)hosts.get(host);
	if( loaded.get( path ) != null )
	    return; // already loaded

	File f=new File( dir, name);
	if (f.isDirectory()) {
	    Context ctx=new Context();
	    ctx.setContextManager( cm );
	    ctx.setPath(path);
	    if( ! "DEFAULT".equals( host ) )
		ctx.setHost( host );
	    ctx.setDocBase( f.getAbsolutePath() );

	    if( debug > 0 )
		log("automatic add " + host + ":" + ctx.toString() + " " +
		    path);
	    cm.addContext(ctx);
	}
    }

    /** Auto-expand wars
     */
    private void expandWar( File dir, String name ) {
	String fname=name.substring(0, name.length()-4);

	File appDir=new File( dir + fname);
	if( ! appDir.exists() ) {
	    // no check if war file is "newer" than directory 
	    // To update you need to "remove" the context first!!!
	    appDir.mkdirs();
	    // Expand war file
	    try {
		FileUtil.expand(dir.getAbsolutePath() + name,
				dir.getAbsolutePath() + fname );
	    } catch( IOException ex) {
		log("expanding webapp " + name, ex);
		// do what ?
	    }
	}
	// we will add the directory to the path
	name=fname;
    }


}

