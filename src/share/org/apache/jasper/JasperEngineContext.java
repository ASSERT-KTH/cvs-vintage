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
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.jasper;

import java.io.IOException;

import javax.servlet.ServletContext;

import org.apache.jasper.compiler.Compiler;
import org.apache.jasper.compiler.JspReader;
import org.apache.jasper.compiler.ServletWriter;

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
	if (uri.charAt(0) == '/') {
	    return uri;
        } else {
            String baseURI = jspFile.substring(0, jspFile.lastIndexOf('/'));
            return baseURI + '/' + uri;
        }
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
