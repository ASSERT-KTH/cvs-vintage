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


package org.apache.tomcat.request;

import org.apache.tomcat.core.*;
import org.apache.tomcat.util.*;
import java.io.*;
import java.net.*;
import java.util.*;
import javax.servlet.http.*;

/**
 *  JDK1.2 specific options. Fix the class loader, etc.
 */
public class Jdk12Interceptor extends  BaseInterceptor implements RequestInterceptor {
    ContextManager cm;
    int debug=0;

    public Jdk12Interceptor() {
    }

    public void setContextManager( ContextManager cm ) {
	this.cm=cm;
    }

    public void setDebug( int i ) {
	debug=i;
    }

    public void preServletInit( Context ctx, ServletWrapper sw )
	throws TomcatException
    {
	ClassLoader originalCL=null;
	originalCL = fixJDKContextClassLoader(ctx.getServletLoader().getClassLoader());
	//	System.out.println("Setting class loader for init()");
    }

    /** Servlet Destroy  notification
     */
    public void preServletDestroy( Context ctx, ServletWrapper sw )
	throws TomcatException
    {
	fixJDKContextClassLoader(ctx.getServletLoader().getClassLoader());
    }
    
    public void postServletInit( Context ctx, ServletWrapper sw )
	throws TomcatException
    {
	// no need to change the cl - next requst will do that
	// ( it's per-thread information )
    }
    
    /** Called before service method is invoked. 
     */
    public int preService(Request request, Response response) {
	if( request.getContext() == null ) return 0;
	fixJDKContextClassLoader(request.getContext().getServletLoader().getClassLoader());
	//	System.out.println("Setting class loader for service()");
	return 0;
    }

    
    
    static boolean haveContextClassLoader=true;
    static Class noParams[]=new Class[0];
    static Class clParam[]=new Class[1];
    static Object noObjs[]=new Object[0];
    static { clParam[0]=ClassLoader.class; }


    // Before we do init() or service(), we need to do some tricks
    // with the class loader - see bug #116.
    // some JDK1.2 code will not work without this fix
    // we save the originalCL because we might be in include
    // and we need to revert to it when we finish
    // that will set a new (JDK)context class loader, and return the old one
    // if we are in JDK1.2
    // XXX move it to interceptor !!!
    /** Reflection trick to set the context class loader for JDK1.2, without
	braking JDK1.1.

	This code can be commented out for 3.1 if it creates any problems -
	it should work.

	XXX We need to find a better way to do that - maybe make it part of
	the ServletLoader interface.
     */
    ClassLoader fixJDKContextClassLoader( ClassLoader cl ) {
	if( cl==null ) return null;
	if( ! haveContextClassLoader ) return null;
	
	Thread t=Thread.currentThread();
	try {
	    java.lang.reflect.Method getCCL=t.getClass().getMethod("getContextClassLoader", noParams);
	    java.lang.reflect.Method setCCL=t.getClass().getMethod("setContextClassLoader", clParam) ;
	    if( (getCCL==null) || (setCCL==null) ) {
		haveContextClassLoader=false;
		return null;
	    }
	    ClassLoader old=( ClassLoader)getCCL.invoke( t, noObjs );
	    Object params[]=new Object[1];
	    params[0]=cl;
	    setCCL.invoke( t, params );
	    // 	    if( context.getDebug() > 5 ) context.log("Setting system loader " + old + " " + cl );
	    // 	    context.log("Setting system loader " + old + " " + cl );
	    
	    return old;
	} catch (NoSuchMethodException ex ) {
	    // we don't have the methods, don't try again
	    haveContextClassLoader=false;
	} catch( Exception ex ) {
	    haveContextClassLoader = false;
	    System.out.println( "Error setting jdk context class loader " + ex );
	}
	return null;
    }

    

    
}
