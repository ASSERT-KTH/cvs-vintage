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


import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import java.util.jar.JarEntry;


/**
 * Abstraction bean that represents the properties of a "resource" that
 * may or may not be a "directory", in a fashion that is independent
 * of the actual underlying medium used to represent those entries.
 * Convenient constructors are provided to populate our properties from
 * common sources, but it is feasible to do everything with property
 * setters if necessary.
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.1 $ $Date: 2000/11/03 21:27:42 $
 */

public class ResourceBean {


    // ----------------------------------------------------------- Constructors


    /**
     * Construct a new resource bean for the named resource, with default
     * properties.
     *
     * @param name Normalized context-relative name of this resource
     */
    public ResourceBean(String name) {

	super();
	setName(name);

    }


    /**
     * Construct a new resource bean for the named resource, with properties
     * populated from the specified object.  Note that the data content of
     * this resource is <strong>not</strong> initialized unless and until
     * <code>setData()</code> is called.
     *
     * @param name Normalized context-relative name of this resource
     * @param file File representing this resource entry
     */
    public ResourceBean(String name, File file) {

	this(name);
	populate(file);

    }


    /**
     * Construct a new resource bean for the named resource, with properties
     * populated from the specified object.  Note that the data content of
     * this resource is <strong>not</strong> initialized unless and until
     * <code>setData()</code> is called.
     *
     * @param name Normalized context-relative name of this resource
     * @param entry JAR entry representing this resource entry
     */
    public ResourceBean(String name, JarEntry entry) {

	this(name);
	populate(entry);

    }


    // ------------------------------------------------------------- Properties


    /**
     * The data content of this resource.  This property is
     * <strong>only</strong> initialized when the corresponding property
     * setter method is called.
     */
    protected byte[] data = null;

    public byte[] getData() {
	return (this.data);
    }

    public void setData(byte[] data) {
	this.data = data;
    }


    /**
     * The last modified date/time for this resource, in milliseconds since
     * the epoch.
     */
    protected long lastModified = 0L;

    public long getLastModified() {
	return (this.lastModified);
    }

    public void setLastModified(long lastModified) {
	this.lastModified = lastModified;
    }


    /**
     * The normalized context-relative name of this resource.
     */
    protected String name = null;

    public String getName() {
	return (this.name);
    }

    public void setName(String name) {
	this.name = name;
    }



    /**
     * The parent resource (normally a directory entry) of this resource.
     * Note that this property is <strong>not</strong> set from an underlying
     * File or JarEntry argument to our constructor -- you must call
     * <code>setParent()</code> explicitly if you wish to maintain this
     * relationship.
     */
    protected DirectoryBean parent = null;

    public DirectoryBean getParent() {
	return (this.parent);
    }

    public void setParent(DirectoryBean parent) {
	if (this.parent != null)
	    this.parent.removeResource(this);
	this.parent = parent;
	if (this.parent != null)
	    this.parent.addResource(this);
    }


    /**
     * The size of this resource, in bytes.
     */
    protected long size = 0L;

    public long getSize() {
	return (this.size);
    }

    public void setSize(long size) {
	this.size = size;
    }


    // --------------------------------------------------------- Public Methods


    /**
     * Cache the data for this resource from the specified input stream.
     *
     * @param input InputStream from which to read the data for this resource
     *
     * @exception IOException if an input/output error occurs
     */
    public void cache(InputStream input) throws IOException {

	BufferedInputStream in = new BufferedInputStream(input);
	ByteArrayOutputStream out = new ByteArrayOutputStream();
	while (true) {
	    int ch = in.read();
	    if (ch < 0)
		break;
	    out.write(ch);
	}
	in.close();
	data = out.toByteArray();

    }


    // ------------------------------------------------------ Protected Methods


    /**
     * Populate our properties from the specified File object.
     *
     * @param file File representing this entry
     */
    protected void populate(File file) {

	this.lastModified = file.lastModified();
	this.size = file.length();

    }


    /**
     * Populate our properties from the specified JarEntry object.
     *
     * @param entry JarEntry representing this entry
     */
    protected void populate(JarEntry entry) {

	this.lastModified = entry.getTime();
	this.size = entry.getSize();

    }


}
