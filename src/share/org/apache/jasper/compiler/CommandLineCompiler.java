/*
 * $Header: /tmp/cvs-vintage/tomcat/src/share/org/apache/jasper/compiler/CommandLineCompiler.java,v 1.9 2004/02/23 02:45:12 billbarker Exp $
 * $Revision: 1.9 $
 * $Date: 2004/02/23 02:45:12 $
 *
 *
 *  Copyright 1999-2004 The Apache Software Foundation
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
 *  See the License for the specific language 
 */

package org.apache.jasper.compiler;

import java.io.File;

import org.apache.jasper.CommandLineContext;
import org.apache.jasper.Constants;

/**
 * Overrides some methods so that we get the desired effects.
 *@author Danno Ferrin
 */
public class CommandLineCompiler extends Compiler implements Mangler {

    String javaFileName;
    String classFileName;
    String pkgName;
    String className;
    File jsp;
    String outputDir;

    public CommandLineCompiler(CommandLineContext ctxt) {
        super(ctxt);

        jsp = new File(ctxt.getJspFile());
        outputDir =  ctxt.getOptions().getScratchDir().getAbsolutePath();

        setMangler(this);

        computePackageName();
        ctxt.setServletPackageName(pkgName);
        className = getBaseClassName();
        // yes this is kind of messed up ... but it works
        if (ctxt.isOutputInDirs()) {
            String pkgName = ctxt.getServletPackageName();
            if (pkgName == null) {
                pkgName = "";
            }
            String tmpDir = outputDir
                   + File.separatorChar
                   + pkgName.replace('.', File.separatorChar);
            File f = new File(tmpDir);
            if (!f.exists()) {
                if (f.mkdirs()) {
                    outputDir = tmpDir;
                }
            } else {
                outputDir = tmpDir;
            }
        }
        computeClassFileName();
        computeJavaFileName();
    }


    /**
     * Always outDated.  (Of course we are, this is an explicit invocation
     *@returns true
     */
    public boolean isOutDated() {
        return true;
    }


    public final void computeJavaFileName() {
	javaFileName = ctxt.getServletClassName() + ".java";
	if ("null.java".equals(javaFileName)) {
    	    javaFileName = getBaseClassName() + ".java";
    	};
	if (outputDir != null && !outputDir.equals(""))
	    javaFileName = outputDir + File.separatorChar + javaFileName;
    }

    void computeClassFileName() {
        String prefix = getPrefix(jsp.getPath());
        classFileName = prefix + getBaseClassName() + ".class";
	if (outputDir != null && !outputDir.equals(""))
	    classFileName = outputDir + File.separatorChar + classFileName;
    }

    public static String [] keywords = {
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

    void computePackageName() {
	String pathName = jsp.getPath();
	StringBuffer modifiedpkgName = new StringBuffer ();
        int indexOfSepChar = pathName.lastIndexOf(File.separatorChar);

        if (("".equals(ctxt.getServletPackageName())) ||
	    (indexOfSepChar == -1) || (indexOfSepChar == 0)) {
	    pkgName = null;
	} else {
	    for (int i = 0; i < keywords.length; i++) {
		char fs = File.separatorChar;
		int index;
		if (pathName.startsWith(keywords[i] + fs)) {
		    index = 0;
		} else {
		    index = pathName.indexOf(fs + keywords[i] + fs);
		}
		while (index != -1) {
		    String tmpathName = pathName.substring (0,index+1) + '%';
		    pathName = tmpathName + pathName.substring (index+2);
		    index = pathName.indexOf(fs + keywords[i] + fs);
		}
	    }
	
	    // XXX fix for paths containing '.'.
	    // Need to be more elegant here.
            pathName = pathName.replace('.','_');
	
	    pkgName = pathName.substring(0, pathName.lastIndexOf(
	    		File.separatorChar)).replace(File.separatorChar, '.');
	    if (ctxt.getServletPackageName() != null) {
	        pkgName = ctxt.getServletPackageName();
	    }
	    for (int i=0; i<pkgName.length(); i++)
		if (Character.isLetter(pkgName.charAt(i)) == true ||
		    pkgName.charAt(i) == '.') {
		    modifiedpkgName.append(pkgName.substring(i,i+1));
		}
		else
		    modifiedpkgName.append(mangleChar(pkgName.charAt(i)));

	    if (modifiedpkgName.charAt(0) == '.') {
                String modifiedpkgNameString = modifiedpkgName.toString();
                pkgName = modifiedpkgNameString.substring(1,
                                                         modifiedpkgName.length ());
            }
	    else
	        pkgName = modifiedpkgName.toString();
	}

    }

					
    private final String getInitialClassName() {
        String prefix = getPrefix(jsp.getPath());

        return prefix + getBaseClassName() + Constants.JSP_TOKEN + "0";
    }

    private final String getBaseClassName() {
	String className = ctxt.getServletClassName();

	if (className == null) {
            if (jsp.getName().endsWith(".jsp"))
                className = jsp.getName().substring(0, jsp.getName().length() - 4);
            else
                className = jsp.getName();
        }
	
	// since we don't mangle extensions like the servlet does,
	// we need to check for keywords as class names
	for (int i = 0; i < keywords.length; i++) {
	    if (className.equals(keywords[i])) {
		className += "%";
	    };
	};
	
	// Fix for invalid characters. If you think of more add to the list.
	StringBuffer modifiedClassName = new StringBuffer();
	char c='/';
	if( Character.isDigit( className.charAt( 0 )  )) {
	    className="_" +className;
	}
	for (int i = 0; i < className.length(); i++) {
	    char prev=c;
	    c=className.charAt(i);
	    // workaround for common "//" problem. Alternative
	    // would be to encode the dot.
	    if( prev=='/' && c=='/' ) {
		continue;
	    }
	    
	    if (Character.isLetterOrDigit(c) == true ||
		c=='_' ||
		c=='/' )
		modifiedClassName.append(className.substring(i,i+1));
	    else
		modifiedClassName.append(mangleChar(className.charAt(i)));
	}
	
	return modifiedClassName.toString();
    }

    private final String getPrefix(String pathName) {
	if (pathName != null) {
	    StringBuffer modifiedName = new StringBuffer();
	    for (int i = 0; i < pathName.length(); i++) {
		if (Character.isLetter(pathName.charAt(i)) == true)
		    modifiedName.append(pathName.substring(i,i+1));
		else
		    modifiedName.append(mangleChar(pathName.charAt(i)));
 	    }
	    return modifiedName.toString();
	}
	else
            return "";
    }

    private static final String mangleChar(char ch) {
	
        if(ch == File.separatorChar) {
	    ch = '/';
	}
	String s = Integer.toHexString(ch);
	int nzeros = 5 - s.length();
	char[] result = new char[6];
	result[0] = '_';
	for (int i = 1; i <= nzeros; i++)
	    result[i] = '0';
	for (int i = nzeros+1, j = 0; i < 6; i++, j++)
	    result[i] = s.charAt(j);
	return new String(result);
    }


    public final String getPackageName() {
        return pkgName;
    }

    public final String getClassName() {
        return className;
    }

    public final String getJavaFileName() {
        return javaFileName;
    }

    public final String getClassFileName() {
        return classFileName;
    }


}
