/*
 * $Header: /tmp/cvs-vintage/tomcat/src/share/org/apache/tomcat/util/Attic/URLResourceReader.java,v 1.1 1999/11/28 23:52:31 harishp Exp $
 * $Revision: 1.1 $
 * $Date: 1999/11/28 23:52:31 $
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

package org.apache.tomcat.util;

import java.util.zip.*;
import java.net.*;
import java.util.*;
import java.io.*;

public class URLResourceReader  {
    private Hashtable resourceCache = new Hashtable();
    private boolean iszip = true;
    private URL url = null;

    /**
     * Creates a new URLResourceReader object. You can either give
     * the URL of the zip/jar file or a base url where to
     * look for additional resources. If the url ends with
     * "/" then it is assumed to be  a Base URL. 
     *
     */
    public URLResourceReader(URL url) throws IOException {
        this.url = url;
        this.iszip = !url.getFile().endsWith("/"); 
        initialize();
    }

    /**
     * Creates a new URLResourceReader object with the given
     * input stream. The stream is assumed to be a zip/jar
     * stream.
     */
    public URLResourceReader(InputStream is) throws IOException {
        init(is);
    }

    private void initialize() throws IOException {
        if(iszip) {
            InputStream is = url.openStream();
            init(is);
            is.close();
        }
    }

    private byte[] readFully(InputStream is) throws IOException {
        byte[] buf = new byte[1024];
        int num = 0;
        ByteArrayOutputStream bout = new ByteArrayOutputStream();

        while( (num = is.read(buf)) != -1) {
            bout.write(buf, 0, num);
        }

        return bout.toByteArray();
    }

    private void init(InputStream is) throws IOException {
        ZipInputStream zstream = new ZipInputStream(is);
        ZipEntry entry;
        
        while( (entry = zstream.getNextEntry()) != null) {
            byte[] entryData = readFully(zstream);
            resourceCache.put(entry.getName(), entryData);
            zstream.closeEntry();
        }
        
        zstream.close();
    }

    /**
     * Returns an Enumeration of all "known" resource names.
     */
    public Enumeration getResourceNames() {
        return resourceCache.keys();
    }

    /**
     * Returns an array of bytes read for this resource if the
     * resource exists. This method blocks until the resource
     * has been fully read. If the resource does not exist,
     * this method returns null.
     */
    public byte[] getResource(String  resource) {
        // lookup the data in the cache...
        byte[] data = (byte[]) resourceCache.get(resource);
        if(data != null) {
            return data;
        }

        // if the data was to come from a zip file that we
        // already read fully, then it is probably not there.
        if(iszip) {
            return null;
        }

        // Now the only choice left is to make a url connection.
        try {
            URL realURL = new URL(url.getProtocol(), url.getHost(),
                                  url.getFile() + resource);
            data = readFully(realURL.openStream());
            // add it to cache..
            resourceCache.put(resource, data);
            return data;
        } catch(Exception e) {
            return null;
        }
    }

    public void close() {
        resourceCache.clear();
        resourceCache = null;
    }

    public static void main(String[] args) {
        try {
            URL url = new URL("http://www.professionals.com/~harish/xml.jar");
            URLResourceReader urdr = new URLResourceReader(url);

            for(Enumeration e=urdr.getResourceNames(); e.hasMoreElements();) {
                System.out.println(e.nextElement());
            }
            System.out.println("length=" + urdr.getResource("Main.class"));
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}








