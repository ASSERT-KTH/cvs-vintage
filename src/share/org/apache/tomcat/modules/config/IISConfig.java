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
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Date;
import java.util.Enumeration;

import org.apache.tomcat.core.Container;
import org.apache.tomcat.core.Context;
import org.apache.tomcat.core.ContextManager;
import org.apache.tomcat.core.TomcatException;
import org.apache.tomcat.util.io.FileUtil;
import org.apache.tomcat.util.log.Log;


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
     <li><b>jkWorker</b> The desired worker.  Must be set to one of the workers
                         defined in the workers.properties file. "ajp12", "ajp13"
                         or "inprocess" are the workers found in the default
                         workers.properties file. If not specified, defaults
                         to "ajp13" if an Ajp13Interceptor is in use, otherwise
                         it defaults to "ajp12".</li>
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
	@version $Revision: 1.15 $
 */
public class IISConfig extends BaseJkConfig  { 

    public static final String WORKERS_CONFIG = "conf/jk/workers.properties";
    public static final String URI_WORKERS_MAP_CONFIG = "conf/auto/uriworkermap.properties";
    public static final String ISAPI_LOG_LOCATION = "logs/iis_redirect.log";
    public static final String ISAPI_REG_FILE = "conf/auto/iis_redirect.reg";    
    public static final String ISAPI_PROP_FILE = "conf/auto/isapi_redirect.properties";
    public static final String ISAPI_REDIRECTOR = "isapi_redirect.dll";

    private File regConfig = null;
    private File propConfig = null;
    private File uriConfig = null;

    private String isapiRedirector = null;

    public IISConfig() 
    {
    }

    //-------------------- Properties --------------------

    private void updatePropFile() {
        propConfig=null;
        if( isapiRedirector != null ) {
            int idx=isapiRedirector.lastIndexOf('.');
            if( idx > 0 ) {
                String dir=(regConfig!=null) ?
                        regConfig.toString().replace(File.separatorChar,'/') :
                        ISAPI_REG_FILE;
                int idx2=dir.lastIndexOf('/');
                if( idx2 > 0 ) {
                    dir = dir.substring(0,idx2 + 1);
                } else {
                    dir = "";
                }
                propConfig = new File(dir + isapiRedirector.substring(0,idx)
                                          + ".properties");
            }
        }
    }
    
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
        updatePropFile();
    }

    /**
        set a path to the uriworkermap.properties file.
        @param <b>path</b> String path to uriworkermap.properties file
    */
    public void setUriConfig(String path){
        uriConfig= (path==null?null:new File(path));
    }

    public void setIsapiRedirector(String s) {
        isapiRedirector=s;
        updatePropFile();
    }

    // -------------------- Initialize/guess defaults --------------------

    /** Initialize defaults for properties that are not set
	explicitely
    */
    protected void initProperties(ContextManager cm) {
        super.initProperties(cm);

	regConfig=FileUtil.getConfigFile( regConfig, configHome, ISAPI_REG_FILE);
	propConfig=FileUtil.getConfigFile( propConfig, configHome, ISAPI_PROP_FILE);
	workersConfig=FileUtil.getConfigFile( workersConfig, configHome, WORKERS_CONFIG);
	uriConfig=FileUtil.getConfigFile( uriConfig, configHome, URI_WORKERS_MAP_CONFIG);
	jkLog=FileUtil.getConfigFile( jkLog, configHome, ISAPI_LOG_LOCATION);
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
	    initWorker(cm);

            PrintWriter regfile = new PrintWriter(new FileWriter(regConfig));
            PrintWriter propfile = new PrintWriter(new FileWriter(propConfig));
            PrintWriter uri_worker = new PrintWriter(new FileWriter(uriConfig));        
    	    log("Generating IIS registry file = "+regConfig );
    	    log("Generating IIS properties file = "+propConfig );
    	    log("Generating IIS URI worker map file = "+uriConfig );

            generateRegistrySettings(regfile);
            generatePropertySettings(propfile);

            generateUriWorkerHeader(uri_worker);            

            // Set up contexts
            // XXX deal with Virtual host configuration !!!!
            Enumeration enum = cm.getContexts();
            while (enum.hasMoreElements()) {
                Context context = (Context)enum.nextElement();

                String vhost = context.getHost();
                if(vhost != null) {
                    // Vhosts are not supported yet for IIS
                    continue;
                }

		if( forwardAll )
		    generateStupidMappings( context, uri_worker );
		else
		    generateContextMappings( context, uri_worker );
            }

            regfile.close();
            propfile.close();
            uri_worker.close();

        } catch(Exception ex) {
            Log loghelper = Log.getLog("tc_log", this);
            loghelper.log("Error generating automatic IIS configuration", ex);
        }
    }

    // -------------------- Config sections  --------------------

    /** Writes the registry settings required by the IIS connector
     */
    private void generateRegistrySettings(PrintWriter regfile)
    {
        regfile.println("REGEDIT4");
        regfile.println();
        regfile.println("[HKEY_LOCAL_MACHINE\\SOFTWARE\\Apache Software Foundation\\Jakarta Isapi Redirector\\1.0]");
        regfile.println("\"extension_uri\"=\"/jakarta/"
                + (isapiRedirector!=null?isapiRedirector:ISAPI_REDIRECTOR) + "\"");
        regfile.println("\"log_file\"=\"" + dubleSlash(jkLog.toString()) +"\"");
        regfile.println("\"log_level\"=\"" + jkDebug + "\"");
        regfile.println("\"worker_file\"=\"" + dubleSlash(workersConfig.toString()) +"\"");
        regfile.println("\"worker_mount_file\"=\"" + dubleSlash(uriConfig.toString()) +"\"");
        regfile.println("\"uri_select\"=\"parsed\"");
    }

    /** Writes the registry settings required by the IIS connector
     */
    private void generatePropertySettings(PrintWriter propfile)
    {
        propfile.println("extension_uri=/jakarta/"
                + (isapiRedirector!=null?isapiRedirector:ISAPI_REDIRECTOR));
        propfile.println("log_file=" + jkLog.toString());
        propfile.println("log_level=" + jkDebug);
        propfile.println("worker_file=" + workersConfig.toString());
        propfile.println("worker_mount_file=" + uriConfig.toString());
        propfile.println("uri_select=parsed");
    }

    /** Writes the header information to the uriworkermap file
     */
    private void generateUriWorkerHeader(PrintWriter uri_worker)
    {
        uri_worker.println("###################################################################");		    
        uri_worker.println("# Auto generated configuration. Dated: " +  new Date());
        uri_worker.println("###################################################################");		    
        uri_worker.println();

        uri_worker.println("#");        
        uri_worker.println("# Default worker to be used through our mappings");
        uri_worker.println("#");        
        uri_worker.println("default.worker=" + jkWorker);        
        uri_worker.println();
    }

    /** Forward all requests for a context to tomcat.
	The default.
     */
    private void generateStupidMappings(Context context, PrintWriter uri_worker )
    {
        String ctxPath  = context.getPath();
	String nPath=("".equals(ctxPath)) ? "/" : ctxPath;

        if( noRoot &&  "".equals(ctxPath) ) {
            log("Ignoring root context in forward-all mode  ");
            return;
        } 

        // map all requests for this context to Tomcat
        uri_worker.println(nPath +"=$(default.worker)");
        if( "".equals(ctxPath) ) {
            uri_worker.println(nPath +"*=$(default.worker)");
            uri_worker.println(
                    "# Note: To correctly serve the Tomcat's root context, IIS's Home Directory must");
            uri_worker.println(
                    "# must be set to: \"" + getAbsoluteDocBase(context) + "\"");
        }
        else
            uri_worker.println(nPath +"/*=$(default.worker)");
    }

    private void generateContextMappings(Context context, PrintWriter uri_worker )
    {
        String ctxPath  = context.getPath();
	String nPath=("".equals(ctxPath)) ? "/" : ctxPath;

        if( noRoot &&  "".equals(ctxPath) ) {
            log("Ignoring root context in forward-all mode  ");
            return;
        } 

        // Static files will be served by IIS
        uri_worker.println();
        uri_worker.println("#########################################################");		    
        uri_worker.println("# Auto configuration for the " + nPath + " context.");
        uri_worker.println("#########################################################");		    
        uri_worker.println();

        // Static mappings are not set in uriworkermap, but must be set with IIS admin.

	// InvokerInterceptor - it doesn't have a container,
	// but it's implemented using a special module.

	// XXX we need to better collect all mappings
	addMapping( ctxPath + "/servlet/*", uri_worker );

	Enumeration servletMaps=context.getContainers();
	while( servletMaps.hasMoreElements() ) {
	    Container ct=(Container)servletMaps.nextElement();
	    addMapping( context, ct , uri_worker );
	}

	// XXX ErrorDocument
	// Security and filter mappings

    }

    /** Add an IIS extension mapping.
     */
    protected boolean addExtensionMapping( String ctxPath, String ext,
					 PrintWriter uri_worker )
    {
        if( debug > 0 )
            log( "Adding extension map for " + ctxPath + "/*." + ext );
	uri_worker.println(ctxPath + "/*." + ext + "=$(default.worker)");
        return true;
    }

    /** Add a fulling specified IIS mapping.
     */
    protected boolean addMapping( String fullPath, PrintWriter uri_worker ) {
        if( debug > 0 )
            log( "Adding map for " + fullPath );
        if( fullPath.endsWith("/*") ) {
            uri_worker.println(fullPath.substring(0, fullPath.length() - 2)
                    + "=$(default.worker)" );
        }
        uri_worker.println(fullPath + "=$(default.worker)" );
        return true;
    }

    // -------------------- Utils --------------------

    private String dubleSlash(String in) 
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
