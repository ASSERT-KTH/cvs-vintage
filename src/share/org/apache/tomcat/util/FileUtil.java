/*
 * $Header: /tmp/cvs-vintage/tomcat/src/share/org/apache/tomcat/util/Attic/FileUtil.java,v 1.5 2000/03/16 20:43:26 costin Exp $
 * $Revision: 1.5 $
 * $Date: 2000/03/16 20:43:26 $
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

import java.io.File;

/*
 * FileUtil contains utils for dealing with Files. Some of these are 
 * already present in JDK 1.2 but since we can rely on that and need 
 * to run on both JDK 1.1.x and JDK 1.2, we are replicating some of 
 * that code here. 
 *
 * FileUtil also takes care of File.getAbsolutePath() and
 * File.getNamePath() troubles when running on JDK 1.1.x/Windows
 *
 * @author James Todd [gonzo@eng.sun.com]
 * @author Anil K. Vijendran [akv@eng.sun.com]
 */

public class FileUtil {

    public static File[] listFiles(File dir) {

	String[] ss = dir.list();
	if (ss == null) 
	    return null;
	int n = ss.length;
	File[] fs = new File[n];
	for(int i = 0; i < n; i++) {
	    fs[i] = new File(dir.getPath(), ss[i]);
	}
	return fs;
    }


    /** Will concatenate 2 paths, dealing with ..
     * ( /a/b/c + d = /a/b/d, /a/b/c + ../d = /a/d )
     * Used in Request.getRD
     * @return null if error occurs
     */
    public static String catPath(String lookupPath, String path) {
	// Cut off the last slash and everything beyond
	int index = lookupPath.lastIndexOf("/");
	lookupPath = lookupPath.substring(0, index);
	
	// Deal with .. by chopping dirs off the lookup path
	while (path.startsWith("../")) { 
	    if (lookupPath.length() > 0) {
		index = lookupPath.lastIndexOf("/");
		lookupPath = lookupPath.substring(0, index);
	    } 
	    else {
		// More ..'s than dirs, return null
		return null;
	    }
	    
	    index = path.indexOf("../") + 3;
	    path = path.substring(index);
	}
	
	return lookupPath + "/" + path;
    }
    
    public static String patch(String path) {
        String patchPath = path.trim();

        // Move drive spec to the front of the path
        if (patchPath.length() >= 3 &&
            patchPath.charAt(0) == '/'  &&
            Character.isLetter(patchPath.charAt(1)) &&
            patchPath.charAt(2) == ':') {
            patchPath=patchPath.substring(1,3)+"/"+patchPath.substring(3);
        }

        // Eliminate consecutive slashes after the drive spec
	if (patchPath.length() >= 2 &&
            Character.isLetter(patchPath.charAt(0)) &&
            patchPath.charAt(1) == ':') {
            char[] ca = patchPath.replace('/', '\\').toCharArray();
            char c;
            StringBuffer sb = new StringBuffer();

            for (int i = 0; i < ca.length; i++) {
                if ((ca[i] != '\\') ||
                    (ca[i] == '\\' &&
                        i > 0 &&
                        ca[i - 1] != '\\')) {
                    if (i == 0 &&
                        Character.isLetter(ca[i]) &&
                        i < ca.length - 1 &&
                        ca[i + 1] == ':') {
                        c = Character.toUpperCase(ca[i]);
                    } else {
                        c = ca[i];
                    }

                    sb.append(c);
                }
            }

            patchPath = sb.toString();
        }

        return patchPath;
    }

    public static boolean isAbsolute( String path ) {
	// normal file
	if( path.startsWith("/" ) ) return true;

	if( path.startsWith(File.separator ) ) return true;

	// win c:
	if (path.length() >= 3 &&
            Character.isLetter(path.charAt(0)) &&
            path.charAt(1) == ':')
	    return true;
	return false;
    }
    
    // Probably not needed, original code used by Context.getRealPath()
    // XXX Find if it is duplicated, merge with the other "path" functions
    public static String normPath( String path ) {
	int i = -1;
	// norm path
	if( path==null) {
	    // Shouldn't happen, find out what is wrong
	    /*DEBUG*/ try {throw new Exception(); } catch(Exception ex) {ex.printStackTrace();}
	    return "";
	}
        while ((i = path.indexOf('\\')) > -1) {
            String a = path.substring(0, i);
            String b = "";
 
            if (i < path.length() - 1) {
                b = path.substring(i + 1);
            } 
 
            path = a + "/" + b;
        }
	return path;
    }

}
