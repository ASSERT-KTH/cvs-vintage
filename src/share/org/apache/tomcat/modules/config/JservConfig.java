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
    *   <<b>JServConfig</b> <i>options</i> />
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
     <li><b>modjserv</b> - path to Apache JServ plugin module file. If not 
                           set, defaults to "modules/ApacheModuleJServ.dll"
                           on windows, "modules/Jserv.nlm" on netware, and 
                           "libexec/mod_jserv.so" everywhere else.</li>
     <li><b>jklog</b> - path to log file to be used by mod_jk.</li>                       
    </ul>
    <p>
    @author Costin Manolache
    @author Mel Martinez
	@version $Revision: 1.2 $ $Date: 2001/07/19 20:23:34 $
 */
public class JservConfig  extends BaseInterceptor { 
    
    /** default path to JServ .conf location */
    public static final String APACHE_CONFIG="conf/jserv/tomcat-apache.conf";
    /** default location of mod_jserv Apache plug-in. */
    public static final String MOD_JSERV;
    public static final String AJPV12="ajpv12";
    
    //set up some defaults based on OS type
    static{
        String os = System.getProperty("os.name").toLowerCase();
        if(os.indexOf("windows")>=0){
           MOD_JSERV = "modules/ApacheModuleJserv.dll";
        }else if(os.indexOf("netware")>=0){
           MOD_JSERV = "modules/Jserv.nlm";
        }else{
           MOD_JSERV = "libexec/mod_jserv.so";
        }
    }
    
    private File configHome = null;
    private File jservConfig = null;
    private File workersConfig = null;
    private File modJserv = null;
    
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
    	    
    	    PrintWriter pw=new PrintWriter(new FileWriter(getJservConfig()));
    	    log("Generating apache mod_jserv config = "+getJservConfig() );

            //insert LoadModule calls:
            pw.println("<IfModule !mod_jserv.c>");
            pw.println("  LoadModule jserv_module "+
                     getModJserv().toString().replace('\\','/'));
            pw.println("</IfModule>");

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
		if( con instanceof  Ajp12Interceptor ) {
		    Ajp12Interceptor tcpCon=(Ajp12Interceptor) con;
		    portInt=tcpCon.getPort();
		}
    	    }
    	    pw.println("ApJServDefaultPort " + portInt);
    	    pw.println();

    	    pw.println("AddType text/jsp .jsp");
    	    pw.println("AddHandler jserv-servlet .jsp");
    	    pw.println();


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
		    
                    // All pages will be served by tomcat.
		    // So far nobody found a solution that can configure apache to
		    // match web.xml, until this happen we can't do too much.

		    // In mod_jk/Ajp14 we'll provide special solution to redirect
		    // static pages to apache, and avoid overhead
                    pw.println("ApJServMount " + path  + " " + path);

                } else {
                    // the root context
                    // XXX use a non-conflicting name
                    pw.println("ApJServMount / /ROOT");
                }

    	    }//end while(enum)

    	    pw.close();
    	} catch( Exception ex ) {
            Log loghelper = Log.getLog("tc_log", this);
    	    loghelper.log("Error generating automatic apache configuration", ex);
    	}
    }//end execute()
    
}//end class ApacheConfig
