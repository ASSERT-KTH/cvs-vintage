/*
 * $Header: /tmp/cvs-vintage/tomcat/src/share/org/apache/tomcat/util/Attic/MimeMap.java,v 1.4 2000/06/03 06:35:23 costin Exp $
 * $Revision: 1.4 $
 * $Date: 2000/06/03 06:35:23 $
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

import java.net.*;
import java.util.*;


/**
 * A mime type map that implements the java.net.FileNameMap interface.
 *
 * @author James Duncan Davidson [duncan@eng.sun.com]
 * @author Jason Hunter [jch@eng.sun.com]
 */
public class MimeMap implements FileNameMap {

    // Defaults - all of them are "well-known" types,
    // you can add using normal web.xml.
    
    public static Hashtable mimeMappings=new Hashtable();
    static {
        mimeMappings.put("txt", "text/plain");
        mimeMappings.put("html","text/html");
        mimeMappings.put("htm", "text/html");
        mimeMappings.put("gif", "image/gif");
        mimeMappings.put("jpg", "image/jpeg");
        mimeMappings.put("jpe", "image/jpeg");
        mimeMappings.put("jpeg", "image/jpeg");
	mimeMappings.put("java", "text/plain");
        mimeMappings.put("body", "text/html");
        mimeMappings.put("rtx", "text/richtext");
        mimeMappings.put("tsv", "text/tab-separated-values");
        mimeMappings.put("etx", "text/x-setext");
        mimeMappings.put("ps", "application/x-postscript");
        mimeMappings.put("class", "application/java");
        mimeMappings.put("csh", "application/x-csh");
        mimeMappings.put("sh", "application/x-sh");
        mimeMappings.put("tcl", "application/x-tcl");
        mimeMappings.put("tex", "application/x-tex");
        mimeMappings.put("texinfo", "application/x-texinfo");
        mimeMappings.put("texi", "application/x-texinfo");
        mimeMappings.put("t", "application/x-troff");
        mimeMappings.put("tr", "application/x-troff");
        mimeMappings.put("roff", "application/x-troff");
        mimeMappings.put("man", "application/x-troff-man");
        mimeMappings.put("me", "application/x-troff-me");
        mimeMappings.put("ms", "application/x-wais-source");
        mimeMappings.put("src", "application/x-wais-source");
        mimeMappings.put("zip", "application/zip");
        mimeMappings.put("bcpio", "application/x-bcpio");
        mimeMappings.put("cpio", "application/x-cpio");
        mimeMappings.put("gtar", "application/x-gtar");
        mimeMappings.put("shar", "application/x-shar");
        mimeMappings.put("sv4cpio", "application/x-sv4cpio");
        mimeMappings.put("sv4crc", "application/x-sv4crc");
        mimeMappings.put("tar", "application/x-tar");
        mimeMappings.put("ustar", "application/x-ustar");
        mimeMappings.put("dvi", "application/x-dvi");
        mimeMappings.put("hdf", "application/x-hdf");
        mimeMappings.put("latex", "application/x-latex");
        mimeMappings.put("bin", "application/octet-stream");
        mimeMappings.put("oda", "application/oda");
        mimeMappings.put("pdf", "application/pdf");
        mimeMappings.put("ps", "application/postscript");
        mimeMappings.put("eps", "application/postscript");
        mimeMappings.put("ai", "application/postscript");
        mimeMappings.put("rtf", "application/rtf");
        mimeMappings.put("nc", "application/x-netcdf");
        mimeMappings.put("cdf", "application/x-netcdf");
        mimeMappings.put("cer", "application/x-x509-ca-cert");
        mimeMappings.put("exe", "application/octet-stream");
        mimeMappings.put("gz", "application/x-gzip");
        mimeMappings.put("Z", "application/x-compress");
        mimeMappings.put("z", "application/x-compress");
        mimeMappings.put("hqx", "application/mac-binhex40");
        mimeMappings.put("mif", "application/x-mif");
        mimeMappings.put("ief", "image/ief");
        mimeMappings.put("tiff", "image/tiff");
        mimeMappings.put("tif", "image/tiff");
        mimeMappings.put("ras", "image/x-cmu-raster");
        mimeMappings.put("pnm", "image/x-portable-anymap");
        mimeMappings.put("pbm", "image/x-portable-bitmap");
        mimeMappings.put("pgm", "image/x-portable-graymap");
        mimeMappings.put("ppm", "image/x-portable-pixmap");
        mimeMappings.put("rgb", "image/x-rgb");
        mimeMappings.put("xbm", "image/x-xbitmap");
        mimeMappings.put("xpm", "image/x-xpixmap");
        mimeMappings.put("xwd", "image/x-xwindowdump");
        mimeMappings.put("au", "audio/basic");
        mimeMappings.put("snd", "audio/basic");
        mimeMappings.put("aif", "audio/x-aiff");
        mimeMappings.put("aiff", "audio/x-aiff");
        mimeMappings.put("aifc", "audio/x-aiff");
        mimeMappings.put("wav", "audio/x-wav");
        mimeMappings.put("mpeg", "video/mpeg");
        mimeMappings.put("mpg", "video/mpeg");
        mimeMappings.put("mpe", "video/mpeg");
        mimeMappings.put("qt", "video/quicktime");
        mimeMappings.put("mov", "video/quicktime");
        mimeMappings.put("avi", "video/x-msvideo");
        mimeMappings.put("movie", "video/x-sgi-movie");
        mimeMappings.put("avx", "video/x-rad-screenplay");
        mimeMappings.put("wrl", "x-world/x-vrml");
        mimeMappings.put("mpv2", "video/mpeg2");
    }
    

    private Hashtable map = new Hashtable();

    public void addContentType(String extn, String type) {
        map.put(extn, type.toLowerCase());
    }

    public Enumeration getExtensions() {
        return map.keys();
    }

    public String getContentType(String extn) {
        String type = (String)map.get(extn.toLowerCase());
	return type;
    }

    public void removeContentType(String extn) {
        map.remove(extn.toLowerCase());
    }

    /** Get extension of file, without fragment id
     */
    public static String getExtension( String fileName ) {
        // play it safe and get rid of any fragment id
        // that might be there
	int length=fileName.length();
	
        int newEnd = fileName.lastIndexOf('#');
	if( newEnd== -1 ) newEnd=length;
	// Instead of creating a new string.
	//         if (i != -1) {
	//             fileName = fileName.substring(0, i);
	//         }
        int i = fileName.lastIndexOf('.', newEnd );
        if (i != -1) {
             return  fileName.substring(i + 1, newEnd );
        } else {
            // no extension, no content type
            return null;
        }
    }
    
    public String getContentTypeFor(String fileName) {
	String extn=getExtension( fileName );
        if (extn!=null) {
            return getContentType(extn);
        } else {
            // no extension, no content type
            return null;
        }
    }

}
