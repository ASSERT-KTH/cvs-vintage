/*
 * $Header: /tmp/cvs-vintage/tomcat/proposals/tomcat.next/Attic/ExampleEngine.java,v 1.1 2000/01/08 03:54:03 craigmcc Exp $
 * $Revision: 1.1 $
 * $Date: 2000/01/08 03:54:03 $
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


// package org.apache.tomcat;


import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import org.w3c.dom.DocumentFragment;


/**
 * Example implementation of the <b>Engine</b> interface.  It assumes that
 * each child Container is a Host implementation named with the fully
 * qualified name of that virtual host.
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.1 $ $Date: 2000/01/08 03:54:03 $
 */

public final class ExampleEngine
    extends ContainerBase
    implements Engine {


    // ----------------------------------------------------- Instance Variables


    /**
     * The descriptive information string for this implementation.
     */
    private static final String info = "ExampleEngine/1.0";


    // --------------------------------------------------------- Public Methods


    /**
     * Add a child Container, only if the proposed child is an implementation
     * of Host.
     *
     * @param child Child container to be added
     */
    public void addChild(Container child) {

	if (!(child instanceof Host))
	    throw new IllegalArgumentException
		("ExampleHost:  Child is not of type Host");
	super.addChild(child);

    }


    /**
     * Return descriptive information about this Container implementation and
     * the corresponding version number, in the format
     * <code>&lt;description&gt;/&lt;version&gt;</code>.
     */
    public String getInfo() {

	return (info);

    }


    /**
     * Select the appropriate child Container to process this request, based
     * on the server host specified in the request.  If no matching host can
     * be found, return an appropriate HTTP error.
     *
     * @param request Request to be processed
     * @param response Response to be produced
     */
    public void service(Request request, Response response)
        throws IOException, ServletException {

	// Extract the requested server host
	String server = request.getRequest().getServerName();
	if (server == null) {
	    HttpServletResponse res = response.getResponse();
	    res.sendError(HttpServletResponse.SC_BAD_REQUEST,
			  "No server host specified in the request");
	    return;
	}

	// Find the matching child Container
	Container child = findChild(server);
	if (child == null) {
	    HttpServletResponse res = response.getResponse();
	    res.sendError(HttpServletResponse.SC_NOT_FOUND,
			  "Virtual host '" + server + "' not found");
	    return;
	}

	// Delegate this request to the appropriate child Container
	child.invoke(request, response);

    }


    /**
     * Disallow any attempt to set a parent for this Container, since an
     * Engine is supposed to be at the top of the Container hierarchy.
     *
     * @param container Proposed parent Container
     */
    public void setParent(Container container) {

	throw new IllegalArgumentException
	    ("ExampleEngine:  No parent container allowed");

    }


}
