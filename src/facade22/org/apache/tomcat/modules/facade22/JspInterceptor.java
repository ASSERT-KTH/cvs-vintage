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
package org.apache.tomcat.modules.facade22;

import javax.servlet.*;
import javax.servlet.http.*;

import javax.servlet.jsp.HttpJspPage;
import javax.servlet.jsp.JspFactory;

import java.util.*;
import java.io.*;
import java.net.*;

import org.apache.tomcat.util.log.*;
import org.apache.tomcat.util.*;
import org.apache.tomcat.util.depend.*;

import org.apache.jasper.*;
import org.apache.jasper.Constants;
import org.apache.jasper.runtime.*;
import org.apache.jasper.compiler.*;
import org.apache.jasper.compiler.Compiler;
import org.apache.tomcat.core.*;
import org.apache.tomcat.facade.*;

/**
 * Plug in the JSP engine (a.k.a Jasper)! 
 *
 * @author Anil K. Vijendran
 * @author Harish Prabandham
 * @author Costin Manolache
 */
public class JspInterceptor extends BaseInterceptor {
    TomcatOptions options=new TomcatOptions();

    static final String JIKES="org.apache.jasper.compiler.JikesJavaCompiler";

    // -------------------- Configurable properties --------------------
    
    public void setJavaCompiler( String type ) {
	// shortcut
	if( "jikes".equals( type ) )
	    type=JIKES;
	
	try {
	    options.jspCompilerPlugin=Class.forName(type);
	} catch(Exception ex ) {
	    ex.printStackTrace();
	}
    }

    // -------------------- Hooks --------------------

    /**
     * Jasper-specific initializations, add work dir to classpath,
     *  make sure we have a dependManager.
     */
    public void addContext(ContextManager cm, Context ctx)
	throws TomcatException 
    {
	// Make sure JspFactory is set ( ? )
	JspFactory.setDefaultFactory(new JspFactoryImpl());

	try {
	    URL url=new URL( "file", null,
			     ctx.getWorkDir().getAbsolutePath() + "/");
	    ctx.addClassPath( url );
	    if( debug > 9 ) log( "Added to classpath: " + url );
	} catch( MalformedURLException ex ) {
	}

	DependManager dm=ctx.getDependManager();
	if( dm==null ) {
	    dm=new DependManager();
	    ctx.setDependManager( dm );
	}
    }

    /** Set the HttpJspBase classloader before init,
     *  as required by Jasper
     */
    public void preServletInit( Context ctx, Handler sw )
	throws TomcatException
    {
	if( ! (sw instanceof ServletHandler) )
	    return;
	try {
	    // requires that everything is compiled
	    Servlet theServlet = ((ServletHandler)sw).getServlet();
	    if (theServlet instanceof HttpJspBase)  {
		if( debug > 9 )
		    log( "PreServletInit: HttpJspBase.setParentClassLoader" +
			 sw );
		HttpJspBase h = (HttpJspBase) theServlet;
		h.setClassLoader(ctx.getClassLoader());
	    }
	} catch(Exception ex ) {
	    throw new TomcatException( ex );
	}
    }

    //-------------------- Main hook - compile the jsp file if needed
    
    /** Detect if the request is for a JSP page and if it is find
	the associated servlet name and compile if needed.

	That insures that init() will take place on the equivalent
	servlet - and behave exactly like a servlet.

	A request is for a JSP if:
	- the handler is a ServletHandler ( i.e. defined in web.xml
	or dynamically loaded servlet ) and it has a "path" instead of
	class name
	- the handler has a special name "jsp". That means a *.jsp -> jsp
	needs to be defined. This is a tomcat-specific mechanism ( not
	part of the standard ) and allow users to associate other extensions
	with JSP by using the "fictious" jsp handler.

	An (cleaner?) alternative for mapping other extensions would be
	to set them on JspInterceptor.
    */
    public int requestMap( Request req ) {
	Handler wrapper=req.getHandler();

	if( wrapper==null )
	    return 0;

	// It's not a jsp if it's not "*.jsp" mapped or a servlet
	if( (! "jsp".equals( wrapper.getName())) &&
	    (! (wrapper instanceof ServletHandler)) ) {
	    return 0;
	}

	ServletHandler handler=null;
	String jspFile=null;

	// if it's an extension mapped file, construct and map a handler
	if( "jsp".equals( wrapper.getName())) {
	    jspFile=req.getServletPath();
	    // extension mapped jsp - define a new handler,
	    // add the exact mapping to avoid future overhead
	    handler= mapJspPage( req.getContext(), jspFile );
	    req.setHandler( handler );
	} else if( wrapper instanceof ServletHandler) {
	    // if it's a simple servlet, we don't care about it
	    handler=(ServletHandler)wrapper;
	    jspFile=handler.getServletInfo().getJspFile();
	    if( jspFile==null )
		return 0; // not a jsp
	}
	
	Dependency dep= handler.getServletInfo().getDependency();
	if( dep!=null && ! dep.isExpired() ) {
	    // if the jspfile is older than the class - we're ok
	    return 0;
	}

	JasperLiaison liasion=new JasperLiaison(getLog(), debug, options);
	liasion.processJspFile(req, jspFile, handler);
	return 0;
    }

    // -------------------- Utils --------------------
    
    private static final String SERVLET_NAME_PREFIX="TOMCAT/JSP";
    
    /** Add an exact map that will avoid *.jsp mapping and intermediate
     *  steps. It's equivalent with declaring
     *  <servlet-name>tomcat.jsp.[uri]</>
     *  <servlet-mapping><servlet-name>tomcat.jsp.[uri]</>
     *                   <url-pattern>[uri]</></>
     */
    ServletHandler mapJspPage( Context ctx, String uri)
    {
	String servletName= SERVLET_NAME_PREFIX + uri;

	if( debug>0)
	    log( "mapJspPage " + ctx + " " + " " + servletName + " " +  uri  );

	Handler h=ctx.getServletByName( servletName );
	if( h!= null ) {
	    log( "Name already exists " + servletName +
		 " while mapping " + uri);
	    return null; // exception ?
	}
	
	ServletHandler wrapper=new ServletHandler();
	wrapper.setModule( this );
	wrapper.setContext(ctx);
	wrapper.setName(servletName);
	wrapper.getServletInfo().setJspFile( uri );
	
	// add the mapping - it's a "invoker" map ( i.e. it
	// can be removed to keep memory under control.
	// The memory usage is smaller than JspSerlvet anyway, but
	// can be further improved.
	try {
	    ctx.addServlet( wrapper );
	    ctx.addServletMapping( uri ,
				   servletName );
	    if( debug > 0 )
		log( "Added mapping " + uri + " path=" + servletName );
	} catch( TomcatException ex ) {
	    log("mapJspPage: ctx=" + ctx +
		", servletName=" + servletName, ex);
	    return null;
	}
	return wrapper;
    }

}

// -------------------- The main Jasper Liaison --------------------

final class JasperLiaison {
    Log log;
    final int debug;
    Options options;
    
    JasperLiaison( Log log, int debug, Options options ) {
	this.log=log;
	this.debug=debug;
	this.options=options;
    }
    
    /** Generate mangled names, check for previous versions,
     *  generate the .java file, compile it - all the expensive
     *  operations. This happens only once ( or when the jsp file
     *  changes ). 
     */
    int processJspFile(Request req, String jspFile,
				ServletHandler handler)
    {
	// ---------- Expensive part - compile and load
	
	// If dep==null, the handler was never used - we need
	// to either compile it or find the previous compiled version
	// If dep.isExpired() we need to recompile.

	if( debug > 10 ) log.log( "Before compile sync  " + jspFile );
	synchronized( handler ) {
	    
	    // double check - maybe another thread did that for us
	    Dependency dep= handler.getServletInfo().getDependency();
	    if( dep!=null && ! dep.isExpired() ) {
		// if the jspfile is older than the class - we're ok
		return 0;
	    }

	    Context ctx=req.getContext();
	    
	    // Mangle the names - expensive operation, but nothing
	    // compared with a compilation :-)
	    JspMangler mangler=
		new JspMangler(ctx.getWorkDir().getAbsolutePath(),
			       ctx.getAbsolutePath(),
			       jspFile );

	    // register the handler as dependend of the jspfile 
	    if( dep==null ) {
		dep=setDependency( ctx, mangler, handler );
		// update the servlet class name
		handler.setServletClassName( mangler.getServletClassName() );

		// check again - maybe we just found a compiled class from
		// a previous run
		if( ! dep.isExpired() )
		    return 0;
	    }

	    if( debug > 3) 
		log.log( "Jsp source changed, recompiling: " + jspFile );
	    
	    //XXX old servlet -  destroy(); 
	    
	    // jump version number - the file needs to be recompiled
	    // reset the handler error, un-initialize the servlet
	    handler.setErrorException( null );
	    handler.setState( Handler.STATE_ADDED );
	    
	    // Move to the next class name
	    mangler.nextVersion();

	    // record time of attempted translate-and-compile
	    // if the compilation fails, we'll not try again
	    // until the jsp file changes
	    dep.setLastModified( System.currentTimeMillis() );

	    // Update the class name in wrapper
	    log.log( "Update class Name " + mangler.getServletClassName());
	    handler.setServletClassName( mangler.getServletClassName() );

	    compile( handler, req, mangler );
	    
	    
	    dep.setExpired( false );
	    
	}

	return 0;
    }

    /** Convert the .jsp file to a java file, then compile it to class
     */
    void compile(Handler wrapper, Request req, JspMangler mangler ) {
	if( debug > 0 ) log.log( "Generating " + mangler.getJavaFileName());
	try {
	    // make sure we have the directories
	    String javaFileName=mangler.getJavaFileName();
	    
	    File javaFile=new File(javaFileName);
	    
	    // make sure the directory is created
	    new File( javaFile.getParent()).mkdirs();

	    JspEngineContext1 ctxt = new JspEngineContext1(log,req, mangler);
	    ctxt.setOptions( options );
	    
	    Compiler compiler=new Compiler(ctxt);
	    compiler.setMangler( mangler );
	    // we will compile ourself
	    compiler.setJavaCompiler( null );
	    
	    
	    synchronized ( mangler ) {
		compiler.compile();
	    }
	    if( debug > 0 ) {
		File f = new File( mangler.getJavaFileName());
		log.log( "Created file : " + f +  " " + f.lastModified());
		
	    }
	    javac( createJavaCompiler( options ), ctxt, mangler );
	    
	    if(debug>0)log.log( "Generated " + mangler.getClassFileName() );
	} catch( Exception ex ) {
	    log.log("compile: req="+req, ex);
	    wrapper.setErrorException(ex);
	    wrapper.setState(Handler.STATE_DISABLED);
	    // until the jsp cahnges, when it'll be enabled again

	}
    }
    
    String javaEncoding = "UTF8";           // perhaps debatable?
    static String sep = System.getProperty("path.separator");

    /** Compile a java to class. This should be moved to util, togheter
	with JavaCompiler - it's a general purpose code, no need to
	keep it part of jasper
    */
    void javac(JavaCompiler javac, JspCompilationContext ctxt,
		      Mangler mangler)
	throws JasperException
    {

        javac.setEncoding(javaEncoding);
	String cp=System.getProperty("java.class.path")+ sep + 
	    ctxt.getClassPath() + sep + ctxt.getOutputDir();
        javac.setClasspath( cp );
	if( debug>5) log.log( "ClassPath " + cp);
	
	ByteArrayOutputStream out = new ByteArrayOutputStream (256);
	javac.setOutputDir(ctxt.getOutputDir());
        javac.setMsgOutput(out);

	String javaFileName = mangler.getJavaFileName();
	if( debug>0) log.log( "Compiling java file " + javaFileName);
	/**
         * Execute the compiler
         */
        boolean status = javac.compile(javaFileName);

        if (!ctxt.keepGenerated()) {
            File javaFile = new File(javaFileName);
            javaFile.delete();
        }
    
        if (status == false) {
            String msg = out.toString ();
            throw new JasperException("Unable to compile "
                                      + msg);
        }
	if( debug > 0 ) log.log("Compiled ok");
    }

    /** tool for customizing javac
     */
    public JavaCompiler createJavaCompiler(Options options)
	throws JasperException
    {
	String compilerPath = options.getJspCompilerPath();
	Class jspCompilerPlugin = options.getJspCompilerPlugin();
        JavaCompiler javac;

	if (jspCompilerPlugin != null) {
            try {
                javac = (JavaCompiler) jspCompilerPlugin.newInstance();
            } catch (Exception ex) {
		Constants.message("jsp.warning.compiler.class.cantcreate",
				  new Object[] { jspCompilerPlugin, ex }, 
				  Logger.FATAL);
                javac = new SunJavaCompiler();
	    }
	} else {
            javac = new SunJavaCompiler();
	}

        if (compilerPath != null)
            javac.setCompilerPath(compilerPath);

	return javac;
    }

    private Dependency setDependency( Context ctx, JspMangler mangler,
				ServletHandler handler )
    {
	ServletInfo info=handler.getServletInfo();
	// create a lastModified checker.
	if( debug>0) log.log("Registering dependency for " + handler );
	Dependency dep=new Dependency();
	dep.setOrigin( new File(mangler.getJspFilePath()) );
	dep.setTarget( handler );
	dep.setLocal( true );
	if( mangler.getVersion() > 0 ) {
	    // it has a previous version
	    File f=new File( mangler.getClassFileName() );
	    dep.setLastModified(f.lastModified());
	    // update the "expired" variable
	    dep.checkExpiry();
	} else {
	    dep.setLastModified( -1 );
	    dep.setExpired( true );
	}
	ctx.getDependManager().addDependency( dep );
	info.setDependency( dep );
	return dep;
    }
	

}

// -------------------- Jasper support - options --------------------

class TomcatOptions implements Options {
    public boolean keepGenerated = true;
    public boolean largeFile = false;
    public boolean mappedFile = false;
    public boolean sendErrorToClient = false;
    public String ieClassId = "clsid:8AD9C840-044E-11D1-B3E9-00805F499D93";
    public Class jspCompilerPlugin = null;
    public String jspCompilerPath = null;
    public int debug=0;
    
    public File scratchDir;
    private Object protectionDomain;
    public String classpath = null;

    public boolean getKeepGenerated() {
        return keepGenerated;
    }

    public boolean getLargeFile() {
        return largeFile;
    }

    public boolean getMappedFile() {
        return mappedFile;
    }
    
    public boolean getSendErrorToClient() {
        return sendErrorToClient;
    }
 
    public String getIeClassId() {
        return ieClassId;
    }

    public void setScratchDir( File f ) {
	scratchDir=f;
    }
    
    public File getScratchDir() {
	if( debug>0 ) log("Options: getScratchDir " + scratchDir);
        return scratchDir;
    }

    public final Object getProtectionDomain() {
	if( debug>0 ) log("Options: GetPD" );
	return protectionDomain;
    }

    public String getClassPath() {
	if( debug>0 ) log("Options: GetCP " + classpath  );
        return classpath;
    }

    public Class getJspCompilerPlugin() {
        return jspCompilerPlugin;
    }

    public String getJspCompilerPath() {
        return jspCompilerPath;
    }

    void log(String s) {
	System.err.println(s);
    }
    
}

// -------------------- Jasper support - JspCompilationContext -------------

class JspEngineContext1 implements JspCompilationContext {
    JspReader reader;
    ServletWriter writer;
    ServletContext context;
    JspLoader loader;
    String classpath; // for compiling JSPs.
    boolean isErrPage;
    String jspFile;
    String servletClassName;
    String servletPackageName;
    String servletJavaFileName;
    String contentType;
    Options options;
    public int debug=0;
    
    Request req;
    Mangler m;
    Log log;
    
    public JspEngineContext1(Log log, Request req, Mangler m)
    {
	this.log=log;
	this.req=req;
	this.m=m;
    }

    public HttpServletRequest getRequest() {
	if( debug>0 ) log.log("getRequest " + req );
        return (HttpServletRequest)req.getFacade();
    }
    

    /**
     * Get the http response we are using now...
     */
    public HttpServletResponse getResponse() {
	if( debug>0 ) log.log("getResponse " );
        return (HttpServletResponse)req.getResponse().getFacade();
    }

    /**
     * The classpath that is passed off to the Java compiler. 
     */
    public String getClassPath() {
	Context ctx=req.getContext();
	URL classP[]=ctx.getClassPath();
	String separator = System.getProperty("path.separator", ":");
        String cpath = "";
	
        for(int i=0; i< classP.length; i++ ) {
            URL cp = classP[i];
            File f = new File( cp.getFile());
            if (cpath.length()>0) cpath += separator;
            cpath += f;
        }

	if( debug>0 ) log.log("getClassPath " + cpath);
	return cpath;
    }
    
    /**
     * Get the input reader for the JSP text. 
     */
    public JspReader getReader() {
	if( debug>0 ) log.log("getReader " + reader );
        return reader;
    }
    
    /**
     * Where is the servlet being generated?
     */
    public ServletWriter getWriter() {
	if( debug>0 ) log.log("getWriter " + writer );
        return writer;
    }
    
    /**
     * Get the ServletContext for the JSP we're processing now. 
     */
    public ServletContext getServletContext() {
	if( debug>0 ) log.log("getCtx " +
			   req.getContext().getFacade());
        return (ServletContext)req.getContext().getFacade();
    }
    
    /**
     * What class loader to use for loading classes while compiling
     * this JSP? I don't think this is used right now -- akv. 
     */
    public ClassLoader getClassLoader() {
	if( debug>0 ) log.log("getLoader " + loader );
        return req.getContext().getClassLoader();
    }

    public void addJar( String jar ) throws IOException {
	if( debug>0 ) log.log("Add jar " + jar);
	//loader.addJar( jar );
    }

    /**
     * Are we processing something that has been declared as an
     * errorpage? 
     */
    public boolean isErrorPage() {
	if( debug>0 ) log.log("isErrorPage " + isErrPage );
        return isErrPage;
    }
    
    /**
     * What is the scratch directory we are generating code into?
     * FIXME: In some places this is called scratchDir and in some
     * other places it is called outputDir.
     */
    public String getOutputDir() {
	if( debug>0 ) log.log("getOutputDir " +
			   req.getContext().getWorkDir().getAbsolutePath());
        return req.getContext().getWorkDir().getAbsolutePath();
    }
    
    /**
     * Path of the JSP URI. Note that this is not a file name. This is
     * the context rooted URI of the JSP file. 
     */
    public String getJspFile() {
	String sP=req.getServletPath();
	Context ctx=req.getContext();
	if( debug>0 ) log.log("getJspFile " +
			   sP);//   ctx.getRealPath( sP ) );
	//        return ctx.getRealPath( sP );
	return sP;
    }
    
    /**
     * Just the class name (does not include package name) of the
     * generated class. 
     */
    public String getServletClassName() {
	if( debug>0 ) log.log("getServletClassName " +
			   m.getClassName());
        return m.getClassName();
    }
    
    /**
     * The package name into which the servlet class is generated. 
     */
    public String getServletPackageName() {
	if( debug>0 ) log.log("getServletPackageName " +
			   servletPackageName );
        return servletPackageName;
    }

    /**
     * Utility method to get the full class name from the package and
     * class name. 
     */
    public final String getFullClassName() {
	if( debug>0 ) log.log("getServletPackageName " +
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
	if( debug>0 ) log.log("getServletPackageName " +
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
    
    public void setServletClassName(String servletClassName) {
        this.servletClassName = servletClassName;
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
	if( debug>0 ) log.log("createCompiler ");
	return null;
    }
    
    public String resolveRelativeUri(String uri)
    {
	if( debug>0 ) log.log("resolveRelativeUri " +
				 uri);
	return null;
    };    

    public java.io.InputStream getResourceAsStream(String res)
    {
	if( debug>0 ) log.log("getResourceAsStream " + res);
        ServletContext sctx=(ServletContext)req.getContext().getFacade();
	return sctx.getResourceAsStream(res);
    };

    /** 
     * Gets the actual path of a URI relative to the context of
     * the compilation.
     */
    public String getRealPath(String path)
    {
	if( debug>0 ) log.log("GetRP " + path);
	Context ctx=req.getContext();
	return FileUtil.safePath( ctx.getAbsolutePath(),
				  path);
    };
}

// -------------------- Jasper support - mangler --------------------

final class JspMangler implements Mangler{

    public JspMangler(String workDir, String docBase, String jspFile)
    {
	this.jspFile=jspFile;
	this.workDir=workDir;
	this.docBase=docBase;
	init();
    }

    /** Versioned class name ( without package ).
     */
    public String getClassName() {
	return JavaGeneratorTool.getVersionedName( baseClassN, version );
    }
    
    /**
     *   Full path to the generated java file ( including version )
     */
    public String getJavaFileName() {
	return javaFileName;
    }

    /** The package name ( "." separated ) of the generated
     *  java file
     */
    public String getPackageName() {
	if( pkgDir!=null ) {
	    return pkgDir.replace('/', '.');
	} else {
	    return null;
	}
    }

    // -------------------- JspInterceptor fields --------------------
    
    /** Returns the jsp file, as declared by <jsp-file> in server.xml
     *  or the context-relative path that was extension mapped to jsp
     */
    public String getJspFile() {
	return jspFile;
    }

    /** Returns the directory where the class is located, using
     *  the normal class loader rules.
     */
    public String getClassDir() {
	return classDir;
    }
    
    /** The class name ( package + class + versioning ) of the
     *  compilation result
     */
    public String getServletClassName() {
	if( pkgDir!=null ) {
	    return getPackageName()  + "." + getClassName();
	} else {
	    return getClassName();
	}
    }

    public int getVersion() {
	return version;
    }

    /** Full path to the compiled class file ( including version )
     */
    public String getClassFileName() {
	return classFileName;
    }
    // In Jasper = not used - it's specific to the class scheme
    // used by JspServlet
    // Full path to the class file - without version.
    

    public String getBaseClassName() {
	return baseClassN;
    }

    public String getPackageDir() {
	return pkgDir;
    }
    
    public String getJspFilePath() {
	return FileUtil.safePath( docBase, jspFile);
    }

    /** compute basic names - pkgDir and baseClassN
     */
    private void init() {
	int lastComp=jspFile.lastIndexOf(  "/" );

	if( lastComp > 0 ) {
	    // has package 
	    // ignore the first "/" of jspFile
	    pkgDir=jspFile.substring( 1, lastComp );
	}
	
	// remove "special" words, replace "."
	if( pkgDir!=null ) {
	    pkgDir=JavaGeneratorTool.manglePackage(pkgDir);
	    pkgDir=pkgDir.replace('.', '_');
	    classDir=workDir + "/" + pkgDir;
	} else {
	    classDir=workDir;
	}
	
	int extIdx=jspFile.lastIndexOf( "." );

	if( extIdx<0 ) {
	    // no "." 
	    if( lastComp > 0 )
		baseClassN=jspFile.substring( lastComp+1 );
	    else
		baseClassN=jspFile.substring( 1 );
	} else {
	    if( lastComp > 0 )
		baseClassN=jspFile.substring( lastComp+1, extIdx );
	    else
		baseClassN=jspFile.substring( 1, extIdx );
	}

	System.out.println("XXXMangler: " + jspFile + " " + pkgDir + " " + baseClassN);

	// extract version from the .class dir, using the base name
	version=JavaGeneratorTool.readVersion(classDir,
					      baseClassN);
	if( version==-1 ) {
	    version=0;
	}
	updateVersionPaths();
    }

    private void updateVersionPaths() {
	// version dependent stuff
	String baseName=classDir + "/" + JavaGeneratorTool.
	    getVersionedName( baseClassN, version);
	
	javaFileName= baseName + ".java";

	classFileName=baseName +  ".class";
    }
    
    /** Move to a new class name, if a changes has been detected.
     */
    void nextVersion() {
	version++;
	JavaGeneratorTool.writeVersion( getClassDir(), baseClassN, version);
	updateVersionPaths();
    }

    // context-relative jsp path 
    // extracted from the <jsp-file> or the result of a *.jsp mapping
    private String jspFile; 
    // version of the compiled java file
    private int version;
    private String workDir;
    private String docBase;
    // the "/" separted version
    private String pkgDir;
    // class name without package and version
    private String baseClassN;
    private String classDir;
    private String javaFileName;
    private String classFileName;
}

