/* ====================================================================
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

// Used to find Ajp1? connector port
import org.apache.tomcat.modules.server.Ajp12Interceptor;

/**
    Generates automatic apache mod_jserv configurations based on
    the Tomcat server.xml settings and the war contexts
    initialized during startup.
    <p>
    This config interceptor is enabled by inserting a JservConfig
    element in the <b>\<ContextManager></b> tag body inside
    the server.xml file like so:
    <pre>
    * < ContextManager ... >
    *   ...
    *   <<b>JservConfig</b> <i>options</i> />
    *   ...
    * < /ContextManager >
    </pre>
    where <i>options</i> can include any of the following attributes:
    <ul>
     <li><b>configHome</b> - default parent directory for the following paths.
                            If not set, this defaults to TOMCAT_HOME. Ignored
                            whenever any of the following paths is absolute.
                             </li>
     <li><b>jservConfig</b> - path to use for writing Apache mod_jserv conf file. If
                              not set, defaults to
                              "conf/auto/tomcat-apache.conf".</li>
     <li><b>modJServ</b> - path to Apache mod_jserv plugin file.  If not set,
                           defaults to "modules/ApacheModuleJserv.dll" on windows,
                           and "libexec/mod_jserv.so" everywhere else.</li>
     <li><b>jservLog</b> - path to log file to be used by mod_jserv.</li>
     <li><b>jservDebug</b> - Jserv Loglevel setting.  May be debug, info, notice,
                             warn, error, crit, alert, or emerg.
                             If not set, defaults to debug.</li>
     <li><b>forwardAll</b> - If true, forward all requests to Tomcat. This helps
                             insure that all the behavior configured in the web.xml
                             file functions correctly.  If false, let Apache serve
                             static resources. The default is true.
                             Warning: When false, some configuration in
                             the web.xml may not be duplicated in Apache.
                             Review the tomcat-apache conf file to see what
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
                         will be served by Apache before mod_jserv gets a chance
                         to claim the request and pass it to Tomcat.
                         The default is true.</li>
    </ul>
  <p>
    @author Costin Manolache
    @author Larry Isaacs
    @author Mel Martinez
        @version $Revision: 1.5 $ $Date: 2001/12/17 05:24:09 $
 */
public class JservConfig  extends BaseInterceptor { 
    
    /** default path to JServ .conf location */
    public static final String APACHE_CONFIG="conf/auto/tomcat-apache.conf";
    /** default mod_jserv log file location */
    public static final String JSERV_LOG_LOCATION = "logs/mod_jserv.log";
    /** default location of mod_jserv Apache plug-in. */
    public static  String MOD_JSERV;
    public static final String AJPV12="ajpv12";
    
    //set up some defaults based on OS type
    static{
        String os = System.getProperty("os.name").toLowerCase();
        if(os.indexOf("windows")>=0){
           MOD_JSERV = "modules/ApacheModuleJserv.dll";
        }else{
           MOD_JSERV = "libexec/mod_jserv.so";
        }
    }
    
    private File configHome = null;
    private File jservConfig = null;
    private File modJserv = null;
    private File jservLog = null;

    private String tomcatHome;

    private String jservDebug=null;
    private boolean noRoot=true;

    // default is true until we can map all web.xml directives
    // Or detect only portable directives were used.
    boolean forwardAll=true;

    Hashtable NamedVirtualHosts=null;
    
    public JservConfig() {
    }

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

    /** Special option - do not generate mappings for the ROOT
        context. The default is true, and will not generate the mappings,
        not redirecting all pages to tomcat (since /* matches everything).
        This means that Apache's root remains intact but isn't completely
        servlet/JSP enabled. If the ROOT webapp can be configured with
        apache serving static files, there's no problem setting this
        option to false. If not, then setting it true means Apache will
        be out of picture for all requests.
    */
    public void setNoRoot( boolean b ) {
        noRoot=b;
    }

    /**
        set a path to the parent directory of the
        conf folder.  That is, the parent directory
        within which setJkConfig() and other path
        setters would be resolved against
        if relative.  For example if ConfigHome is set to "/home/tomcat"
        and JservConfig is set to "conf/tomcat-apache.conf" then the resulting 
        path used would be: 
        "/home/tomcat/conf/tomcat-apache.conf".</p>
        <p>
        However, if JservConfig or other path
        is set to an absolute path, this attribute is ignored.
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
                "JservConfig.setConfigHome(): "+
                "Configuration Home must be a directory! : "+dir);
        }
        configHome = f;
    }
    
    /**
        @return the parent directory of the conf directory
            or null if not set.
    */
    public File getConfigHome(){
        return configHome;
    }
    
    /**
        sets a path pointing to the output file
        in which to write the mod_jserv configuration.
    */
    public void setJservConfig(String path){
        setJservConfig(path==null?null:new File(path));
    }
    
    /**
        sets a File object pointing to the output file
        in which to write the mod_jserv configuration.
    */
    public void setJservConfig(File path){
        jservConfig=path;
        
    }

    /**
        set the path to the Jserv Apache Module
        @param <b>path</b> String path to a file
    */
    public void setModJserv(String path){
        setModJserv(path==null?null:new File(path));
    }
    
    /**
        set the path to the Jserv Apache Module
        @param <b>path</b> File object
    */
    public void setModJserv(File path){
        modJserv=path;
    }

    /**
        set the path to the mod_jserv log file
        @param <b>path</b> String path to a file
    */
    public void setJservLog(String path){
        jservLog= ( path==null?null:new File(path));
    }

    /** Set the verbosity level for mod_jserv.
        ( use debug, error, etc. ) If not set, no log is written.
     */
    public void setJservDebug( String level ) {
        jservDebug=null;
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
        
        jservConfig=FileUtil.getConfigFile( jservConfig, configHome, APACHE_CONFIG);

        if( modJserv == null )
            modJserv=new File(MOD_JSERV);
        else
            modJserv=FileUtil.getConfigFile( modJserv, configHome, MOD_JSERV );
        jservLog=FileUtil.getConfigFile( jservLog, configHome, JSERV_LOG_LOCATION);
    }


    // -------------------- Generate config --------------------

    /**
        executes the JservConfig interceptor. This method generates apache
        configuration files for use with mod_jserv.  If not
        already set, this method will setConfigHome() to the value returned
        from <i>cm.getHome()</i>.
        <p>
        @param <b>cm</b> a ContextManager object.
    */
    public void execute(ContextManager cm) throws TomcatException {
        try {
            initProperties(cm);

            NamedVirtualHosts = new Hashtable();  

            PrintWriter pw=new PrintWriter(new FileWriter(jservConfig));
            log("Generating apache mod_jserv config = "+jservConfig );

            // generate header
            generateJservHead(pw,cm);

            Hashtable vhosts = new Hashtable();

    	    // Set up contexts
    	    // XXX deal with Virtual host configuration !!!!
    	    Enumeration  enum = cm.getContexts();
    	    while (enum.hasMoreElements()) {
                Context context = (Context)enum.nextElement();
                String host = context.getHost();
                if( host == null ) {
                    if( forwardAll )
                        generateStupidMappings( context, pw );
                    else
                        generateContextMappings( context, pw );
                } else {
                    Vector vhostContexts = (Vector)vhosts.get(host);
                    if ( vhostContexts == null ) {
                        vhostContexts = new Vector();
                        vhosts.put(host,vhostContexts);
                    }
                    vhostContexts.addElement(context);
                }
    	    }

            enum = vhosts.elements();
            while( enum.hasMoreElements() ) {
                Vector vhostContexts = (Vector)enum.nextElement();
                for( int i = 0; i < vhostContexts.size(); i++ ) {
                    Context context = (Context)vhostContexts.elementAt(i);
                    if( i == 0 )
                        generateVhostHead( context, pw );
                    if( forwardAll )
                        generateStupidMappings( context, pw );
                    else
                        generateContextMappings( context, pw );
                }
                generateVhostTail( pw );
            }

            pw.close();
        } catch( Exception ex ) {
            Log loghelper = Log.getLog("tc_log", this);
            loghelper.log("Error generating automatic apache mod_jserv configuration", ex);
        }
    }//end execute()

    // -------------------- Config sections  --------------------

    /** Generate the loadModule and general options
     */
    private boolean generateJservHead(PrintWriter pw, ContextManager cm)
        throws TomcatException
    {
        //insert LoadModule calls:
        pw.println("<IfModule !mod_jserv.c>");
        pw.println("  LoadModule jserv_module "+
                 modJserv.toString().replace('\\','/'));
        pw.println("</IfModule>");

        pw.println("ApJServManual on");
        pw.println("ApJServDefaultProtocol " + AJPV12);
        pw.println("ApJServSecretKey DISABLED");
        pw.println("ApJServMountCopy on");
        pw.println("ApJServLogLevel notice");
        pw.println();

        // Find Ajp12 connector
        int portInt=8007;
        BaseInterceptor ci[]=cm.getContainer().getInterceptors();
        for( int i=0; i<ci.length; i++ ) {
            Object con=ci[i];
            if( con instanceof  Ajp12Interceptor ) {
                Ajp12Interceptor tcpCon=(Ajp12Interceptor) con;
                portInt=tcpCon.getPort();
            }
        }
        pw.println("ApJServDefaultPort " + portInt);
        pw.println();
        return true;
    }

    private void generateVhostHead(Context context, PrintWriter pw) {
	String ctxPath  = context.getPath();
	String vhost = context.getHost();

        pw.println();
        String vhostip = getVirtualHostAddress(vhost,
                                            context.getHostAddress());
        generateNameVirtualHost(pw, vhostip);
        pw.println("<VirtualHost "+ vhostip + ">");
        pw.println("    ServerName " + vhost );
        Enumeration aliases=context.getHostAliases();
        if( aliases.hasMoreElements() ) {
            pw.print("    ServerAlias " );
            while( aliases.hasMoreElements() ) {
                pw.print( (String)aliases.nextElement() + " " );
            }
            pw.println();
        }
        indent="    ";
    }

    private void generateVhostTail(PrintWriter pw) {
        pw.println("</VirtualHost>");
        indent="";
    }

    // -------------------- Forward all mode --------------------
    String indent="";
    
    /** Forward all requests for a context to tomcat.
        The default.
     */
    private void generateStupidMappings(Context context,
                                           PrintWriter pw )
    {
        String ctxPath  = context.getPath();
        String vhost = context.getHost();
        String nPath=("".equals(ctxPath)) ? "/" : ctxPath;

        if( noRoot &&  "".equals(ctxPath) ) {
            log("Ignoring root context in forward-all mode  ");
            return;
        } 

        pw.println();
        pw.println(indent + "ApJServMount " +  nPath + " " + nPath );
        if( "".equals(ctxPath) ) {
            pw.println(indent + "ApJServMount " +  nPath + "* " + nPath );
            if ( vhost != null ) {
                pw.println(indent + "DocumentRoot \"" +
                            getApacheDocBase(context) + "\"");
            } else {
                pw.println(indent +
                        "# To avoid Apache serving root welcome files from htdocs, update DocumentRoot");
                pw.println(indent +
                        "# to point to: \"" + getApacheDocBase(context) + "\"");
            }

        } else
            pw.println(indent + "ApJServMount " +  nPath + "/* " + nPath );
    }    

    private void generateNameVirtualHost( PrintWriter pw, String ip ) {
        if( !NamedVirtualHosts.containsKey(ip) ) {
            pw.println("NameVirtualHost " + ip + "");
            NamedVirtualHosts.put(ip,ip);
        }
    }


    // -------------------- Apache serves static mode --------------------
    // This is not going to work for all apps. We fall back to stupid mode.
    
    private void generateContextMappings(Context context, PrintWriter pw )
    {
        String ctxPath  = context.getPath();
        String vhost = context.getHost();

        if( noRoot &&  "".equals(ctxPath) ) {
            log("Ignoring root context in non-forward-all mode  ");
            return;
        } 
        pw.println();
        pw.println("#################### " +
                       ((vhost!=null ) ? vhost + ":" : "" ) +
                       (("".equals(ctxPath)) ? "/" : ctxPath ) +
                       " ####################" );
        pw.println();
        // Dynamic /servet pages go to Tomcat
        
        generateStaticMappings( context, pw );

        // InvokerInterceptor - it doesn't have a container,
        // but it's implemented using a special module.
        
        // XXX we need to better collect all mappings
        addMapping( ctxPath + "/servlet/*", ctxPath, pw );
            
        Enumeration servletMaps=context.getContainers();
        while( servletMaps.hasMoreElements() ) {
            Container ct=(Container)servletMaps.nextElement();
            addMapping( context, ct , pw );
        }
        
        // There is a big problem with this one - it is
        // equivalent with JkMount path/*...
        // The good news - there is a container with exactly this
        // map ( the real path that is used by form auth ), so no need
        // for this one
        //mod_jk.println("JkMount " + path + "/*j_security_check " +
        //                   jkProto);
        //mod_jk.println();
        
        // XXX ErrorDocument
        // Security and filter mappings
            
    }

    // -------------------- Config Utils  --------------------

    protected boolean addMapping( Context ctx, Container ct,
                                PrintWriter pw )
    {
        int type=ct.getMapType();
        String ctPath=ct.getPath();
        String ctxPath=ctx.getPath();

        if( type==Container.EXTENSION_MAP ) {
            if( ctPath.length() < 3 ) return false;
            String ext=ctPath.substring( 2 );
            return addExtensionMapping( ctxPath, ext , pw );
        }
        String fullPath=null;
        if( ctPath.startsWith("/" ))
            fullPath=ctxPath+ ctPath;
        else
            fullPath=ctxPath + "/" + ctPath;
        return addMapping( fullPath, ctxPath, pw);
    }

    /** Add an Apache extension mapping.
     */
    protected boolean addExtensionMapping( String ctxPath, String ext,
                                         PrintWriter pw )
    {
        if( debug > 0 )
            log( "Adding extension map for " + ctxPath + "/*." + ext );
        pw.println(indent + "AddHandler jserv-servlet ." + ext);
        if ( "jsp".equals(ext) ) {
            pw.println(indent + "# Forward non-cookie session requests");
            pw.println(indent + "<LocationMatch \"" + ctxPath + "/.*;jsessionid=.*\">");
            pw.println(indent + "    SetHandler jserv-servlet");
            pw.println(indent + "</LocationMatch>");
        }
        return true;
    }
   
    /** Add a fulling specified Appache mapping.
     */
    protected boolean addMapping( String fullPath, String app, PrintWriter pw ) {
        if( debug > 0 )
            log( "Adding map for " + fullPath );
        pw.println(indent + "ApJServMount " + fullPath + "  " + app );
        return true;
    }


    private void generateWelcomeFiles(Context context, PrintWriter pw ) {
        String wf[]=context.getWelcomeFiles();
        if( wf==null || wf.length == 0 )
            return;
        pw.print(indent + "    DirectoryIndex ");
        for( int i=0; i<wf.length ; i++ ) {
            pw.print( wf[i] + " " );
        }
        pw.println();
    }

    /** Mappings for static content. XXX need to add welcome files,
     *  mime mappings ( all will be handled by Mime and Static modules of
     *  apache ).
     */
    private void generateStaticMappings(Context context, PrintWriter pw ) {
        String ctxPath  = context.getPath();

        // Calculate the absolute path of the document base
        String docBase = getApacheDocBase(context);

        if( !"".equals(ctxPath) ) {
            // Static files will be served by Apache
            pw.println(indent + "# Static files ");    
            pw.println(indent + "Alias " + ctxPath + " \"" + docBase + "\"");
            pw.println();
        } else {
            if ( context.getHost() != null ) {
                pw.println(indent + "DocumentRoot \"" +
                            getApacheDocBase(context) + "\"");
            } else {
                // For root context, ask user to update DocumentRoot setting.
                // Using "Alias / " interferes with the Alias for other contexts.
                pw.println(indent +
                        "# Be sure to update DocumentRoot");
                pw.println(indent +
                        "# to point to: \"" + docBase + "\"");
            }
        }
        pw.println(indent + "<Directory \"" + docBase + "\">");
        pw.println(indent + "    Options Indexes FollowSymLinks");

        generateWelcomeFiles(context, pw);

        // XXX XXX Here goes the Mime types and welcome files !!!!!!!!
        pw.println(indent + "</Directory>");
        pw.println();            
        

        // Deny serving any files from WEB-INF
        pw.println();            
        pw.println(indent +
                       "# Deny direct access to WEB-INF and META-INF");
        pw.println(indent + "#");                        
        pw.println(indent + "<Location \"" + ctxPath + "/WEB-INF/*\">");
        pw.println(indent + "    AllowOverride None");
        pw.println(indent + "    deny from all");
        pw.println(indent + "</Location>");
        // Deny serving any files from META-INF
        pw.println();            
        pw.println(indent + "<Location \"" + ctxPath + "/META-INF/*\">");
        pw.println(indent + "    AllowOverride None");
        pw.println(indent + "    deny from all");
        pw.println(indent + "</Location>");
        if (File.separatorChar == '\\') {
            pw.println(indent + "#");    
            pw.println(indent +
                           "# Use Directory too. On Windows, Location doesn't"
                           + " work unless case matches");
            pw.println(indent + "#");                        
            pw.println(indent +
                           "<Directory \"" + docBase + "/WEB-INF/\">");
            pw.println(indent + "    AllowOverride None");
            pw.println(indent + "    deny from all");
            pw.println(indent + "</Directory>");
            pw.println();
            pw.println(indent +
                           "<Directory \"" + docBase + "/META-INF/\">");
            pw.println(indent + "    AllowOverride None");
            pw.println(indent + "    deny from all");
            pw.println(indent + "</Directory>");
        }
        pw.println();
    }    

    // -------------------- Utils --------------------

    private String getAbsoluteDocBase(Context context)
    {
        // Calculate the absolute path of the document base
        String docBase = context.getDocBase();
        if (!FileUtil.isAbsolute(docBase)){
            docBase = tomcatHome + "/" + docBase;
        }
        docBase = FileUtil.patch(docBase);
        return docBase;
    }

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

}//end class JservConfig
