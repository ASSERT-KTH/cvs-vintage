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
package org.apache.tomcat.task;

import org.apache.tomcat.core.*;
import org.apache.tomcat.util.*;
import java.io.*;
import java.net.*;
import java.util.*;
import javax.servlet.http.*;


/**
 * Used by ContextManager to generate automatic apache configurations
 *
 * @author costin@dnt.ro
 */
public class ApacheConfig  { // implements XXX
    // XXX maybe conf/
    public static final String APACHE_CONFIG  = "/conf/tomcat-apache.conf";
    public static final String MOD_JK_CONFIG  = "/conf/mod_jk.conf";
    public static final String WORKERS_CONFIG = "/conf/workers.properties";
    public static final String URL_WORKERS_MAP_CONFIG = "/conf/uriworkermap.properties";
    public static final String JK_LOG_LOCATION = "/log/mod_jk.log";

    public ApacheConfig() {
    }

    String findApache() {
	return null;
    }

    public void execute(ContextManager cm) throws TomcatException {
	try {
	    String tomcatHome = cm.getHome();
	    String apacheHome = findApache();

	    //System.out.println("Tomcat home= " + tomcatHome);

	    FileWriter configW=new FileWriter(tomcatHome + APACHE_CONFIG);
	    PrintWriter pw=new PrintWriter(configW);
        PrintWriter mod_jk = new PrintWriter(new FileWriter(tomcatHome + MOD_JK_CONFIG));
        PrintWriter uri_worker = new PrintWriter(new FileWriter(tomcatHome + URL_WORKERS_MAP_CONFIG));        

        uri_worker.println("###################################################################");		    
        uri_worker.println("# Auto generated configuration. Dated: " +  new Date());
        uri_worker.println("###################################################################");		    
        uri_worker.println();

        mod_jk.println("###################################################################");
        mod_jk.println("# Auto generated configuration. Dated: " +  new Date());
        mod_jk.println("###################################################################");
        mod_jk.println();
        
        mod_jk.println("#");
        mod_jk.println("# The following line instructs Apache to load the jk module");
        mod_jk.println("#");
	    if( System.getProperty( "os.name" ).toLowerCase().indexOf("windows") >= 0 ) {
		pw.println("LoadModule jserv_module modules/ApacheModuleJServ.dll");
                mod_jk.println("LoadModule jk_module modules/mod_jk.dll");
                mod_jk.println();                
                mod_jk.println("JkWorkersFile \"" + new File(tomcatHome, WORKERS_CONFIG).toString().replace('\\', '/') + "\"");
                mod_jk.println("JkWorkersFile \"" + new File(tomcatHome, JK_LOG_LOCATION).toString().replace('\\', '/') + "\"");
	    } else {
		// XXX XXX change it to mod_jserv_${os.name}.so, put all so in tomcat
		// home
		pw.println("LoadModule jserv_module libexec/mod_jserv.so");
                mod_jk.println("LoadModule jk_module libexec/mod_jk.so");
                mod_jk.println();                                
                mod_jk.println("JkWorkersFile " + new File(tomcatHome, WORKERS_CONFIG));
                mod_jk.println("JkWorkersFile " + new File(tomcatHome, JK_LOG_LOCATION));
	    }


	    pw.println("ApJServManual on");
	    pw.println("ApJServDefaultProtocol ajpv12");
	    pw.println("ApJServSecretKey DISABLED");
	    pw.println("ApJServMountCopy on");
	    pw.println("ApJServLogLevel notice");
	    pw.println();

	    // XXX read it from ContextManager
	    pw.println("ApJServDefaultPort 8007");
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

        mod_jk.println("#");        
        mod_jk.println("# Root context mounts for Tomcat");
        mod_jk.println("#");        
        mod_jk.println("JkMount /*.jsp ajp12");
        mod_jk.println("JkMount /servlet/* ajp12");
        mod_jk.println();

        uri_worker.println("#");        
        uri_worker.println("# Root context mounts for Tomcat");
        uri_worker.println("#");        
		uri_worker.println("/servlet/*=ajp12");
		uri_worker.println("/*.jsp=ajp12"); 
        uri_worker.println();            


	    // Set up contexts
	    // XXX deal with Virtual host configuration !!!!
	    Enumeration enum = cm.getContexts();
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
		    if (!FileUtil.isAbsolute(docBase))
			docBase = tomcatHome + "/" + docBase;
		    docBase = FileUtil.patch(docBase);

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
		    mod_jk.println("JkMount " + path +"/servlet ajp12");
		    mod_jk.println("JkMount " + path +"/*.jsp ajp12");


		    // Deny serving any files from WEB-INF
            mod_jk.println();            
            mod_jk.println("#");		    
            mod_jk.println("# The following line prohibits users from directly access WEB-INF");
            mod_jk.println("#");                        
		    mod_jk.println("<Location \"" + path + "/WEB-INF/\">");
		    mod_jk.println("    AllowOverride None");
		    mod_jk.println("    deny from all");
		    mod_jk.println("</Location>");
		    mod_jk.println();

            mod_jk.println("#######################################################");		    
            mod_jk.println("# Auto configuration for the " + path + " context ends.");
            mod_jk.println("#######################################################");		    
            mod_jk.println();

            // Static files will be served by Apache
            uri_worker.println("#########################################################");		    
            uri_worker.println("# Auto configuration for the " + path + " context starts.");
            uri_worker.println("#########################################################");		    
            uri_worker.println();
            

            uri_worker.println("#");		    
            uri_worker.println("# The following line mounts all JSP file and the /servlet/ uri to tomcat");
            uri_worker.println("#");                        
		    uri_worker.println(path +"/servlet/*=ajp12");
		    uri_worker.println(path +"/*.jsp=ajp12"); 
            uri_worker.println();            

            uri_worker.println("#######################################################");		    
            uri_worker.println("# Auto configuration for the " + path + " context ends.");
            uri_worker.println("#######################################################");		    
            uri_worker.println();

		    // SetHandler broken in jserv ( no zone is sent )
		    // 		    pw.println("<Location " + path + "/servlet/ >");
		    // 		    pw.println("    AllowOverride None");
		    // 		    pw.println("    SetHandler jserv-servlet");
		    // 		    pw.println("</Location>");
		    // 		    pw.println();

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

		    // SetHandler broken in jserv ( no zone is sent )
		    // 		    pw.println("<Location " + path + " >");
		    // 		    pw.println("    AllowOverride None");
		    // 		    pw.println("    AddHandler jserv-servlet .jsp");
		    // 		    pw.println("    Options Indexes");
		    // 		    pw.println("</Location>");

		    // XXX ErrorDocument

		    // XXX mime types - AddEncoding, AddLanguage, TypesConfig


		} else {
		    // the root context
		    // XXX use a non-conflicting name
		    pw.println("ApJServMount /servlet /ROOT");
		}

	    }

	    pw.close();
	    mod_jk.close();
	    uri_worker.close();
	        
	} catch( Exception ex ) {
	    //	    ex.printStackTrace();
	    //throw new TomcatException( "Error generating Apache config", ex );
	    System.out.println("Error generating automatic apache configuration " + ex);
	    ex.printStackTrace(System.out);
	}

    }

}
