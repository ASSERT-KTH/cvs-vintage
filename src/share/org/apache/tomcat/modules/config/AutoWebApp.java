/*   
 *  Copyright 1999-2004 The Apache Sofware Foundation.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.tomcat.modules.config;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;

import org.apache.tomcat.core.BaseInterceptor;
import org.apache.tomcat.core.Context;
import org.apache.tomcat.core.ContextManager;
import org.apache.tomcat.core.TomcatException;

/**
 * Automatically add all the web applications from a directory.
 * You can use multiple AutoWebApp modules with different locations.
 * 
 * This module will not "deploy" wars or do any other configuration. It'll
 * just use all the sub-directories as web application bases, and use
 * a simple escaping scheme. 
 * 
 * Based on the original AutoSetup.
 * 
 * @author cmanolache@yahoo.com
 */
public class AutoWebApp extends BaseInterceptor {
    int debug=0;
    Hashtable hosts=new Hashtable();
    String appsD="webapps";
    String defaultHost=null; 
    boolean flat=true;
    boolean ignoreDot=true;
    String profile=null;
    boolean trusted=false;
    String prefix="";
    boolean reloadable=true;
    
    // encoding scheme - XXX review, customize, implement
    char hostSeparator='@'; // if support for vhost configuration is enabled
    // instead of one-dir-per-host, this char will separate the host part.
    char dotReplacement='_'; // use this in the host part to replace dots.
    char slashReplacement='_'; // use this in the path part to replace /
    
    public AutoWebApp() {
    }

    //-------------------- Config --------------------
    
    /** Use this directory for auto configuration. Default is
     *  TOMCAT_HOME/webapps.
     *  @param d A directory containing your applications.
     *    If it's not an absoulte path, TOMCAT_HOME will be used as base.
     */
    public void setDir( String d ) {
	appsD=d;
    }

    /** Add a prefix to all deployed context paths
     */
    public void setPrefix(String s ) {
	prefix=s;
    }
    
    /** All applications in the directory will be added to a
	single virtual host. If not set, an encoding scheme
	will be used to extract the virtual host name from
	the application name. For backward compatibilty you
	can set it to "DEFAULT". This is also usefull when you
	want each virtual host to have it's own directory.
    */
    public void setHost( String h ) {
	defaultHost=h;
    }

    /** Ignore directories starting with a "."
     */
    public void setIngoreDot( boolean b ) {
	ignoreDot=b;
    }
    

    /** Not implemented - default is true. If flat==false, virtual
	hosts will be configured using the hierarchy in webapps.
	( webapps/DEFAULT/, webapps/VHOST1, etc ).
    */
    public void setFlat( boolean b ) {
	flat=b;
    }

    /** Set the "profile" attribute on each context. This
	can be used by a profile module to configure the
	context with special settings.
    */
    public void setProfile( String s ) {
	profile=s;
    }

    /** Set the trusted attribute to all apps. This is
	used for "internal" apps, to reduce the number 
	of manual configurations. It works by creating
	a special directory and using <AutoWebApp> to
	add all the apps inside with a trusted attribute.
    */
    public void setTrusted( boolean b ) {
	trusted=b;
    }

    public void setReloadable( boolean b ) {
        reloadable=b;
    }

    public void setHostChar( String c ) {
        if ( c.length() > 0 ) {
            hostSeparator = c.charAt(0);
        } else {
            hostSeparator = '\0';
        }
    }

    public void setHostDotChar( String c ) {
        if ( c.length() > 0 ) {
            dotReplacement = c.charAt(0);
        } else {
            dotReplacement = '\0';
        }
    }

    public void setPathSlashChar( String c ) {
        if ( c.length() > 0 ) {
            slashReplacement = c.charAt(0);
        } else {
            slashReplacement = '\0';
        }
    }
    
    //-------------------- Implementation --------------------
    
    /** 
     */
    public void engineInit(ContextManager cm) throws TomcatException {
	// Make sure we know about all contexts added before.
	Enumeration loadedCtx=cm.getContexts();
	// loaded but not initialized - since we are still configuring
	// the server
	while( loadedCtx.hasMoreElements() ) {
	    Context ctx=(Context)loadedCtx.nextElement();
	    String host=ctx.getHost();
	    if(host==null) host="DEFAULT";
	    
	    Hashtable loaded=(Hashtable)hosts.get( host );
	    if( loaded==null ) {
		loaded=new Hashtable();
		hosts.put(host, loaded );
	    }
	    loaded.put( ctx.getPath(), ctx );
	}
	
        File webappD=new File(appsD);
        if( !webappD.isAbsolute() ) {
            webappD=new File( cm.getHome(), appsD);
        }
	
	if (! webappD.exists() || ! webappD.isDirectory()) {
	    log("No autoconf directory " + webappD );
	    return ; // nothing to set up
	}
	
	String[] list = webappD.list();

	if( flat ) {
	    for (int i = 0; i < list.length; i++) {
		String name = list[i];
		if( ignoreDot && name.startsWith( "." ))
		    continue;
		File f=new File( webappD, name );
		if( f.isDirectory() ) {
		    String appHost=defaultHost;
		    // Decode the host ( only if a host is not specified )
		    if( defaultHost==null ) {
			int idx=name.indexOf( hostSeparator ); // may change
			if( idx > 0 ) {
			    appHost=name.substring( 0, idx );
			    name=name.substring( idx + 1 );
			}
		    }
		    if( appHost == null )
			appHost="DEFAULT";

		    addWebApp( cm, f, appHost, name );
		}
	    }
	} else {
	    for (int i = 0; i < list.length; i++) {
		String name = list[i];
		File f=new File( webappD, name );
		if( f.isDirectory() ) {
		    if( ignoreDot && name.startsWith("." )) {
			continue;
		    } else
			addVHost( cm, webappD, name );
		}
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
	if( loaded != null && loaded.get( path ) != null ) {
	    log( "Loaded from config: " + host + ":" +
		 ( "".equals(path) ? "/" : path ) );
	    return; // already loaded
	}
	log("Auto-Adding " + host + ":" +
	    ( "".equals(path) ? "/" : path ) );

	if (dir.isDirectory()) {
	    Context ctx=cm.createContext();
	    ctx.setContextManager( cm );
	    ctx.setPath(prefix + path);
            ctx.setReloadable(reloadable);
	    if( ! "DEFAULT".equals( host ) )
		ctx.setHost( host );
	    try {
		ctx.setDocBase( dir.getCanonicalPath() );
	    } catch(IOException ex ) {
		ctx.setDocBase( dir.getAbsolutePath());
	    }

	    if( trusted ) 
		ctx.setTrusted( true );
	    if( profile!=null )
		ctx.setProperty( "profile", profile );
	    
	    if( debug > 0 )
		log("automatic add " + host + ":" + ctx.toString() + " " +
		    path);
	    cm.addContext(ctx);
	} else {
	    log( "Not a dir " + dir.getAbsolutePath());
	}
    }

   /** Add all the contexts for a virtual host
     */
   private void addVHost( ContextManager cm, File dir, String host )
       throws TomcatException
    {
        File webappD=new File( dir, host );
	
        String[] list = webappD.list();
	if( list.length==0 ) {
	    log("No contexts in " + webappD );
	}
	
	for (int i = 0; i < list.length; i++) {
	    String name = list[i];
	    File f=new File(webappD, name );
	    if( f.isDirectory() ) {
		addWebApp( cm, webappD,host,  name );
	    }
	}
    }

    // -------------------- Escaping --------------------
        
    private String unEscapeHost( String hostName ) {
	return unEscapeString( hostName, dotReplacement , '.' );
    }

    private String unEscapePath( String pathDir ) {
	return unEscapeString( pathDir, slashReplacement, '/' );
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

}
