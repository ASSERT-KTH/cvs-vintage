/*
 * $Header: /tmp/cvs-vintage/tomcat/src/share/org/apache/jasper/runtime/JspLoader.java,v 1.3 1999/12/21 12:34:31 rubys Exp $
 * $Revision: 1.3 $
 * $Date: 1999/12/21 12:34:31 $
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

package org.apache.jasper.runtime;

import java.io.FileInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.IOException;

import java.util.Hashtable;
import java.util.Vector;
import java.util.zip.ZipFile;
import java.util.zip.ZipEntry;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.jasper.JasperException;
import org.apache.jasper.Constants;
import org.apache.jasper.JspEngineContext;
import org.apache.jasper.Options;
import org.apache.jasper.compiler.Compiler;

/**
 * This is a class loader that loads JSP files as though they were
 * Java classes. It calls the compiler to compile the JSP file into a
 * servlet and then loads the generated class. 
 *
 * This code is quite fragile and needs careful
 * treatment/handling/revisiting. I know this doesn't work very well
 * right now for:  
 * 
 * 	(a) inner classes
 *	(b) does not work at all for tag handlers that have inner
 *          classes; but that is likely to change with the new JSP PR2
 *          spec. 
 *
 * @author Anil K. Vijendran
 * @author Harish Prabandham
 */
public class JspLoader extends ClassLoader {
    Hashtable loadedJSPs = new Hashtable();
    ClassLoader parent;
    ServletContext context;
    Options options;
    

    JspLoader(ServletContext context, 
              ClassLoader cl, Options options)
    {
	//	super(cl); // JDK 1.2 FIXME
	super();
        this.context = context;
	this.parent = cl;
        this.options = options;
    }

    public Class getJspServletClass(String name) {
	return (Class) loadedJSPs.get(name);
    }

    protected synchronized Class loadClass(String name, boolean resolve)
	throws ClassNotFoundException
    {
	// First, check if the class has already been loaded
	Class c = findLoadedClass(name);
	if (c == null) {
	    try {
		//System.out.println("JspLoader: parent = " + parent);
		if (parent != null) {
		    c = parent.loadClass(name);
                }
		else {
		    c = findSystemClass(name);
                }
	    } catch (ClassNotFoundException e) {
		// If still not found, then call findClass in order
		// to find the class.
		try {
		    c = findClass(name);
		} catch (ClassNotFoundException ex) {
		    throw ex;
		}
	    }
	}
	if (resolve) {
	    resolveClass(c);
	}
	return c;
    }

    protected Class findClass(String className) throws ClassNotFoundException {
	try {
	    int beg = className.lastIndexOf(".") == -1 ? 0 :
		className.lastIndexOf(".")+1;
	    int end = className.lastIndexOf("_jsp_");

	    if (end <= 0) {     // this is a class that the JSP file depends on (example: class
                                // in a tag library)
                byte[] classBytes = loadClassDataFromJar(className);
                if (classBytes == null)
                    throw new ClassNotFoundException(className);
                return defClass(className, classBytes);
	    } else {
                String fileName = null;
                String outputDir = options.scratchDir().toString();
            
                if (className.indexOf('$', end) != -1) {
                    // this means we're loading an inner class
                    fileName = outputDir + File.separatorChar + 
                        className.replace('.', File.separatorChar) + ".class";
                } else {
                    fileName = className.substring(beg, end) + ".class";
                    fileName = outputDir + File.separatorChar + fileName;
                }
                FileInputStream fin = new FileInputStream(fileName);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte buf[] = new byte[1024];
                for(int i = 0; (i = fin.read(buf)) != -1; )
                    baos.write(buf, 0, i);
                fin.close();
                baos.close();
                byte[] classBytes = baos.toByteArray();
                return defClass(className, classBytes);
            }
	} catch (Exception ex) {
	    throw new ClassNotFoundException(Constants.getString(
	    				     "jsp.error.unable.loadclass", 
					      new Object[] {className}));
	}
    }

    /**
     * Just a short hand for defineClass now... I suspect we might need to
     * make this public at some point of time. 
     */
    private final Class defClass(String className, byte[] classData) {
        return defineClass(className, classData, 0, classData.length);
    }

    /*  Check if we need to reload a JSP page.
     *
     *  Side-effect: re-compile the JSP page.
     *
     *  @param classpath explicitly set the JSP compilation path.
     *  @return true if JSP files is newer
     */
    public synchronized boolean loadJSP(String name, String classpath, 
	boolean isErrorPage, HttpServletRequest req, HttpServletResponse res) 
	throws JasperException, FileNotFoundException 
    {
	Class jspClass = (Class) loadedJSPs.get(name);
	boolean firstTime = jspClass == null;

        JspEngineContext ctxt = new JspEngineContext(this, classpath,
                                                     context, name, 
                                                     isErrorPage, options,
                                                     req, res);
	boolean outDated = false; 

        Compiler compiler = ctxt.createCompiler();
        
        try {
            outDated = compiler.compile();
        } catch (FileNotFoundException ex) {
            throw ex;
        } catch (JasperException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new JasperException(Constants.getString("jsp.error.unable.compile"),
                                      ex);
        }

	// Reload only if it's outdated
	if((jspClass == null) || outDated) {
	    try {
		jspClass = loadClass(ctxt.getFullClassName(), true);
	    } catch (ClassNotFoundException cex) {
		throw new JasperException(Constants.getString("jsp.error.unable.load"), 
					  cex);
	    }
	    loadedJSPs.put(name, jspClass);
	}
	
	return outDated;
    }

    private Vector jars = new Vector();
    
    private byte[] loadClassDataFromJar(String className) {
        String entryName = className.replace('.','/')+".class";
	InputStream classStream = null;
        
        for(int i = 0; i < jars.size(); i++) {
            File thisFile = new File((String) jars.elementAt(i));
            try {
                // check if it exists...
                if (!thisFile.exists())
                    return null;
                
                if (thisFile.isFile()) {
                    ZipFile zip = new ZipFile(thisFile);
                    ZipEntry entry = zip.getEntry(entryName);
                    if (entry != null) {
			classStream = zip.getInputStream(entry);
                        byte[] classData = getClassData(classStream);
			zip.close();
			return classData;
		    } else {
			zip.close();
		    }
                } else { // its a directory...
                    File classFile = new File(thisFile,
                                              entryName.replace('/', File.separatorChar));
                    if (classFile.exists()) {
                        classStream = new FileInputStream(classFile);
                        byte[] classData = getClassData(classStream);
                        classStream.close();
                        return classData;
                    }
                }
            } catch (IOException ioe) {
                return null;
            }
        }
        
        return null;
    }

    private byte[] getClassData(InputStream istream) throws IOException {
	byte[] buf = new byte[1024];
	ByteArrayOutputStream bout = new ByteArrayOutputStream();
	int num = 0;
	while((num = istream.read(buf)) != -1) {
	    bout.write(buf, 0, num);
	}

	return bout.toByteArray();
    }

    
    public void addJar(String jarFileName) throws IOException {
        if (!jars.contains(jarFileName)) {
            Constants.message("jsp.message.adding_jar",
                              new Object[] { jarFileName },
                              Constants.MED_VERBOSITY);
            
            jars.addElement(jarFileName);
        }
    }
    
    public String getClassPath() {
        StringBuffer cpath = new StringBuffer();
        String sep = System.getProperty("path.separator");

        for(int i = 0; i < jars.size(); i++) {
            cpath.append((String)jars.elementAt(i)+sep);
        }
        
        return cpath.toString();
    }
}
