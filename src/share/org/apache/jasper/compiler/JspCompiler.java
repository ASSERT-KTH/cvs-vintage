/*
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

import org.apache.jasper.Constants;
import org.apache.jasper.JasperException;
import org.apache.jasper.JspCompilationContext;
import org.apache.tomcat.util.log.Log;

/**
 * JspCompiler is an implementation of Compiler with a funky code
 * mangling and code generation scheme!
 *
 * The reason that it is both a sub-class of compiler and an implementation
 * of mangler is because the isOutDated method that is overridden and the
 * name mangulation both depend on the actual existance of other class and
 * java files.  I.e. the value of a mangled name is a function of both the
 * name to be mangled and also of the state of the scratchdir.
 *
 * @author Anil K. Vijendran
 */
public class JspCompiler extends Compiler implements Mangler {
    
    String pkgName, javaFileName, classFileName;
    String realClassName;

    File jsp;
    String outputDir;

    //    ClassFileData cfd;
    boolean outDated;
    static final int JSP_TOKEN_LEN= Constants.JSP_TOKEN.length();

    Log loghelper = Log.getLog("JASPER_LOG", "JspCompiler");
    
    public JspCompiler(JspCompilationContext ctxt) throws JasperException {
        super(ctxt);
        
        this.jsp = new File(ctxt.getJspFile());
        this.outputDir = ctxt.getOutputDir();
        this.outDated = false;
        setMangler(this);

	// If the .class file exists and is outdated, compute a new
	// class name
	if( isOutDated() ) {
	    generateNewClassName();
	}
    }

    private void generateNewClassName() {
	File classFile = new File(getClassFileName());
	if (! classFile.exists()) {
	     String prefix = getPrefix(jsp.getPath());
	     realClassName= prefix + getBaseClassName() +
		 Constants.JSP_TOKEN + "0";
	    return;
	} 

	String cn=getRealClassName();
	String baseClassName = cn.
	    substring(0, cn.lastIndexOf(Constants.JSP_TOKEN));
	int jspTokenIdx=cn.lastIndexOf(Constants.JSP_TOKEN);
	String versionS=cn.substring(jspTokenIdx + JSP_TOKEN_LEN,
				     cn.length());
	int number= Integer.valueOf(versionS).intValue();
	number++;
	realClassName = baseClassName + Constants.JSP_TOKEN + number;
    }
    
    /** Return the real class name for the JSP, including package and
     *   version.
     *
     *  This method is called when the server is started and a .class file
     *  is found from a previous compile or when the .class file is older,
     *  to find next version.
     */
    public final String getRealClassName() {
	if( realClassName!=null ) return realClassName;

        try {
            realClassName = ClassName.getClassName( getClassFileName() );
        } catch( JasperException ex) {
            // ops, getClassName should throw something
	    loghelper.log("Exception in getRealClassName", ex);
	    return null;
        }
	return realClassName;

    }
    
    public final String getClassName() {
        // CFD gives you the whole class name
        // This method returns just the class name sans the package

	String cn=getRealClassName();
        int lastDot = cn.lastIndexOf('.');
	String className=null;
        if (lastDot != -1) 
            className = cn.substring(lastDot+1,
                                     cn.length());
        else // no package name case
            className = cn;

        return className;
    }

    public final String getJavaFileName() {
        if( javaFileName!=null ) return javaFileName;
	javaFileName = getClassName() + ".java";
 	if (outputDir != null && !outputDir.equals(""))
 	    javaFileName = outputDir + File.separatorChar + javaFileName;
	return javaFileName;
    }
    
    public final String getClassFileName() {
        if( classFileName!=null) return classFileName;

	//        computeClassFileName();
        String prefix = getPrefix(jsp.getPath());
        classFileName = prefix + getBaseClassName() + ".class";
	if (outputDir != null && !outputDir.equals(""))
	    classFileName = outputDir + File.separatorChar + classFileName;
	return classFileName;
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

    public final String getPackageName() {
        if( pkgName!=null) return pkgName;

	// compute package name 
	String pathName = jsp.getPath();
	StringBuffer modifiedpkgName = new StringBuffer ();
        int indexOfSepChar = pathName.lastIndexOf(File.separatorChar);
        
	if (indexOfSepChar == -1 || indexOfSepChar == 0)
	    pkgName = null;
	else {
	    for (int i = 0; i < keywords.length; i++) {
		char fs = File.separatorChar;
		int index1 = pathName.indexOf(fs + keywords[i]);
		int index2 = pathName.indexOf(keywords[i]);
		if (index1 == -1 && index2 == -1) continue;
		int index = (index2 == -1) ? index1 : index2;
		while (index != -1) {
		    String tmpathName = pathName.substring (0,index+1) + '%';
		    pathName = tmpathName + pathName.substring (index+2);
		    index = pathName.indexOf(fs + keywords[i]);
		}
	    }
	    
	    // XXX fix for paths containing '.'.
	    // Need to be more elegant here.
            pathName = pathName.replace('.','_');
	    
	    pkgName = pathName.substring(0, pathName.lastIndexOf(
	    		File.separatorChar)).replace(File.separatorChar, '.');
	    for (int i=0; i<pkgName.length(); i++) 
		if (Character.isLetter(pkgName.charAt(i)) == true ||
		    pkgName.charAt(i) == '.') {
		    modifiedpkgName.append(pkgName.substring(i,i+1));
		}
		else
		    modifiedpkgName.append(mangleChar(pkgName.charAt(i)));

	    if (modifiedpkgName.charAt(0) == '.') {
                String modifiedpkgNameString = modifiedpkgName.toString();
                pkgName = modifiedpkgNameString.
		    substring(1, 
			      modifiedpkgName.length ());
            }
	    else 
	        pkgName = modifiedpkgName.toString();
	}
	return pkgName;
    }

    private final String getBaseClassName() {
	String className;
        
        if (jsp.getName().endsWith(".jsp"))
            className = jsp.getName().substring(0, jsp.getName().length() - 4);
        else
            className = jsp.getName();
            
	
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

    /**
     * Determines whether the current JSP class is older than the JSP file
     * from whence it came
     */
    public boolean isOutDated() {
        File jspReal = null;

        String realPath = ctxt.getRealPath(jsp.getPath());
        if (realPath == null)
            return true;

        jspReal = new File(realPath);

		  if(!jspReal.exists()){
			  return true;
		  }

		  File classFile = new File(getClassFileName());
        if (classFile.exists()) {
            outDated = classFile.lastModified() < jspReal.lastModified();
        } else {
            outDated = true;
        }

        return outDated;
    }
}

