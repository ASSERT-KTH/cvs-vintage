/*
 *  Copyright 1999-2004 The Apache Software Foundation
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
 *  See the License for the specific language 
 */

package org.apache.jasper;

import org.apache.tomcat.util.log.Log;
import org.apache.tomcat.util.res.StringManager;
 
/**
 * Some constants and other global data that are used by the compiler
 * and the runtime.
 *
 * @author Anil K. Vijendran
 * @author Harish Prabandham
 */
public class Constants {
    public static String JSP_RUNTIME_PACKAGE =
	"org.apache.jasper.runtime";

    /**
     * The base class of the generated servlets. 
     */
    public static String JSP_SERVLET_BASE =
	JSP_RUNTIME_PACKAGE + ".HttpJspBase";

    /**
     * _jspService is the name of the method that is called by 
     * HttpJspBase.service(). This is where most of the code generated
     * from JSPs go.
     */
    public static final String SERVICE_METHOD_NAME = "_jspService";

    /**
     * Default servlet content type.
     */
    public static final String SERVLET_CONTENT_TYPE = "text/html";

    /**
     * These classes/packages are automatically imported by the
     * generated code. 
     */
    public static final String[] STANDARD_IMPORTS = { 
	"javax.servlet.*",
	"javax.servlet.http.*",
	"javax.servlet.jsp.*"
    };
    
    /**
     * ServletContext attribute for classpath. This is tomcat specific. 
     * Other servlet engines can choose to have this attribute if they 
     * want to have this JSP engine running on them. 
     */
    public static final String SERVLET_CLASSPATH = "org.apache.tomcat.jsp_classpath";

    /**
     * ServletContext attribute for classpath. This is tomcat specific. 
     * Other servlet engines can choose to have this attribute if they 
     * want to have this JSP engine running on them. 
     */
    public static final String SERVLET_CLASS_LOADER = "org.apache.tomcat.classloader";


    /**
     * Default size of the JSP buffer.
     */
    public static final int K = 1024;
    public static final int DEFAULT_BUFFER_SIZE = 8*K;

    /**
     * The query parameter that causes the JSP engine to just
     * pregenerated the servlet but not invoke it. 
     */
    public static final String PRECOMPILE = "jsp_precompile";

    /**
     * Servlet context and request attributes that the JSP engine
     * uses. 
     */
    public static final String INC_REQUEST_URI = "javax.servlet.include.request_uri";
    public static final String INC_SERVLET_PATH = "javax.servlet.include.servlet_path";
    public static final String TMP_DIR = "javax.servlet.context.tempdir";

    /**
     * ProtectionDomain to use for JspLoader defineClass() for current
     * Context when using a SecurityManager.
     */
    public static final String ATTRIB_JSP_ProtectionDomain = "tomcat.context.jsp.protection_domain";

    /**
     * A token which is embedded in file names of the generated
     * servlet. 
     */
    public static final String JSP_TOKEN = "_jsp_";

    /**
     * ID and location of the DTD for tag library descriptors. 
     */
    public static final String 
        TAGLIB_DTD_PUBLIC_ID = "-//Sun Microsystems, Inc.//DTD JSP Tag Library 1.1//EN";
    public static final String
        TAGLIB_DTD_RESOURCE = "/org/apache/jasper/resources/web-jsptaglib_1_1.dtd";

    /**
     * ID and location of the DTD for web-app deployment descriptors. 
     */
    public static final String 
        WEBAPP_DTD_PUBLIC_ID = "-//Sun Microsystems, Inc.//DTD Web Application 2.2//EN";
    public static final String
        WEBAPP_DTD_RESOURCE = "/org/apache/jasper/resources/web.dtd";
    
    /**
     * Default URLs to download the pluging for Netscape and IE.
     */
    public static final String NS_PLUGIN_URL = 
        "http://java.sun.com/products/plugin/";

    public static final String IE_PLUGIN_URL = 
        "http://java.sun.com/products/plugin/1.2.2/jinstall-1_2_2-win.cab#Version=1,2,2,0";

    /**
     * This is where all our error messages and such are stored. 
     */
    private static StringManager resources;
    
    private static void initResources() {
        resources = StringManager.getManager(
                    "org.apache.jasper.resources");
    }

    /**
     * Get hold of a "message" or any string from our resources
     * database. 
     */
    public static final String getString(String key) {
        return getString(key, null);
    }

    /**
     * Format the string that is looked up using "key" using "args". 
     */
    public static final String getString(String key, Object[] args) {
        if(resources==null){
            initResources();
        }
        return resources.getString(key,args);
    }

    /** 
     * Print a message into standard error with a certain verbosity
     * level. 
     * 
     * @param key is used to look up the text for the message (using
     *            getString()). 
     * @param verbosityLevel is used to determine if this output is
     *                       appropriate for the current verbosity
     *                       level. 
     */
    public static final void message(String key, int verbosityLevel) {
        message(key, null, verbosityLevel);
    }


    /**
     * Print a message into standard error with a certain verbosity
     * level after formatting it using "args". 
     *
     * @param key is used to look up the message. 
     * @param args is used to format the message. 
     * @param verbosityLevel is used to determine if this output is
     *                       appropriate for the current verbosity
     *                       level. 
     */
    public static final void message(String key, Object[] args, int verbosityLevel) {
	    if (jasperLog == null)
	        jasperLog = Log.getLog("JASPER_LOG", null);

	    if (jasperLog != null){
	        String msg = getString(key,args);
	        msg=(msg==null)?key:msg;
	        jasperLog.log(msg, verbosityLevel);
        }
    }

    public static Log jasperLog = null;
}

