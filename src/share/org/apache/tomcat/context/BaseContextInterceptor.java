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


package org.apache.tomcat.context;

import org.apache.tomcat.core.*;
import org.apache.tomcat.core.Constants;
import org.apache.tomcat.util.*;
import org.apache.tomcat.deployment.*;
import java.io.*;
import java.net.*;
import java.util.*;
import javax.servlet.http.*;


/**
 * @author costin@dnt.ro
 */
public class BaseContextInterceptor implements ContextInterceptor {

    public BaseContextInterceptor() {
    }
	
    public int contextInit(Context ctx) {
	return 0;
    }

    public int contextShutdown(Context ctx) {
	return 0;
    }

    /** Notify when a new servlet is added
     */
    public int addServlet( Context ctx, ServletWrapper sw) {
	return 0;
    }
    
    /** Notify when a servlet is removed from context
     */
    public int removeServlet( Context ctx, ServletWrapper sw) {
	return 0;
    }

    public int addMapping( Context ctx, String path, ServletWrapper servlet) {
	return 0;
    }


    public int removeMapping( Context ctx, String path ) {
	return 0;
    }

    /** Called when the ContextManger is started
     */
    public int engineInit(ContextManager cm) {
	return 0;
    }

    /** Called before the ContextManager is stoped.
     *  You need to stop any threads and remove any resources.
     */
    public int engineShutdown(ContextManager cm) {
	return 0;
    }


    /** Called when a context is added to a CM
     */
    public int addContext( ContextManager cm, Context ctx ) {
	return 0;
    }

    /** Called when a context is removed from a CM
     */
    public int removeContext( ContextManager cm, Context ctx ) {
	return 0;
    }

    /** Servlet Init  notification
     */
    public int preServletInit( Context ctx, ServletWrapper sw ) {
	return 0;
    }

    
    public int postServletInit( Context ctx, ServletWrapper sw ) {
	return 0;
    }

    /** Servlet Destroy  notification
     */
    public int preServletDestroy( Context ctx, ServletWrapper sw ) {
	return 0;
    }

    
    public int postServletDestroy( Context ctx, ServletWrapper sw ) {
	return 0;
    }
    
}
