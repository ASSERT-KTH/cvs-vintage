/*
 * $Header: /tmp/cvs-vintage/tomcat/src/share/org/apache/jasper/JspC.java,v 1.1 2000/02/07 07:51:17 shemnon Exp $
 * $Revision: 1.1 $
 * $Date: 2000/02/07 07:51:17 $
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

import java.io.*;
import org.apache.jasper.compiler.JspReader;
import org.apache.jasper.compiler.ServletWriter;
import org.apache.jasper.compiler.TagLibraries;
import org.apache.jasper.compiler.Compiler;
import org.apache.jasper.compiler.CommandLineCompiler;

import org.apache.jasper.runtime.JspLoader;

/**
 * Shell for the jspc compiler.  Handles all options associated with the 
 * command line and creates compilation contexts which it then compiles
 * according to the specified options.
 *@author Danno Ferrin
 */
public class JspC implements Options { //, JspCompilationContext {

    public static final String DEFAULT_IE_CLASS_ID = 
            "clsid:8AD9C840-044E-11D1-B3E9-00805F499D93";
    
    public static final String SWITCH_VERBOSE = "-v";
    public static final String SWITCH_QUIET = "-q";
    public static final String SWITCH_OUTPUT_DIR = "-d";
    public static final String SWITCH_IE_CLASS_ID = "-ieplugin";
    public static final String SWITCH_PACKAGE_NAME = "-p";
    public static final String SWITCH_CLASS_NAME = "-c";
    public static final String SWITCH_FULL_STOP = "--";
    public static final String SWITCH_URI_BASE = "-uribase";
    public static final String SWITCH_URI_ROOT = "-uriroot";

    // future direction
    //public static final String SWITCH_XML_OUTPUT = "-xml";
  
    
    boolean largeFile = false;

    int jspVerbosityLevel = Constants.MED_VERBOSITY;

    File scratchDir;

    String ieClassId = DEFAULT_IE_CLASS_ID;

    //String classPath;
    
    String targetPackage;
    
    String targetClassName;

    String uriBase;

    String uriRoot;

    //JspLoader loader;


    public boolean getKeepGenerated() {
        // isn't this why we are running jspc?
        return true;
    }
    
    public boolean getLargeFile() {
        return largeFile;
    }
    
    public boolean getSendErrorToClient() {
        // implied send to System.err
        return true;
    }
 
    public String getIeClassId() {
        return ieClassId;
    }
    
    public int getJspVerbosityLevel() {
        return jspVerbosityLevel;
    }

    public File getScratchDir() {
        return scratchDir;
    }

    //public String getClassPath() {
    //    return classpath;
    //}

    public Class getJspCompilerPlugin() {
       // we don't compile, so this is meanlingless
        return null;
    }

    public String getJspCompilerPath() {
       // we don't compile, so this is meanlingless
        return null;
    }

    public String getClassPath() {
        return System.getProperty("java.class.path");
    }
    
    int argPos;
    // value set by beutifully obsfucscated java
    boolean fullstop = false;
    String args[];

    private void pushBackArg() {
        if (!fullstop) {
            argPos--;
        };
    };

    private String nextArg() {
        if ((argPos >= args.length)
            || (fullstop = SWITCH_FULL_STOP.equals(args[argPos]))) {
            return null;
        } else {
            return args[argPos++];
        }
    };
        
    private String nextFile() {
        if (fullstop) argPos++;
        if (argPos >= args.length) {
            return null;
        } else {
            return args[argPos++];
        }
    };

    public JspC(String[] arg, PrintStream log) {
        args = arg;
        String tok;

        while ((tok = nextArg()) != null) {
            if (tok.equals(SWITCH_QUIET)) {
                jspVerbosityLevel = Constants.WARNING;
            } else if (tok.equals(SWITCH_VERBOSE)) {
                jspVerbosityLevel = Constants.HIGH_VERBOSITY;
            } else if (tok.startsWith(SWITCH_VERBOSE)) {
                try {
                    jspVerbosityLevel = Integer.parseInt(
                            tok.substring(SWITCH_VERBOSE.length()));
                } catch (NumberFormatException nfe) {
                    log.println(
                        "Verbosity level " 
                        + tok.substring(SWITCH_VERBOSE.length()) 
                        + " is not valid.  Option ignored.");
                }
            } else if (tok.equals(SWITCH_OUTPUT_DIR)) {
                tok = nextArg();
                if (tok != null) {
                    scratchDir = new File(tok);
                } else {
                    // either an in-java call with an explicit null
                    // or a "-d --" sequence should cause this,
                    // which would mean default handling
                    /* no-op */
                    scratchDir = null;
                }
            } else if (tok.equals(SWITCH_PACKAGE_NAME)) {
                targetPackage = nextArg();
            } else if (tok.equals(SWITCH_CLASS_NAME)) {
                targetClassName = nextArg();
            } else if (tok.equals(SWITCH_URI_BASE)) {
                uriBase = nextArg();
            } else if (tok.equals(SWITCH_URI_ROOT)) {
                uriRoot = nextArg();
            } else {
                pushBackArg();
                // Not a recognized Option?  Start treting them as JSP Pages
                break;
            }
        }
    };
    
    public void parseFiles(PrintStream log)  throws JasperException {
        if (scratchDir == null) {
            String temp = System.getProperty("java.io.tempdir");
            if (temp == null) {
                temp = "";
            }
            scratchDir = new File(temp);
        }
        String file = nextFile();
        while (file != null) {
            try {
                JspLoader loader =
                        new JspLoader(getClass().getClassLoader(), this);
                CommandLineContext clctxt = new CommandLineContext(
                        loader, getClassPath(), file, uriBase, uriRoot, false,
                        this);
                loader.addJar(clctxt.getRealPath("/WEB-INF/classes"));
                CommandLineCompiler clc = new CommandLineCompiler(clctxt);
                
                clc.compile();
            } catch (JasperException je) {
                je.printStackTrace(System.err);
                System.out.print("error:");
                System.out.println(je.getMessage());
            } catch (Exception e) {
                System.out.print("ERROR:");
                System.out.println(e.toString());
            };
            targetClassName = null;
            file = nextFile();
        }
    };

    public static void main(String arg[]) {
        if (arg.length == 0) {
           System.out.println(Constants.getString("jspc.usage"));
        } else {
            try {
                JspC jspc = new JspC(arg, System.out);
                jspc.parseFiles(System.out);
            } catch (JasperException je) {
                System.err.print("error:");
                System.err.println(je.getMessage());
            }
        }
    };

}

