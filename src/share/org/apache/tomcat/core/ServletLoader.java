/*
 * $Header: /tmp/cvs-vintage/tomcat/src/share/org/apache/tomcat/core/Attic/ServletLoader.java,v 1.1 1999/10/09 00:30:20 duncan Exp $
 * $Revision: 1.1 $
 * $Date: 1999/10/09 00:30:20 $
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
 * [Additional notices, if required by prior licensing conditions]
 *
 */ 


package org.apache.tomcat.core;

import org.apache.tomcat.util.*;
import java.io.*;
import java.net.*;
import java.util.*;

/**
 * 
 * @author James Duncan Davidson [duncan@eng.sun.com]
 * @author James Todd [gonzo@eng.sun.com]
 */

class ServletLoader
extends ClassLoader {
    private StringManager sm =
        StringManager.getManager(Constants.Package);
    private Container container;
    protected Hashtable classes = new Hashtable();
    
    ServletLoader(Container container) {
	this.container = container;
    }

    synchronized Class loadServlet(ServletWrapper wrapper, String name)
    throws ClassNotFoundException {
	Class clazz = loadClass(name, true);

	// do whatever marking we need to do

	return clazz;
    }

    public InputStream getResourceAsStream(String name) {
        InputStream is = null;
	URL u = getResource(name);

	if (u != null) {
	    try {
	        is = u.openStream();
	    } catch (IOException ioe) {
	    }
	}

	return is;
    }

    public URL getResource(String name) {
        URL u = null;
	URL base = container.getServletBase();

	if (base != null) {
            // try class paths

            u = getResource(base, container.getClassPaths(), name);

            if (u == null) {
                // try libs

                u = getResource(base, container.getLibPaths(), name,
		    true);
            }
	} 

	return u;
    }
    
    protected synchronized Class loadClass(String name, boolean resolve)
    throws ClassNotFoundException {
        Class clazz = null;

        // try the system class loader first

	clazz = loadFromSystemLoader(name);

        // check cache

	if (clazz == null) {
	    clazz = (Class)classes.get(name);
	}

        // try this class loader

	if (clazz == null) {
	    clazz = loadFromLocalLoader(name);
	}

	if (clazz != null) {
	    classes.put(name, clazz);

	    if (resolve) {
	        resolveClass(clazz);
	    }
	}

        return clazz;
    }

    String getClassPath() {
        String cp = "";
        URL base = container.getServletBase();

        if (base != null) {
            Enumeration e =
                getPaths(base, container.getClassPaths()).elements();

            cp = classPathFormat(e);
 
            e = getJars(base, container.getLibPaths()).elements();

            cp += ((cp.length() > 0) ? File.pathSeparator : "") +
                classPathFormat(e);
        }

        return cp;
    }

    private URL getResource(URL base, Enumeration paths, String name) {
        return getResource(base, paths, name, false);
    }

    private URL getResource(URL base, Enumeration paths, String name,
        boolean lib) {
        URL u = null;

        while (paths.hasMoreElements()) {
            String path = (String)paths.nextElement();
	    Vector v = new Vector();

	    if (! lib) {
	        v = getPathURL(base, path, name);
	    } else {
	        v = getJarURLs(base, path, name);
	    }

	    Enumeration enum = v.elements();

	    while (enum.hasMoreElements()) {
	        try {
		    u = (URL)enum.nextElement();
		    u.openStream();

		    break;
		} catch (IOException ioe) {
		    u = null;
		}
	    }
        }

        return u;
    }

    private Class loadFromSystemLoader(String name) {
        Class clazz = null;

	try {
	    ClassLoader parent = container.getClassLoader();

	    if (parent != null) {
	        clazz = parent.loadClass(name);
	    } else { 
	        clazz = findSystemClass(name);
	    }
	} catch (ClassNotFoundException cnfe) {
	}

	return clazz;
    }

    private Class loadFromLocalLoader(String name)
    throws ClassNotFoundException {
	byte[] ba = loadClassData(name);

	return defineClass(name, ba, 0, ba.length);
    }

    private byte[] loadClassData(String name)
    throws ClassNotFoundException {
        byte[] ba = null;
        URL base = container.getServletBase();

	if (base != null) {
            // try class paths

	    ba = loadClassData(base, container.getClassPaths(), name);

            if (ba == null) {
	        // try libs

	        ba = loadClassData(base, container.getLibPaths(), name,
		    true);
            }
	}

	if (ba == null) {
	    String msg = sm.getString("servletLoader.load.cnfe", name);
 
	    throw new ClassNotFoundException(msg);
	}

        return ba;
    }

    private byte[] loadClassData(URL base, Enumeration paths,
        String name) {
        return loadClassData(base, paths, name, false);
    }

    private byte[] loadClassData(URL base, Enumeration paths,
        String name, boolean lib) {
        byte[] ba = null;

        while (paths.hasMoreElements()) {
            String path = (String)paths.nextElement();
            String entryName = name.replace('.', '/') + ".class";
	    Vector v = new Vector();

	    if (! lib) {
	        v = getPathURL(base, path, entryName);
	    } else {
	        v = getJarURLs(base, path, entryName);
	    }

	    Enumeration enum = v.elements();

	    while (enum.hasMoreElements()) {
	        URL u = (URL)enum.nextElement();

		try {
		    ba = getBytes(u);
		} catch (IOException ioe) {
		}

		if (ba != null) {
		    break;
		}
	    }
        }

        return ba;
    }

    private Vector getPathURL(URL base, String path, String entryName) {
        Vector v = new Vector();
	URL u = null;

	try {
	    u = resolveURL(base, path, entryName);
	} catch (MalformedURLException mue) {
	    u = null;
	}

	if (u != null) {
	    v.addElement(u);
	}

	return v;
    }

    private Vector getPaths(URL base, Enumeration paths) {
        Vector v = new Vector();

        while (paths.hasMoreElements()) {
            String path = (String)paths.nextElement();
            URL u = null;

            try {
                u = resolveURL(base, path);
            } catch (MalformedURLException mue) {
            }

            if (u != null) {
                v.addElement(u);
            }
        }

        return v;
    }

    private Vector getJarURLs(URL base, String path, String entryName) {
        Vector v = new Vector();
	URL u = null;

        // XXX
        // this won't work for war
	try {
	    u = resolveURL(base, path);
	} catch (MalformedURLException mue) {
	}

	if (u != null) {
	    File f = new File(u.getFile());

	    if (f.exists() &&
	        f.isDirectory()) {
		String[] jars = getJarFiles(f);

		for (int i = 0; i < jars.length; i++) {
		    String s = f.toString() + File.separator + jars[i];

		    try {
		        URL tURL = new URL(Constants.Request.FILE,
                            null, s);
			URL jURL = new URL(Constants.Request.WAR +
                            ":" + tURL);

			u = resolveURL(jURL, null, entryName);
		    } catch (MalformedURLException mue) {
		        u = null;
		    }

		    if (u != null) {
		        v.addElement(u);
		    }
		}
	    }
	}

	return v;
    }

    private Vector getJars(URL base, Enumeration paths) {
        Vector v = new Vector();

        while (paths.hasMoreElements()) {
            String path = (String)paths.nextElement();
            URL u = null;

            // XXX
            // this won't work for war
            try {
                u = resolveURL(base, path);
            } catch (MalformedURLException mue) {
            }

            if (u != null) {
                File f = new File(u.getFile());

                if (f.exists() &&
                    f.isDirectory()) {
                    String[] jars = getJarFiles(f);

                    for (int i = 0; i < jars.length; i++) {
                        String s = f.toString() + File.separator +
                            jars[i];

                        try {
                            URL tURL = new URL(Constants.Request.FILE,
                                null, s);

                            u = resolveURL(tURL);
                        } catch (MalformedURLException mue) {
                            u = null;
                        }

                        if (u != null) {
                            v.addElement(u);
                        }
                    }
                }
            }
        }

        return v;
    }

    private URL resolveURL(URL base)
    throws MalformedURLException {
        return resolveURL(base, null, null);
    }

    private URL resolveURL(URL base, String path)
    throws MalformedURLException {
        return resolveURL(base, path, null);
    }

    private URL resolveURL(URL base, String path, String name)
    throws MalformedURLException {
        URL u = null;
	String s = "";

	if (path != null &&
	    path.trim().length() > 0) {
	    s += ((s.length() > 0) ? "/" : "") + path.trim();
	}

	if (name != null &&
	    name.trim().length() > 0) {
	    s += ((! s.endsWith("/")) ? "/" : "") + name.trim();
	}

	if (base.getProtocol().equalsIgnoreCase(Constants.Request.WAR)) {
	    u = new URL(base.toString() +
                ((s.length() > 0) ? "!" : "") + s);
	} else {
	    u = new URL(base.toString() +
                ((s.length() > 0) ? "/" : "") + s);
	}

	return u;
    }

    private byte[] getBytes(URL u)
    throws IOException {
	ByteArrayOutputStream baos = new ByteArrayOutputStream();
	InputStream is = u.openConnection().getInputStream();
	byte[] buf = new byte[1024];
	int read = 0;

	while ((read = is.read(buf)) != -1) {
	    baos.write(buf, 0, read);
	}

	is.close();

	return baos.toByteArray();
    }

    private String[] getJarFiles(File f) {
        FilenameFilter filter = new JarFilter("jar");

        return f.list(filter);
    }

    private String classPathFormat(Enumeration e) {
        String cp = "";

        while (e.hasMoreElements()) {
            URL u = (URL)e.nextElement();

            if (u != null) {
                cp += ((cp.length() > 0) ? File.pathSeparator : "") +
                    u.getFile();
// XXX
// may need this for war
//                    u.toString();
            }
        }

        return cp;
    }
}

class JarFilter implements FilenameFilter {
    String extension = "";

    JarFilter(String extension) {
        this.extension = extension;
    }

    public boolean accept(File dir, String name) {
        return (name != null &&
	    name.endsWith("." + this.extension));
    }
}
