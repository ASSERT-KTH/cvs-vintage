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


package org.apache.tomcat.webdav.resources;


import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import org.apache.tomcat.webdav.util.StringManager;

/**
 * Implementation of the <b>Resources</b> that decompresses and renders
 * entries from a JAR file that is located either locally or remotely.
 * Valid syntax for the <code>docBase</code> property corresponds to the
 * syntax supported by the <code>java.net.JarURLConnection</code> class,
 * and is illustrated by the following examples:
 * <ul>
 * <li><b>jar:file:/path/to/filename.jar!/</b> - Uses the JAR file
 *     <code>/path/to/filename.jar</code> as the source of resources.
 * <li><b>jar:http://www.foo.com/bar/baz.jar!/</b> - Uses the JAR file
 *     retrieved by doing an HTTP GET operation on the URL
 *     <code>http://www.foo.com/bar/baz.jar</code> (which makes this
 *     instance of Catalina serve as a proxy for the specified application).
 * </ul>
 * In all cases, the <code>docBase</code> you specify must begin with
 * <code>jar:</code>.  If your <code>docBase</code> value does not end with
 * "!/", this will be added for you.
 * <p>
 * <strong>IMPLEMENTATION NOTE</strong>:  It is assumed that the underlying
 * JAR file itself will not be modified without restarting this web application
 * (or at least this <code>Resources</code> implementation).  Therefore, the
 * set of directory and resource entries is pre-loaded into our resource cache.
 * The actual data associated with these resources is not cached until it is
 * requested the first time (and passes the "cacheable" test).
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.1 $ $Date: 2000/11/03 21:27:41 $
 */

public final class JarResources extends ResourcesBase {


    // ----------------------------------------------------- Instance Variables

    // title used when rendering directory resources ( no other use )
    String contextPath;
    public static String serverInfo="Apache Tomcat/3.3";
    // XXX add code to set it

    /**
     * The URLConnection to our JAR file.
     */
    protected JarURLConnection conn = null;


    /**
     * The descriptive information string for this implementation.
     */
    protected static final String info =
	"org.apache.catalina.resources.JarResources/1.0";


    /**
     * The JarFile object associated with our document base.
     */
    protected JarFile jarFile = null;


    // ------------------------------------------------------------- Properties


    /**
     * Set the document root for this component.
     *
     * @param docBase The new document root
     *
     * @exception IllegalArgumentException if the specified value is not
     *  supported by this implementation
     * @exception IllegalArgumentException if this would create a
     *  malformed URL
     */
    public void setDocBase(String docBase) {

	// Validate the format of the proposed document root
	if (docBase == null)
	    throw new IllegalArgumentException
		(sm.getString("resources.null"));
	if (!docBase.startsWith("jar:"))
	    throw new IllegalArgumentException
		(sm.getString("jarResources.syntax", docBase));
	if (!docBase.endsWith("!/"))
	    docBase += "!/";

	// Close any previous JAR that we have opened
	if (jarFile != null) {
	    try {
		jarFile.close();
	    } catch (IOException e) {
		log("Closing JAR file", e);
	    }
	    jarFile = null;
	    conn = null;
	}

	// Open a URLConnection to the specified JAR file
	try {
	    URL url = new URL(docBase);
	    conn = (JarURLConnection) url.openConnection();
	    conn.setAllowUserInteraction(false);
	    conn.setDoInput(true);
	    conn.setDoOutput(false);
	    conn.connect();
	    jarFile = conn.getJarFile();
	} catch (Exception e) {
	    log("Establishing connection", e);
	    throw new IllegalArgumentException
		(sm.getString("resources.connect", docBase));
	}

	// Populate our cache of directory and resource entries
	populate();

	// Perform the standard superclass processing
	super.setDocBase(docBase);

    }

    public void setContextPath( String cp ) {
	contextPath=cp;
    }

    // --------------------------------------------------------- Public Methods


    /**
     * Return the real path for a given virtual path, or <code>null</code>
     * if no such path can be identified.
     *
     * @param path Context-relative path starting with '/'
     *
     * @exception IllegalArgumentException if the path argument is null
     *  or does not start with a '/'
     */
    public String getRealPath(String path) {

	ResourceUtils.validate(path);
	return (null);	// JAR entries do not have a real path

    }


    /**
     * Return a URL to the resource specified by the given virtual path,
     * or <code>null</code> if no such URL can be identified.
     * <p>
     * <b>IMPLEMENTATION NOTE</b>:  Use of this method bypasses any caching
     * performed by this component.  To take advantage of local caching,
     * use <code>getResourceAsStream()</code> instead.
     *
     * @param path Context-relative path starting with '/'
     *
     * @exception IllegalArgumentException if the path argument is null
     *  or does not start with a '/'
     * @exception MalformedURLException if the resulting URL does not
     *  have legal syntax
     */
    public URL getResource(String path) throws MalformedURLException {

	ResourceUtils.validate(path);

	// Construct a URL from the normalized version of the specified path
	String normalized = ResourceUtils.normalize(path);
	if (normalized != null)
	    return (new URL(docBase + normalized.substring(1)));
	else
	    return (null);

    }


    /**
     * Return an InputStream to the contents of the resource specified
     * by the given virtual path, or <code>null</code> if no resource
     * exists at the specified path.
     *
     * @param path Context-relative path starting with '/'
     *
     * @exception IllegalArgumentException if the path argument is null
     *  or does not start with a '/'
     */
    public InputStream getResourceAsStream(String path) {

	ResourceUtils.validate(path);

	// Look up the cached resource entry (if it exists) for this path
	String normalized = ResourceUtils.normalize(path);
	if (normalized == null)
	    return (null);
	ResourceBean resource = null;
	synchronized (resourcesCache) {
	    resource = (ResourceBean) resourcesCache.get(normalized);
	}
	if (resource == null)
	    return (null);

	// Special handling for directories
	if (resource instanceof DirectoryBean) {
	    if (expand) {
		if (contextPath == null)
		    contextPath = "";
		return (((DirectoryBean) resource).render(contextPath,
							  serverInfo));
	    } else {
		return (null);
	    }
	}

	// Cache the data for this resource (if appropriate and not yet done)
	if (resource.getData() == null) {
	    try {
		resource.cache(inputStream(resource.getName()));
	    } catch (IOException e) {
		log(sm.getString("resources.input", resource.getName()), e);
		return (null);
	    }
	    if (resource.getData() != null)
		resourcesCount++;
	}

	// Return an input stream to the cached or uncached data
	if (resource.getData() != null)
	    return (new ByteArrayInputStream(resource.getData()));
	else {
	    try {
		return (inputStream(normalized));
	    } catch (IOException e) {
		log(sm.getString("resources.input", resource.getName()), e);
		return (null);
	    }
	}

    }


    /**
     * Returns true if a resource exists at the specified path,
     * where <code>path</code> would be suitable for passing as an argument to
     * <code>getResource()</code> or <code>getResourceAsStream()</code>.
     * If there is no resource at the specified location, return false.
     *
     * @param path The path to the desired resource
     */
    public boolean exists(String path) {

	// Look up and return the last modified time for this resource
	String normalized = ResourceUtils.normalize(path);
	if (normalized == null)
	    return (false);
	ResourceUtils.validate(normalized);

	ResourceBean resource = null;
	synchronized (resourcesCache) {
	    resource = (ResourceBean) resourcesCache.get(normalized);
	}
	if (resource != null)
	    return (true);
	else
	    return (false);

    }


    /**
     * Return the last modified time for the resource specified by
     * the given virtual path, or -1 if no such resource exists (or
     * the last modified time cannot be determined).
     * <p>
     * <strong>IMPLEMENTATION NOTE</strong>: We are assuming that the
     * underlying JAR file will not be modified without restarting our
     * associated Context, so it is sufficient to return the last modified
     * timestamp from our cached resource bean.
     *
     * @param path Context-relative path starting with '/'
     *
     * @exception IllegalArgumentException if the path argument is null
     *  or does not start with a '/'
     */
    public long getResourceModified(String path) {

	ResourceUtils.validate(path);

	// Look up and return the last modified time for this resource
	String normalized = ResourceUtils.normalize(path);
	if (normalized == null)
	    return (-1L);
	ResourceBean resource = null;
	synchronized (resourcesCache) {
	    resource = (ResourceBean) resourcesCache.get(normalized);
	}
	if (resource != null)
	    return (resource.getLastModified());
	else
	    return (-1L);

    }


    /**
     * Return the set of context-relative paths of all available resources.
     * Each path will begin with a "/" character.
     */
     public String[] getResourcePaths() {

        ArrayList paths = new ArrayList();
        // NOTE: assumes directories are included
        synchronized (resourcesCache) {
            Iterator names = resourcesCache.keySet().iterator();
            while (names.hasNext())
                paths.add((String) names.next());
        }
        String results[] = new String[paths.size()];
        return ((String[]) paths.toArray(results));

     }


    /**
     * Return the creation date/time of the resource at the specified
     * path, where <code>path</code> would be suitable for passing as an
     * argument to <code>getResource()</code> or
     * <code>getResourceAsStream()</code>.  If there is no resource at the
     * specified location, return -1. If this time is unknown, the
     * implementation should return getResourceModified(path).
     *
     * @param path The path to the desired resource
     */
    public long getResourceCreated(String path) {
        return 0;
    }


    /**
     * Return the content length of the resource at the specified
     * path, where <code>path</code> would be suitable for passing as an
     * argument to <code>getResource()</code> or
     * <code>getResourceAsStream()</code>.  If the content length
     * of the resource can't be determined, return -1. If no content is
     * available (when for exemple, the resource is a collection), return 0.
     *
     * @param path The path to the desired resource
     */
    public long getResourceLength(String path) {
        return -1;
    }


    /**
     * Return true if the resource at the specified path is a collection. A
     * collection is a special type of resource which has no content but
     * contains child resources.
     *
     * @param path The path to the desired resource
     */
    public boolean isCollection(String path) {
        return false;
    }


    /**
     * Return the children of the resource at the specified path, if any. This
     * will return null if the resource is not a collection, or if it is a
     * collection but has no children.
     *
     * @param path The path to the desired resource
     */
    public String[] getCollectionMembers(String path) {
        return null;
    }


    /**
     * Set the content of the resource at the specified path. If the resource
     * already exists, its previous content is overwritten. If the resource
     * doesn't exist, its immediate parent collection (according to the path
     * given) exists, then its created, and the given content is associated
     * with it. Return false if either the resource is a collection, or
     * no parent collection exist.
     *
     * @param path The path to the desired resource
     * @param content InputStream to the content to be set
     */
    public boolean setResource(String path, InputStream content) {
        return false;
    }


    /**
     * Create a collection at the specified path. A parent collection for this
     * collection must exist. Return false if a resource already exist at the
     * path specified, or if the parent collection doesn't exist.
     *
     * @param path The path to the desired resource
     */
    public boolean createCollection(String path) {
        return false;
    }


    /**
     * Delete the specified resource. Non-empty collections cannot be deleted
     * before deleting all their member resources. Return false is deletion
     * fails because either the resource specified doesn't exist, or the
     * resource is a non-empty collection.
     *
     * @param path The path to the desired resource
     */
    public boolean deleteResource(String path) {
        return false;
    }


    // -------------------------------------------------------- Private Methods


    /**
     * Return an input stream to the data content of the underlying JAR entry
     * that corresponds to the specified normalized context-relative path.
     *
     * @param name Normalized context-relative path (with leading '/')
     *
     * @exception IOException if an input/output error occurs
     */
    private InputStream inputStream(String name) throws IOException {

	if (name == null)
	    return (null);
	JarEntry entry = jarFile.getJarEntry(name.substring(1));
	if (entry == null)
	    return (null);
	return (jarFile.getInputStream(entry));

    }


    /**
     * Populate our resources cache based on all of the entries in the
     * underlying JAR file.
     * <p>
     * <strong>IMPLEMENTATION NOTE</strong>: This method assumes that the
     * "name" of an entry within the JAR file is exactly the same as the
     * result of performing a <code>normalize()</code> call on that name,
     * with the exception of the leading slash that is added.
     */
    private void populate() {

	synchronized (resourcesCache) {

	    // Erase the existing cache
	    resourcesCache.clear();
	    resourcesCount = 0;

	    // Construct a pseudo-directory for the entire JAR file
	    DirectoryBean top = new DirectoryBean("/");
	    resourcesCache.put(top.getName(), top);

	    // Process the entries in this JAR file
	    Enumeration entries = jarFile.entries();
	    while (entries.hasMoreElements()) {

		JarEntry entry = (JarEntry) entries.nextElement();

		// Create and cache the resource for this entry
		String name = "/" + entry.getName();
		ResourceBean resource = null;
		if (entry.isDirectory())
		    resource = new DirectoryBean(name, entry);
		else
		    resource = new ResourceBean(name, entry);

		// Connect to our parent entry (if any)
		int last = name.lastIndexOf("/");
		String parentName = name.substring(0, last);
		if (parentName.length() < 1)
		    parentName = "/";
		ResourceBean parent =
		    (ResourceBean) resourcesCache.get(parentName);
		if ((parent != null) && (parent instanceof DirectoryBean))
		    resource.setParent((DirectoryBean) parent);

	    }

	}

    }


    // ------------------------------------------------------ Lifecycle Methods


    /**
     * Shut down this component.
     *
     * @exception LifecycleException if a major problem occurs
     */
    public void stop()  {

	// Shut down our current connection (if any)
	if (jarFile != null) {
	    try {
		jarFile.close();
	    } catch (IOException e) {
		log("Closing JAR file", e);
	    }
	    jarFile = null;
	}
	conn = null;

	// Perform standard superclass processing
	super.stop();

    }


}
