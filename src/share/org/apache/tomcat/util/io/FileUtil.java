/*
 * $Header: /tmp/cvs-vintage/tomcat/src/share/org/apache/tomcat/util/io/FileUtil.java,v 1.7 2002/01/22 06:09:51 billbarker Exp $
 * $Revision: 1.7 $
 * $Date: 2002/01/22 06:09:51 $
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


package org.apache.tomcat.util.io;

import java.io.*;
import java.util.zip.*;

import org.apache.tomcat.util.log.*;

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
    static final String osName = System.getProperty("os.name");
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

    // XXX tc_log is the default channel in tomcat, this component
    //should be able to log in a specific channel.
    static Log loghelper = Log.getLog("tc/FileUtil", "FileUtil");
    
    /** All the safety checks from getRealPath() and
	DefaultServlet.

    */
    public static String safePath( String base, String path ) {
	// Hack for Jsp ( and other servlets ) that use rel. paths 
	// if( ! path.startsWith("/") ) path="/"+ path;
	if( path==null || path.equals("") ) return base;
	
	String normP=path;
	if( path.indexOf('\\') >=0 )
	    normP= path.replace('\\', '/');
	if ( !normP.startsWith("/"))
	    normP = "/" + normP;

	int index = normP.indexOf("/../");
	if (index >= 0) {

	    // Clean out "//" and "/./" so they will not be confused
	    // with real parent directories
	    int index2 = 0;
	    while ((index2 = normP.indexOf("//", index2)) >= 0) {
		normP = normP.substring(0, index2) +
		    normP.substring(index2 + 1);
		if (index2 < index)
		    index--;
	    }
	    index2 = 0;
	    while ((index2 = normP.indexOf("/./", index2)) >= 0) {
		normP = normP.substring(0, index2) +
		    normP.substring(index2 + 2);
		if (index2 < index)
		    index -= 2;
	    }

	    // Remove cases of "/{directory}/../"
	    while (index >= 0) {
		// If no parent directory to remove, return null
		if (index == 0)
		    return (null);	// Trying to leave our context
		index2 = normP.lastIndexOf('/', index-1);
		normP = normP.substring(0, index2) +
		    normP.substring(index + 3);
		index = normP.indexOf("/../", index2);
	    }

	}

	String realPath= base + normP;

	// Probably not needed - it will be used on the local FS
	realPath = FileUtil.patch(realPath);
	String canPath=null;
	
	try {
	    canPath=new File(realPath).getCanonicalPath();
	} catch( IOException ex ) {
	    //log("safePath: " + realPath, ex);
	    loghelper.log("in safePath(" + base +", "+path + "), realPath=" + realPath, ex);
	    return null;
	}

	// This absPath/canPath comparison plugs security holes...
	// On Windows, makes "x.jsp.", "x.Jsp", and "x.jsp%20"
        // return 404 instead of the JSP source
	// On all platforms, makes sure we don't let ../'s through
        // Unfortunately, on Unix, it prevents symlinks from working
	// So, a check for File.separatorChar='\\' ..... It hopefully
	// happens on flavors of Windows.
	if (File.separatorChar  == '\\') {
	    // On Windows check ignore case....
	    if (!realPath.equals(canPath)){
            int ls=realPath.lastIndexOf('\\');
            if ( (ls > 0) && !realPath.substring(0,ls).equals(canPath) )
        		return null;
	    }
	}

	// The following code on Non Windows disallows ../
	// in the path but also disallows symlinks....
	//
	// if( ! canPath.startsWith(base) ) {
	// 	// no access to files in a different context.
	//		return null;
	//   }
	// if(!absPath.equals(canPath)) {
	// response.sendError(response.SC_NOT_FOUND);
	// return;
	// }
	// instead lets look for ".." in the absolute path
	// and disallow only that.
	// Why should we loose out on symbolic links?
	//
	
	if(realPath.indexOf("..") != -1) {
	    // We have .. in the path...
	    return null;
	}
	// extra-extra safety check, ( but slow )
	return realPath;
    }
    
    public static String patch(String path) {
        String patchPath = path;

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

	// fix path on NetWare - all '/' become '\\' and remove duplicate '\\'
	if (osName.startsWith("NetWare") && 
	    path.length() >=3 &&
	    path.indexOf(':') > 0) {
	    char[] ca = patchPath.replace('/', '\\').toCharArray();
	    StringBuffer sb = new StringBuffer();

	    for (int i = 0; i < ca.length; i++) {
		if ((ca[i] != '\\') ||
		    (ca[i] == '\\' && i > 0 && ca[i - 1] != '\\')) {
		    sb.append(ca[i]);
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

	// NetWare volume:
	if (osName.startsWith("NetWare") &&
	    path.length() >=3 &&
	    path.indexOf(':') > 0)
	    return true;

	return false;
    }
    
    // Used in few places.
    public static String getCanonicalPath(String name ) {
	if( name==null ) return null;
        File f = new File(name);
        try {
            return  f.getCanonicalPath();
        } catch (IOException ioe) {
	    System.err.println("getCanonicalPath(" + name + ")");
	    ioe.printStackTrace();
	    return name; // oh well, we tried...
        }
    }
    
    public static String removeLast( String s) {
	int i = s.lastIndexOf("/");
	
	if (i > 0) {
	    s = s.substring(0, i);
	} else if (i == 0 && ! s.equals("/")) {
	    s = "/";
	} else {
	    s = "";
	}
	return s;
    }

    public static String getExtension( String path ) {
        int i = path.lastIndexOf(".");
	int j = path.lastIndexOf(File.separator);

	if ((i > 0) && (i > j))
	    return path.substring(i);
	else
	    return null;
    }

    /** Name without path and extension. 
     */
    public static String getBase( String path ) {
        int i = path.lastIndexOf(".");
	int j = path.lastIndexOf(File.separator);

	if( j < 0 ) {// no /
	    if( i<0 )
		return path;
	    else
		return path.substring( 0, i );
	} else {
	    if( i<j ) {
		// . in a dir, before last component, or no "."
		return path.substring( j );
	    } else {
		return path.substring( j+1, i );
	    }
	}
    }

    public static void expand( String src, String dest)
	throws IOException
    {
	File srcF=new File( src);
	File dir=new File( dest );
	
	ZipInputStream zis = new ZipInputStream(new FileInputStream(srcF));
	ZipEntry ze = null;
	
	while ((ze = zis.getNextEntry()) != null) {
	    try {

		// Bug 2033
		File f;
		if( File.separatorChar == '\\' ) // NT
		    f = new File( dir, ze.getName().replace('/','\\') );
		else
		    f = new File( dir, ze.getName() );
		// create intermediary directories - sometimes zip don't add them
		File dirF=new File(f.getParent());
		dirF.mkdirs();
		
		if (ze.isDirectory()) {
		    f.mkdirs(); 
		} else {
		    byte[] buffer = new byte[1024];
		    int length = 0;
		    FileOutputStream fos = new FileOutputStream(f);
		    
		    while ((length = zis.read(buffer)) >= 0) {
			fos.write(buffer, 0, length);
		    }
		    
		    fos.close();
		}
	    } catch( FileNotFoundException ex ) {
		//loghelper.log("FileNotFoundException: " +
		//   ze.getName(), Logger.ERROR );
		throw ex;
	    }
	}

    }

    public static void clearDir(File dir) {
        String[] files = dir.list();

        if (files != null) {
	    for (int i = 0; i < files.length; i++) {
	        File f = new File(dir, files[i]);

	        if (f.isDirectory()) {
		    clearDir(f);
	        }

	        try {
	            f.delete();
	        } catch (Exception e) {
	        }
	    }

	    try {
	        dir.delete();
	    } catch (Exception e) {
	    }
        }
    }


    public static File getConfigFile( File base, File configDir, String defaultF )
    {
        if( base==null )
            base=new File( defaultF );
        if( ! base.isAbsolute() ) {
            if( configDir != null )
                base=new File( configDir, base.getPath());
            else
                base=new File( base.getAbsolutePath()); //??
        }
        File parent=new File(base.getParent());
        if(!parent.exists()){
            if(!parent.mkdirs()){
                throw new RuntimeException(
                    "Unable to create path to config file :"+
                    base.getAbsolutePath());
            }
        }
        return base;
    }

}
