/*
 * $Header: /tmp/cvs-vintage/tomcat/src/share/org/apache/tomcat/Attic/Response.java,v 1.1 2000/01/09 03:20:02 craigmcc Exp $
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
import java.io.OutputStream;
import java.util.Enumeration;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;


/**
 * A <b>Response</b> is the Tomcat-internal facade for an
 * <code>HttpServletResponse</code> that is to be produced, based on the
 * processing of a corresponding Request.
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.1 $ $Date: 2000/01/09 03:20:02 $
 */

public interface Response {


    // ------------------------------------------------------------- Properties


    /**
     * Return the Context with which this Response is associated.
     */
    public Context getContext();


    /**
     * Set the Context with which this Response is associated.  This should
     * be called as soon as the appropriate Context is identified.
     *
     * @param context The associated Context
     */
    public void setContext(Context context);


    /**
     * Return descriptive information about this Response implementation and
     * the corresponding version number, in the format
     * <code>&lt;description&gt;/&lt;version&gt;</code>.
     */
    public String getInfo();


    /**
     * Return the Request with which this Response is associated.
     */
    public Request getRequest();


    /**
     * Set the Request with which this Response is associated.
     *
     * @param request The new associated request
     */
    public void setRequest(Request request);


    /**
     * Return the <code>HttpServletResponse</code> for which this object
     * is the facade.
     */
    public HttpServletResponse getResponse();


    // --------------------------------------------------------- Public Methods


    /**
     * Ensure that the HTTP headers (and any buffered output) have been
     * flushed to the output stream, even if it was never acquired by
     * a servlet.
     *
     * @exception IOException if an input/output error occurs
     */
    public void flush() throws IOException;


    /**
     * Return the content length that was set or calculated for this Response.
     */
    public int getContentLength();


    /**
     * Return the content type that was set or calculated for this response,
     * or <code>null</code> if no content type was set.
     */
    public String getContentType();


    /**
     * Return the value for the specified header, or <code>null</code> if this
     * header has not been set.  If more than one value was added for this
     * name, only the first is returned; use getHeaderValues() to retrieve all
     * of them.
     *
     * @param name Header name to look up
     */
    public String getHeader(String name);


    /**
     * Return an enumeration all the header names set for this response, or
     * an empty Enumeration if no headers have been set.
     */
    public Enumeration getHeaderNames();


    /**
     * Return an enumeration of all the header values associated with the
     * specified header name, or an empty enumeration if there are no such
     * header values.
     *
     * @param name Header name to look up
     */
    public Enumeration getHeaderValues(String name);


    /**
     * Return the HTTP status code associated with this Response.
     */
    public int getStatus();


    /**
     * Release all object references, and initialize instance variables, in
     * preparation for reuse of this object.
     */
    public void recycle();


    /**
     * Set the output stream associated with this Response.  This stream will
     * be wrapped by a PrintWriter if <code>getWriter()</code> is called.
     *
     * @param stream The new output stream
     */
    public void setOutputStream(OutputStream stream);


}
