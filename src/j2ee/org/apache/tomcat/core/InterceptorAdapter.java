/*
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
import javax.servlet.Servlet;
import org.apache.tomcat.request.*;
import org.apache.tomcat.context.*;


/** Plugs Lifecylce and Service interceptors into tomcat
 */
public class InterceptorAdapter {
    /**
     * Adds an interceptor for init() method.
     * If Interceptors a, b and c are added to a context, the
     * implementation would guarantee the following call order:
     * (no matter what happens, for eg.Exceptions ??)
     *
     * <P>
     * <BR> a.preInvoke(...)
     * <BR> b.preInvoke(...)
     * <BR> c.preInvoke(...)
     * <BR> init()
     * <BR> c.postInvoke(...)
     * <BR> b.postInvoke(...)
     * <BR> a.postInvoke(...)
     */
    public static void addInitInterceptor(Context ctx, LifecycleInterceptor interceptor) {
	ctx.addContextInterceptor( new InitInterceptorAdapter( interceptor ) );
    }

    /**
     * Adds an interceptor for destroy() method.
     * If Interceptors a, b and c are added to a context, the
     * implementation would guarantee the following call order:
     * (no matter what happens, for eg.Exceptions ??)
     *
     * <P>
     * <BR> a.preInvoke(...)
     * <BR> b.preInvoke(...)
     * <BR> c.preInvoke(...)
     * <BR> destroy()
     * <BR> c.postInvoke(...)
     * <BR> b.postInvoke(...)
     * <BR> a.postInvoke(...)
     */
    public  static  void addDestroyInterceptor(Context ctx, LifecycleInterceptor interceptor) {
	ctx.addContextInterceptor( new DestroyInterceptorAdapter( interceptor ) );
    }

    /**
     * Adds an interceptor for service() method.
     * If Interceptors a, b and c are added to a context, the
     * implementation would guarantee the following call order:
     * (no matter what happens, for eg.Exceptions ??)
     *
     * <P>
     * <BR> a.preInvoke(...)
     * <BR> b.preInvoke(...)
     * <BR> c.preInvoke(...)
     * <BR> service()
     * <BR> c.postInvoke(...)
     * <BR> b.postInvoke(...)
     * <BR> a.postInvoke(...)
     */
    public  static void addServiceInterceptor(Context ctx, ServiceInterceptor interceptor) {
	ctx.addRequestInterceptor( new ServiceInterceptorAdapter( interceptor ) );
    }
    
}

class InitInterceptorAdapter extends BaseContextInterceptor implements ContextInterceptor {
    LifecycleInterceptor interceptor;
    
    InitInterceptorAdapter( LifecycleInterceptor interceptor) {
	this.interceptor=interceptor;
    }

    public int preServletInit(Context ctx, ServletWrapper sw ) {
	try {
	    interceptor.preInvoke( ctx, sw.getServlet());
	    return 0;
	} catch( InterceptorException ex ) {
	    return -1; // map exceptions to error codes
	}
    }

    public int postServletInit(Context ctx, ServletWrapper sw ) {
	try {
	    interceptor.postInvoke( ctx, sw.getServlet());
	    return 0;
	} catch( InterceptorException ex ) {
	    return -1; // map exceptions to error codes
	}
    }
    
}

class DestroyInterceptorAdapter extends BaseContextInterceptor implements ContextInterceptor {
    LifecycleInterceptor interceptor;
    
    DestroyInterceptorAdapter( LifecycleInterceptor interceptor) {
	this.interceptor=interceptor;
    }


    public int preServletDestroy(Context ctx, ServletWrapper sw ) {
	try {
	    interceptor.preInvoke( ctx, sw.getServlet());
	    return 0;
	} catch( InterceptorException ex ) {
	    return -1; // map exceptions to error codes
	}
    }

    public int postServletDestroy(Context ctx, ServletWrapper sw ) {
	try {
	    interceptor.postInvoke( ctx, sw.getServlet());
	    return 0;
	} catch( InterceptorException ex ) {
	    return -1; // map exceptions to error codes
	}
    }

}

class ServiceInterceptorAdapter extends BaseInterceptor implements RequestInterceptor {
    ServiceInterceptor interceptor;
    
    ServiceInterceptorAdapter( ServiceInterceptor interceptor) {
	this.interceptor=interceptor;
    }


    public int preService(Request req, Response resp ) {
	try {
	    interceptor.preInvoke( req.getContext(), req.getWrapper().getServlet(),
				    req.getFacade(),  resp.getFacade());
	    return 0;
	} catch( InterceptorException ex ) {
	    return -1; // map exceptions to error codes
	}
    }

    public int postService(Request req, Response resp ) {
	try {
	    interceptor.postInvoke( req.getContext(), req.getWrapper().getServlet(),
				    req.getFacade(),  resp.getFacade());
	    return 0;
	} catch( InterceptorException ex ) {
	    return -1; // map exceptions to error codes
	}
    }

}
