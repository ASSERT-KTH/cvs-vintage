/*
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
 */ 
package org.apache.jasper;

import javax.servlet.*;
import javax.servlet.http.*;
import org.apache.jasper.compiler.JspReader;
import org.apache.jasper.compiler.ServletWriter;
import org.apache.jasper.compiler.Compiler;

import java.util.*;
import java.io.*;
import java.net.*;

import org.apache.jasper.JspCompilationContext;

/** Alternative implementation of JspCompilationContext ( in addition
    to the servlet and standalone ). Used by JspInterceptor - but
    it's in no way specific to tomcat.
*/
public class JasperEngineContext implements JspCompilationContext {
    JspReader reader;
    ServletWriter writer;
    ServletContext context;
    ClassLoader loader;
    boolean isErrPage;
    String jspFile;
    String servletClassName;
    String servletPackageName;
    String servletJavaFileName;
    String contentType;
    Options options;

    String cpath;    // for compiling JSPs.
    ServletContext sctx;
    String outputDir;

    public JasperEngineContext()
    {
    }

    public void setClassPath( String s ) {
	cpath=s;
    }
    
    /**
     * The classpath that is passed off to the Java compiler. 
     */
    public String getClassPath() {
	return cpath;
    }
    
    /**
     * Get the input reader for the JSP text. 
     */
    public JspReader getReader() {
	if( debug>0 ) log("getReader " + reader );
        return reader;
    }
    
    /**
     * Where is the servlet being generated?
     */
    public ServletWriter getWriter() {
	if( debug>0 ) log("getWriter " + writer );
        return writer;
    }

    public void setServletContext( Object o ) {
	sctx=(ServletContext)o;
    }
    
    /**
     * Get the ServletContext for the JSP we're processing now. 
     */
    public ServletContext getServletContext() {
	if( debug>0 ) log("getCtx " + sctx );
        return sctx;
    }
    
    /**
     * What class loader to use for loading classes while compiling
     * this JSP? I don't think this is used right now -- akv. 
     */
    public ClassLoader getClassLoader() {
	if( debug>0 ) log("getLoader " + loader );
        return loader;
    }

    public void setClassLoader( ClassLoader cl ) {
	loader=cl;
    }

    public void addJar( String jar ) throws IOException {
	if( debug>0 ) log("Add jar " + jar);
	//loader.addJar( jar );
    }

    /**
     * Are we processing something that has been declared as an
     * errorpage? 
     */
    public boolean isErrorPage() {
	if( debug>0 ) log("isErrorPage " + isErrPage );
        return isErrPage;
    }
    
    /**
     * What is the scratch directory we are generating code into?
     * FIXME: In some places this is called scratchDir and in some
     * other places it is called outputDir.
     */
    public String getOutputDir() {
	if( debug>0 ) log("getOutputDir " + outputDir  );
        return outputDir;
    }

    public void setOutputDir(String s ) {
	outputDir=s;
    }
    
    /**
     * Path of the JSP URI. Note that this is not a file name. This is
     * the context rooted URI of the JSP file. 
     */
    public String getJspFile() {
	if( debug>0 ) log("getJspFile " +  jspFile);
	return jspFile;
    }

    public void setJspFile( String s ) {
	jspFile=s;
    }
    
    /**
     * Just the class name (does not include package name) of the
     * generated class. 
     */
    public String getServletClassName() {
	if( debug>0 ) log("getServletClassName " +  servletClassName);
        return servletClassName;
    }

    public void setServletClassName( String s ) {
	servletClassName=s;
    }
    
    /**
     * The package name into which the servlet class is generated. 
     */
    public String getServletPackageName() {
	if( debug>0 ) log("getServletPackageName " +
			   servletPackageName );
        return servletPackageName;
    }

    /**
     * Utility method to get the full class name from the package and
     * class name. 
     */
    public final String getFullClassName() {
	if( debug>0 ) log("getServletPackageName " +
			   servletPackageName + "." + servletClassName);
        if (servletPackageName == null)
            return servletClassName;
        return servletPackageName + "." + servletClassName;
    }

    /**
     * Full path name of the Java file into which the servlet is being
     * generated. 
     */
    public String getServletJavaFileName() {
	if( debug>0 ) log("getServletPackageName " +
			   servletPackageName + "." + servletClassName);
        return servletJavaFileName;
    }

    /**
     * Are we keeping generated code around?
     */
    public boolean keepGenerated() {
        return options.getKeepGenerated();
    }

    /**
     * What's the content type of this JSP? Content type includes
     * content type and encoding. 
     */
    public String getContentType() {
        return contentType;
    }

    /**
     * Get hold of the Options object for this context. 
     */
    public Options getOptions() {
        return options;
    }

    public void setOptions(Options options) {
	this.options=options;
    }
    
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public void setReader(JspReader reader) {
        this.reader = reader;
    }
    
    public void setWriter(ServletWriter writer) {
        this.writer = writer;
    }
    
    public void setServletPackageName(String servletPackageName) {
        this.servletPackageName = servletPackageName;
    }
    
    public void setServletJavaFileName(String servletJavaFileName) {
        this.servletJavaFileName = servletJavaFileName;
    }
    
    public void setErrorPage(boolean isErrPage) {
        this.isErrPage = isErrPage;
    }

    public Compiler createCompiler() throws JasperException {
	if( debug>0 ) log("createCompiler ");
	return null;
    }
    
    public String resolveRelativeUri(String uri)
    {
	if( debug>0 ) log("resolveRelativeUri " + uri);
	return null;
    }

    public java.io.InputStream getResourceAsStream(String res)
    {
	if( debug>0 ) log("getResourceAsStream " + res);
	return sctx.getResourceAsStream(res);
    }

    /** 
     * Gets the actual path of a URI relative to the context of
     * the compilation.
     */
    public String getRealPath(String path)
    {
	if( debug>0 ) log("getRealPath " + path + " = " +
			  sctx.getRealPath( path ));
	return sctx.getRealPath( path );
    }

    // development tracing 
    private static int debug=0;
    private void log( String s ) {
	System.out.println("JasperEngineContext: "+ s);
    }
}
