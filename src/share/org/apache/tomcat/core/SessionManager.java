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

import org.apache.tomcat.util.*;
import java.io.*;
import java.net.*;
import java.util.*;
import javax.servlet.http.*;

/**
 *
 * @author Craig R. McClanahan
 * @author costin@dnt.ro
 * @author Gal Shachor shachor@il.ibm.com
 */
public interface SessionManager {

    /**
     * Construct and return a new session object, based on the default
     * settings specified by this Manager's properties.  The session
     * id will be assigned by this method, and available via the getId()
     * method of the returned session.  If a new session cannot be created
     * for any reason, return <code>null</code>.
     *
     * @param ctx The session will be created for the specified context
     * @exception IllegalStateException if a new session cannot be
     *  instantiated for any reason
     */
    public HttpSession createSession(Context ctx);


    /** Will mark the session lastAccess time.
     *  Will be called for each request that has a valid sessionId
     *
     *  A simple SessionManager will just use the id, a complex
     * manager may use the ctx and req to do additional work.
     *
     * @param ctx the context where the request belong
     * @param req the request having the id
     * @param id
     */
    public void accessed(Context ctx, Request req, String id);
    //  we can pass only Request, as it contains both, but it's better to
    // show explicitely what this method uses.


    /**
     * Return the active Session, associated with this Manager, with the
     * specified session id (if any); otherwise return <code>null</code>.
     *
     * @param id The session id for the session to be returned
     * @param ctx The session needs to belong to the context
     *
     * @exception ClassNotFoundException if a deserialization error occurs
     *  while processing this request
     * @exception IllegalStateException if a new session cannot be
     *  instantiated for any reason
     */
    public HttpSession findSession(Context ctx, String id);


    /** Used by context when stoped, need to remove all sessions used by that context
     */
    public void removeSessions( Context ctx );

    /**
     * Used by context to configure the session manager's inactivity timeout.
     *
     * The SessionManager may have some default session time out, the
     * Context on the other hand has it's timeout set by the deployment
     * descriptor (web.xml). This method lets the Context conforgure the
     * session manager according to this value.
     *
     * @param minutes The session inactivity timeout in minutes.
     */
    public void setSessionTimeOut(int minutes);
}
