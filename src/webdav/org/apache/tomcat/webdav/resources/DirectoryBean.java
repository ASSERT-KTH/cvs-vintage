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
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TreeMap;
import java.util.jar.JarEntry;

import org.apache.tomcat.webdav.util.StringManager;


/**
 * Abstraction bean that represents the properties of a "resource" that is
 * actually a "directory", in a fashion that independent of the actual
 * underlying medium used to represent those entries.  Convenient constructors
 * are provided to populate our properties from common sources, but it is
 * feasible to do everything with property setters if necessary.
 * <p>
 * <strong>IMPLEMENTATION NOTE</strong>:  It is assumed that access to the
 * set of resources associated with this directory are done in a thread safe
 * manner.  No internal synchronization is performed.
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.1 $ $Date: 2000/11/03 21:27:40 $
 */

public final class DirectoryBean extends ResourceBean {

    // ----------------------------------------------------------- Constructors


    /**
     * Construct a new directory bean for the named resource, with default
     * properties.
     *
     * @param name Normalized context-relative name of this resource
     */
    public DirectoryBean(String name) {

	super(name);

    }


    /**
     * Construct a new directory bean for the named resource, with properties
     * populated from the specified object.  Note that the data content of
     * this resource is <strong>not</strong> initialized unless and until
     * <code>setData()</code> is called.
     *
     * @param name Normalized context-relative name of this resource
     * @param file File representing this resource entry
     */
    public DirectoryBean(String name, File file) {

	super(name, file);

    }


    /**
     * Construct a new directory bean for the named resource, with properties
     * populated from the specified object.  Note that the data content of
     * this resource is <strong>not</strong> initialized unless and until
     * <code>setData()</code> is called.
     *
     * @param name Normalized context-relative name of this resource
     * @param entry JAR entry representing this resource entry
     */
    public DirectoryBean(String name, JarEntry entry) {

	super(name, entry);

    }


    // ----------------------------------------------------- Instance Variables


    /**
     * The system default Locale.
     */
    private static Locale defaultLocale = Locale.getDefault();


    /**
     * The date format pattern for rendering last modified date and time.
     */
    private static String pattern = "EEE, dd MMM yyyy HH:mm z";


    /**
     * The collection of resources for this directory, sorted by name.
     */
    private TreeMap resources = new TreeMap();


    /**
     * The string manager for this package.
     */
    protected static final StringManager sm =
	StringManager.getManager("org.apache.tomcat.webdav.resources");


    /**
     * The date format for rendering last modified date and time.
     */
    private static DateFormat timestamp =
	new SimpleDateFormat(pattern, defaultLocale);


    // --------------------------------------------------------- Public Methods


    /**
     * Add a new resource to this directory.
     *
     * @param entry ResourceBean for the resource to be added
     */
    public void addResource(ResourceBean resource) {

	resources.put(resource.getName(), resource);

    }


    /**
     * Return the set of resources that belong to this directory,
     * in alphabetical order based on their names.
     */
    public ResourceBean[] findResources() {

	ResourceBean results[] = new ResourceBean[resources.size()];
	return ((ResourceBean[]) resources.values().toArray(results));

    }


    /**
     * Remove an existing resource from this directory.
     *
     * @param entry ResourceBean for the resource to be removed
     */
    public void removeResource(ResourceBean resource) {

	resources.remove(resource.getName());

    }


    /**
     * Return an InputStream to an HTML representation of the contents
     * of this directory.
     *
     * @param contextPath Context path to which our internal paths are
     *  relative
     */
    public InputStream render(String contextPath, String serverInfo) {

	// Number of characters to trim from the beginnings of filenames
	int trim = name.length();
	if (!name.endsWith("/"))
	    trim += 1;
	if (name.equals("/"))
	    trim = 1;

	// Prepare a writer to a buffered area
	ByteArrayOutputStream stream = new ByteArrayOutputStream();
	PrintWriter writer = new PrintWriter(stream);

	// FIXME - Currently pays no attention to the user's Locale

	// Render the page header
	writer.print("<html>\r\n");
	writer.print("<head>\r\n");
	writer.print("<title>");
	writer.print(sm.getString("directory.title", name));
	writer.print("</title>\r\n</head>\r\n");
	writer.print("<body bgcolor=\"white\">\r\n");
	writer.print("<table width=\"90%\" cellspacing=\"0\"" +
		     " cellpadding=\"5\" align=\"center\">\r\n");

	// Render the in-page title
	writer.print("<tr><td colspan=\"3\"><font size=\"+2\">\r\n<strong>");
	writer.print(sm.getString("directory.title", name));
	writer.print("</strong>\r\n</font></td></tr>\r\n");

	// Render the link to our parent (if required)
        String parentDirectory = name;
        if (parentDirectory.endsWith("/")) {
            parentDirectory = 
                parentDirectory.substring(0, parentDirectory.length() - 1);
        }
	int slash = parentDirectory.lastIndexOf("/");
	if (slash >= 0) {
	    String parent = name.substring(0, slash);
	    writer.print("<tr><td colspan=\"3\" bgcolor=\"#ffffff\">\r\n");
	    writer.print("<a href=\"");
	    writer.print(rewriteUrl(contextPath));
            if (parent.equals(""))
                parent = "/";
	    writer.print(parent);
	    writer.print("\">");
	    writer.print(sm.getString("directory.parent", parent));
	    writer.print("</a>\r\n");
	    writer.print("</td></tr>\r\n");
	}

	// Render the column headings
	writer.print("<tr bgcolor=\"#cccccc\">\r\n");
	writer.print("<td align=\"left\"><font size=\"+1\"><strong>");
	writer.print(sm.getString("directory.filename"));
	writer.print("</strong></font></td>\r\n");
	writer.print("<td align=\"center\"><font size=\"+1\"><strong>");
	writer.print(sm.getString("directory.size"));
	writer.print("</strong></font></td>\r\n");
	writer.print("<td align=\"right\"><font size=\"+1\"><strong>");
	writer.print(sm.getString("directory.lastModified"));
	writer.print("</strong></font></td>\r\n");
	writer.print("</tr>\r\n");

	// Render the directory entries within this directory
	ResourceBean resources[] = findResources();
	boolean shade = false;
	for (int i = 0;i < resources.length; i++) {

	    String trimmed = resources[i].getName().substring(trim);
	    if (trimmed.equalsIgnoreCase("WEB-INF") ||
		trimmed.equalsIgnoreCase("META-INF"))
		continue;

	    writer.print("<tr");
	    if (shade)
		writer.print(" bgcolor=\"eeeeee\"");
	    writer.print(">\r\n");
	    shade = !shade;

	    writer.print("<td align=\"left\">&nbsp;&nbsp;\r\n");
	    writer.print("<a href=\"");
	    writer.print(rewriteUrl(contextPath));
	    writer.print(rewriteUrl(resources[i].getName()));
	    writer.print("\"><tt>");
	    writer.print(trimmed);
	    if (resources[i] instanceof DirectoryBean)
		writer.print("/");
	    writer.print("</tt></a></td>\r\n");

	    writer.print("<td align=\"right\"><tt>");
	    if (resources[i] instanceof DirectoryBean)
		writer.print("&nbsp;");
	    else
		writer.print(renderSize(resources[i].getSize()));
	    writer.print("</tt></td>\r\n");

	    writer.print("<td align=\"right\"><tt>");
	    writer.print(renderLastModified(resources[i].getLastModified()));
	    writer.print("</tt></td>\r\n");

	    writer.print("</tr>\r\n");
	}

	// Render the page footer
	writer.print("<tr><td colspan=\"3\">&nbsp;</td></tr>\r\n");
	writer.print("<tr><td colspan=\"3\" bgcolor=\"#cccccc\">");
	writer.print("<font size=\"-1\">");
	writer.print(serverInfo);
	writer.print("</font></td></tr>\r\n");
	writer.print("</table>\r\n");
	writer.print("</body>\r\n");
	writer.print("</html>\r\n");

	// Return an input stream to the underlying bytes
	writer.flush();
	return (new ByteArrayInputStream(stream.toByteArray()));

    }


    /**
     * Render the last modified date and time for the specified timestamp.
     *
     * @param lastModified Last modified date and time, in milliseconds since
     *  the epoch
     */
    private String renderLastModified(long lastModified) {

	return (timestamp.format(new Date(lastModified)));

    }


    /**
     * Render the specified file size (in bytes).
     *
     * @param size File size (in bytes)
     */
    private String renderSize(long size) {

	long leftSide = size / 1024;
	long rightSide = (size % 1024) / 103;	// Makes 1 digit
	if ((leftSide == 0) && (rightSide == 0) && (size > 0))
	    rightSide = 1;

	return ("" + leftSide + "." + rightSide + " kb");

    }


    /**
     * URL rewriter.
     * 
     * @param path Path which has to be rewiten
     */
    private String rewriteUrl(String path) {
        
        String normalized = path;
        
	// Replace " " with "%20"
	while (true) {
	    int index = normalized.indexOf(" ");
	    if (index < 0)
		break;
	    normalized = normalized.substring(0, index) + "%20"
		+ normalized.substring(index + 1);
	}

        return normalized;
        
    }


}
