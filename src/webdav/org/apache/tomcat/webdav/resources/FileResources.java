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
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.OutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.apache.tomcat.webdav.util.StringManager;

/**
 * Implementation of the <b>Resources</b> that operates off a document
 * base that is a directory in the local filesystem.  If the specified
 * document base is relative, it is resolved against the application base
 * directory for our surrounding virtual host (if any), or against the
 * value of the "catalina.home" system property.
 * <p>
 * <strong>IMPLEMENTATION NOTE</strong>:  It is assumed that new files may
 * be added, and existing files modified, while this web application is
 * running.  Therefore, only resources (not directories) are cached, and
 * the background thread must remove cached entries that have been modified
 * since they were cached.
 *
 * @author Craig R. McClanahan
 * @author Remy Maucherat
 * @version $Revision: 1.1 $ $Date: 2000/11/03 21:27:41 $
 */
public final class FileResources extends ResourcesBase {


    // ----------------------------------------------------- Instance Variables

    // title used when rendering directory resources ( no other use )
    String contextPath;

    public static String serverInfo="Apache Tomcat/3.3";
    // XXX add code to set it
    

    /**
     * The document base directory for this component.
     */
    protected File base = null;


    /**
     * The descriptive information string for this implementation.
     */
    protected static final String info =
	"org.apache.catalina.resources.FileResources/1.0";


    /**
     * The descriptive information string for this implementation.
     */
    protected static final int BUFFER_SIZE = 2048;


    // ------------------------------------------------------------- Properties


    /**
     * Set the document root for this component.
     *
     * @param docBase The new document root - it must be an absolute path
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

	// Calculate a File object referencing this document base directory
	File base = new File(docBase);

	// Validate that the document base is an existing directory
	if (!base.exists() || !base.isDirectory() || !base.canRead())
	    throw new IllegalArgumentException
		(sm.getString("fileResources.base", docBase));
	this.base = base;

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

        String normalized = ResourceUtils.normalize(path);
	if (normalized == null) {
            //            if (debug >= 1)
            //                log("getRealPath(" + path + ") --> NULL");
	    return (null);
        }
        try {
            ResourceUtils.validate(normalized);
        } catch (IllegalArgumentException e) {
            //            if (debug >= 1)
            //                log("getRealPath(" + path + ") --> IAE");
            throw e;
        }

	// Return a real path to where this file does, or would, exist
	File file = new File(base, normalized.substring(1));
        //        if (debug >= 1)
        //            log("getRealPath(" + path + ") --> " + file.getAbsolutePath());
	return (file.getAbsolutePath());

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

	// Acquire an absolute pathname for the requested resource
	String pathname = getRealPath(path);
	if (pathname == null) {
            //            if (debug >= 1)
            //                log("getResource(" + path + ") --> NULL");
	    return (null);
        }

	// Construct a URL that refers to this file
        URL url = new URL("file", null, 0, pathname);
        //        if (debug >= 1)
        //            log("getResource(" + path + ") --> " + url.toString());
        return (url);

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

	String normalized = ResourceUtils.normalize(path);
	if (normalized == null) {
            //            if (debug >= 1)
            //                log("getResourceAsStream(" + path + ") --> NULL");
	    return (null);
        }
        try {
            ResourceUtils.validate(normalized);
        } catch (IllegalArgumentException e) {
            //            if (debug >= 1)
            //                log("getResourceAsStream(" + path + ") --> IAE");
            throw e;
        }

	// Look up the cached resource entry (if it exists) for this path
	ResourceBean resource = null;
	synchronized (resourcesCache) {
	    resource = (ResourceBean) resourcesCache.get(normalized);
	}
	if (resource != null) {
            //            if (debug >= 1)
            //                log("getResourceAsStream(" + path + ") --> CACHED");
	    return (new ByteArrayInputStream(resource.getData()));
        }

	// Create a File object referencing the requested resource
	File file = file(normalized);
	if ((file == null) || !file.exists() || !file.canRead()) {
            //            if (debug >= 1)
            //                log("getResourceAsStream(" + path + ") --> NO FILE");
	    return (null);
        }

        // If the resource path ends in "/", this *must* be a directory
        if (normalized.endsWith("/") && !file.isDirectory()) {
            //            if (debug >= 1)
            //                log("getResourceAsStream(" + path + ") --> NOT DIR");
            return (null);
        }

        // Special handling for directories
	if (file.isDirectory()) {
	    if (contextPath == null)
		contextPath = "";
	    DirectoryBean directory = new DirectoryBean(normalized, file);
            File[] fileList = file.listFiles();
            for (int i=0; i<fileList.length; i++) {
                File currentFile = fileList[i];
                ResourceBean newEntry = null;
                if (currentFile.isDirectory()) {
                    newEntry =
                        new DirectoryBean(ResourceUtils.normalize(normalized + "/"
                                                    + currentFile.getName()),
                                                    currentFile);
                } else {
                    newEntry =
                        new ResourceBean(ResourceUtils.normalize(normalized + "/"
                                                   + currentFile.getName()),
                                                   currentFile);
                }
                directory.addResource(newEntry);
            }
            //            if (debug >= 1)
            //                log("getResourceAsStream(" + path + ") --> DIRECTORY");
	    return (directory.render(contextPath, serverInfo));
	}

	// Cache the data for this resource (if appropriate and not yet done)
	if (cacheable(normalized, file.length())) {
	    resource = new ResourceBean(normalized, file);
	    try {
		resource.cache(inputStream(resource.getName()));
	    } catch (IOException e) {
		log(sm.getString("resources.input", resource.getName()), e);
		return (null);
	    }
	    synchronized (resourcesCache) {
		resourcesCache.put(resource.getName(), resource);
		resourcesCount++;
	    }
            //            if (debug >= 1)
            //                log("getResourceAsStream(" + path + ") --> CACHE AND SERVE");
	    return (new ByteArrayInputStream(resource.getData()));
	}

	// Serve the contents directly from the filesystem
	try {
            //            if (debug >= 1)
            //                log("getResourceAsStream(" + path + ") --> SERVE FILE");
	    return (new FileInputStream(file));
	} catch (IOException e) {
	    log(sm.getString("resoruces.input", resource.getName()), e);
	    return (null);
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

        String normalized = ResourceUtils.normalize(path);
	if (normalized == null) {
            //            if (debug >= 1)
            //                log("exists(" + path + ") --> NULL");
	    return (false);
        }
        try {
            ResourceUtils.validate(normalized);
        } catch (IllegalArgumentException e) {
            //            if (debug >= 1)
            //                log("exists(" + path + ") --> IAE");
            throw e;
        }

	File file = new File(base, normalized.substring(1));
        if (file != null) {
            //            if (debug >= 1)
            //                log("exists(" + path + ") --> " + file.exists() +
            //                    " isDirectory=" + file.isDirectory());
            return (file.exists());
        } else {
            //            if (debug >= 1)
            //                log("exists(" + path + ") --> NO FILE");
            return (false);
        }

    }


    /**
     * Return the last modified time for the resource specified by
     * the given virtual path, or -1 if no such resource exists (or
     * the last modified time cannot be determined).
     * <p>
     * <strong>IMPLEMENTATION NOTE</strong>: We are assuming that
     * files may be modified while the application is running, so we
     * bypass the cache and check the filesystem directly.
     *
     * @param path Context-relative path starting with '/'
     *
     * @exception IllegalArgumentException if the path argument is null
     *  or does not start with a '/'
     */
    public long getResourceModified(String path) {

        String normalized = ResourceUtils.normalize(path);
	if (normalized == null)
	    return (-1L);
	ResourceUtils.validate(normalized);

	File file = file(normalized);
        if (file != null)
	    return (file.lastModified());
	else
	    return (-1L);

    }


    /**
     * Return the set of context-relative paths of all available resources.
     * Each path will begin with a "/" character.
     */
     public String[] getResourcePaths() {

        ArrayList paths = new ArrayList();
        paths.add("/"); // NOTE: Assumes directories are included
        appendResourcePaths(paths, "", base);
        String results[] = new String[paths.size()];
        return (results);

     }


    /**
     * Return the creation date/time of the resource at the specified
     * path, where <code>path</code> would be suitable for passing as an
     * argument to <code>getResource()</code> or
     * <code>getResourceAsStream()</code>.  If there is no resource at the
     * specified location, return -1. If this time is unknown, the
     * implementation should return getResourceModified(path).
     * <p>
     * <strong>IMPLEMENTATION NOTE</strong>: The creation date of a file
     * shouldn't change except if the file is deleted and the recreated, so
     * this method uses the cache.
     *
     * @param path Context-relative path starting with '/'
     *
     * @exception IllegalArgumentException if the path argument is null
     *  or does not start with a '/'
     */
    public long getResourceCreated(String path) {

        String normalized = ResourceUtils.normalize(path);
	if (normalized == null)
	    return (-1L);
	ResourceUtils.validate(normalized);

	File file = file(normalized);
	if (file != null)
	    return (file.lastModified());
	else
	    return (-1L);

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

        String normalized = ResourceUtils.normalize(path);
	if (normalized == null)
	    return (-1L);
	ResourceUtils.validate(normalized);

	// Look up the cached resource entry (if it exists) for this path
	ResourceBean resource = null;
	synchronized (resourcesCache) {
	    resource = (ResourceBean) resourcesCache.get(normalized);
	}
        if (resource != null) {
            return (resource.getSize());
        }

        // No entry was found in the cache
	File file = file(normalized);
	if (file != null)
	    return (file.length());
	else
	    return (-1L);

    }


    /**
     * Return true if the resource at the specified path is a collection. A
     * collection is a special type of resource which has no content but
     * contains child resources.
     *
     * @param path The path to the desired resource
     */
    public boolean isCollection(String path) {

        String normalized = ResourceUtils.normalize(path);
	if (normalized == null)
	    return (false);
	ResourceUtils.validate(normalized);

	File file = file(normalized);
	if (file != null)
	    return (file.isDirectory());
	else
	    return (false);

    }


    /**
     * Return the children of the resource at the specified path, if any. This
     * will return null if the resource is not a collection, or if it is a
     * collection but has no children.
     *
     * @param path The path to the desired resource
     */
    public String[] getCollectionMembers(String path) {

        String normalized = ResourceUtils.normalize(path);
	if (normalized == null)
	    return (null);
	ResourceUtils.validate(normalized);

	File file = file(normalized);
	if (file != null) {
	    String[] dirList = file.list();
            for (int i=0; i<dirList.length; i++) {
                dirList[i] = ResourceUtils.normalize(normalized + "/" + dirList[i]);
            }
            return dirList;
        } else {
	    return (null);
        }

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

        String normalized = ResourceUtils.normalize(path);
	if (normalized == null)
	    return (false);
	ResourceUtils.validate(normalized);

	File file = new File(base, normalized.substring(1));
        //if ((file.exists()) && (file.isDirectory()))
        //return (false);

        OutputStream os = null;

        try {
            os = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            return (false);
	} catch (IOException e) {
	  return (false);
        }

        try {
            byte[] buffer = new byte[BUFFER_SIZE];
            while (true) {
                int nb = content.read(buffer);
                if (nb == -1)
                    break;
                os.write(buffer, 0, nb);
            }
        } catch (IOException e) {
            return (false);
        }

        try {
            os.close();
        } catch (IOException e) {
            return (false);
        }

        try {
            content.close();
        } catch (IOException e) {
            return (false);
        }

        return (true);

    }


    /**
     * Create a collection at the specified path. A parent collection for this
     * collection must exist. Return false if a resource already exist at the
     * path specified, or if the parent collection doesn't exist.
     *
     * @param path The path to the desired resource
     */
    public boolean createCollection(String path) {

        String normalized = ResourceUtils.normalize(path);
	if (normalized == null)
	    return (false);
	ResourceUtils.validate(normalized);

	File file = new File(base, normalized.substring(1));
	if (file != null)
	    return (file.mkdir());
	else
	    return (false);

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

        String normalized = ResourceUtils.normalize(path);
	if (normalized == null) {
            return (false);
        }
	ResourceUtils.validate(normalized);

	File file = file(normalized);
	if (file != null) {
            return (file.delete());
	} else {
	    return (false);
        }

    }


    // -------------------------------------------------------- Private Methods


    /**
     * Append resource paths for files in the specified directory to the
     * list we are accumulating.
     *
     * @param paths The list containing our accumulated paths
     * @param path Context-relative path for this directory
     * @param dir File object for this directory
     */
    private void appendResourcePaths(List paths, String path, File dir) {

        String names[] = dir.list();
        for (int i = 0; i < names.length; i++) {
            paths.add(path + "/" + names[i]);
            File file = new File(dir, names[i]);        // Assume dirs included
            if (file.isDirectory())
                appendResourcePaths(paths, path + "/" + names[i], file);
        }

    }


    /**
     * Return a File object representing the specified normalized
     * context-relative path if it exists and is readable.  Otherwise,
     * return <code>null</code>.
     *
     * @param name Normalized context-relative path (with leading '/')
     */
    private File file(String name) {

	if (name == null)
	    return (null);
	File file = new File(base, name.substring(1));
	if (file.exists() && file.canRead())
	    return (file);
	else
	    return (null);

    }


    /**
     * Return an input stream to the data content of the underlying file
     * that corresponds to the specified normalized context-relative path.
     *
     * @param name Normalized context-relative path (with leading '/')
     *
     * @exception IOException if an input/output error occurs
     */
    private InputStream inputStream(String name) throws IOException {

	File file = file(name);
	if ((file == null) || file.isDirectory())
	    return (null);
	else
	    return (new FileInputStream(file));

    }


}
