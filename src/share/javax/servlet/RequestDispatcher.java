/*
 * $Id: RequestDispatcher.java,v 1.1 1999/10/09 00:20:29 duncan Exp $
 * 
 * Copyright (c) 1998-1999 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the confidential and proprietary information of Sun
 * Microsystems, Inc. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Sun.
 * 
 * SUN MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF THE
 * SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE, OR NON-INFRINGEMENT. SUN SHALL NOT BE LIABLE FOR ANY DAMAGES
 * SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR DISTRIBUTING
 * THIS SOFTWARE OR ITS DERIVATIVES.
 * 
 * CopyrightVersion 1.0
 */



package javax.servlet;

import java.io.IOException;


/**
 * Defines an object that receives requests from the client
 * and sends them to any resource (such as a servlet, 
 * HTML file, or JSP file) on the server. The servlet
 * container creates the <code>RequestDispatcher</code> object,
 * which is used as a wrapper around a server resource located
 * at a particular path or given by a particular name.
 *
 * <p>This interface is intended to wrap servlets,
 * but a servlet container can create <code>RequestDispatcher</code>
 * objects to wrap any type of resource.
 *
 * @author 	Various
 * @version 	$Version$
 *
 * @see 	ServletContext#getRequestDispatcher(java.lang.String)
 * @see 	ServletContext#getNamedDispatcher(java.lang.String)
 * @see 	ServletRequest#getRequestDispatcher(java.lang.String)
 *
 */
 
public interface RequestDispatcher {





/**
 * Forwards a request from
 * a servlet to another resource (servlet, JSP file, or
 * HTML file) on the server. This method allows
 * one servlet to do preliminary processing of
 * a request and another resource to generate
 * the response.
 *
 * <p>For a <code>RequestDispatcher</code> obtained via 
 * <code>getRequestDispatcher()</code>, the <code>ServletRequest</code> 
 * object has its path elements and parameters adjusted to match
 * the path of the target resource.
 *
 * <p><code>forward</code> should be called before the response has been 
 * committed to the client (before response body output has been flushed).  
 * If the response already has been committed, this method throws
 * an <code>IllegalStateException</code>.
 * Uncommitted output in the response buffer is automatically cleared 
 * before the forward.
 *
 * <p>The request and response parameters must be the same
 * objects as were passed to the calling servlet's service method.
 *
 *
 * @param request		a {@link ServletRequest} object
 *				that represents the request the client
 * 				makes of the servlet
 *
 * @param response		a {@link ServletResponse} object
 *				that represents the response the servlet
 *				returns to the client
 *
 * @exception ServletException	if the target resource throws this exception
 *
 * @exception IOException	if the target resource throws this exception
 *
 * @exception IllegalStateException	if the response was already committed
 *
 */

    public void forward(ServletRequest request, ServletResponse response)
	throws ServletException, IOException;




    /**
     *
     * Includes the content of a resource (servlet, JSP page,
     * HTML file) in the response. In essence, this method enables 
     * programmatic server-side includes.
     *
     * <p>The {@link ServletResponse} object has its path elements
     * and parameters remain unchanged from the caller's. The included
     * servlet cannot change the response status code or set headers;
     * any attempt to make a change is ignored.
     *
     * <p>The request and response parameters must be the same
     * objects as were passed to the calling servlet's service method.
     *
     *
     * @param request 			a {@link ServletRequest} object 
     *					that contains the client's request
     *
     * @param response 			a {@link ServletResponse} object 
     * 					that contains the servlet's response
     *
     * @exception ServletException 	if the included resource throws this exception
     *
     * @exception IOException 		if the included resource throws this exception
     *
     *
     */
     
    public void include(ServletRequest request, ServletResponse response)
	throws ServletException, IOException;
}








