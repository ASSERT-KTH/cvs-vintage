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

package org.apache.jasper.runtime;

import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
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


/**
 * The JSP engine (a.k.a Jasper)! 
 *
 * @author Anil K. Vijendran
 * @author Harish Prabandham
 */
public class JspServlet extends HttpServlet {
    class JspServletWrapper {
	HttpJspPage theServlet;
	String jspUri;
	boolean isErrorPage;
	
	JspServletWrapper(String jspUri, boolean isErrorPage) {
	    this.jspUri = jspUri;
	    this.isErrorPage = isErrorPage;
	    this.theServlet = null;
	}
	
	private void load() throws JasperException, ServletException {
	    try {  
		Class servletClass = loader.getJspServletClass(jspUri);
		// This is for the original protocol.
		destroy();
		theServlet = (HttpJspPage) servletClass.newInstance();
	    } catch (Exception ex) {
		throw new JasperException(ex);
	    }
	    theServlet.init(JspServlet.this.config);
	    if (theServlet instanceof HttpJspBase)  {
                ((HttpJspBase)theServlet).setClassLoader(JspServlet.this.parentClassLoader);
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
                accordingto = "according to the init parameter is";
                cp = options.getClassPath();
            } else 
                accordingto = "according to the Servlet Engine is";
            
            
            Constants.message("jsp.message.cp_is", 
                              new Object[] { accordingto, cp }, 
                              Constants.MED_VERBOSITY);

	    if (loader.loadJSP(jspUri, cp, isErrorPage, req, res) || theServlet == null) {
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

                theServlet.service(request, response);
            } catch (FileNotFoundException ex) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, 
                                   Constants.getString("jsp.error.file.not.found", 
                                                       new Object[] {
                                                           ex.getMessage()
                                                       }));
                
                return;
            }
	}

	public void destroy() {
	    if (theServlet != null)
		theServlet.destroy();
	}
    }
	
	
    ServletContext context = null;
    Hashtable jsps = new Hashtable();
    ServletConfig config;
    JspLoader loader;
    Options options;
    ClassLoader parentClassLoader;
    ServletEngine engine;
    String serverInfo;

    static boolean firstTime = true;

    public void init(ServletConfig config)
	throws ServletException
    {
	super.init(config);
	this.config = config;
	this.context = config.getServletContext();
        this.serverInfo = context.getServerInfo();
        this.engine = ServletEngine.getServletEngine(serverInfo);
        
        if (engine == null)
            Constants.message("jsp.error.bad-servlet-engine", Constants.FATAL_ERRORS);
        else {
            options = new Options(config, context);

            parentClassLoader = (ClassLoader) context.getAttribute(Constants.SERVLET_CLASS_LOADER);
            if (parentClassLoader == null)
                parentClassLoader = this.getClass().getClassLoader();
            
            Constants.message("jsp.message.parent_class_loader_is", 
                              new Object[] {
                                  parentClassLoader.toString()
                              }, Constants.MED_VERBOSITY);

            this.loader = new JspLoader(context, 
                                        parentClassLoader, 
                                        options);

            if (firstTime) {
                firstTime = false;
                Constants.message("jsp.message.scratch.dir.is", 
                                  new Object[] { 
                                      options.scratchDir().toString() 
                                  }, Constants.LOW_VERBOSITY );
                Constants.message("jsp.message.dont.modify.servlets", Constants.LOW_VERBOSITY);
            }
            
        }

        JspFactory.setDefaultFactory(new JspFactoryImpl());
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


    final void unknownException(HttpServletResponse response, 
    						Throwable t) 
    {
    	PrintWriter writer = new PrintWriter(System.err, true);
	if (options.sendErrorToClient()) {
	    try {
	        response.setContentType ("text/html");
	    	writer = response.getWriter ();
	    } catch (IOException ioex) {
	        writer = new PrintWriter(System.err, true);
	    }
	}
        writer.println(Constants.getString("jsp.error.unknownException"));

        if (options.sendErrorToClient()) {
            writer.println("<pre>");
        }

	if (t instanceof JasperException) {
            Throwable x = ((JasperException) t).getRootCause();
	    (x != null ? x : t).printStackTrace (writer);
	} else {
	    t.printStackTrace (writer);
	}

        if (options.sendErrorToClient()) {
            writer.println("</pre>");
        }
        
	if (!options.sendErrorToClient()) {
            try {
	        String message = t.getMessage ();
		if (message == null)
		    message = "No detailed message";
                response.sendError(HttpServletResponse.
					SC_INTERNAL_SERVER_ERROR, message);
            } catch (IOException ex) {
            }
	}
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
					       " jsp_precomile set to " + precom);
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
                = (String) request.getAttribute(Constants.INC_REQUEST_URI);

            String jspUri;

            if (includeUri == null)
		jspUri = request.getServletPath();
            else
                jspUri = includeUri;

            boolean precompile = preCompile(request);

            if (Constants.matchVerbosity(Constants.MED_VERBOSITY)) {
		System.err.println("JspEngine --> "+jspUri);
                System.err.println("\t     ServletPath: "+request.getServletPath());
                System.err.println("\t        PathInfo: "+request.getPathInfo());
		System.err.println("\t        RealPath: "
                                   +getServletConfig().getServletContext().getRealPath(jspUri));
                System.err.println("\t      RequestURI: "+request.getRequestURI());
                System.err.println("\t     QueryString: "+request.getQueryString());
                System.err.println("\t  Request Params: ");
                Enumeration e = request.getParameterNames();
                while (e.hasMoreElements()) {
                    String name = (String) e.nextElement();
                    System.err.println("\t\t "+name+" = "+request.getParameter(name));
                }
            }
            serviceJspFile(request, response, jspUri, null, precompile);
	    
	} catch (RuntimeException e) {
	    throw e;
	} catch (ServletException e) {
	    throw e;
	} catch (Exception e) {
	    throw new ServletException(e);
	} catch (Throwable t) {
	    unknownException(response, t);
	}
    }

    public void destroy() {
	if (Constants.matchVerbosity(2))
	    System.err.println("JspServlet.destroy()");
	Enumeration servlets = jsps.elements();
	while (servlets.hasMoreElements()) 
	    ((JspServletWrapper) servlets.nextElement()).destroy();
    }

}
