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

package org.apache.jasper.servlet;

import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.SingleThreadModel;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.HttpJspPage;
import javax.servlet.jsp.JspFactory;

import java.util.Hashtable;
import java.util.Enumeration;
import java.io.File;
import java.io.PrintWriter;
import java.io.IOException;
import java.io.FileNotFoundException;

import org.apache.jasper.JasperException;
import org.apache.jasper.Constants;
import org.apache.jasper.Options;
import org.apache.jasper.EmbededServletOptions;
import org.apache.jasper.JspCompilationContext;
import org.apache.jasper.JspEngineContext;
import org.apache.jasper.compiler.Mangler;
import org.apache.jasper.runtime.*;

import org.apache.jasper.compiler.Compiler;

import org.apache.tomcat.util.log.*;

/**
 * The JSP engine (a.k.a Jasper)! 
 *
 * @author Anil K. Vijendran
 * @author Harish Prabandham
 */
public class JspServlet extends HttpServlet {

    Log loghelper = new Log("JASPER_LOG", "JspServlet");

    class JspServletWrapper {
        Servlet theServlet;
	String jspUri;
	boolean isErrorPage;
	// ServletWrapper will set this 
	Class servletClass;
	
	JspServletWrapper(String jspUri, boolean isErrorPage) {
	    this.jspUri = jspUri;
	    this.isErrorPage = isErrorPage;
	    this.theServlet = null;
	}
	
	private void load() throws JasperException, ServletException {
	    try {
		// Class servletClass = (Class) loadedJSPs.get(jspUri);
		// This is to maintain the original protocol.
		destroy();

		theServlet = (Servlet) servletClass.newInstance();
	    } catch (Exception ex) {
		throw new JasperException(ex);
	    }
	    theServlet.init(JspServlet.this.config);
	    if (theServlet instanceof HttpJspBase)  {
                HttpJspBase h = (HttpJspBase) theServlet;
                h.setClassLoader(JspServlet.this.parentClassLoader);
	    }
	}
	
	private void loadIfNecessary(HttpServletRequest req, HttpServletResponse res) 
            throws JasperException, ServletException, FileNotFoundException 
        {
            // First try context attribute; if that fails then use the 
            // classpath init parameter. 

            // Should I try to concatenate them if both are non-null?

            String cp = (String) context.getAttribute(Constants.SERVLET_CLASSPATH);

            String accordingto;

            if (cp == null || cp.equals("")) {
                accordingto = "according to the init parameter";
                cp = options.getClassPath();
            } else 
                accordingto = "according to the Servlet Engine";
            
            Constants.message("jsp.message.cp_is", 
                              new Object[] { 
                                  accordingto,
                                  cp == null ? "" : cp
                              }, 
                              Logger.INFORMATION);

            if (loadJSP(jspUri, cp, isErrorPage, req, res) 
                    || theServlet == null) {
                load();
            }
	}
	
	public void service(HttpServletRequest request, 
			    HttpServletResponse response,
			    boolean precompile)
	    throws ServletException, IOException, FileNotFoundException 
	{
            try {
                loadIfNecessary(request, response);

		// If a page is to only to be precompiled return.
		if (precompile)
		    return;

		if (theServlet instanceof SingleThreadModel) {
		    // sync on the wrapper so that the freshness
		    // of the page is determined right before servicing
		    synchronized (this) {
			theServlet.service(request, response);
		    }
		} else {
		    theServlet.service(request, response);
		}

            } catch (FileNotFoundException ex) {
		try {
		    response.sendError(HttpServletResponse.SC_NOT_FOUND, 
				       Constants.getString
				       ("jsp.error.file.not.found", 
					new Object[] {
					    ex.getMessage()
					}));
		} catch (IllegalStateException ise) {
		    Constants.jasperLog.log(Constants.getString
					    ("jsp.error.file.not.found",
					     new Object[] {
						 ex.getMessage()
					     }), ex,
					    Logger.ERROR);
		}
                return;
            }
	}

	public void destroy() {
	    if (theServlet != null)
		theServlet.destroy();
	}
    }
	
	
    protected ServletContext context = null;
    protected Hashtable jsps = new Hashtable();
    //    protected Hashtable loadedJSPs = new Hashtable();
    protected ServletConfig config;
    protected JasperLoader loader;
    protected Options options;
    protected ClassLoader parentClassLoader;
    protected ServletEngine engine;
    protected String serverInfo;

    static boolean firstTime = true;
    static boolean jdk12=false;
    static {
	try {
	    Class.forName( "java.security.PrivilegedAction" );
	    jdk12=true;
	} catch(Throwable ex ) {
	}
    }

    public void init(ServletConfig config)
	throws ServletException
    {
	super.init(config);
	this.config = config;
	this.context = config.getServletContext();
        this.serverInfo = context.getServerInfo();
        
	options = new EmbededServletOptions(config, context);

	parentClassLoader = (ClassLoader) context.getAttribute(Constants.SERVLET_CLASS_LOADER);
	if (parentClassLoader == null)
	    parentClassLoader = this.getClass().getClassLoader();
            
	// getClass().getClassLoader() returns null in JDK 1.1.6/1.1.8
	if (parentClassLoader != null) {
            Constants.message("jsp.message.parent_class_loader_is", 
                              new Object[] {
                                  parentClassLoader.toString()
                              }, Logger.DEBUG);
            }
	else {
            Constants.message("jsp.message.parent_class_loader_is", 
                              new Object[] {
                                  "<none>"
                              }, Logger.DEBUG);
	}
	//	System.out.println("JspServlet: init " + config.getServletName() );
	if( loader==null ) {
	    if( jdk12 ) {
		try {
		    Class ld=Class.forName("org.apache.jasper.servlet.JasperLoader12");
		    loader=(JasperLoader)ld.newInstance();
		} catch(Throwable t ) {
		    loghelper.log("Loading org.apache.jasper.servlet.JasperLoader12", t);
		}
	    }
	    if( loader==null )
		loader = new org.apache.jasper.servlet.JasperLoader();

	    loader.setParentClassLoader(parentClassLoader);
	    loader.setOptions(options);
	    Object pd=context.getAttribute("org.apache.tomcat.protection_domain");
	    loader.setProtectionDomain( pd );
	}
	if (firstTime) {
	    firstTime = false;
	    Constants.message("jsp.message.scratch.dir.is", 
			      new Object[] { 
				  options.getScratchDir().toString() 
			      }, Logger.INFORMATION );
	    Constants.message("jsp.message.dont.modify.servlets", Logger.INFORMATION);
	    JspFactory.setDefaultFactory(new JspFactoryImpl());
	}
    }

    private void serviceJspFile(HttpServletRequest request, 
      				HttpServletResponse response, String jspUri, 
				Throwable exception, boolean precompile) 
	throws ServletException, IOException
    {
	boolean isErrorPage = exception != null;
	
	JspServletWrapper wrapper = (JspServletWrapper) jsps.get(jspUri);
	if (wrapper == null) {
	    wrapper = new JspServletWrapper(jspUri, isErrorPage);
	    jsps.put(jspUri, wrapper);
	}
	
	wrapper.service(request, response, precompile);
    }


    boolean preCompile(HttpServletRequest request) 
        throws ServletException 
    {
        boolean precompile = false;
        String precom = request.getParameter(Constants.PRECOMPILE);
        String qString = request.getQueryString();
        
        if (precom != null) {
            if (precom.equals("true")) 
                precompile = true;
            else if (precom.equals("false")) 
                precompile = false;
            else {
		    // This is illegal.
		    throw new ServletException("Can't have request parameter " +
					       Constants.PRECOMPILE + " set to " + precom);
		}
	    }
        else if (qString != null && (qString.startsWith(Constants.PRECOMPILE) ||
                                     qString.indexOf("&" + Constants.PRECOMPILE)
                                     != -1))
            precompile = true;

        return precompile;
    }
    
    

    public void service (HttpServletRequest request, 
    			 HttpServletResponse response)
	throws ServletException, IOException
    {
	try {
            String includeUri 
                = (String) request.getAttribute(Constants.INC_SERVLET_PATH);

            String jspUri;

            if (includeUri == null)
		jspUri = request.getServletPath();
            else
                jspUri = includeUri;

//	    System.out.println("JspServletWrapper: " + includeUri + " " +
//                            jspUri + 
// 			       (String) request.getAttribute(
//                                  Constants.INC_REQUEST_URI));

            boolean precompile = preCompile(request);

	    Logger jasperLog = Constants.jasperLog;
	    
            if (jasperLog != null &&
		jasperLog.matchVerbosityLevel(Logger.INFORMATION))
		{
		    jasperLog.log("JspEngine --> "+jspUri);
		    jasperLog.log("\t     ServletPath: "+request.getServletPath());
		    jasperLog.log("\t        PathInfo: "+request.getPathInfo());
		    jasperLog.log("\t        RealPath: "
				  +getServletConfig().getServletContext().getRealPath(jspUri));
		    jasperLog.log("\t      RequestURI: "+request.getRequestURI());
		    jasperLog.log("\t     QueryString: "+request.getQueryString());
		    jasperLog.log("\t  Request Params: ");
		    Enumeration e = request.getParameterNames();
		    while (e.hasMoreElements()) {
			String name = (String) e.nextElement();
			jasperLog.log("\t\t "+name+" = "+request.getParameter(name));
		    }
		}
            serviceJspFile(request, response, jspUri, null, precompile);
	} catch (RuntimeException e) {
	    throw e;
	} catch (ServletException e) {
	    throw e;
	} catch (IOException e) {
	    throw e;
	} catch (Throwable e) {
	    throw new ServletException(e);
	}

	// It's better to throw the exception - we don't
	// know if we can deal with sendError ( buffer may be
	// commited )
	// catch (Throwable t) {
	// 	    unknownException(response, t);
	// 	}
    }

    public void destroy() {
	if (Constants.jasperLog != null)
	    Constants.jasperLog.log("JspServlet.destroy()", Logger.INFORMATION);

	Enumeration servlets = jsps.elements();
	while (servlets.hasMoreElements()) 
	    ((JspServletWrapper) servlets.nextElement()).destroy();
    }


    /*  Check if we need to reload a JSP page.
     *
     *  Side-effect: re-compile the JSP page.
     *
     *  @param classpath explicitly set the JSP compilation path.
     *  @return true if JSP files is newer
     */
    boolean loadJSP(String jspUri, String classpath, 
	boolean isErrorPage, HttpServletRequest req, HttpServletResponse res) 
	throws JasperException, FileNotFoundException 
    {
	// Loader knows how to set the right priviledges, and call
	// doLoadeJsp
	return loader.loadJSP( this,jspUri, classpath, isErrorPage, req, res );
    }

    /*  Check if we need to reload a JSP page.
     *
     *  Side-effect: re-compile the JSP page.
     *
     *  @param classpath explicitly set the JSP compilation path.
     *  @return true if JSP files is newer
     */
    protected boolean doLoadJSP(String jspUri, String classpath, 
	boolean isErrorPage, HttpServletRequest req, HttpServletResponse res) 
	throws JasperException, FileNotFoundException 
    {
	JspServletWrapper jsw=(JspServletWrapper) jsps.get(jspUri);
	if( jsw==null ) {
	    throw new JasperException("Can't happen - JspServletWrapper=null");
	}
	//	Class jspClass = (Class) loadedJSPs.get(jspUri);
	boolean firstTime = jsw.servletClass == null;
        JspCompilationContext ctxt = new JspEngineContext(loader, classpath,
                                                     context, jspUri, 
                                                     isErrorPage, options,
                                                     req, res);
	boolean outDated = false; 

        Compiler compiler = ctxt.createCompiler();
        
        try {
            outDated = compiler.compile();
            if ( (jsw.servletClass == null) || (compiler.isOutDated()) ) {
                synchronized ( this ) {
                    if ((jsw.servletClass == null) ||
			(compiler.isOutDated() ))  {
                        outDated = compiler.compile();
                    }
		}
            }
        } catch (FileNotFoundException ex) {
            throw ex;
        } catch (JasperException ex) {
            throw ex;
        } catch (Exception ex) {
	    throw new JasperException(Constants.getString("jsp.error.unable.compile"),
                                      ex);
	}

	// Reload only if it's outdated
	if((jsw.servletClass == null) || outDated) {
	    try {
		if( null ==ctxt.getServletClassName() ) {
		    compiler.computeServletClassName();
		}
		jsw.servletClass = loader.loadClass(ctxt.getFullClassName());
                        //loadClass(ctxt.getFullClassName(), true);
	    } catch (ClassNotFoundException cex) {
		throw new JasperException(Constants.getString("jsp.error.unable.load"), 
					  cex);
	    }
	    
	    //	    loadedJSPs.put(jspUri, jspClass);
	}
	
	return outDated;
    }

        /**
     * Determines whether the current JSP class is older than the JSP file
     * from whence it came
     */
    public boolean isOutDated(File jsp, JspCompilationContext ctxt,
			      Mangler mangler ) {
        File jspReal = null;
	boolean outDated;
	
        jspReal = new File(ctxt.getRealPath(jsp.getPath()));

        File classFile = new File(mangler.getClassFileName());
        if (classFile.exists()) {
            outDated = classFile.lastModified() < jspReal.lastModified();
        } else {
            outDated = true;
        }

        return outDated;
    }

}
