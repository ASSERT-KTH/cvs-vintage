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

package org.apache.jasper.servlet;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.jasper.Constants;
import org.apache.jasper.JasperException;

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
public class JasperLoader extends org.apache.jasper.runtime.JspLoader {
//     ClassLoader parent;
//     Options options;
    Object pd;

    /*
     * This should be factoried out
     */
    public JasperLoader() {
	super();
    }

//     public void setParentClassLoader( ClassLoader cl) 
//     {
// 	this.parent = cl;
//     }
    
//     public void setOptions( Options options) {
// 	this.options = options;
//     }

    public void setProtectionDomain( Object pd ) {
	this.pd=pd;
    }
    
    protected synchronized Class loadClass(String name, boolean resolve)
	throws ClassNotFoundException
    {
	if( debug>0) log("load " + name );
	// First, check if the class has already been loaded
	Class c = findLoadedClass(name);
	if (c == null) {
	    try {
		if (parent != null) {
		    if(debug>0) log("load from parent " + name );
		    c = parent.loadClass(name);
                }
		else {
		    if(debug>0) log("load from system " + name );
		    c = findSystemClass(name);
                }
	    } catch (ClassNotFoundException e) {
		// If still not found, then call findClass in order
		// to find the class.
		try {
		    if(debug>0) log("local jsp loading " + name );
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
    public InputStream getResourceAsStream(String name) {
	if( debug>0) log("getResourcesAsStream()" + name );
	URL url = getResource(name);
	try {
	    return url != null ? url.openStream() : null;
	} catch (IOException e) {
	    return null;
	}
    }
    
    public URL getResource(String name) {
	if( debug>0) log( "getResource() " + name );
	if( parent != null )
	    return parent.getResource(name);
	return super.getResource(name);
    }

    private static final int debug=0;

    private void log( String s ) {
	System.out.println("JspLoader: " + s );
    }
    
    protected Class findClass(String className) throws ClassNotFoundException {
	try {
	    int beg = className.lastIndexOf(".") == -1 ? 0 :
		className.lastIndexOf(".")+1;
	    int end = className.lastIndexOf("_jsp_");

	    if (end <= 0) {     
                // this is a class that the JSP file depends on 
                // (example: class in a tag library)
                byte[] classBytes = loadClassDataFromJar(className);
                if (classBytes == null)
                    throw new ClassNotFoundException(className);
                return defClass(className, classBytes);
	    } else {
                String fileName = null;
                String outputDir = options.getScratchDir().toString();
            
                if (className.indexOf('$', end) != -1) {
                    // this means we're loading an inner class
                    fileName = outputDir + File.separatorChar + 
                        className.replace('.', File.separatorChar) + ".class";
                } else {
                    fileName = className.substring(beg, end) + ".class";
                    fileName = outputDir + File.separatorChar + fileName;
                }
                byte [] classBytes = null;
                /**
                 * When using a SecurityManager and a JSP page itself triggers
                 * another JSP due to an errorPage or from a jsp:include,
                 * the loadClass must be performed with the Permissions of
                 * this class using doPriviledged because the parent JSP
                 * may not have sufficient Permissions.
                 */
		classBytes = loadClassDataFromFile(fileName);
                if( classBytes == null ) {
                    throw new ClassNotFoundException(Constants.getString(
                                             "jsp.error.unable.loadclass", 
                                              new Object[] {className})); 
                }
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
    protected  Class defClass(String className, byte[] classData) {
        return defineClass(className, classData, 0, classData.length);
    }

    /**
     * Load JSP class data from file, method may be called from
     * within a doPriviledged if a SecurityManager is installed.
     */
    protected byte[] loadClassDataFromFile(String fileName) {
	return doLoadClassDataFromFile( fileName );
    }

    /**
     * Load JSP class data from file, method may be called from
     * within a doPriviledged if a SecurityManager is installed.
     */
    protected byte[] doLoadClassDataFromFile(String fileName) {
        byte[] classBytes = null;
        try {
            FileInputStream fin = new FileInputStream(fileName);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte buf[] = new byte[1024];
            for(int i = 0; (i = fin.read(buf)) != -1; )
                baos.write(buf, 0, i);
            fin.close();
            baos.close();
            classBytes = baos.toByteArray();
        } catch(Exception ex) {
            return null;
        }
        return classBytes;
    }

//     private Vector jars = new Vector();
    
    private byte[] loadClassDataFromJar(String className) {
        String entryName = className.replace('.','/')+".class";
	InputStream classStream = null;
	//System.out.println("Loading " + className);

        for(int i = 0; i < jars.size(); i++) {
            File thisFile = new File((String) jars.elementAt(i));
            try {
                //System.out.println(" - trying " + thisFile.getAbsolutePath());
                // check if it exists...
                if (!thisFile.exists()) {
                    continue; 
                };
                
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
                    File classFile = 
                        new File(thisFile,
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

    public String toString() {
        Object obj = (options==null)?null: options.getScratchDir();
        String s = (obj==null)?"null":obj.toString();
	    return "JspLoader@"+hashCode()+"( " + s  + " ) / " + parent;
    }

    boolean loadJSP(JspServlet jspS, String name, String classpath, 
		    boolean isErrorPage, HttpServletRequest req,
		    HttpServletResponse res) 
	throws JasperException, FileNotFoundException 
    {
	return jspS.doLoadJSP( name, classpath, isErrorPage, req, res );
    }

}
