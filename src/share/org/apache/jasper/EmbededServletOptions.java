/*
 * $Header: /tmp/cvs-vintage/tomcat/src/share/org/apache/jasper/EmbededServletOptions.java,v 1.10 2004/02/23 03:41:26 billbarker Exp $
 * $Revision: 1.10 $
 * $Date: 2004/02/23 03:41:26 $
 *
 *
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

import java.io.File;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;

import org.apache.tomcat.util.log.Log;

/**
 * A class to hold all init parameters specific to the JSP engine. 
 *
 * @author Anil K. Vijendran
 * @author Hans Bergsten
 */
public final class EmbededServletOptions implements Options {
    /**
     * Do you want to keep the generated Java files around?
     */
    public boolean keepGenerated = true;

    /**
     * Do you want support for "large" files? What this essentially
     * means is that we generated code so that the HTML data in a JSP
     * file is stored separately as opposed to those constant string
     * data being used literally in the generated servlet. 
     */
    public boolean largeFile = false;
    
    /**
     * Do you want support for "mapped" files? This will generate
     * servlet that has a print statement per line of the JSP file.
     * This seems like a really nice feature to have for debugging.
     */
    public boolean mappedFile = false;
    
    /**
     * Do you want stack traces and such displayed in the client's
     * browser? If this is false, such messages go to the standard
     * error or a log file if the standard error is redirected. 
     */
    public boolean sendErrorToClient = false;

    /**
     * Do we want to include debugging information in the class file?
     */
    public boolean classDebugInfo = false;

    /**
     * I want to see my generated servlets. Which directory are they
     * in?
     */
    public File scratchDir;
    /**
     * When used with a Securitymanager, what ProtectionDomain to use.
     */
    private Object protectionDomain;

    
    /**
     * Need to have this as is for versions 4 and 5 of IE. Can be set from
     * the initParams so if it changes in the future all that is needed is
     * to have a jsp initParam of type ieClassId="<value>"
     */
    public String ieClassId = "clsid:8AD9C840-044E-11D1-B3E9-00805F499D93";

    /**
     * What classpath should I use while compiling generated servlets?
     */
    public String classpath = null;
    
    /**
     * Plugin class to use to compile JSP pages.
     */
    public Class jspCompilerPlugin = null;

    /**
     * Path of the compiler to use for compiling JSP pages.
     */
    public String jspCompilerPath = null;

    /**
     * Java platform encoding to generate the JSP
     * page servlet.
     */
    private String javaEncoding;

    /**
     * Are we keeping generated code around?
     */
    public boolean getKeepGenerated() {
        return keepGenerated;
    }
    
    /**
     * Are we supporting large files?
     */
    public boolean getLargeFile() {
        return largeFile;
    }
    
    /**
     * Are we supporting HTML mapped servlets?
     */
    public boolean getMappedFile() {
        return mappedFile;
    }
    
    /**
     * Should errors be sent to client or thrown into stderr?
     */
    public boolean getSendErrorToClient() {
        return sendErrorToClient;
    }
 
    /**
     * Should class files be compiled with debug information?
     */
    public boolean getClassDebugInfo() {
        return classDebugInfo;
    }

    /**
     * Class ID for use in the plugin tag when the browser is IE. 
     */
    public String getIeClassId() {
        return ieClassId;
    }
    
    /**
     * What is my scratch dir?
     */
    public File getScratchDir() {
        return scratchDir;
    }

    /**
     * ProtectionDomain for this JSP Context when using a SecurityManager
     */
    public final Object getProtectionDomain() {
        return protectionDomain;
    }
    /**
     * What classpath should I use while compiling the servlets
     * generated from JSP files?
     */
    public String getClassPath() {
        return classpath;
    }

    /**
     * What compiler plugin should I use to compile the servlets
     * generated from JSP files?
     */
    public Class getJspCompilerPlugin() {
        return jspCompilerPlugin;
    }

    /**
     * Path of the compiler to use for compiling JSP pages.
     */
    public String getJspCompilerPath() {
        return jspCompilerPath;
    }

    public String getJavaEncoding() {
	return javaEncoding;
    }

    /**
     * Create an EmbededServletOptions object using data available from
     * ServletConfig and ServletContext. 
     */
    public EmbededServletOptions(ServletConfig config, ServletContext context) {
        String keepgen = config.getInitParameter("keepgenerated");
        if (keepgen != null) {
            if (keepgen.equalsIgnoreCase("true"))
                this.keepGenerated = true;
            else if (keepgen.equalsIgnoreCase("false"))
                this.keepGenerated = false;
            else Constants.message ("jsp.warning.keepgen", Log.WARNING);
        }
            

        String largeFile = config.getInitParameter("largefile"); 
        if (largeFile != null) {
            if (largeFile.equalsIgnoreCase("true"))
                this.largeFile = true;
            else if (largeFile.equalsIgnoreCase("false"))
                this.largeFile = false;
            else Constants.message ("jsp.warning.largeFile", Log.WARNING);
        }
	
        String mapFile = config.getInitParameter("mappedfile"); 
        if (mapFile != null) {
            if (mapFile.equalsIgnoreCase("true"))
                this.mappedFile = true;
            else if (mapFile.equalsIgnoreCase("false"))
                this.mappedFile = false;
            else Constants.message ("jsp.warning.mappedFile", Log.WARNING);
        }
	
        String senderr = config.getInitParameter("sendErrToClient");
        if (senderr != null) {
            if (senderr.equalsIgnoreCase("true"))
                this.sendErrorToClient = true;
            else if (senderr.equalsIgnoreCase("false"))
                this.sendErrorToClient = false;
            else Constants.message ("jsp.warning.sendErrToClient", Log.WARNING);
        }

        String debugInfo = config.getInitParameter("classdebuginfo");
        if (debugInfo != null) {
            if (debugInfo.equalsIgnoreCase("true"))
                this.classDebugInfo  = true;
            else if (debugInfo.equalsIgnoreCase("false"))
                this.classDebugInfo  = false;
            else Constants.message ("jsp.warning.classDebugInfo", Log.WARNING);
        }

        String ieClassId = config.getInitParameter("ieClassId");
        if (ieClassId != null)
            this.ieClassId = ieClassId;

        String classpath = config.getInitParameter("classpath");
        if (classpath != null)
            this.classpath = classpath;

        String dir = config.getInitParameter("scratchdir"); 

        if (dir != null)
            scratchDir = new File(dir);
        else {
            // First we try the Servlet 2.2 javax.servlet.context.tempdir property
            scratchDir = (File) context.getAttribute(Constants.TMP_DIR);
            if (scratchDir == null) {
                // Not running in a Servlet 2.2 container.
                // Try to get the JDK 1.2 java.io.tmpdir property
                dir = System.getProperty("java.io.tmpdir");
                if (dir != null)
                    scratchDir = new File(dir);
            }
        }
                
        // Get the ProtectionDomain for this Context in case
        // we are using a SecurityManager
        protectionDomain = context.getAttribute(Constants.ATTRIB_JSP_ProtectionDomain);
        if (this.scratchDir == null) {
            Constants.message("jsp.error.no.scratch.dir", Log.FATAL);
            return;
        }
            
        if (!(scratchDir.exists() && scratchDir.canRead() &&
              scratchDir.canWrite() && scratchDir.isDirectory()))
            Constants.message("jsp.error.bad.scratch.dir",
                              new Object[] {
                                  scratchDir.getAbsolutePath()
                              }, Log.FATAL);
                                  
        String jspCompilerPath = config.getInitParameter("jspCompilerPath");
        if (jspCompilerPath != null) {
            if (new File(jspCompilerPath).exists()) {
                this.jspCompilerPath = jspCompilerPath;
            } else { 
                Constants.message("jsp.warning.compiler.path.notfound",
                                  new Object[] { jspCompilerPath }, 
                                  Log.FATAL);
            }
        }

        String jspCompilerPlugin = config.getInitParameter("jspCompilerPlugin");
        if (jspCompilerPlugin != null) {
            try {
                this.jspCompilerPlugin = Class.forName(jspCompilerPlugin);
            } catch (ClassNotFoundException cnfe) {
                Constants.message("jsp.warning.compiler.class.notfound",
                                  new Object[] { jspCompilerPlugin },
                                  Log.FATAL);
            }
        }

        this.javaEncoding = config.getInitParameter("javaEncoding");
    }
}

