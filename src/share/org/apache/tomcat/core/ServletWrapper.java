/*
 * $Header: /tmp/cvs-vintage/tomcat/src/share/org/apache/tomcat/core/Attic/ServletWrapper.java,v 1.8 2000/01/09 02:57:40 costin Exp $
 * $Revision: 1.8 $
 * $Date: 2000/01/09 02:57:40 $
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


package org.apache.tomcat.core;

import org.apache.tomcat.util.*;
import java.io.*;
import java.net.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;

/**
 * 
 * @author James Duncan Davidson [duncan@eng.sun.com]
 * @author Jason Hunter [jch@eng.sun.com]
 * @author James Todd [gonzo@eng.sun.com]
 * @author Harish Prabandham
 */

//
// WARNING: Some of the APIs in this class are used by J2EE. 
// Please talk to harishp@eng.sun.com before making any changes.
//
public class ServletWrapper {

    private StringManager sm =
        StringManager.getManager(Constants.Package);
    private Context context;
    private String description = null;
    private String servletClassName;
    private Class servletClass;
    private File servletClassFile;
    private String path = null;
    private Servlet servlet;
    private long lastAccessed;
    private ServletConfigImpl config;
    private boolean isReloadable = false;
    private long classFileLastMod = 0;
    private int serviceCount = 0;

    ServletWrapper(Context context) {
        this.context = context;

        config = new ServletConfigImpl(context);
    }

    void setReloadable(boolean reloadable) {
	isReloadable = reloadable;
    }

    public String getServletName() {
        return config.getServletName();
    }

    void setServletName(String servletName) {
        config.setServletName(servletName);
    }

    String getServletDescription() {
        return this.description;
    }

    void setServletDescription(String description) {
        this.description = description;
    }

    public String getPath() {
        return this.path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    void setServletClassFile(File servletClassFile) {
	this.servletClassFile = servletClassFile;
	classFileLastMod = servletClassFile.lastModified();

	config.setServletClassName(this.servletClassFile.getName());
    }

    public String getServletClass() {
        return this.servletClassName;
    }

    public void setServletClass(String servletClassName) {
        this.servletClassName = servletClassName;

	config.setServletClassName(servletClassName);
    }

    void setServletClass(Class servletClass) {
	this.servletClass = servletClass;

	config.setServletClassName(this.servletClass.getName());
    }

    void setInitArgs(Hashtable initArgs) {
        config.setInitArgs(initArgs);
    }

    void destroy() {
	// Fancy sync logic is to make sure that no threads are in the
	// handlerequest when this is called and, furthermore, that
	// no threads go through handle request after this method starts!
	
	if (servlet != null) {
	    synchronized (this) {
		// Wait until there are no outstanding service calls,
		// or until 30 seconds have passed (to avoid a hang)

		while (serviceCount > 0) {
		    try {
		        wait(30000);

			break;
		    } catch (InterruptedException e) { }
		}
		
		try {
		    final Servlet sinstance = servlet;

		    handleInvocation(
		        context.getDestroyInterceptors().elements(), 
			new LifecycleInvocationHandler(context, servlet) {
		            void method() throws ServletException {
			        sinstance.destroy();
			    }
		        });
		} catch(IOException ioe) {
		    // Should never come here...
		} catch(ServletException se) {
		    // Should never come here...
		}
	    }
	}
    }
    
    public void loadServlet()
    throws ClassNotFoundException, InstantiationException,
        IllegalAccessException, ServletException {
        // Check if this is a JSP, they get special treatment

        if (path != null &&
            servletClass == null &&
            servletClassName == null) {
	    // XXX XXX XXX
	    // core shouldn't depend on a particular connector!
	    // need to find out what this code does!
	    RequestAdapterImpl reqA=new RequestAdapterImpl();
	    ResponseAdapterImpl resA=new ResponseAdapterImpl();
	    
	    Request request = new Request();
            Response response = new Response();
            request.recycle();
            response.recycle();

	    request.setRequestAdapter( reqA );
	    response.setResponseAdapter( resA );

            request.setResponse(response);
            response.setRequest(request);

            String requestURI = path + "?" +
                Constants.JSP.Directive.Compile.Name + "=" +
                Constants.JSP.Directive.Compile.Value;

            reqA.setRequestURI(context.getPath() + path);
	    reqA.setQueryString( Constants.JSP.Directive.Compile.Name + "=" +
				 Constants.JSP.Directive.Compile.Value );

            request.setContext(context);
	    request.updatePaths();
            request.getSession(true);

            RequestDispatcher rd =
                config.getServletContext().getRequestDispatcher(requestURI);

            try {
                rd.forward(request.getFacade(), response.getFacade());
            } catch (ServletException se) {
            } catch (IOException ioe) {
            }
        } else {
	    if (servletClass == null) {
	        if (servletClassName == null) {
		    String msg = sm.getString("wrapper.load.noclassname");

		    throw new IllegalStateException(msg);
	        }

	        servletClass = context.getLoader().loadServlet(this,
                    servletClassName);
	    }

            // make sure we have a classname or class def
            //if (servletClassName == null || servletClass == null) {
            //    String msg = sm.getString("wrapper.load.noclassname");
            //    throw new IllegalStateException(msg);
	    // }
	    //Class c = context.getLoader().loadServlet(this,
	    //servletClassName);

	    servlet = (Servlet)servletClass.newInstance();

	    config.setServletClassName(servlet.getClass().getName());

	    try {
	        final Servlet sinstance = servlet;
	        final ServletConfigImpl servletConfig = config;

	        handleInvocation(context.getInitInterceptors().elements(), 
	            new LifecycleInvocationHandler(context, servlet) {
	                void method() throws ServletException {
		            sinstance.init(servletConfig);
		        }
	            });
	    } catch(IOException ioe) {
	    // Should never come here...
	    }
        }
    }

    private Context getContext() {
	return context;
    } 

    void handleRequest(final HttpServletRequestFacade request,
        final HttpServletResponseFacade response)
    throws IOException {
	//  if (isReloadable) {
//  	    long lm = servletClassFile.lastModified();
//  	    if (lm > classFileLastMod) {
//  		//context.recycle();
//  	    }
//  	}
	// make sure that only one thread goes through
	// this block at a time!

        synchronized (this) {
	    // XXX
	    // rather klunky - this method needs a once over

	    if (path != null &&
                servletClass == null &&
                servletClassName == null) {
                String requestURI = path + request.getPathInfo();
	        RequestDispatcher rd =
                    request.getRequestDispatcher(requestURI);

		try {
		    // Watch out, ugly code ahead...
		    // We need to do a forward or include here, but we can't
		    // easily determine which.  So we try a forward, and if
		    // there's an IllegalStateException thrown, then we know
		    // we should have tried an include, so we do the include.
		    // It's so ugly I have to giggle.
		    // All this to support dispatching to named JSPs!
		    try {
		        rd.forward(request, response);
		    } catch (IllegalStateException e) {
			rd.include(request, response);
		    }
		} catch (ServletException se) {
		    response.sendError(404);
		} catch (IOException ioe) {
		    response.sendError(404);
		}
		
		return;
	    } else {
	        if (servlet == null) {
		    try {
		        loadServlet();
		    } catch (ClassNotFoundException e) {
		        response.sendError(404);

			return;
		    } catch (Exception e) {
		        // Make sure the servlet will never
		        // service a request

		        servlet = null;
		    
			// XXX
			// check to see what kind of exception it was --
			// maybe it should be reported to the user
			// differently or at least logged differently
		    
			// XXX
			// we really need to pick up an error file on
			// a per context basis or, failing that from the
			// classpath
		    
			sendInternalServletError(e, response);

			return;
		    }
		}
	    }
	}

        try {
	    synchronized(this) {
		serviceCount++;
	    }
	
	    Context context = getContext();
            Enumeration serviceInterceptors =
                context.getServiceInterceptors().elements();
            ServiceInvocationHandler serviceHandler =
                new ServiceInvocationHandler(context, servlet,
                    request, response);

	    handleInvocation(serviceInterceptors, serviceHandler);
	} catch (ServletException e) {
            // XXX
	    // check to see if it's unavailable and set internal status
	    // appropriately

	    // XXX
	    // if it's an unvailable exception, we probably want
	    // to paint a different screen

            handleException(request, response, e);

	    return;
        } catch (SocketException e) {
	    // XXX
	    // Catch and eat all SocketExceptions
	    // *Should* only eat client disconnected socket exceptions

	    return;
        } catch (Throwable e) {
	    // if we are in an include, then we should rethrow the
	    // exception

	    // XXX
	    // we need a better way of dealing with figuring out
	    // if we are in an include -- this particular gem
	    // will pass IllegalStateException when we are in
	    // an include 'cause users will like to know when
	    // that happens in their included servlet
	    
	    //if (e instanceof IllegalStateException) {
	    //    String str = (String)request.getAttribute
	    //    (Constants.Attribute.RequestURI);
	    //    if (str != null) {
	    //        throw (IllegalStateException)e;
	    //    }
	    //}
	    
	    // XXX
	    // decide which exceptions we should not eat at this point
            handleException(request, response, e);

	    return;
	} finally {
	    synchronized(this) {
		serviceCount--;
		notifyAll();
	    }
	}
    }

    public void handleException(HttpServletRequestFacade request,
        HttpServletResponseFacade response,
    Throwable t) {
        Context context = request.getRealRequest().getContext();
        ServletContextFacade contextFacade = context.getFacade();

        // Scan the exception's inheritance tree looking for a rule
        // that this type of exception should be forwarded

        String path = null;
        Class clazz = t.getClass();

        while (path == null && clazz != null) {
            String name = clazz.getName();
            path = context.getErrorPage(name);
            clazz = clazz.getSuperclass();
        }

        // If path is non-null, we should do a forward
        // Don't do a forward if exception_type is already defined though to
        // avoid an infinite loop.

        if (path != null &&
	    request.getAttribute(
                Constants.Attribute.ERROR_EXCEPTION_TYPE) == null) {
            RequestDispatcher rd = contextFacade.getRequestDispatcher(path);

            // XXX 
            // The spec should really be changed to allow us to include
            // the full exception object.  Oh well.

            request.setAttribute(Constants.Attribute.ERROR_EXCEPTION_TYPE,
	        t.getClass().getName());
            request.setAttribute(Constants.Attribute.ERROR_MESSAGE,
                t.getMessage());

            try {
                try {
                    // A forward would be ideal, so reset and try it

                    response.getRealResponse().reset();
                    rd.forward(request, response);
                } catch (IllegalStateException ise) {
                    // Oops, too late for a forward; settle for an include

                    rd.include(request, response);
                }
            } catch (IOException e) {
                // Shouldn't get here
            } catch (ServletException e) {
                // Shouldn't get here
            }
        } else {
            try {
                sendInternalServletError(t, response);
            } catch (IOException e) {
                // ???
            }
        }
    }

    void sendInternalServletError(Throwable e,
        HttpServletResponseFacade response)
    throws IOException {
	StringWriter sw = new StringWriter();
	PrintWriter pw = new PrintWriter(sw);

	pw.println("<b>Internal Servlet Error:</b><br>");
        pw.println("<pre>");
	e.printStackTrace(pw);
	pw.println("</pre>");

        if (e instanceof ServletException) {
	    printRootCause((ServletException) e, pw);
	}

	response.sendError(500, sw.toString());
    }

    void printRootCause(ServletException e, PrintWriter out) {
        Throwable cause = e.getRootCause();

	if (cause != null) {
	    out.println("<b>Root cause:</b>");
	    out.println("<pre>");
	    cause.printStackTrace(out);
	    out.println("</pre>");

	    if (cause instanceof ServletException) {
		printRootCause((ServletException)cause, out);  // recurse
	    }
	}
    }

    private void handleInvocation(Enumeration interceptors,
        InvocationHandler inv)
    throws ServletException, IOException {
	Stack iStack = new Stack();

	try {
	    for (Enumeration e = interceptors; e.hasMoreElements(); ) {
		iStack.push(e.nextElement());
		inv.preInvoke(iStack.peek());
	    }

	    inv.method();
	} catch(InterceptorException ie) {
	} finally {
	    // in any case, we should make sure we call the
	    // postInvoke before leaving.

	    while (! iStack.empty()) {
		try {
		    inv.postInvoke(iStack.pop());
		} catch(InterceptorException ie) {
		    // can't do much ....
		}
	    }
	}
    }
}


//
// WARNING: Some of the APIs in this class are used by J2EE. 
// Please talk to harishp@eng.sun.com before making any changes.
//
abstract class InvocationHandler {
    protected Servlet servlet;
    protected Context context;

    InvocationHandler(Context context, Servlet servlet) {
	this.context = context;
	this.servlet = servlet;
    }

    abstract void preInvoke(Object interceptor)
    throws InterceptorException;

    abstract void method()
    throws ServletException, IOException;

    abstract void postInvoke(Object interceptor)
    throws InterceptorException;
}


//
// WARNING: Some of the APIs in this class are used by J2EE. 
// Please talk to harishp@eng.sun.com before making any changes.
//
abstract class LifecycleInvocationHandler extends InvocationHandler {
    LifecycleInvocationHandler(Context context, Servlet servlet) {
	super(context, servlet);
    }

    void preInvoke(Object interceptor)
    throws InterceptorException {
	((LifecycleInterceptor)interceptor).preInvoke(context, servlet);
    }

    void postInvoke(Object interceptor)
    throws InterceptorException {
	((LifecycleInterceptor)interceptor).postInvoke(context, servlet);
    }
}

//
// WARNING: Some of the APIs in this class are used by J2EE. 
// Please talk to harishp@eng.sun.com before making any changes.
//
class ServiceInvocationHandler extends InvocationHandler {
    private HttpServletRequestFacade request;
    private HttpServletResponseFacade response;

    ServiceInvocationHandler(Context context, Servlet servlet,
        HttpServletRequestFacade request,
	HttpServletResponseFacade response) {
	super(context, servlet);

	this.request = request;
	this.response = response;
    }

    void preInvoke(Object interceptor)
    throws InterceptorException {
	((ServiceInterceptor)interceptor).preInvoke(context, servlet,
            request, response);
    }

    void method()
    throws ServletException, IOException {
	if (servlet instanceof SingleThreadModel) {
	    synchronized(servlet) {
		servlet.service(request, response);
	    }
	} else {
	    servlet.service(request, response);
	}
    }
    
    void postInvoke(Object interceptor)
    throws InterceptorException {
	((ServiceInterceptor)interceptor).postInvoke(context, servlet,
            request, response);
    }
}
