/*
 * $Header: /tmp/cvs-vintage/tomcat/src/share/org/apache/jasper/compiler/Compiler.java,v 1.26 2004/02/23 02:45:12 billbarker Exp $
 * $Revision: 1.26 $
 * $Date: 2004/02/23 02:45:12 $
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

package org.apache.jasper.compiler;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Hashtable;

import org.apache.jasper.Constants;
import org.apache.jasper.JasperException;
import org.apache.jasper.JspCompilationContext;
import org.apache.tomcat.util.log.Log;

/**
 * If you want to customize JSP compilation aspects, this class is
 * something you should take a look at. 
 * 
 * Hope is that people can just extend Compiler and override things
 * like isOutDated() but inherit things like compile(). This might
 * change. 
 *
 * @author Anil K. Vijendran
 * @author Mandar Raje
 */
public class Compiler {
    protected JavaCompiler javac;
    protected Mangler mangler;
    protected JspCompilationContext ctxt;

    public Compiler(JspCompilationContext ctxt) {
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
        String pkgName = mangler.getPackageName();
        String classFileName = mangler.getClassFileName();

        ctxt.setServletPackageName(pkgName);
        Constants.message("jsp.message.package_name_is",
                          new Object[] { (pkgName==null)?
                                          "[default package]":pkgName },
                          Log.DEBUG);
        Constants.message("jsp.message.class_file_name_is",
                          new Object[] { classFileName },
                          Log.DEBUG);

	if (!isOutDated())
            return false;

	// Hack to avoid readign the class file every time -
	// getClassName() is an _expensive_ operation, and it's needed only
	// if isOutDated() return true. 
        String javaFileName = mangler.getJavaFileName();
        ctxt.setServletJavaFileName(javaFileName);

        Constants.message("jsp.message.java_file_name_is",
                          new Object[] { javaFileName },
                          Log.DEBUG);

	String className = mangler.getClassName();
        ctxt.setServletClassName(className);
        Constants.message("jsp.message.class_name_is",
                          new Object[] { className },
                          Log.DEBUG);

        
        
        // Need the encoding specified in the JSP 'page' directive for
        //  - reading the JSP page
        //  - writing the JSP servlet source
        //  - compiling the generated servlets (pass -encoding to javac).
        // XXX - There are really three encodings of interest.

        String jspEncoding = "ISO-8859-1";          // default per JSP spec

	// We try UTF8 by default. If it fails, we use the java encoding 
	// specified for JspServlet init parameter "javaEncoding".
        String javaEncoding = "UTF8";

	// This seems to be a reasonable point to scan the JSP file
	// for a 'contentType' directive. If it found then the set
	// the value of 'jspEncoding to reflect the value specified.
	// Note: if (true) is convenience programming. It can be
	// taken out once we have a more efficient method.

	if (true) {
	    JspReader tmpReader = JspReader.createJspReader(
							    ctxt.getJspFile(),
							    ctxt,
							    jspEncoding);
	    String newEncode = changeEncodingIfNecessary(tmpReader);
	    if (newEncode != null) jspEncoding = newEncode;
	}

        JspReader reader = JspReader.createJspReader(
            ctxt.getJspFile(),
            ctxt,
            jspEncoding
        );

	OutputStreamWriter osw; 
	try {
	    osw = new OutputStreamWriter(
		      new FileOutputStream(javaFileName),javaEncoding);
	} catch (java.io.UnsupportedEncodingException ex) {
	    // Try to get the java encoding from the "javaEncoding"
	    // init parameter for JspServlet.
	    javaEncoding = ctxt.getOptions().getJavaEncoding();
	    if (javaEncoding != null) {
		try {
		    osw = new OutputStreamWriter(
			      new FileOutputStream(javaFileName),javaEncoding);
		} catch (java.io.UnsupportedEncodingException ex2) {
		    // no luck :-(
		    throw new JasperException(
			Constants.getString("jsp.error.invalid.javaEncoding",
					    new Object[] { 
						"UTF8", 
						javaEncoding,
					    }));
		}
	    } else {
		throw new JasperException(
		    Constants.getString("jsp.error.needAlternateJavaEncoding",
					new Object[] { "UTF8" }));		
	    }
	}
	ServletWriter writer = new ServletWriter(new PrintWriter(osw));

        ctxt.setReader(reader);
        ctxt.setWriter(writer);

        ParseEventListener listener = new JspParseEventListener(ctxt);
        
        Parser p = new Parser(reader, listener);
        listener.beginPageProcessing();
        p.parse();
        listener.endPageProcessing();
        writer.close();

        String classpath = ctxt.getClassPath(); 

        // I'm nuking
        //          System.getProperty("jsp.class.path", ".") 
        // business. If anyone badly needs this we can talk. -akv

        // I'm adding tc_path_add because it solves a real problem
        // and nobody has yet to come up with a better alternative.
        // Note: this is in two places.  Search for tc_path_add below.
        // If you have one, please let me know.  -Sam Ruby

        String sep = System.getProperty("path.separator");
        String[] argv = new String[] 
        {
            "-encoding",
            javaEncoding,
            "-classpath",
	    System.getProperty("java.class.path")+ sep + classpath + sep +
                System.getProperty("tc_path_add") + sep + ctxt.getOutputDir(),
            "-d", ctxt.getOutputDir(),
            javaFileName
        };

        StringBuffer b = new StringBuffer();
        for(int i = 0; i < argv.length; i++) {
            b.append(argv[i]);
            b.append(" ");
        }

        Constants.message("jsp.message.compiling_with",
                          new Object[] { b.toString() },
                          Log.DEBUG);

        /**
         * 256 chosen randomly. The default is 32 if you don't pass
         * anything to the constructor which will be less. 
         */
        ByteArrayOutputStream out = new ByteArrayOutputStream (256);

        // if no compiler was set we can kick out now

        if (javac == null) {
            return true;
        }

        /**
         * Configure the compiler object
         * See comment above: re tc_path_add
         */
        javac.setEncoding(javaEncoding);
        javac.setClasspath( System.getProperty("java.class.path")+ sep + 
                            System.getProperty("tc_path_add") + sep +
                            classpath + sep + ctxt.getOutputDir());
        javac.setOutputDir(ctxt.getOutputDir());
        javac.setMsgOutput(out);
        javac.setClassDebugInfo(ctxt.getOptions().getClassDebugInfo());

        /**
         * Execute the compiler
         */
        boolean status = javac.compile(javaFileName);

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

    public void computeServletClassName() {
	// Hack to avoid readign the class file every time -
	// getClassName() is an _expensive_ operation, and it's needed only
	// if isOutDated() return true. 
	String className = mangler.getClassName();
        ctxt.setServletClassName(className);
        Constants.message("jsp.message.class_name_is",
                          new Object[] { className },
                          Log.DEBUG);
    }
    
    /**
     * This is a protected method intended to be overridden by 
     * subclasses of Compiler. This is used by the compile method
     * to do all the compilation. 
     */
    public boolean isOutDated() {
	return true;
    }
    
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

    /**
     * Change the encoding for the reader if specified.
     */
    public String changeEncodingIfNecessary(JspReader tmpReader)
    throws ParseException {

	// A lot of code replicated from Parser.java
	// Main aim is to "get-it-to-work".
	while (tmpReader.skipUntil("<%@") != null) {

	    tmpReader.skipSpaces();

	    // check if it is a page directive.
	    if (tmpReader.matches("page")) {

		tmpReader.advance(4);
		tmpReader.skipSpaces();
		
		try {
		    Hashtable attrs = tmpReader.parseTagAttributes();
		    String ct = (String) attrs.get("contentType");
		    if (ct != null) {
			int loc = ct.indexOf("charset=");
			if (loc > 0) {
			    String encoding = ct.substring(loc + 8);
			    return encoding;
			}
		    }
		} catch (ParseException ex) {
		    // Ignore the exception here, it will be caught later.
		    return null;
		}
	    }
	}
	return null;
    }

	 /**
	  * Remove generated files
	  */
	 public void removeGeneratedFiles()
	 {
		 try{
			 // XXX Should we delete the generated .java file too?
			 String classFileName = mangler.getClassFileName();
			 if(classFileName != null){
				 File classFile = new File(classFileName);
				 classFile.delete();
			 }
		 }catch(Exception e){
		 }
	 }
}


