/*   
 *  Copyright 1999-2004 The Apache Sofware Foundation.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.tomcat.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

/** General-purpose utilities to help generation of syntetic java
 *  classes
 */
public class JavaGeneratorTool {
    static org.apache.commons.logging.Log logger = 
	    org.apache.commons.logging.LogFactory.getLog(JavaGeneratorTool.class);
    /** Mangle Package names to avoid reserver words
     **/
    public static final String manglePackage( String s ) {
	for (int i = 0; i < keywords.length; i++) {
            char fs = File.separatorChar;
            int index = s.indexOf(keywords[i]);
            if(index == -1 ) continue;
            while (index != -1) {
		int endIdx=index+keywords[i].length();
		//			System.out.println("XXX " + s + " " + index + " " + endIdx );
		// Is it a full word ?
		if( index>0 && s.charAt( index-1 ) != '/' ) {
		    index = s.indexOf(keywords[i],index+3);
		    continue;
		}
		    
		if( (s.length()>endIdx) && s.charAt( endIdx ) != '/' ) {
		    index = s.indexOf(keywords[i],index+3);
		    continue;
		}
                String tmpathName = s.substring (0,index) + "_";
                s = tmpathName + s.substring (index);
                index = s.indexOf(keywords[i],index+2);
	    }
        }
	s=fixDigits( s );
	//	System.out.println("XXX " + s );
        return(s);
    }

    public static boolean isKeyword( String s ) {
	for (int i = 0; i < keywords.length; i++) {
	    if( s.equals( keywords[i] ) )
		return true;
	}
	return false;
    }

    /** Make sure package components or class name doesn't start with a digit
     */
    public static  String fixDigits( String s ) {
	int i=0;
	if(s.length() == 0 ) return s;
	if( Character.isDigit( s.charAt( 0 )  )) {
	    s="_" +s;
	}
	do {
	    i= s.indexOf( "/", i+1 );
	    if( i<0 || i==s.length() )
		break;
	    if( Character.isDigit( s.charAt( i + 1 )  ) ) {
		s=s.substring( 0, i+1 ) + "_" + s.substring( i+1 );
		i++;
	    }
	} while( i> 0 );
	
	return s;
    }


    
    /** 
     * 	Generated java files may be versioned, to avoid full reloading
     *  when the source changes.
     *
     *  Before generating a file, we check if it is already generated,
     *  and for that we need the latest version of the file. One way
     *  to do it ( the original jasper ) is to modify the class file
     *  and use a class name without version number, then use a class
     *  loader trick to load the file and extract the version from the
     *  class name.
     * 
     *  This method implements a different strategy - the classes are generate
     *  with version number, and we use a map file to find the latest
     *  version of a class. ( we could list )
     *  That can be improved by using a single version file per directory,
     *  or by listing the directory.
     *
     *  The class file is generated to use _version extension.
     *
     *  @return int version number of the latest class file, or -1 if
     *          the mapFile or the coresponding class file is not found
     */
    public static int readVersion(String classDir, String baseClassName) {
	File mapFile=new File( classDir + File.separator + baseClassName + ".ver");
	if( ! mapFile.exists() )
	    return -1;
	
	int version=0;
	try {
	    FileInputStream fis=new FileInputStream( mapFile );
            // The following helps avoid blocking on Windows DOS devices
            // if someone tries to access something like aux.jsp.
            if( fis.available() > 0 ) {
                version=(int)fis.read();
            }
	    fis.close();
	} catch( Exception ex ) {
	    logger.info("readVersion() mapPath=" + mapFile, ex);
	    return -1;
	}

	// check if the file exists
	String versionedFileName=classDir + "/" +
	    getVersionedName( baseClassName, version ) + ".class";

	File vF=new File( versionedFileName );
	if( ! vF.exists() )
	    return -1;
	
	return version;
    }

    /** After we compile a page, we save the version in a
	file with known name, so we can restore the state when we
	restart. Note that this should move to a general-purpose
	persist repository ( on my plans for next version of tomcat )
    */
    public static void writeVersion(String classDir, String baseClassName,
				    int version)
    {
	File mapFile=new File( 	classDir + File.separator + baseClassName + ".ver");

	try {
	    File dir=new File(mapFile.getParent());
	    dir.mkdirs();
	    FileOutputStream fis=new FileOutputStream( mapFile );
	    fis.write(version);
	    fis.close();
	} catch( Exception ex ) {
	    logger.info("writeVersion() " + mapFile , ex);
	}
    }

    public static String getVersionedName( String baseName, int version )
    {
	return baseName + "_" + version;
    }

    // -------------------- Constants --------------------
    
    private static final String [] keywords = {
        "abstract", "boolean", "break", "byte",
        "case", "catch", "char", "class",
        "const", "continue", "default", "do",
        "double", "else", "extends", "final",
        "finally", "float", "for", "goto",
        "if", "implements", "import",
        "instanceof", "int", "interface",
        "long", "native", "new", "package",
        "private", "protected", "public",
        "return", "short", "static", "super",
        "switch", "synchronized", "this",
        "throw", "throws", "transient",
        "try", "void", "volatile", "while"
    };

}
