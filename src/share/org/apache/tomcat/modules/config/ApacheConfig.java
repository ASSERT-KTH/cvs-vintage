/* $Id: ApacheConfig.java,v 1.25 2001/08/16 05:22:41 larryi Exp $
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
import org.apache.tomcat.util.io.FileUtil;
import org.apache.tomcat.util.log.*;
import java.io.*;
import java.util.*;

/* The idea is to keep all configuration in server.xml and
   the normal apache config files. We don't want people to
   touch apache ( or IIS, NES ) config files unless they
   want to and know what they're doing ( better than we do :-).

   One nice feature ( if someone sends it ) would be to
   also edit httpd.conf to add the include.

   We'll generate a number of configuration files - this one
   is trying to generate a native apache config file.

   Some web.xml mappings do not "map" to server configuration - in
   this case we need to fallback to forward all requests to tomcat.

   Ajp14 will add to that the posibility to have tomcat and
   apache on different machines, and many other improvements -
   but this should also work for Ajp12, Ajp13 and Jni.

*/

/**
    Generates automatic apache mod_jk configurations based on
    the Tomcat server.xml settings and the war contexts
    initialized during startup.
    <p>
    This config interceptor is enabled by inserting an ApacheConfig
    element in the <b>&lt;ContextManager&gt;</b> tag body inside
    the server.xml file like so:
    <pre>
    * < ContextManager ... >
    *   ...
    *   <<b>ApacheConfig</b> <i>options</i> />
    *   ...
    * < /ContextManager >
    </pre>
    where <i>options</i> can include any of the following attributes:
    <ul>
     <li><b>configHome</b> - default parent directory for the following paths.
                            If not set, this defaults to TOMCAT_HOME. Ignored
                            whenever any of the following paths is absolute.
                             </li>
     <li><b>jkConfig</b> - path to use for writing Apache mod_jk conf file. If
                            not set, defaults to
                            "conf/auto/mod_jk.conf".</li>
     <li><b>workersConfig</b> - path to workers.properties file used by 
                            mod_jk. If not set, defaults to
                            "conf/jk/workers.properties".</li>
     <li><b>modJk</b> - path to Apache mod_jk plugin file.  If not set,
                        defaults to "modules/mod_jk.dll" on windows,
                        "modules/mod_jk.nlm" on netware, and
                        "libexec/mod_jk.so" everywhere else.</li>
     <li><b>jkLog</b> - path to log file to be used by mod_jk.</li>
     <li><b>jkDebug</b> - JK Loglevel setting.  May be debug, info, error, or emerg.
                          If not set, defaults to emerg.</li>
     <li><b>jkProtocol</b> The desired protocal, "ajp12" or "ajp13" or "inprocess". If not
                           specified, defaults to "ajp13" if an Ajp13Interceptor
                           is in use, otherwise it defaults to "ajp12".</li>
     <li><b>forwardAll</b> - If true, forward all requests to Tomcat. This helps
                             insure that all the behavior configured in the web.xml
                             file functions correctly.  If false, let Apache serve
                             static resources. The default is true.
                             Warning: When false, some configuration in
                             the web.xml may not be duplicated in Apache.
                             Review the mod_jk conf file to see what
                             configuration is actually being set in Apache.</li>
     <li><b>noRoot</b> - If true, the root context is not mapped to
                         Tomcat.  If false and forwardAll is true, all requests
                         to the root context are mapped to Tomcat. If false and
                         forwardAll is false, only JSP and servlets requests to
                         the root context are mapped to Tomcat. When false,
                         to correctly serve Tomcat's root context you must also
                         modify the DocumentRoot setting in Apache's httpd.conf
                         file to point to Tomcat's root context directory.
                         Otherwise some content, such as Apache's index.html,
                         will be served by Apache before mod_jk gets a chance
                         to claim the request and pass it to Tomcat.
                         The default is true.</li>
    </ul>
    <p>
    @author Costin Manolache
    @author Larry Isaacs
    @author Mel Martinez
	@version $Revision: 1.25 $ $Date: 2001/08/16 05:22:41 $
 */
public class ApacheConfig  extends BaseJkConfig { 
    
    /** default path to mod_jk .conf location */
    public static final String MOD_JK_CONFIG = "conf/auto/mod_jk.conf";
    /** default path to workers.properties file
	This should be also auto-generated from server.xml.
    */
    public static final String WORKERS_CONFIG = "conf/jk/workers.properties";
    /** default mod_jk log file location */
    public static final String JK_LOG_LOCATION = "logs/mod_jk.log";
    /** default location of mod_jk Apache plug-in. */
    public static final String MOD_JK;
    
    //set up some defaults based on OS type
    static{
        String os = System.getProperty("os.name").toLowerCase();
        if(os.indexOf("windows")>=0){
           MOD_JK = "modules/mod_jk.dll";
        }else if(os.indexOf("netware")>=0){
           MOD_JK = "modules/mod_jk.nlm";
        }else{
           MOD_JK = "libexec/mod_jk.so";
        }
    }
    
    private File jkConfig = null;
    private File modJk = null;

    // ssl settings 
    private boolean sslExtract=true;
    private String sslHttpsIndicator="HTTPS";
    private String sslSessionIndicator="SSL_SESSION_ID";
    private String sslCipherIndicator="SSL_CIPHER";
    private String sslCertsIndicator="SSL_CLIENT_CERT";

    Hashtable NamedVirtualHosts=null;
    
    public ApacheConfig() {
    }

    //-------------------- Properties --------------------

    /**
        set the path to the output file for the auto-generated
        mod_jk configuration file.  If this path is relative
        then it will be resolved absolutely against
        the getConfigHome() path.
        <p>
        @param <b>path</b> String path to a file
    */
    public void setJkConfig(String path){
	jkConfig= (path==null)?null:new File(path);
    }

    /**
        set the path to the mod_jk Apache Module
        @param <b>path</b> String path to a file
    */
    public void setModJk(String path){
        modJk=( path==null?null:new File(path));
    }

    /** By default mod_jk is configured to collect SSL information from
	the apache environment and send it to the Tomcat workers. The
	problem is that there are many SSL solutions for Apache and as
	a result the environment variable names may change.

	The following JK related SSL configureation
	can be used to customize mod_jk's SSL behaviour.

	Should mod_jk send SSL information to Tomact (default is On)
    */
    public void setExtractSSL( boolean sslMode ) {
	this.sslExtract=sslMode;
    }

    /** What is the indicator for SSL (default is HTTPS)
     */
    public void setHttpsIndicator( String s ) {
	sslHttpsIndicator=s;
    }

    /**What is the indicator for SSL session (default is SSL_SESSION_ID)
     */
    public void setSessionIndicator( String s ) {
	sslSessionIndicator=s;
    }
    
    /**What is the indicator for client SSL cipher suit (default is SSL_CIPHER)
     */
    public void setCipherIndicator( String s ) {
	sslCipherIndicator=s;
    }

    /** What is the indicator for the client SSL certificated(default
	is SSL_CLIENT_CERT
     */
    public void setCertsIndicator( String s ) {
	sslCertsIndicator=s;
    }

    // -------------------- Initialize/guess defaults --------------------

    /** Initialize defaults for properties that are not set
	explicitely
    */
    protected void initProperties(ContextManager cm) {
        super.initProperties(cm);

	jkConfig=FileUtil.getConfigFile( jkConfig, configHome, MOD_JK_CONFIG);
	workersConfig=FileUtil.getConfigFile( workersConfig, configHome,
				     WORKERS_CONFIG);
	if( modJk == null )
	    modJk=new File(MOD_JK);
	else
	    modJk=FileUtil.getConfigFile( modJk, configHome, MOD_JK );
	jkLog=FileUtil.getConfigFile( jkLog, configHome, JK_LOG_LOCATION);
    }

    // -------------------- Generate config --------------------
    
    /**
        executes the ApacheConfig interceptor. This method generates apache
        configuration files for use with  mod_jk.  If not
        already set, this method will setConfigHome() to the value returned
        from <i>cm.getHome()</i>.
        <p>
        @param <b>cm</b> a ContextManager object.
    */
    public void execute(ContextManager cm) throws TomcatException {
    	try {
	    initProperties(cm);
	    initProtocol(cm);

            NamedVirtualHosts = new Hashtable();  

	    StringBuffer sb=new StringBuffer();
    	    PrintWriter mod_jk = new PrintWriter(new FileWriter(jkConfig));
    	    log("Generating apache mod_jk config = "+jkConfig );

	    generateJkHead( mod_jk );

	    // XXX Make those options configurable in server.xml
	    generateSSLConfig( mod_jk );

    	    // Set up contexts
    	    // XXX deal with Virtual host configuration !!!!
    	    Enumeration  enum = cm.getContexts();
    	    while (enum.hasMoreElements()) {
                Context context = (Context)enum.nextElement();
		if( forwardAll )
		    generateStupidMappings( context, mod_jk );
		else
		    generateContextMappings( context, mod_jk );
    	    }

    	    mod_jk.close();        
    	} catch( Exception ex ) {
            Log loghelper = Log.getLog("tc_log", this);
    	    loghelper.log("Error generating automatic apache configuration",
			  ex);
    	}
    }//end execute()

    // -------------------- Config sections  --------------------

    /** Generate the loadModule and general options
     */
    private boolean generateJkHead(PrintWriter mod_jk)
	throws TomcatException
    {

	mod_jk.println("########## Auto generated on " +  new Date() +
		       "##########" );
	mod_jk.println();

	// Fail if mod_jk not found, let the user know the problem
	// instead of running into problems later.
	if( ! modJk.exists() ) {
	    log( "mod_jk location: " + modJk );
	    log( "Make sure it is installed corectly or " +
		 " set the config location" );
	    log( "Using <ApacheConfig modJk=\"PATH_TO_MOD_JK.SO_OR_DLL\" />" );
	    //throw new TomcatException( "mod_jk not found ");
	}
            
	// Verify the file exists !!
	mod_jk.println("<IfModule !mod_jk.c>");
	mod_jk.println("  LoadModule jk_module "+
		       modJk.toString().replace('\\','/'));
	mod_jk.println("</IfModule>");
	mod_jk.println();                

	
	// Fail if workers file not found, let the user know the problem
	// instead of running into problems later.
	if( ! workersConfig.exists() ) {
	    log( "Can't find workers.properties at " + workersConfig );
	    log( "Please install it in the default location or " +
		 " set the config location" );
	    log( "Using <ApacheConfig workersConfig=\"FULL_PATH\" />" );
	    throw new TomcatException( "workers.properties not found ");
	}
            
	mod_jk.println("JkWorkersFile \"" 
		       + workersConfig.toString().replace('\\', '/') 
		       + "\"");

	mod_jk.println("JkLogFile \"" 
		       + jkLog.toString().replace('\\', '/') 
		       + "\"");
	mod_jk.println();

	if( jkDebug != null ) {
	    mod_jk.println("JkLogLevel " + jkDebug);
	    mod_jk.println();
	}
	return true;
    }
    
    private void generateSSLConfig(PrintWriter mod_jk) {
	if( ! sslExtract ) {
	    mod_jk.println("JkExtractSSL Off");        
	}
	if( ! "HTTPS".equalsIgnoreCase( sslHttpsIndicator ) ) {
	    mod_jk.println("JkHTTPSIndicator " + sslHttpsIndicator);        
	}
	if( ! "SSL_SESSION_ID".equalsIgnoreCase( sslSessionIndicator )) {
	    mod_jk.println("JkSESSIONIndicator " + sslSessionIndicator);
	}
	if( ! "SSL_CIPHER".equalsIgnoreCase( sslCipherIndicator )) {
	    mod_jk.println("JkCIPHERIndicator " + sslCipherIndicator);
	}
	if( ! "SSL_CLIENT_CERT".equalsIgnoreCase( sslCertsIndicator )) {
	    mod_jk.println("JkCERTSIndicator " + sslCertsIndicator);
	}

	mod_jk.println();
    }

    // -------------------- Forward all mode --------------------
    String indent="";
    
    /** Forward all requests for a context to tomcat.
	The default.
     */
    private void generateStupidMappings(Context context,
					   PrintWriter mod_jk )
    {
	String ctxPath  = context.getPath();
	String vhost = context.getHost();
	String nPath=("".equals(ctxPath)) ? "/" : ctxPath;
	
        if( noRoot &&  "".equals(ctxPath) ) {
            log("Ignoring root context in forward-all mode  ");
            return;
        } 
	if( vhost != null ) {
            String vhostip = getVirtualHostAddress(vhost,
                                            context.getHostAddress());
	    generateNameVirtualHost(mod_jk, vhostip);
	    mod_jk.println("<VirtualHost "+ vhostip + ">");
	    mod_jk.println("    ServerName " + vhost );
	    Enumeration aliases=context.getHostAliases();
	    if( aliases.hasMoreElements() ) {
		mod_jk.print("    ServerAlias " );
		while( aliases.hasMoreElements() ) {
		    mod_jk.print( (String)aliases.nextElement() + " " );
		}
		mod_jk.println();
	    }
	    indent="    ";
	}
	mod_jk.println(indent + "JkMount " +  nPath + " " + jkProto );
	if( "".equals(ctxPath) ) {
	    mod_jk.println(indent + "JkMount " +  nPath + "* " + jkProto );
            mod_jk.println(indent +
                    "# Note: To correctly serve the Tomcat's root context, DocumentRoot must");
            mod_jk.println(indent +
                    "# must be set to: \"" + getApacheDocBase(context) + "\"");
	} else
	    mod_jk.println(indent + "JkMount " +  nPath + "/* " + jkProto );
	if( vhost != null ) {
	    mod_jk.println("</VirtualHost>");
            mod_jk.println();
	    indent="";
	}
    }    

    
    private void generateNameVirtualHost( PrintWriter mod_jk, String ip ) {
        if( !NamedVirtualHosts.containsKey(ip) ) {
            mod_jk.println("NameVirtualHost " + ip + "");
            NamedVirtualHosts.put(ip,ip);
        }
    }
    
    // -------------------- Apache serves static mode --------------------
    // This is not going to work for all apps. We fall back to stupid mode.
    
    private void generateContextMappings(Context context, PrintWriter mod_jk )
    {
	String ctxPath  = context.getPath();
	String vhost = context.getHost();

        if( noRoot &&  "".equals(ctxPath) ) {
            log("Ignoring root context in non-forward-all mode  ");
            return;
        } 
	mod_jk.println();
	mod_jk.println("#################### " +
		       ((vhost!=null ) ? vhost + ":" : "" ) +
		       (("".equals(ctxPath)) ? "/" : ctxPath ) +
		       " ####################" );
        mod_jk.println();
	if( vhost != null ) {
            String vhostip = getVirtualHostAddress(vhost,
                                            context.getHostAddress());
	    generateNameVirtualHost(mod_jk, vhostip);
	    mod_jk.println("<VirtualHost " + vhostip + ">");
	    mod_jk.println("    ServerName " + vhost );
	    Enumeration aliases=context.getHostAliases();
	    if( aliases.hasMoreElements() ) {
		mod_jk.print("    ServerAlias " );
		while( aliases.hasMoreElements() ) {
		    mod_jk.print( (String)aliases.nextElement() + " " );
		}
		mod_jk.println();
	    }
	    indent="    ";
	}
	// Dynamic /servet pages go to Tomcat
	
	generateStaticMappings( context, mod_jk );

	// InvokerInterceptor - it doesn't have a container,
	// but it's implemented using a special module.
	
	// XXX we need to better collect all mappings
	addMapping( ctxPath + "/servlet/*", mod_jk );
	    
	Enumeration servletMaps=context.getContainers();
	while( servletMaps.hasMoreElements() ) {
	    Container ct=(Container)servletMaps.nextElement();
	    addMapping( context, ct , mod_jk );
	}
	
	// There is a big problem with this one - it is
	// equivalent with JkMount path/*...
	// The good news - there is a container with exactly this
	// map ( the real path that is used by form auth ), so no need
	// for this one
	//mod_jk.println("JkMount " + path + "/*j_security_check " +
	//		   jkProto);
	//mod_jk.println();
	
	// XXX ErrorDocument
	// Security and filter mappings
	    
	if( vhost != null ) {
	    mod_jk.println("</VirtualHost>");
	    indent="";
	}
    }

    /** Add an Apache extension mapping.
     */
    protected boolean addExtensionMapping( String ctxPath, String ext,
					 PrintWriter mod_jk )
    {
        if( debug > 0 )
            log( "Adding extension map for " + ctxPath + "/*." + ext );
	mod_jk.println(indent + "JkMount " + ctxPath + "/*." + ext
		       + " " + jkProto);
	return true;
    }
    
    
    /** Add a fulling specified Appache mapping.
     */
    protected boolean addMapping( String fullPath, PrintWriter mod_jk ) {
        if( debug > 0 )
            log( "Adding map for " + fullPath );
	mod_jk.println(indent + "JkMount " + fullPath + "  " + jkProto );
	return true;
    }

    private void generateWelcomeFiles(Context context, PrintWriter mod_jk ) {
	String wf[]=context.getWelcomeFiles();
	if( wf==null || wf.length == 0 )
	    return;
	mod_jk.print(indent + "    DirectoryIndex ");
	for( int i=0; i<wf.length ; i++ ) {
	    mod_jk.print( wf[i] + " " );
	}
	mod_jk.println();
    }

    /** Mappings for static content. XXX need to add welcome files,
     *  mime mappings ( all will be handled by Mime and Static modules of
     *  apache ).
     */
    private void generateStaticMappings(Context context, PrintWriter mod_jk ) {
	String ctxPath  = context.getPath();

	// Calculate the absolute path of the document base
	String docBase = getApacheDocBase(context);

        if( !"".equals(ctxPath) ) {
            // Static files will be served by Apache
            mod_jk.println(indent + "# Static files ");		    
            mod_jk.println(indent + "Alias " + ctxPath + " \"" + docBase + "\"");
            mod_jk.println();
        } else {
            // For root context, ask user to update DocumentRoot setting.
            // Using "Alias / " interferes with the Alias for other contexts.
            mod_jk.println(indent +
                    "# To correctly serve the Tomcat's root context, DocumentRoot must");
            mod_jk.println(indent +
                    "# must be set to: \"" + docBase + "\"");
        }
	mod_jk.println(indent + "<Directory \"" + docBase + "\">");
	mod_jk.println(indent + "    Options Indexes FollowSymLinks");

	generateWelcomeFiles(context, mod_jk);

	// XXX XXX Here goes the Mime types and welcome files !!!!!!!!
	mod_jk.println(indent + "</Directory>");
	mod_jk.println();            
	

	// Deny serving any files from WEB-INF
	mod_jk.println();            
	mod_jk.println(indent +
		       "# Deny direct access to WEB-INF and META-INF");
	mod_jk.println(indent + "#");                        
	mod_jk.println(indent + "<Location \"" + ctxPath + "/WEB-INF/*\">");
	mod_jk.println(indent + "    AllowOverride None");
	mod_jk.println(indent + "    deny from all");
	mod_jk.println(indent + "</Location>");
	// Deny serving any files from META-INF
	mod_jk.println();            
	mod_jk.println(indent + "<Location \"" + ctxPath + "/META-INF/*\">");
	mod_jk.println(indent + "    AllowOverride None");
	mod_jk.println(indent + "    deny from all");
	mod_jk.println(indent + "</Location>");
	if (File.separatorChar == '\\') {
	    mod_jk.println(indent + "#");		    
	    mod_jk.println(indent +
			   "# Use Directory too. On Windows, Location doesn't"
			   + " work unless case matches");
	    mod_jk.println(indent + "#");                        
	    mod_jk.println(indent +
			   "<Directory \"" + docBase + "/WEB-INF/\">");
	    mod_jk.println(indent + "    AllowOverride None");
	    mod_jk.println(indent + "    deny from all");
	    mod_jk.println(indent + "</Directory>");
	    mod_jk.println();
	    mod_jk.println(indent +
			   "<Directory \"" + docBase + "/META-INF/\">");
	    mod_jk.println(indent + "    AllowOverride None");
	    mod_jk.println(indent + "    deny from all");
	    mod_jk.println(indent + "</Directory>");
	}
	mod_jk.println();
    }    

    // -------------------- Utils --------------------

    private String getApacheDocBase(Context context)
    {
	// Calculate the absolute path of the document base
	String docBase = getAbsoluteDocBase(context);
	if (File.separatorChar == '\\') {
	    // use separator preferred by Apache
	    docBase = docBase.replace('\\','/');
	}
        return docBase;
    }

    private String getVirtualHostAddress(String vhost, String vhostip) {
        if( vhostip == null ) {
            if ( vhost != null && vhost.length() > 0 && Character.isDigit(vhost.charAt(0)) )
                vhostip=vhost;
            else
                vhostip="*";
        }
        return vhostip;
    }

}
