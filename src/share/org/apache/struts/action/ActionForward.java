/*
 * $Header: /tmp/cvs-vintage/struts/src/share/org/apache/struts/action/ActionForward.java,v 1.13 2004/01/10 21:03:38 dgraham Exp $
 * $Revision: 1.13 $
 * $Date: 2004/01/10 21:03:38 $
 *
 * ====================================================================
 *
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2000-2003 The Apache Software Foundation.  All rights
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
 *    any, must include the following acknowledgement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "The Jakarta Project", "Struts", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their name, without prior written
 *    permission of the Apache Software Foundation.
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


package org.apache.struts.action;


import org.apache.struts.config.ForwardConfig;


/**
 * <p>An <strong>ActionForward</strong> represents a destination to which the
 * controller, <code>RequestProcessor</code>, might be directed to
 * perform a <code>RequestDispatcher.forward</code> or
 * <code>HttpServletResponse.sendRedirect</code> to, as a result of
 * processing activities of an <code>Action</code> class. Instances of this
 * class may be created dynamically as necessary, or configured in association
 * with an <code>ActionMapping</code> instance for named lookup of potentially
 * multiple destinations for a particular mapping instance.</p>
 *
 * <p>An <code>ActionForward</code> has the following minimal set of properties.
 * Additional properties can be provided as needed by subclassses.</p>
 * <ul>
 * <li><strong>contextRelative</strong> - Should the <code>path</code>
 *     value be interpreted as context-relative (instead of
 *     module-relative, if it starts with a '/' character? [false]</li>
 * <li><strong>name</strong> - Logical name by which this instance may be
 *     looked up in relationship to a particular <code>ActionMapping</code>.
 *     </li>
 * <li><strong>path</strong> - Module-relative or context-relative URI to
 *     which control should be forwarded, or an absolute or relative URI to
 *     which control should be redirected.</li>
 * <li><strong>redirect</strong> - Set to <code>true</code> if the controller
 *     servlet should call <code>HttpServletResponse.sendRedirect()</code>
 *     on the associated path; otherwise <code>false</code>.  [false]</li>
 * </ul>
 *
 * <p>Since Struts 1.1 this class extends <code>ForwardConfig</code>
 * and inherits the <code>contextRelative</code> property.
 *
 * <p><strong>NOTE</strong> - This class would have been deprecated and
 * replaced by <code>org.apache.struts.config.ForwardConfig</code> except
 * for the fact that it is part of the public API that existing applications
 * are using.</p>
 *
 * @version $Revision: 1.13 $ $Date: 2004/01/10 21:03:38 $
 */

public class ActionForward extends ForwardConfig {


    /**
     * <p>Construct a new instance with default values.</p>
     */
    public ActionForward() {

        this(null, false);

    }


    /**
     * <p>Construct a new instance with the specified path.</p>
     *
     * @param path Path for this instance
     */
    public ActionForward(String path) {

        this(path, false);

    }


    /**
     * <p>Construct a new instance with the specified
     * <code>path</code> and <code>redirect</code> flag.</p>
     *
     * @param path Path for this instance
     * @param redirect Redirect flag for this instance
     */
    public ActionForward(String path, boolean redirect) {

        super();
        setName(null);
        setPath(path);
        setRedirect(redirect);

    }


    /**
     * <p>Construct a new instance with the specified <code>name</code>,
     * <code>path</code> and <code>redirect</code> flag.</p>
     *
     * @param name Name of this instance
     * @param path Path for this instance
     * @param redirect Redirect flag for this instance
     */
    public ActionForward(String name, String path, boolean redirect) {

        super();
        setName(name);
        setPath(path);
        setRedirect(redirect);

    }


    /**
     * <p>Construct a new instance with the specified values.</p>
     *
     * @param name Name of this instance
     * @param path Path for this instance
     * @param redirect Redirect flag for this instance
     * @param contextRelative Context relative flag for this instance
     */
    public ActionForward(String name, String path, boolean redirect,
                         boolean contextRelative) {

        super();
        setName(name);
        setPath(path);
        setRedirect(redirect);
        setContextRelative(contextRelative);

    }


}
