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
package org.apache.tomcat.loader;

import org.apache.tomcat.util.*;
import org.apache.tomcat.core.*;
import java.io.*;
import java.net.*;
import java.util.*;

/**
 * This class now extends NetworkClassLoader. Previous
 * implementation of GenericClassLoader was called ServletLoader.
 * This implementation is a complete rewrite of the earlier
 * class loader. This should speed up performance compared
 * to the earlier class loader.
 *
 * Changed to use ServletLoader interface ( costin )
 *
 * @author Harish Prabandham
 */
public class ServletClassLoaderImpl extends NetworkClassLoader implements ServletLoader  {
    Vector classP;

    public ServletClassLoaderImpl() {
	super(null); // Who is the parent ??
	// this class will not be used as a class loader, it's just a trick for
	// protected loadClass()
	classP=new Vector();
    }

    
    /** Check if we need to reload one particular class.
     *  No check is done for dependent classes.
     *  The final decision about reloading is left to the caller.
     */
    public boolean shouldReload( String className ) {
	return false;
    }

    
    /** Check if we need to reload. All loaded classes are
     *  checked.
     *  The final decision about reloading is left to the caller.
     */
    public boolean shouldReload() {
	return false;
    }

    

    /** Reset the class loader. The caller should take all actions
     *  required by this step ( free resources for GC, etc)
     */
    public void reload() {
    }
		

    /** Return a real class loader
     */
    public ClassLoader getClassLoader() {
	return this;
    }

    
    /** Handle servlet loading. Same as getClassLoader().loadClass(name, true); 
     */
    public Class loadClass( String name)
	throws ClassNotFoundException
    {
	return loadClass(name, true);
    }


    /** Return the class loader view of the class path
     */
    public String getClassPath() {
        String separator = System.getProperty("path.separator", ":");
        String cpath = "";

        for(Enumeration e = classP.elements() ; e.hasMoreElements(); ) {
            File f = (File) e.nextElement();
            if (cpath.length()>0) cpath += separator;
	    cpath += f;
        }

        return cpath;
    }


    /** Add a new directory or jar to the class loader.
     *  Not all loaders can add resources dynamically -
     *  that may require a reload.
     */
    public void addRepository( File f ) {
	try {
	    classP.addElement(new File(FileUtil.patch(f.getCanonicalPath())));

	    String path=f.getCanonicalPath();
	    // NetworkClassLoader will use the last char to
	    // decide if it's a directory or a jar.
	    // X  Can we change that ?
	    if( ! path.endsWith("/") && f.isDirectory() )
		path=path+"/";
	    
	    URL url=new URL( "file", "", path);
	    //	    System.out.println("Adding " + url );
	    addURL( url );
	} catch( MalformedURLException ex) {
	    ex.printStackTrace();
	} catch( IOException ex1) {
	    ex1.printStackTrace();
	}
    }

    /** Add a new remote repository. Not all class loader will
     *  support remote resources, use File if it's a local resource.
     */
    public void addRepository( URL url ) {
	return;// no support for URLs in AdaptiveClassLoader
    }    
}
