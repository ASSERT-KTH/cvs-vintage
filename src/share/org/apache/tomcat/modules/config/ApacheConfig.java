/* $Id: ApacheConfig.java,v 1.10 2001/05/27 23:11:07 costin Exp $
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
     <li><b>jservconfig</b> - path to write apache jserv conf file to. If
                             not set, defaults to
                             "conf/jserv/tomcat-apache.conf".</li>
     <li><b>jkconfig</b> - path to write apacke mod_jk conf file to. If
                            not set, defaults to
                            "conf/jk/mod_jk.conf".</li>
     <li><b>workersconfig</b> - path to workers.properties file used by 
                            mod_jk. If not set, defaults to
                            "conf/jk/workers.properties".</li>
     <li><b>modjserv</b> - path to Apache JServ plugin module file. If not 
                           set, defaults to "modules/ApacheModuleJServ.dll"
                           on windows, "modules/Jserv.nlm" on netware, and 
                           "libexec/mod_jserv.so" everywhere else.</li>
     <li><b>modjk</b> - path to Apache mod_jk plugin file.  If not set,
                        defaults to "modules/mod_jk.dll" on windows,
                        "modules/mod_jk.nlm" on netware, and
                        "libexec/mod_jk.so" everywhere else.</li>
     <li><b>jklog</b> - path to log file to be used by mod_jk.</li>                       
    </ul>
    <p>
    @author Costin Manolache
    @author Mel Martinez
	@version $Revision: 1.10 $ $Date: 2001/05/27 23:11:07 $
 */
public class ApacheConfig  extends BaseInterceptor { 
    
    /** default path to JServ .conf location */
    public static final String APACHE_CONFIG="conf/jserv/tomcat-apache.conf";
    /** default path to mod_jk .conf location */
    public static final String MOD_JK_CONFIG = "conf/jk/mod_jk.conf";
    /** default path to workers.properties file */
    public static final String WORKERS_CONFIG = "conf/jk/workers.properties";
    /** default mod_jk log file location */
    public static final String JK_LOG_LOCATION = "logs/mod_jk.log";
    /** default location of mod_jserv Apache plug-in. */
    public static final String MOD_JSERV;
    /** default location of mod_jk Apache plug-in. */
    public static final String MOD_JK;
    
    //set up some defaults based on OS type
    static{
        String os = System.getProperty("os.name").toLowerCase();
        if(os.indexOf("windows")>=0){
           MOD_JSERV = "modules/ApacheModuleJserv.dll";
           MOD_JK = "modules/mod_jk.dll";
        }else if(os.indexOf("netware")>=0){
           MOD_JSERV = "modules/Jserv.nlm";
           MOD_JK = "modules/mod_jk.nlm";
        }else{
           MOD_JSERV = "libexec/mod_jserv.so";
           MOD_JK = "libexec/mod_jk.so";
        }
    }
    
    public static final String[] JkMount = { "ajp12", "ajp13" };
    public static final int AJP12 = 0;
    public static final int AJP13 = 1;
    public static final String AJPV12 = "ajpv12";


    private File configHome = null;
    private File jservConfig = null;
    private File jkConfig = null;
    private File workersConfig = null;
    private File modJserv = null;
    private File modJk = null;
    private File jkLog = null;


    
    public ApacheConfig() {
    }

    String findApache() {
	return null;
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
    
    /**
        this method helps the context's XMLMapper to work when
        setting properties.
    */
    public void setProperty(String name,String value){
        name = name.toLowerCase(); //case-insensitive
        if(name.equals("confighome")) setConfigHome(value);
        if(name.equals("jservconfig")) setJservConfig(value);
        if(name.equals("jkconfig")) setJkConfig(value);
        if(name.equals("workersconfig")) setWorkersConfig(value);
        if(name.equals("modjserv")) setModJserv(value);
        if(name.equals("modjk")) setModJk(value);
        if(name.equals("jklog")) setJkLog(value);
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
        setConfigHome(dir==null?null:new File(dir));
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
        @param <b>dir</b> - path to a directory
    */    
    public void setConfigHome(File dir){
        if(!dir.isDirectory()){
            throw new IllegalArgumentException(
                "ApacheConfig.setConfigHome(): "+
                "Configuration Home must be a directory! : "+dir);
        }
        configHome = dir;
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
        return a File object pointing to the output file
        in which to write the mod_jserv configuration.
        If the path set using setJservConfig() was absolute,
        then this simply returns that File object.
        If the path set using setJservConfig() was relative
        then this method will first try to resolve it
        absolutely against the path returned from getConfigHome().
        If getConfigHome()==null, then instead the path
        will be resolved absolutely against the current
        directory (System.getProperty("user.dir")).
        <p>
        @return a File object.
    */
    public File getJservConfig(){
        if(jservConfig==null){
            jservConfig = new File(APACHE_CONFIG);
        }
        File jservF = jservConfig;
        if(!jservF.isAbsolute()){
            if(getConfigHome()!=null){
                jservF = new File(
                    getConfigHome(),jservF.getPath());
            }else{ //resolve against user.dir (implicit)
                jservF = new File(jservF.getAbsolutePath());
            }
        }
        File parent = new File(jservF.getParent());
        if(!parent.exists()){
            if(!parent.mkdirs()){
                throw new RuntimeException(
                    "Unable to create path to config file :"+jservF);
            }
        }
        return jservF;
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
        setJkConfig(path==null?null:new File(path));
    }
    
    /**
        set the path to the output file for the auto-generated
        mod_jk configuration file.  If this path is relative
        then getJkConfig() will resolve it absolutely against
        the getConfigHome() path.
        <p>
        @param <b>path</b> File object
    */
    public void setJkConfig(File path){
        jkConfig = path;
    }
    
    /**
        return a File object pointing to the output file
        in which to write the mod_jk configuration.
        If the path set using setJkConfig() was absolute,
        then this simply returns that File object.
        If the path set using setJkConfig() was relative
        then this method will first try to resolve it
        absolutely against the path returned from getConfigHome().
        If getConfigHome()==null, then instead the path
        will be resolved absolutely against the current
        directory (System.getProperty("user.dir")).
        <p>
        @return a File object.
    */
    public File getJkConfig(){
        if(jkConfig==null){
            jkConfig = new File(MOD_JK_CONFIG+"-auto");
        }
        File jkF = jkConfig;
        if(!jkF.isAbsolute()){
            if(getConfigHome()!=null){
                jkF = new File(getConfigHome(),jkF.getPath());
            }else{//resolve against user.dir
                jkF = new File(jkF.getAbsolutePath());
            }
        }
        File parent = new File(jkF.getParent());
        if(!parent.exists()){
            if(!parent.mkdirs()){
                throw new RuntimeException(
                    "Unable to create path to config file :"+jkF.getAbsolutePath());
            }
        }
        return jkF;
    }
    
    /**
        set a path to the workers.properties file.
        @param <b>path</b> String path to workers.properties file
    */
    public void setWorkersConfig(String path){
        setWorkersConfig(path==null?null:new File(path));
    }
    
    /**
        set a path to the workers.properties file.
        @param <b>path</b> a File object pointing to the
            workers.properties file.
    */
    public void setWorkersConfig(File path){
        workersConfig = path;
    }
    
    /**
        returns the path to the workers.properties file to be used
        by mod_jk.  If the path set with setWorkersConfig was relative,
        this method will try first to resolve it absolutely against
        the return value of getConfigHome().  If that is null, then
        it instead will resolve against the current user.dir.
        <p>
        @return a File object with the path to the workers.properties 
                file to be used by mod_jk.
    */
    public File getWorkersConfig(){
        if(workersConfig==null){
            workersConfig = new File(WORKERS_CONFIG);
        }
        File workersF = workersConfig;
        if(!workersF.isAbsolute()){
            if(getConfigHome()!=null){
                workersF = new File(getConfigHome(),workersF.getPath());
            }else{//resolve against user.dir
                workersF = new File(workersF.getAbsolutePath());
            }
        }
       return workersF;
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
        returns the path to the apache module mod_jserv.  
        If the path set with setModJserv() was relative, this method 
        will try first to resolve it absolutely 
        against the return value of getConfigHome().  If that is null, then
        it instead will resolve against the current user.dir.
        If this file doesn't exist, the relative path is returned.
        <p>
        @return a File object with the path to the mod_jserv.so file.
    */
    public File getModJserv(){
        if(modJserv==null){
            modJserv=new File(MOD_JSERV);
        }
        File jservF = modJserv;
        if(!jservF.isAbsolute()){
            if(getConfigHome()!=null){
                jservF = new File(getConfigHome(),jservF.getPath());
            }else{//resolve against user.dir
                jservF = new File(jservF.getAbsolutePath());
            }
	    if( !jservF.exists() )
		jservF = modJserv;
        }
       return jservF;
    }
    
    /**
        set the path to the mod_jk Apache Module
        @param <b>path</b> String path to a file
    */
    public void setModJk(String path){
        setModJk(path==null?null:new File(path));
    }
    
    /**
        set the path to the mod_jk Apache Module
        @param <b>path</b> File object
    */
    public void setModJk(File path){
        modJk=path;
    }
    
    /**
        returns the path to the apache module mod_jk.  
        If the path set with setModJk() was relative, this method 
        will try first to resolve it absolutely 
        against the return value of getConfigHome().  If that is null, then
        it instead will resolve against the current user.dir.
        If this file doesn't exist, the relative path is returned.
        <p>
        @return a File object with the path to the mod_jk.so file.
    */
    public File getModJk(){
        if(modJk==null){
            modJk=new File(MOD_JK);
        }
        File jkF = modJk;
        if(!jkF.isAbsolute()){
            if(getConfigHome()!=null){
                jkF = new File(getConfigHome(),jkF.getPath());
            }else{//resolve against user.dir
                jkF = new File(jkF.getAbsolutePath());
            }
	    if( !jkF.exists() )
		jkF = modJk;
        }
       return jkF;
    }
    
   /**
        set the path to the mod_jk log file
        @param <b>path</b> String path to a file
    */
    public void setJkLog(String path){
        setJkLog(path==null?null:new File(path));
    }
    

    /**
        set the path to the mod_jk log file.
        @param <b>path</b> File object
    */
    public void setJkLog(File path){
        jkLog=path;
    }
    
    /**
        returns the path to the mod_jk log file.  
        If the path set with setJkLog() was relative, this method 
        will try first to resolve it absolutely 
        against the return value of getConfigHome().  If that is null, then
        it instead will resolve against the current user.dir.
        <p>
        @return a File object with the path to the mod_jk log file.
    */
    public File getJkLog(){
        if(jkLog==null){
            jkLog=new File(JK_LOG_LOCATION);
        }
        File logF = jkLog;
        if(!logF.isAbsolute()){
            if(getConfigHome()!=null){
                logF = new File(getConfigHome(),logF.getPath());
            }else{//resolve against user.dir
                logF = new File(logF.getAbsolutePath());
            }
        }
       return logF;
    }
    

    
    /**
        executes the ApacheConfig interceptor. This method generates apache
        configuration files for use with mod_jserv or mod_jk.  If not
        already set, this method will setConfigHome() to the value returned
        from <i>cm.getHome()</i>.
        <p>
        @param <b>cm</b> a ContextManager object.
    */
    public void execute(ContextManager cm) throws TomcatException {
    	try {
    	    String tomcatHome = cm.getHome();
    	    File tomcatDir = new File(tomcatHome);
    	    
    	    if(getConfigHome()==null){
    	        setConfigHome(tomcatDir);
    	    }
    	    
    	    //String apacheHome = findApache();
    	    int jkConnector = AJP12;


    	    PrintWriter pw=new PrintWriter(new FileWriter(getJservConfig()));
    	    log("Generating apache mod_jserv config = "+getJservConfig() );

    	    PrintWriter mod_jk = new PrintWriter(new FileWriter(getJkConfig()));
    	    log("Generating apache mod_jk config = "+getJkConfig() );


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
            
            //insert LoadModule calls:
            pw.println("<IfModule !mod_jserv.c>");
            pw.println("  LoadModule jserv_module "+
                     getModJserv().toString().replace('\\','/'));
            pw.println("</IfModule>");

            mod_jk.println("<IfModule !mod_jk.c>");
            mod_jk.println("  LoadModule jk_module "+
                         getModJk().toString().replace('\\','/'));
            mod_jk.println("</IfModule>");
            mod_jk.println();                
            mod_jk.println("JkWorkersFile \"" 
             + getWorkersConfig().toString().replace('\\', '/') 
             + "\"");
            mod_jk.println("JkLogFile \"" 
             + getJkLog().toString().replace('\\', '/') 
             + "\"");

    	    pw.println("ApJServManual on");
    	    pw.println("ApJServDefaultProtocol " + AJPV12);
    	    pw.println("ApJServSecretKey DISABLED");
    	    pw.println("ApJServMountCopy on");
    	    pw.println("ApJServLogLevel notice");
    	    pw.println();

    	    // Find Ajp1? connectors
    	    int portInt=8007;
    	    BaseInterceptor ci[]=cm.getContainer().getInterceptors();
    	    for( int i=0; i<ci.length; i++ ) {
    		    Object con=ci[i];
    /*		    if( con instanceof  Ajp12ConnectionHandler ) {
    		    PoolTcpConnector tcpCon=(PoolTcpConnector) con;
    		    portInt=tcpCon.getPort();
    		    }*/
    		    if( con instanceof  Ajp12Interceptor ) {
    		        Ajp12Interceptor tcpCon=(Ajp12Interceptor) con;
    		        portInt=tcpCon.getPort();
    		    }
    		    if( con instanceof  Ajp13Interceptor ) {
          		    jkConnector = AJP13;
    		    }
    	    }
    	    pw.println("ApJServDefaultPort " + portInt);
    	    pw.println();

    	    pw.println("AddType text/jsp .jsp");
    	    pw.println("AddHandler jserv-servlet .jsp");
    	    pw.println();

            mod_jk.println();
            mod_jk.println("#");        
            mod_jk.println("# Log level to be used by mod_jk");
            mod_jk.println("#");        
            mod_jk.println("JkLogLevel error");
    	    mod_jk.println();

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
            mod_jk.println("# What is the indicator for the client SSL certificated (default is SSL_CLIENT_CERT)");        
            mod_jk.println("# JkCERTSIndicator SSL_CLIENT_CERT");
            mod_jk.println("# ");        
            mod_jk.println("#                                                                 #");        
            mod_jk.println("###################################################################");
            mod_jk.println();


            mod_jk.println("#");        
            mod_jk.println("# Root context mounts for Tomcat");
            mod_jk.println("#");        
            mod_jk.println("JkMount /*.jsp " + JkMount[jkConnector]);
            mod_jk.println("JkMount /servlet/* " + JkMount[jkConnector]);
            mod_jk.println();

    	    // Set up contexts
    	    // XXX deal with Virtual host configuration !!!!
    	    Enumeration  enum = cm.getContexts();
    	    while (enum.hasMoreElements()) {
                Context context = (Context)enum.nextElement();
                String path  = context.getPath();
                String vhost = context.getHost();

                if( vhost != null ) {
                    // Generate Apache VirtualHost section for this host
                    // You'll have to do it manually right now
                    // XXX
                    continue;
                }
                if( path.length() > 1) {

                    // It's not the root context
                    // assert path.startsWith( "/" )

                    // Calculate the absolute path of the document base
                    String docBase = context.getDocBase();
                    if (!FileUtil.isAbsolute(docBase)){
                	    docBase = tomcatHome + "/" + docBase;
                    }
                    docBase = FileUtil.patch(docBase);
                	if (File.separatorChar == '\\')
                		docBase = docBase.replace('\\','/');// use separator preferred by Apache

                    // Static files will be served by Apache
                    pw.println("Alias " + path + " \"" + docBase + "\"");
                    pw.println("<Directory \"" + docBase + "\">");
                    pw.println("    Options Indexes FollowSymLinks");
                    pw.println("</Directory>");

                    // Dynamic /servet pages go to Tomcat
                    pw.println("ApJServMount " + path +"/servlet" + " " + path);

                    // Deny serving any files from WEB-INF
                    pw.println("<Location \"" + path + "/WEB-INF/\">");
                    pw.println("    AllowOverride None");
                    pw.println("    deny from all");
                    pw.println("</Location>");
                	// For Windows, use Directory too. Location doesn't work unless case matches
                	if (File.separatorChar == '\\') {
                		pw.println("<Directory \"" + docBase + "/WEB-INF/\">");
                		pw.println("    AllowOverride None");
                		pw.println("    deny from all");
                		pw.println("</Directory>");
                	}

                    // Deny serving any files from META-INF
                	pw.println("<Location \"" + path + "/META-INF/\">");
                	pw.println("    AllowOverride None");
                	pw.println("    deny from all");
                	pw.println("</Location>");
                	// For Windows, use Directory too. Location doesn't work unless case matches
                	if (File.separatorChar  == '\\') {
                		pw.println("<Directory \"" + docBase + "/META-INF/\">");
                		pw.println("    AllowOverride None");
                		pw.println("    deny from all");
                		pw.println("</Directory>");
                	}
                    pw.println();


                    // Static files will be served by Apache
                    mod_jk.println("#########################################################");		    
                    mod_jk.println("# Auto configuration for the " + path + " context starts.");
                    mod_jk.println("#########################################################");		    
                    mod_jk.println();

                    mod_jk.println("#");		    
                    mod_jk.println("# The following line makes apache aware of the location of the " + path + " context");
                    mod_jk.println("#");                        
                    mod_jk.println("Alias " + path + " \"" + docBase + "\"");
                    mod_jk.println("<Directory \"" + docBase + "\">");
                    mod_jk.println("    Options Indexes FollowSymLinks");
                    mod_jk.println("</Directory>");
                    mod_jk.println();            

                    // Dynamic /servet pages go to Tomcat
                    mod_jk.println("#");		    
                    mod_jk.println("# The following line mounts all JSP files and the /servlet/ uri to tomcat");
                    mod_jk.println("#");                        
                    mod_jk.println("JkMount " + path +"/servlet/* " + JkMount[jkConnector]);
                    mod_jk.println("JkMount " + path +"/*.jsp " + JkMount[jkConnector]);
		    mod_jk.println("# The following line mounts the " +
				   "form-based authenticator for the "+
				   path+" context");
		    mod_jk.println("#");
		    mod_jk.println("JkMount " + path +
				   "/*j_security_check" +
				   JkMount[jkConnector]);



                    // Deny serving any files from WEB-INF
                    mod_jk.println();            
                    mod_jk.println("#");		    
                    mod_jk.println("# The following line prohibits users from directly accessing WEB-INF");
                    mod_jk.println("#");                        
                    mod_jk.println("<Location \"" + path + "/WEB-INF/\">");
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
                	}

                	// Deny serving any files from META-INF
                    mod_jk.println();            
                    mod_jk.println("#");		    
                    mod_jk.println("# The following line prohibits users from directly accessing META-INF");
                    mod_jk.println("#");                        
                	mod_jk.println("<Location \"" + path + "/META-INF/\">");
                	mod_jk.println("    AllowOverride None");
                	mod_jk.println("    deny from all");
                	mod_jk.println("</Location>");
                	if (File.separatorChar == '\\') {
                		mod_jk.println("#");		    
                		mod_jk.println("# Use Directory too. On Windows, Location doesn't work unless case matches");
                		mod_jk.println("#");                        
                		mod_jk.println("<Directory \"" + docBase + "/META-INF/\">");
                		mod_jk.println("    AllowOverride None");
                		mod_jk.println("    deny from all");
                		mod_jk.println("</Directory>");
                	}
                    mod_jk.println();

                    mod_jk.println("#######################################################");		    
                    mod_jk.println("# Auto configuration for the " + path + " context ends.");
                    mod_jk.println("#######################################################");		    
                    mod_jk.println();

                    // XXX check security
                    if( false ) {
                	pw.println("<Location " + path + "/servlet/ >");
                	pw.println("    AllowOverride None");
                	pw.println("   AuthName \"restricted \"");
                	pw.println("    AuthType Basic");
                	pw.println("    AuthUserFile conf/users");
                	pw.println("    require valid-user");
                	pw.println("</Location>");
                    }

                   // XXX ErrorDocument

                    // XXX mime types - AddEncoding, AddLanguage, TypesConfig
                } else {
                    // the root context
                    // XXX use a non-conflicting name
                    pw.println("ApJServMount /servlet /ROOT");
                }

    	    }//end while(enum)

    	    pw.close();
    	    mod_jk.close();        
    	} catch( Exception ex ) {
            Log loghelper = Log.getLog("tc_log", this);
    	    loghelper.log("Error generating automatic apache configuration", ex);
    	}
    }//end execute()
    
}//end class ApacheConfig
