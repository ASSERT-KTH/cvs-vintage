/*
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001-2002 The Apache Software Foundation.  All rights
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
 * 4. The names "The Jakarta Project", "Ant", and "Apache Software
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
 */

package org.apache.tomcat.ant;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.CommandlineJava;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.taskdefs.optional.jsp.JspC;
import org.apache.tools.ant.taskdefs.optional.jsp.JspMangler;
import org.apache.tools.ant.taskdefs.optional.jsp.JspNameMangler;
import org.apache.tools.ant.taskdefs.optional.jsp.compilers.DefaultJspCompilerAdapter;
import org.apache.tools.ant.taskdefs.Java;

import java.io.File;
import java.util.Enumeration;
import java.util.Vector;

/**
 * This compiler adaptor builds jsps with a naming convention that is
 * ready-to-use by Tomcat 3.x.  Files are compiled from <i>foo.jsp</i>
 * to <i>foo_1.java</i>.
 *
 * <p>Use this task with the Tomcat3JSPVersionFile to create the appropriate
 * files to pre-populate the Tomcat work directory.
 *
 * @author Keith Wannamaker <a href="mailto:Keith@Apache.org">Keith@Apache.org</a>
 * @since ant1.6
 */
public class Tomcat3Precompiler extends DefaultJspCompilerAdapter {
    /**
     * our execute method which does dependency checks
     */
    public boolean execute()
        throws BuildException {
        getJspc().log("Using Tomcat 3 precompiler", Project.MSG_VERBOSE);

        Vector sources = getJspc().getCompileList();
        Enumeration enum = sources.elements();
        while (enum.hasMoreElements()) {
            CommandlineJava cmd = setupJasperCommand();
            String source = (String) enum.nextElement();
            String base = getBase(source);
            String classname = base + "_1";
            addArg(cmd, "-c", classname);
            cmd.createArgument().setValue(source);
            getJspc().log(
                  "Compiling file [" + source + "] to class [" + classname + "]",
                  Project.MSG_VERBOSE);
            compile(cmd);
        }
        return true;
    }

    /** Execute Jasper */
    private boolean compile(CommandlineJava cmd) throws BuildException {        
        try {
            // Create an instance of the compiler, redirecting output to
            // the project log
            // REVISIT. ugly. 
            Java java = (Java) (getJspc().getProject()).createTask("java");
            if (getJspc().getClasspath() != null) {
                java.setClasspath(getJspc().getClasspath());
            } else {
                java.setClasspath(Path.systemClasspath);
            }
            java.setClassname("org.apache.jasper.JspC");
            String args[] = cmd.getJavaCommand().getArguments();
            for (int i = 0; i < args.length; i++) {
                java.createArg().setValue(args[i]);
            }
            java.setFailonerror(getJspc().getFailonerror());
            //fork to catch JspC CompileExceptions
            java.setFork(true);
            java.execute();
            return true;
        } catch (Exception ex) {
            //@todo implement failonerror support here?
            if (ex instanceof BuildException) {
                throw (BuildException) ex;
            } else {
                throw new BuildException("Error running jsp compiler: ",
                                         ex, getJspc().getLocation());
            }
        } finally {
            getJspc().deleteEmptyJavaFiles();
        }
    }
    
    /**
     * Returns filename sans extension and prefix
     */
    private String getBase(String filename) {
        int lastslash = filename.lastIndexOf(File.separator);
        if (lastslash == -1) {
            lastslash = 0;
        }
        int lastdot = filename.lastIndexOf('.');
        if (lastdot == -1) {
            lastdot = filename.length();
        }
        return filename.substring(lastslash + 1, lastdot);
    }

    /**
     * build up a command line
     * @return a command line for jasper
     */
    private CommandlineJava setupJasperCommand() {
        CommandlineJava cmd = new CommandlineJava();
        JspC jspc = getJspc();
        addArg(cmd, "-d", jspc.getDestdir());
        addArg(cmd, "-p", jspc.getPackage());
        addArg(cmd, "-v" + jspc.getVerbose());
        addArg(cmd, "-uriroot", jspc.getUriroot());
        addArg(cmd, "-ieplugin", jspc.getIeplugin());
        addArg(cmd, "-die9");

        if (jspc.isMapped()){
            addArg(cmd, "-mapped");
        }       
        if (jspc.getWebApp() != null) {
            File dir = jspc.getWebApp().getDirectory();
            addArg(cmd, "-webapp", dir);
        }
        return cmd;
    }

    /**
     * @return an instance of the mangler this compiler uses
     */
    public JspMangler createMangler() {
        return new TomcatJSPMangler();
    }

    /**
     * Special mangler which coverts *.jsp -> *_1.java 
     */
    private class TomcatJSPMangler implements JspMangler {
        public TomcatJSPMangler() {
            mangler = new JspNameMangler();
        }

        public String mapJspToJavaName(File jspFile) {
            String javaFile = mangler.mapJspToJavaName(jspFile);
            int ext = javaFile.lastIndexOf(".java");
            if (ext != -1) {
              javaFile = javaFile.substring(0, ext) + "_1.java";
            }
            return javaFile;
        }

        public String mapPath(String path) {
            return mangler.mapPath(path);
        }

        private JspMangler mangler;
    }

}
