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


import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import org.apache.tomcat.webdav.util.StringManager;

/**
 * Convenience base class for implementations of the <b>Resources</b>
 * interface.  It is expected that subclasses of this class will be
 * created for each flavor of document root to be supported.
 * <p>
 * Included in the basic support provided by this class is provisions
 * for caching of resources according to configurable policy properties.
 * This will be especially useful for web applications with relatively
 * small amounts of static content (such as a 100% dynamic JSP based
 * application with just a few images), as well as environments where
 * accessing the underlying resources is relatively time consuming
 * (such as a local or remote JAR file).
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.1 $ $Date: 2000/11/03 21:27:42 $
 */

public class ResourceUtils {

    /** Find extension
     */
    public static String getExtension(String file) {
	if (file == null)
	    return (null);
	int period = file.lastIndexOf(".");
	if (period < 0)
	    return (null);
	String extension = file.substring(period + 1);
	if (extension.length() < 1)
	    return (null);
	return extension;
    }


    /**
     * Return a context-relative path, beginning with a "/", that represents
     * the canonical version of the specified path after ".." and "." elements
     * are resolved out.  If the specified path attempts to go outside the
     * boundaries of the current context (i.e. too many ".." path elements
     * are present), return <code>null</code> instead.
     *
     * @param path Path to be normalized
     */
    public static String normalize(String path) {

	// Normalize the slashes and add leading slash if necessary
	String normalized = path;
	if (normalized.indexOf('\\') >= 0)
	    normalized = normalized.replace('\\', '/');
	if (!normalized.startsWith("/"))
	    normalized = "/" + normalized;

	// Resolve occurrences of "//" in the normalized path
	while (true) {
	    int index = normalized.indexOf("//");
	    if (index < 0)
		break;
	    normalized = normalized.substring(0, index) +
		normalized.substring(index + 1);
	}

	// Resolve occurrences of "%20" in the normalized path
	while (true) {
	    int index = normalized.indexOf("%20");
	    if (index < 0)
		break;
	    normalized = normalized.substring(0, index) + " " +
		normalized.substring(index + 3);
        }

	// Resolve occurrences of "/./" in the normalized path
	while (true) {
	    int index = normalized.indexOf("/./");
	    if (index < 0)
		break;
	    normalized = normalized.substring(0, index) +
		normalized.substring(index + 2);
	}

	// Resolve occurrences of "/../" in the normalized path
	while (true) {
	    int index = normalized.indexOf("/../");
	    if (index < 0)
		break;
	    if (index == 0)
		return (null);	// Trying to go outside our context
	    int index2 = normalized.lastIndexOf('/', index - 1);
	    normalized = normalized.substring(0, index2) +
		normalized.substring(index + 3);
	}

	// Return the normalized path that we have completed
	return (normalized);

    }


    private static final StringManager sm =
	StringManager.getManager("org.apache.tomcat.webdav.resources");

    /**
     * Validate the format of the specified path, which should be context
     * relative and begin with a slash character.
     *
     * @param path Context-relative path to be validated
     *
     * @exception IllegalArgumentException if the specified path is null
     *  or does not have a valid format
     */
    public static void validate(String path) {
	if ((path == null) || !path.startsWith("/"))
	    throw new IllegalArgumentException
		(sm.getString("resources.path", path));
    }

}
