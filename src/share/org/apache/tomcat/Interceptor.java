/*
 * $Header: /tmp/cvs-vintage/tomcat/src/share/org/apache/tomcat/Attic/Interceptor.java,v 1.1 2000/01/09 03:20:02 craigmcc Exp $
 * $Revision: 1.1 $
 * $Date: 2000/01/09 03:20:02 $
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


package org.apache.tomcat;


import java.io.IOException;
import javax.servlet.ServletException;


/**
 * An <b>Interceptor</b>, as the name implies, intercepts request processing
 * prior to, and after, the call to a Container's <code>service()</code>
 * method.  Conceptually, a stack of Interceptors can be associated with a
 * particular Container, which are processed as described for
 * <code>Container.invoke()</code>.
 * <p>
 * Note that Interceptors may be added to <b>any</b> type of Container, so it
 * is up to the Interceptor implementation to deal avoid class cast exceptions.
 * An Interceptor can refuse to be attached to a particular Container by
 * returning a <code>IllegalArgumentException</code> to the
 * <code>setContainer()</code> method.
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.1 $ $Date: 2000/01/09 03:20:02 $
 */

public interface Interceptor {


    // ------------------------------------------------------------- Properties


    /**
     * Return the Container to which this Interceptor was added, if any.
     */
    public Container getContainer();


    /**
     * Set the Container to which this Interceptor is being added.  This method
     * must be called from within the <code>addInterceptor()</code> method of
     * the specified Container.  This Interceptor may refuse to become
     * attached to the specified Container by throwing an exception.
     *
     * @param container Container to which this Interceptor is being added
     *
     * @exception IllegalArgumentException if this Interceptor refuses to be
     *  attached to the specified Container
     * @exception IllegalStateException if this Interceptor is already
     *  attached to a different Container.
     */
    public void setContainer(Container container);


    /**
     * Return descriptive information about this Interceptor implementation and
     * the corresponding version number, in the format
     * <code>&lt;description&gt;/&lt;version&gt;</code>.
     */
    public String getInfo();


    // --------------------------------------------------------- Public Methods


    /**
     * Perform pre-processing for this request.  The <code>preService()</code>
     * method of all Interceptors associated with a Container are called before
     * the Container's <code>service()</code> method is called, starting with
     * the most recently added one, until a <code>preService()</code> method
     * returns <code>false</code> or throws an Exception.  This method may
     * proceed in any of the following ways:
     * <ul>
     * <li>Do nothing, and return <code>true</code>  This would be normal for
     *     an Interceptor that only required post-processing of the request.
     * <li>Examine and/or modify the properties of the specified Request and/or
     *     Response, and return <code>true</code>.  This would be normal for
     *     an Interceptor that performed pre-processing, but did not complete
     *     the response.
     * <li>Examine and/or modify the properties of the specified Request and/or
     *     Response, complete the generation of the Response, and return
     *     <code>false</code>.  This would be normal for an Interceptor that
     *     decided to pre-empt the normal processing cycle for this request,
     *     such as a security implementation that needs to challenge the user
     *     for credentials.
     * <li>Throw an <code>IOException</code> if encountered when processing the
     *     specified Request or Response.  This will abort all further
     *     processing of this request.
     * <li>Throw a <code>ServletException</code> if an unacceptable condition
     *     is encountered.  This will abort all further processing of this
     *     request.
     * </ul>
     *
     * @param request Request to be processed
     * @param response Response to be produced
     *
     * @return <code>true</code> if processing of this request and response
     *  should continue, or <code>false</code> if creation of this response
     *  has been completed
     *
     * @exception IOException if an input/output error occurred while
     *  processing this request
     * @exception ServletException if a ServletException was thrown
     *  while processing this request
     */
    public boolean preService(Request request, Response response)
	throws IOException, ServletException;


    /**
     * Perform post-processing for this request.  The
     * <code>postService()</code> method of all Interceptors associated with
     * a Container, where the <code>preService()</code> method was actually
     * called (i.e. for those Interceptors up to and including the one whose
     * <code>preService()</code> method returned <code>false</code>, if any),
     * starting with the least recently added one.
     * <p>
     * The <code>postService()</code> method may examine, but not modify, the
     * properties of the specified Request and Response.  <b>FIXME:  Is this
     * requirement too restrictive?</b>.  It may, however, throw an exception,
     * which bypasses the call to <code>postService()</code> for any
     * remaining Interceptor associated with this Container.
     *
     * @param request Request that was processed
     * @param response Response that was produced
     *
     * @exception IOException if an input/output error occurred while
     *  processing this request
     * @exception ServletException if a ServletException was thrown
     *  while processing this request
     */
    public void postService(Request request, Response response)
	throws IOException, ServletException;


}
