/* $Id: BaseJkConfig.java,v 1.1 2001/08/10 03:57:07 larryi Exp $
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
import java.io.*;

import org.apache.tomcat.modules.server.Ajp13Interceptor;

/**
    Base class for automatic jk based configurations based on
    the Tomcat server.xml settings and the war contexts
    initialized during startup.
    <p>
    This config interceptor is enabled by inserting a Config
    element in the <b>&lt;ContextManager&gt;</b> tag body inside
    the server.xml file like so:
    <pre>
    * < ContextManager ... >
    *   ...
    *   <<b>???Config</b> <i>options</i> />
    *   ...
    * < /ContextManager >
    </pre>
    where <i>options</i> can include any of the following attributes:
    <ul>
     <li><b>configHome</b> - default parent directory for the following paths.
                             If not set, this defaults to TOMCAT_HOME. Ignored
                             whenever any of the following paths is absolute.
                             </li>
     <li><b>workersConfig</b> - path to workers.properties file used by 
                                jk connector. If not set, defaults to
                                "conf/jk/workers.properties".</li>
     <li><b>jkLog</b> - path to log file to be used by jk connector.</li>
     <li><b>jkDebug</b> - Loglevel setting.  May be debug, info, error, or emerg.
                          If not set, defaults to emerg.</li>
     <li><b>jkProtocol</b> The desired protocal, "ajp12" or "ajp13". If not
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
                         to correctly serve Tomcat's root context you may also
                         need to modify the web server to point it's home
                         directory to Tomcat's root context directory.
                         Otherwise some content, such as the root index.html,
                         may be served by the web server before the connector
                         gets a chance to claim the request and pass it to Tomcat.
                         The default is true.</li>
    </ul>
    <p>
    @author Costin Manolache
    @author Larry Isaacs
	@version $Revision: 1.1 $
 */
public class BaseJkConfig  extends BaseInterceptor { 
    protected File configHome = null;
    protected File workersConfig = null;

    protected File jkLog = null;
    protected String jkDebug="emerg";
    protected String jkProto = null;

    protected boolean noRoot=true;
    protected boolean forwardAll=true;

    protected String tomcatHome;

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
        This means that the web server's root remains intact but isn't
        completely servlet/JSP enabled. If the ROOT webapp can be configured
        with the web server serving static files, there's no problem setting
        this option to false. If not, then setting it true means the web
        server will be out of picture for all requests.
    */
    public void setNoRoot( boolean b ) {
        noRoot=b;
    }
    
    /**
        set a path to the parent directory of the
        conf folder.  That is, the parent directory
        within which path setters would be resolved against,
        if relative.  For example if ConfigHome is set to "/home/tomcat"
        and regConfig is set to "conf/mod_jk.conf" then the resulting 
        path used would be: 
        "/home/tomcat/conf/mod_jk.conf".</p>
        <p>
        However, if the path is set to an absolute path,
        this attribute is ignored.
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
                "BaseConfig.setConfigHome(): "+
                "Configuration Home must be a directory! : "+dir);
        }
        configHome = f;
    }

    /**
        set a path to the workers.properties file.
        @param <b>path</b> String path to workers.properties file
    */
    public void setWorkersConfig(String path){
        workersConfig= (path==null?null:new File(path));
    }

    /**
        set the path to the mod_jk log file
        @param <b>path</b> String path to a file
    */
    public void setJkLog(String path){
        jkLog= ( path==null?null:new File(path));
    }
    
    /** Set the verbosity level for mod_jk.
        ( use debug, error, etc. ) If not set, no log is written.
     */
    public void setJkDebug( String level ) {
        jkDebug=level;
    }

    /**
        set the Ajp protocal
        @param <b>protocal</b> String protocol, "ajp12" or "ajp13"
     */
    public void setJkProtocol(String protocol){
        jkProto = protocol;
    }

    // -------------------- Initialize/guess defaults --------------------

    /** Initialize defaults for properties that are not set
        explicitely
    */
    protected void initProperties(ContextManager cm) {
        tomcatHome = cm.getHome();
        File tomcatDir = new File(tomcatHome);
        if(configHome==null){
            configHome=tomcatDir;
        }
    }

    protected void initProtocol(ContextManager cm) {
	// Find Ajp1? connectors
	BaseInterceptor ci[]=cm.getContainer().getInterceptors();
	    
	for( int i=0; i<ci.length; i++ ) {
	    Object con=ci[i];
	    // if jkProto not specified and Ajp13 Interceptor found, use Ajp13
	    if( jkProto == null && (con instanceof Ajp13Interceptor) ) {
		jkProto = "ajp13";
	    }
	}

	// default to ajp12
	if( jkProto==null ) jkProto="ajp12";
    }

    // -------------------- Utils --------------------

    protected File getConfigFile( File base, File configDir, String defaultF )
    {
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
                    base.getAbsolutePath());
            }
        }
        return base;
    }

}