/*
 * $Header: /tmp/cvs-vintage/tomcat/src/share/org/apache/jasper/compiler/Compiler.java,v 1.3 1999/11/03 23:43:02 costin Exp $
 * $Revision: 1.3 $
 * $Date: 1999/11/03 23:43:02 $
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
package org.apache.jasper.compiler;

import java.io.FileNotFoundException;
import java.io.File;
import java.io.PrintWriter;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;

import org.apache.jasper.JspEngineContext;
import org.apache.jasper.Constants;
import org.apache.jasper.JasperException;

/**
 * If you want to customize JSP compilation aspects, this class is
 * something you should take a look at. 
 * 
 * Hope is that people can just extend Compiler and override things
 * like isOutDated() but inherit things like compile(). This might
 * change. 
 *
 * @author Anil K. Vijendran
 */
public abstract class Compiler {
    protected JavaCompiler javac;
    protected Mangler mangler;
    protected JspEngineContext ctxt;

    protected Compiler(JspEngineContext ctxt) {
        this.ctxt = ctxt;
    }
    
    /** 
     * Compile the jsp file from the current engine context
     *
     * @return true if the class file was outdated the jsp file
     *         was recompiled. 
     */
    public boolean compile()
        throws FileNotFoundException, JasperException, Exception 
    {
        if (!isOutDated())
            return false;
        
        String pkgName = mangler.getPackageName();
        String className = mangler.getClassName();
        String javaFileName = mangler.getJavaFileName();
        String classFileName = mangler.getClassFileName();

        Constants.message("jsp.message.package_name_is",
                          new Object[] { pkgName },
                          Constants.MED_VERBOSITY);
        Constants.message("jsp.message.class_name_is",
                          new Object[] { className },
                          Constants.MED_VERBOSITY);
        Constants.message("jsp.message.java_file_name_is",
                          new Object[] { javaFileName },
                          Constants.MED_VERBOSITY);
        Constants.message("jsp.message.class_file_name_is",
                          new Object[] { classFileName },
                          Constants.MED_VERBOSITY);

        JspReader reader = JspReader.createJspReader(ctxt.getJspFile(), ctxt.getServletContext());

        ServletWriter writer = 
            (new ServletWriter
                (new PrintWriter
                    (new EscapeUnicodeWriter
                        (new FileOutputStream(javaFileName)))));

        ctxt.setReader(reader);
        ctxt.setWriter(writer);
        ctxt.setServletClassName(className);
        ctxt.setServletPackageName(pkgName);
        ctxt.setServletJavaFileName(javaFileName);
        
        ParseEventListener listener = new JspParseEventListener(ctxt);
        
        Parser p = new Parser(reader, listener);
        listener.beginPageProcessing();
        p.parse();
        listener.endPageProcessing();
        writer.close();

        // For compiling the generated servlets, you need to 
        // pass -encoding to javac.

        String encoding = ctxt.getContentType();
        String classpath = ctxt.getClassPath(); 

        // Pick up everything after ";charset="
        if (encoding != null) {
            int semi = encoding.indexOf(";");
            if (semi == -1)
                encoding = null;
            else {
                String afterSemi = encoding.substring(semi+1);
                int charsetLocation = afterSemi.indexOf("charset=");
                if (charsetLocation == -1)
                    encoding = null;
                else {
                    String afterCharset = afterSemi.substring(charsetLocation+8);
                    encoding = afterCharset.trim();
                }
            }
        }

        // I'm nuking
        //          System.getProperty("jsp.class.path", ".") 
        // business. If anyone badly needs this we can talk. -akv

        String sep = System.getProperty("path.separator");
        String[] argv = new String[] 
        {
            "-classpath",
            System.getProperty("java.class.path")+ sep + classpath 
            + sep + ctxt.getOutputDir(),
            "-d", ctxt.getOutputDir(),
            javaFileName
        };

        if (encoding != null && !encoding.equals("")) {
            String[] args = new String[argv.length+2];
            args[0] = "-encoding";
            args[1] = encoding;
            for(int i = 0; i < argv.length; i++)
                args[i+2] = argv[i];
            argv = args;
        }

        StringBuffer b = new StringBuffer();
        for(int i = 0; i < argv.length; i++) {
            b.append(argv[i]);
            b.append(" ");
        }

        Constants.message("jsp.message.compiling_with",
                          new Object[] { b.toString() },
                          Constants.MED_VERBOSITY);

        /**
         * 256 chosen randomly. The default is 32 if you don't pass
         * anything to the constructor which will be less. 
         */
        ByteArrayOutputStream out = new ByteArrayOutputStream (256);
    
        javac.setOut(out);
        boolean status = javac.compile(argv);

        if (!ctxt.keepGenerated()) {
            File javaFile = new File(javaFileName);
            javaFile.delete();
        }
    
        if (status == false) {
            String msg = out.toString ();
            throw new JasperException(Constants.getString("jsp.error.unable.compile")
                                      + msg);
        }

        String classFile = ctxt.getOutputDir() + File.separatorChar;
        if (pkgName != null && !pkgName.equals(""))
            classFile = classFile + pkgName.replace('.', File.separatorChar) + 
                File.separatorChar;
        classFile = classFile + className + ".class";

        if (!classFile.equals(classFileName)) {
            File classFileObject = new File(classFile);
            File myClassFileObject = new File(classFileName);
            if (myClassFileObject.exists())
                myClassFileObject.delete();
            if (classFileObject.renameTo(myClassFileObject) == false)
                throw new JasperException(Constants.getString("jsp.error.unable.rename",
                                                              new Object[] { 
                                                                  classFileObject, 
                                                                  myClassFileObject
                                                              }));
        }

        return true;
    }
    
    /**
     * This is a protected method intended to be overridden by 
     * subclasses of Compiler. This is used by the compile method
     * to do all the compilation. 
     */
    protected abstract boolean isOutDated();
    
    /**
     * Set java compiler info
     */
    public void setJavaCompiler(JavaCompiler javac) {
        this.javac = javac;
    }

    /**
     * Set Mangler which will be used as part of compile().
     */
    public void setMangler(Mangler mangler) {
        this.mangler = mangler;
    }
}
