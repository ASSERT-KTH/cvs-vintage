/* $Id: ApacheConfig.java,v 1.15 2001/07/04 05:09:56 costin Exp $
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
import java.net.*;
import java.util.*;

// Used to find Ajp1? connector port
import org.apache.tomcat.modules.server.Ajp12Interceptor;
import org.apache.tomcat.modules.server.Ajp13Interceptor;

/**
    Generates automatic apache configurations based on
    the Tomcat server.xml settings and the war contexts
    initialized during startup.
    <p>
    This config interceptor is enabled by inserting an ApacheConfig
    element in the <b>\<ContextManager></b> tag body inside
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
     <li><b>confighome</b> - default parent directory for the following paths.
                            If not set, this defaults to TOMCAT_HOME. Ignored
                            whenever any of the following paths is absolute.
                             </li>
     <li><b>jkconfig</b> - path to write apacke mod_jk conf file to. If
                            not set, defaults to
                            "conf/jk/mod_jk.conf".</li>
     <li><b>workersconfig</b> - path to workers.properties file used by 
                            mod_jk. If not set, defaults to
                            "conf/jk/workers.properties".</li>
     <li><b>modjk</b> - path to Apache mod_jk plugin file.  If not set,
                        defaults to "modules/mod_jk.dll" on windows,
                        "modules/mod_jk.nlm" on netware, and
                        "libexec/mod_jk.so" everywhere else.</li>
     <li><b>jklog</b> - path to log file to be used by mod_jk.</li>                       
    </ul>
    <p>
    @author Costin Manolache
    @author Mel Martinez
	@version $Revision: 1.15 $ $Date: 2001/07/04 05:09:56 $
 */
public class ApacheConfig  extends BaseInterceptor { 
    
    /** default path to mod_jk .conf location */
    public static final String MOD_JK_CONFIG = "conf/auto/mod_jk.conf";
    /** default path to workers.properties file */
    public static final String WORKERS_CONFIG = "conf/auto/workers.properties";
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
    
    public static final String JTC_AJP13_INTERCEPTOR =
            "org.apache.ajp.tomcat33.Ajp13Interceptor";

    private File configHome = null;
    private File jkConfig = null;
    private File workersConfig = null;
    private File modJk = null;
    private File jkLog = null;

    private String jkProto = null;
    private int portInt=0;
    String tomcatHome;
    private boolean useJkMount=true;
    
    private String jkDebug=null;
    
    // default is true until we can map all web.xml directives
    // Or detect only portable directives were used.
    boolean forwardAll=true;
    
    public ApacheConfig() {
    }

    // -------------------- Tomcat callbacks --------------------
    // ApacheConfig should be able to react to dynamic config changes,
    // and regenerate the config.
    
    /** Generate the apache configuration - only when the server is
     *  completely initialized ( before starting )
     */
    public void engineState( ContextManager cm, int state )
    	throws TomcatException
    {
	if( state != ContextManager.STATE_INIT )
	    return;
	execute( cm );
    }

    public void contextInit(Context ctx)
	throws TomcatException
    {
	ContextManager cm=ctx.getContextManager();
    	if( cm.getState() >= ContextManager.STATE_INIT ) {
    	    // a context has been added after the server was started.
    	    // regenerate the config ( XXX send a restart signal to
    	    // the server )
    	    execute( cm );
    	}
    }

    //-------------------- Properties --------------------

    /** If false, we'll try to generate a config that will
     *  let apache serve static files.
     *  The default is true, forward all requests in a context
     *  to tomcat. 
     */
    public void setForwardAll( boolean b ) {
	forwardAll=b;
    }

    /** Use JkMount directives ( default ) or <Location>
	and SetHandler ( if false )
    */
    public void setUseJkMount( boolean b ) {
	useJkMount=b;
    }
    
    /**
        set a path to the parent directory of the
        conf folder.  That is, the parent directory
        within which setJservConfig(), setJkConfig()
        and setWorkerConfig() paths would be resolved against
        if relative.  For example if ConfigHome is set to "/home/tomcat"
        and JkConfig is set to "conf/mod_jk.conf" then the resulting 
        path returned from getJkConfig() would be: 
        "/home/tomcat/conf/mod_jk.conf".</p>
        <p>
        However, if JkConfig, JservConfig or WorkersConfig
        are set to absolute paths, this attribute is ignored.
        <p>
        If not set, execute() will set this to TOMCAT_HOME.
        <p>
        @param <b>dir</b> - path to a directory
    */
    public void setConfigHome(String dir){
	if( dir==null ) return;
        File f=new File(dir);
        if(!f.isDirectory()){
            throw new IllegalArgumentException(
                "ApacheConfig.setConfigHome(): "+
                "Configuration Home must be a directory! : "+dir);
        }
        configHome = f;
    }
    
    /**
        set the path to the output file for the auto-generated
        mod_jk configuration file.  If this path is relative
        then getJkConfig() will resolve it absolutely against
        the getConfigHome() path.
        <p>
        @param <b>path</b> String path to a file
    */
    public void setJkConfig(String path){
	jkConfig= (path==null)?null:new File(path);
    }

    /**
        set a path to the workers.properties file.
        @param <b>path</b> String path to workers.properties file
    */
    public void setWorkersConfig(String path){
        workersConfig= (path==null?null:new File(path));
    }
    
    /**
        set the path to the mod_jk Apache Module
        @param <b>path</b> String path to a file
    */
    public void setModJk(String path){
        modJk=( path==null?null:new File(path));
    }
   /**
        set the path to the mod_jk log file
        @param <b>path</b> String path to a file
    */
    public void setJkLog(String path){
        jkLog= ( path==null?null:new File(path));
    }
    
    /**
        set the Ajp protocal
        @param <b>protocal</b> String protocol, "ajp12" or "ajp13"
     */
    public void setJkProtocol(String protocol){
        jkProto = protocol;
    }


    /** Set the verbosity level for mod_jk.
	( use debug, error, etc )
     */
    public void setJkDebug( String level ) {
	jkDebug=level;
    }
    
    // -------------------- Initialize/guess defaults --------------------

    /** Initialize defaults for properties that are not set
	explicitely
    */
    public void initProperties(ContextManager cm) {
	tomcatHome = cm.getHome();
	File tomcatDir = new File(tomcatHome);
	if(configHome==null){
	    configHome=tomcatDir;
	}
	
	jkConfig=getConfigFile( jkConfig, configHome, MOD_JK_CONFIG);
	workersConfig=getConfigFile( workersConfig, configHome,
				     WORKERS_CONFIG);
	modJk=getConfigFile( modJk, configHome, MOD_JK );
	jkLog=getConfigFile( jkLog, configHome, JK_LOG_LOCATION);
    }

    private void initProtocol(ContextManager cm) {
	if( portInt == 0 )
	    portInt=8007;

	// Find Ajp1? connectors
	BaseInterceptor ci[]=cm.getContainer().getInterceptors();
	// try to get jakarta-tomcat-connectors Ajp13 Interceptor class
	Class jtcAjp13 = null;
	try {
	    jtcAjp13 = Class.forName(JTC_AJP13_INTERCEPTOR);
	} catch ( ClassNotFoundException e ) { }
	    
	for( int i=0; i<ci.length; i++ ) {
	    Object con=ci[i];
	    if( con instanceof  Ajp12Interceptor ) {
		Ajp12Interceptor tcpCon=(Ajp12Interceptor) con;
		portInt=tcpCon.getPort();
	    }
	    // if jkProtocol not specified and Ajp13 Interceptor found, use Ajp13
	    // ??? XXX
	    if( jkProto == null &&
		( con instanceof  Ajp13Interceptor ||
		  ( jtcAjp13 != null && jtcAjp13.isInstance(con) ) ) ) {
		jkProto = "ajp13";
	    }
	}

	// default to ajp12
	if( jkProto==null ) jkProto="ajp12";
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
	    
    	    
    	    PrintWriter mod_jk = new PrintWriter(new FileWriter(jkConfig));
    	    log("Generating apache mod_jk config = "+jkConfig );

	    generateJkHead( mod_jk );

	    // XXX Make those options configurable in server.xml
	    generateSSLConfig( mod_jk );


            // XXX
	    mod_jk.println("#");        
            mod_jk.println("# Root context mounts for Tomcat");
            mod_jk.println("#");        
            mod_jk.println("JkMount /*.jsp " + jkProto);
            mod_jk.println("JkMount /servlet/* " + jkProto);
            mod_jk.println();

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
    	    loghelper.log("Error generating automatic apache configuration", ex);
    	}
    }//end execute()

    // -------------------- Config sections  --------------------

    /** Generate the loadModule and general options
     */
    private void generateJkHead(PrintWriter mod_jk) {

	mod_jk.println("###################################################################");
	mod_jk.println("# Auto generated configuration. Dated: " +  new Date());
	mod_jk.println("###################################################################");
	mod_jk.println();
	
	mod_jk.println("#");
	mod_jk.println("# The following lines instruct Apache to load the jk module");
	mod_jk.println("# if it has not already been loaded.  This script assumes");
	mod_jk.println("# that the module is in the path below.  If you need to ");
	mod_jk.println("# deploy the module in another location, be sure to use a  ");
	mod_jk.println("# LoadModule statement prior to Include'ing this conf file.");
	mod_jk.println("# For example:");
	mod_jk.println("# ");
	mod_jk.println("#   LoadModule jk_module d:/mypath/modules/win32/mod_jk.dll");
	mod_jk.println("# or");
	mod_jk.println("#   LoadModule jk_module /mypath/modules/linux/mod_jk.so");
	mod_jk.println("#");
            
	// Verify the file exists !!
	mod_jk.println("<IfModule !mod_jk.c>");
	mod_jk.println("  LoadModule jk_module "+
		       modJk.toString().replace('\\','/'));
	mod_jk.println("</IfModule>");
	mod_jk.println();                
	mod_jk.println("JkWorkersFile \"" 
		       + workersConfig.toString().replace('\\', '/') 
		       + "\"");
	mod_jk.println("JkLogFile \"" 
		       + jkLog.toString().replace('\\', '/') 
		       + "\"");
	mod_jk.println();

	// XXX Make it configurable 
	if( jkDebug != null ) {
	    mod_jk.println("JkLogLevel " + jkDebug);
	    mod_jk.println();
	}

    }

    private void generateSSLConfig(PrintWriter mod_jk) {
	// XXX mod_jk should try few and detect automatically - it's not difficult 

	mod_jk.println("###################################################################");
	mod_jk.println("#                     SSL configuration                           #");
	mod_jk.println("# ");                
	mod_jk.println("# By default mod_jk is configured to collect SSL information from");
	mod_jk.println("# the apache environment and send it to the Tomcat workers. The");
	mod_jk.println("# problem is that there are many SSL solutions for Apache and as");
	mod_jk.println("# a result the environment variable names may change.");
	mod_jk.println("#");        
	mod_jk.println("# The following (commented out) JK related SSL configureation");        
	mod_jk.println("# can be used to customize mod_jk's SSL behaviour.");        
	mod_jk.println("# ");        
	mod_jk.println("# Should mod_jk send SSL information to Tomact (default is On)");        
	mod_jk.println("# JkExtractSSL Off");        
	mod_jk.println("# ");        
	mod_jk.println("# What is the indicator for SSL (default is HTTPS)");        
	mod_jk.println("# JkHTTPSIndicator HTTPS");        
	mod_jk.println("# ");        
	mod_jk.println("# What is the indicator for SSL session (default is SSL_SESSION_ID)");        
	mod_jk.println("# JkSESSIONIndicator SSL_SESSION_ID");        
	mod_jk.println("# ");        
	mod_jk.println("# What is the indicator for client SSL cipher suit (default is SSL_CIPHER)");        
	mod_jk.println("# JkCIPHERIndicator SSL_CIPHER");
	mod_jk.println("# ");        
	mod_jk.println("# What is the indicator for the client SSL certificated(default is SSL_CLIENT_CERT)");        
	mod_jk.println("# JkCERTSIndicator SSL_CLIENT_CERT");
	mod_jk.println("# ");        
	mod_jk.println("#                                                                 #");        
	mod_jk.println("###################################################################");
	mod_jk.println();
    }

    /** Forward all requests for a context to tomcat.
	The default.
     */
    private void generateStupidMappings(Context context, PrintWriter mod_jk ) {
	String path  = context.getPath();
	String vhost = context.getHost();
	
	if( vhost != null ) {
	    // Generate Apache VirtualHost section for this host
	    // You'll have to do it manually right now
	    return;
	}
	if( path.length() > 1) {
	    if( useJkMount ) {
		mod_jk.println("JkMount " +  path + " " + jkProto );
	    } else {
		mod_jk.println("<Location \"" + path + "\">");
		mod_jk.println("    SetHandler jakarta-servlet");
		mod_jk.println("</Location>");
	    }
	} else {
	    // the root context
	    // XXX If tomcat has a root context it should get all requests
	    // - which means apache will have absolutely nothing to do except
	    // forwarding requests.

	    // We should at least try to see if the root context has
	    // a mappable configuration and generate a smart mapping
	    
	}
    }    

    
    private void generateContextMappings(Context context, PrintWriter mod_jk )
    {
	String path  = context.getPath();
	String vhost = context.getHost();
	
	if( vhost != null ) {
	    // Generate Apache VirtualHost section for this host
	    // You'll have to do it manually right now
	    // XXX
	    return;
	}
	if( path.length() > 1) {
	    // Dynamic /servet pages go to Tomcat
	    
	    generateStaticMappings( context, mod_jk );
	    
	    Enumeration servletMaps=context.getContainers();
	    while( servletMaps.hasMoreElements() ) {
		Container ct=(Container)servletMaps.nextElement();
		addMapping( context, ct , mod_jk );
	    }

	    mod_jk.println("JkMount " + path + "/*j_security_check " +
			   jkProto);
	    mod_jk.println();

	    // XXX ErrorDocument
	    // Security and filter mappings
	    
	} else {
	    // the root context
	    // XXX use a non-conflicting name
	}
    }

    private void addMapping( Context ctx, Container ct, PrintWriter mod_jk ) {
	int type=ct.getMapType();
	String ctPath=ct.getPath();
	String ctxPath=ctx.getPath();
	String fullPath=null;
	if( ctxPath.equals("/") )
	    fullPath=ctPath;
	else if( ctPath.startsWith("/" ))
	    fullPath=ctxPath+ ctPath;
	else
	    fullPath=ctxPath + "/" + ctPath;
	log( "Adding map for " + fullPath );

	if( useJkMount ) {
	    mod_jk.println("JkMount " + fullPath + "  " + jkProto );
	} else {
	    mod_jk.println("<Location " + fullPath + " >");
	    mod_jk.println("    SetHandler jakarta-servlet ");
	    // XXX Other nice things like setting servlet and other attributes
	    mod_jk.println("</Location>");
	    mod_jk.println();
	}

	// XXX deal with security mappings
	// XXX better deal with extension mappings. 
    }

    /** Mappings for static content. XXX need to add welcome files,
     *  mime mappings ( all will be handled by Mime and Static modules of apache ).
     */
    private void generateStaticMappings(Context context, PrintWriter mod_jk ) {
	String path  = context.getPath();
	// Calculate the absolute path of the document base
	String docBase = context.getDocBase();
	if (!FileUtil.isAbsolute(docBase)){
	    docBase = tomcatHome + "/" + docBase;
	}
	docBase = FileUtil.patch(docBase);
	if (File.separatorChar == '\\')
	    docBase = docBase.replace('\\','/');// use separator preferred by Apache
	
	// Static files will be served by Apache
	mod_jk.println("#");		    
	mod_jk.println("# The following line allow apache to serve static files for " + path );
	mod_jk.println("#");                        
	mod_jk.println("Alias " + path + " \"" + docBase + "\"");
	mod_jk.println("<Directory \"" + docBase + "\">");
	mod_jk.println("    Options Indexes FollowSymLinks");

	// XXX XXX Here goes the Mime types and welcome files !!!!!!!!
	mod_jk.println("</Directory>");
	mod_jk.println();            
	

	// Deny serving any files from WEB-INF
	mod_jk.println();            
	mod_jk.println("# Deny direct access to WEB-INF and META-INF");
	mod_jk.println("#");                        
	mod_jk.println("<Location \"" + path + "/WEB-INF/\">");
	mod_jk.println("    AllowOverride None");
	mod_jk.println("    deny from all");
	mod_jk.println("</Location>");
	// Deny serving any files from META-INF
	mod_jk.println();            
	mod_jk.println("<Location \"" + path + "/META-INF/\">");
	mod_jk.println("    AllowOverride None");
	mod_jk.println("    deny from all");
	mod_jk.println("</Location>");
	if (File.separatorChar == '\\') {
	    mod_jk.println("#");		    
	    mod_jk.println("# Use Directory too. On Windows, Location doesn't work unless case matches");
	    mod_jk.println("#");                        
	    mod_jk.println("<Directory \"" + docBase + "/WEB-INF/\">");
	    mod_jk.println("    AllowOverride None");
	    mod_jk.println("    deny from all");
	    mod_jk.println("</Directory>");
	    mod_jk.println();
	    mod_jk.println("<Directory \"" + docBase + "/META-INF/\">");
	    mod_jk.println("    AllowOverride None");
	    mod_jk.println("    deny from all");
	    mod_jk.println("</Directory>");
	}
	mod_jk.println();
    }    

    // -------------------- Utils --------------------

    private File getConfigFile( File base, File configDir, String defaultF )
    {
	//log( "getConfigFile " + base + " " + configDir + " " +defaultF );
	if( base==null )
	    base=new File( defaultF );
	if( ! base.isAbsolute() ) {
	    if( configDir != null )
		base=new File( configDir, base.getPath());
	    else
		base=new File( base.getAbsolutePath()); //??
	}
	File parent=new File(base.getParent());
        if(!parent.exists()){
            if(!parent.mkdirs()){
                throw new RuntimeException(
                    "Unable to create path to config file :"+
		    jkConfig.getAbsolutePath());
            }
        }
	return base;
    }

}
