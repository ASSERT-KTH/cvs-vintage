/*
 * $Header: /tmp/cvs-vintage/tomcat/src/share/org/apache/jasper/Options.java,v 1.5 1999/12/28 13:25:31 rubys Exp $
 * $Revision: 1.5 $
 * $Date: 1999/12/28 13:25:31 $
 *
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
 */ 

package org.apache.jasper;

import java.io.File;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;

/**
 * A class to hold all init parameters specific to the JSP engine. 
 *
 * @author Anil K. Vijendran
 * @author Hans Bergsten
 */
public final class Options {
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
     * Do you want stack traces and such displayed in the client's
     * browser? If this is false, such messages go to the standard
     * error or a log file if the standard error is redirected. 
     */
    public boolean sendErrorToClient = false;

    /**
     * Current verbosity level. FIXME: This stuff is duplicated: see
     * Constants.jspVerbosityLevel. 
     */
    public int jspVerbosityLevel = Constants.MED_VERBOSITY;

    /**
     * I want to see my generated servlets. Which directory are they
     * in?
     */
    public File scratchDir;
    
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
     * Path of the compiler to use for compiling JSP pages.
     */
    public String jspCompilerPath = null;


    /**
     * Are we keeping generated code around?
     */
    public boolean keepGenerated() {
        return keepGenerated;
    }
    
    /**
     * Are we supporting large files?
     */
    public boolean largeFile() {
        return largeFile;
    }
    
    /**
     * Should errors be sent to client or thrown into stderr?
     */
    public boolean sendErrorToClient() {
        return sendErrorToClient;
    }
 
    /**
     * Class ID for use in the plugin tag when the browser is IE. 
     */
    public String ieClassId() {
        return ieClassId;
    }
    
    /**
     * What is the current verbosity level?
     */
    public int jspVerbosityLevel() {
        return jspVerbosityLevel;
    }

    /**
     * What is my scratch dir?
     */
    public File scratchDir() {
        return scratchDir;
    }

    /**
     * What classpath should I use while compiling the servlets
     * generated from JSP files?
     */
    public String getClassPath() {
        return classpath;
    }

    /**
     * Path of the compiler to use for compiling JSP pages.
     */
    public String getJspCompilerPath() {
	return jspCompilerPath;
    }

    /**
     * Create an Options object using data available from
     * ServletConfig and ServletContext. 
     */
    public Options(ServletConfig config, ServletContext context) {
        String keepgen = config.getInitParameter("keepgenerated");
        if (keepgen != null) {
            if (keepgen.equalsIgnoreCase("true"))
                this.keepGenerated = true;
            else if (keepgen.equalsIgnoreCase("false"))
                this.keepGenerated = false;
            else Constants.message ("jsp.warning.keepgen", Constants.FATAL_ERRORS);
        }
            

        String largeFile = config.getInitParameter("largefile"); 
        if (largeFile != null) {
            if (largeFile.equalsIgnoreCase("true"))
                this.largeFile = true;
            else if (largeFile.equalsIgnoreCase("false"))
                this.largeFile = false;
            else Constants.message ("jsp.warning.largeFile", Constants.FATAL_ERRORS);
        }

        String senderr = config.getInitParameter("sendErrToClient");
        if (senderr != null) {
            if (senderr.equalsIgnoreCase("true"))
                this.sendErrorToClient = true;
            else if (senderr.equalsIgnoreCase("false"))
                this.sendErrorToClient = false;
            else Constants.message ("jsp.warning.sendErrToClient", Constants.FATAL_ERRORS);
        }

        String ieClassId = config.getInitParameter("ieClassId");
        if (ieClassId != null)
            this.ieClassId = ieClassId;

        String classpath = config.getInitParameter("classpath");
        if (classpath != null)
            this.classpath = classpath;

        String verbosityLevel = config.getInitParameter("jspVerbosityLevel");
            
        if (verbosityLevel != null)
            try {
                int vl = Integer.parseInt(verbosityLevel);
                jspVerbosityLevel = vl;
                Constants.jspVerbosityLevel = jspVerbosityLevel;
            } catch (NumberFormatException nex) {
            }

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
                scratchDir = new File(dir);
            }
        }
                
        if (this.scratchDir == null) {
            Constants.message("jsp.error.no.scratch.dir", Constants.FATAL_ERRORS);
            return;
        }
            
        if (!(scratchDir.exists() && scratchDir.canRead() &&
              scratchDir.canWrite() && scratchDir.isDirectory()))
            Constants.message("jsp.error.bad.scratch.dir",
                              new Object[] {
                                  scratchDir.getAbsolutePath()
                              }, Constants.FATAL_ERRORS);
				  
	String jspCompilerPath = config.getInitParameter("jspCompilerPath");
	if (jspCompilerPath != null) {
	    if (new File(jspCompilerPath).exists()) {
		this.jspCompilerPath = jspCompilerPath;
	    } else { 
		Constants.message("jsp.warning.compiler.notfound",
				  new Object[] { jspCompilerPath }, 
				  Constants.FATAL_ERRORS);
	    }
	}
  
    }
}

