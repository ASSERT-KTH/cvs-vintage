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
import org.apache.tomcat.util.log.*;
import java.io.*;
import java.util.*;


/**
    Generates automatic IIS isapi_redirect configurations based on
    the Tomcat server.xml settings and the war contexts
    initialized during startup.
    <p>
    This config interceptor is enabled by inserting an IISConfig
    element in the <b>&lt;ContextManager&gt;</b> tag body inside
    the server.xml file like so:
    <pre>
    * < ContextManager ... >
    *   ...
    *   <<b>IISConfig</b> <i>options</i> />
    *   ...
    * < /ContextManager >
    </pre>
    where <i>options</i> can include any of the following attributes:
    <ul>
     <li><b>configHome</b> - default parent directory for the following paths.
                            If not set, this defaults to TOMCAT_HOME. Ignored
                            whenever any of the following paths is absolute.
                             </li>
     <li><b>regConfig</b> - path to use for writing IIS isapi_redirect registry
                            file. If not set, defaults to
                            "conf/auto/iis_redirect.reg".</li>
     <li><b>workersConfig</b> - path to workers.properties file used by 
                                isapi_redirect. If not set, defaults to
                                "conf/jk/workers.properties".</li>
     <li><b>uriConfig</b> - path to use for writing IIS isapi_redirect uriworkermap
                            file. If not set, defaults to
                            "conf/auto/uriworkermap.properties".</li>
     <li><b>jkLog</b> - path to log file to be used by isapi_redirect.</li>
     <li><b>jkDebug</b> - Loglevel setting.  May be debug, info, error, or emerg.
                          If not set, defaults to emerg.</li>
     <li><b>jkProtocol</b> The desired protocal, "ajp12" or "ajp13". If not
                           specified, defaults to "ajp13" if an Ajp13Interceptor
                           is in use, otherwise it defaults to "ajp12".</li>
     <li><b>forwardAll</b> - If true, forward all requests to Tomcat. This helps
                             insure that all the behavior configured in the web.xml
                             file functions correctly.  If false, let IIS serve
                             static resources assuming it has been configured
                             to do so. The default is true.
                             Warning: When false, some configuration in
                             the web.xml may not be duplicated in IIS.
                             Review the uriworkermap file to see what
                             configuration is actually being set in IIS.</li>
     <li><b>noRoot</b> - If true, the root context is not mapped to
                         Tomcat.  If false and forwardAll is true, all requests
                         to the root context are mapped to Tomcat. If false and
                         forwardAll is false, only JSP and servlets requests to
                         the root context are mapped to Tomcat. When false,
                         to correctly serve Tomcat's root context you must also
                         modify the Home Directory setting in IIS
                         to point to Tomcat's root context directory.
                         Otherwise some content, such as the root index.html,
                         will be served by IIS before isapi_redirect gets a chance
                         to claim the request and pass it to Tomcat.
                         The default is true.</li>
    </ul>
  <p>
    @author Costin Manolache
    @author Larry Isaacs
    @author Gal Shachor
	@version $Revision: 1.5 $
 */
public class IISConfig extends BaseJkConfig  { 

    public static final String WORKERS_CONFIG = "/conf/jk/workers.properties";
    public static final String URI_WORKERS_MAP_CONFIG = "/conf/auto/uriworkermap.properties";
    public static final String ISAPI_LOG_LOCATION = "/logs/isapi_redirect.log";
    public static final String ISAPI_REG_FILE = "/conf/auto/isapi_redirect.reg";    

    private File regConfig = null;
    private File uriConfig = null;

    Log loghelper = Log.getLog("tc_log", "IISConfig");

    public IISConfig() 
    {
    }

    public void engineInit(ContextManager cm) throws TomcatException
    {
	execute( cm );
    }

    //-------------------- Properties --------------------
    
    /**
        set the path to the output file for the auto-generated
        isapi_redirect registry file.  If this path is relative
        then getRegConfig() will resolve it absolutely against
        the getConfigHome() path.
        <p>
        @param <b>path</b> String path to a file
    */
    public void setRegConfig(String path){
	regConfig= (path==null)?null:new File(path);
    }

    /**
        set a path to the uriworkermap.properties file.
        @param <b>path</b> String path to uriworkermap.properties file
    */
    public void setUriConfig(String path){
        uriConfig= (path==null?null:new File(path));
    }

    // -------------------- Initialize/guess defaults --------------------

    /** Initialize defaults for properties that are not set
	explicitely
    */
    protected void initProperties(ContextManager cm) {
        super.initProperties(cm);

	regConfig=getConfigFile( regConfig, configHome, ISAPI_REG_FILE);
	workersConfig=getConfigFile( workersConfig, configHome, WORKERS_CONFIG);
	uriConfig=getConfigFile( uriConfig, configHome, URI_WORKERS_MAP_CONFIG);
	jkLog=getConfigFile( jkLog, configHome, ISAPI_LOG_LOCATION);
    }

    // -------------------- Generate config --------------------
    
    /**
        executes the IISConfig interceptor. This method generates IIS
        configuration files for use with isapi_redirect.  If not
        already set, this method will setConfigHome() to the value returned
        from <i>cm.getHome()</i>.
        <p>
        @param <b>cm</b> a ContextManager object.
    */
    public void execute(ContextManager cm) throws TomcatException 
    {
        try {
	    initProperties(cm);
	    initProtocol(cm);

            PrintWriter regfile = new PrintWriter(new FileWriter(regConfig));
            PrintWriter uri_worker = new PrintWriter(new FileWriter(uriConfig));        

            regfile.println("REGEDIT4");
            regfile.println();
            regfile.println("[HKEY_LOCAL_MACHINE\\SOFTWARE\\Apache Software Foundation\\Jakarta Isapi Redirector\\1.0]");
            regfile.println("\"extension_uri\"=\"/jakarta/isapi_redirect.dll\"");
            regfile.println("\"log_file\"=\"" + dubleSlash(jkLog.toString()) +"\"");
            regfile.println("\"log_level\"=\"" + jkDebug + "\"");
            regfile.println("\"worker_file\"=\"" + dubleSlash(workersConfig.toString()) +"\"");
            regfile.println("\"worker_mount_file\"=\"" + dubleSlash(uriConfig.toString()) +"\"");

            
            uri_worker.println("###################################################################");		    
            uri_worker.println("# Auto generated configuration. Dated: " +  new Date());
            uri_worker.println("###################################################################");		    
            uri_worker.println();

            uri_worker.println("#");        
            uri_worker.println("# Default worker to be used through our mappings");
            uri_worker.println("#");        
            uri_worker.println("default.worker=" + jkProto);        
            uri_worker.println();
            
            uri_worker.println("#");                    
            uri_worker.println("# Root context mounts for Tomcat");
            uri_worker.println("#");        
		    uri_worker.println("/servlet/*=$(default.worker)");
		    uri_worker.println("/*.jsp=$(default.worker)");
            uri_worker.println();            


	        // Set up contexts
	        // XXX deal with Virtual host configuration !!!!
	        Enumeration enum = cm.getContexts();
	        while (enum.hasMoreElements()) {
		        Context context = (Context)enum.nextElement();
		        String path  = context.getPath();
		        String vhost = context.getHost();

		        if(vhost != null) {
		            // Vhosts are not supported yet for IIS
		            continue;
		        }
		        if(path.length() > 1) {
                    // Static files will be served by IIS
                    uri_worker.println("#########################################################");		    
                    uri_worker.println("# Auto configuration for the " + path + " context starts.");
                    uri_worker.println("#########################################################");		    
                    uri_worker.println();
            

                    uri_worker.println("#");		    
                    uri_worker.println("# The following line mounts all JSP file and the /servlet/ uri to tomcat");
                    uri_worker.println("#");                        
		            uri_worker.println(path +"/servlet/*=$(default.worker)");
		            uri_worker.println(path +"/*.jsp=$(default.worker)");
                    uri_worker.println();            

                    uri_worker.println("#");		    
                    uri_worker.println("# The following line specifies that tomcat should serve all the resources");
                    uri_worker.println("# (including static) that are port of the " + path + " context.  This insures");
                    uri_worker.println("# that behavior specified in the web.xml functions correctly.  If you want");
                    uri_worker.println("# IIS to serve static resources, comment out this line and replace with");
                    uri_worker.println("# appropriate mappings.  Then update the IIS configuration as needed.");
                    uri_worker.println("#");                        
                    uri_worker.println(path +"=$(default.worker)");
                    uri_worker.println(path +"/*=$(default.worker)");

                    uri_worker.println("#######################################################");		    
                    uri_worker.println("# Auto configuration for the " + path + " context ends.");
                    uri_worker.println("#######################################################");		    
                    uri_worker.println();
		        }
	        }

	        regfile.close();
	        uri_worker.close();
	        
	    } catch(Exception ex) {
	        loghelper.log("Error generating automatic IIS configuration", ex);
	    }
    }

    // -------------------- Utils --------------------

    protected String dubleSlash(String in) 
    {
        StringBuffer sb = new StringBuffer();
        
        for(int i = 0 ; i < in.length() ; i++) {
            char ch = in.charAt(i);
            if('\\' == ch) {
                sb.append("\\\\");
            } else {
                sb.append(ch);
            }
        }
        
        return sb.toString();
    }
}
