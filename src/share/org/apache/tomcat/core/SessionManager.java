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

    /* XXX The manager should try to reuse the session objects and avoid
     * allocating. The session can be "released" by Manager via timeout
     * or invalidate. 
     *
     * This is not easy right now - the session may expire while the servlet
     * is running in extreme cases - like very long transactions - and we may
     * end up with another user's session.
     *
     *  The fix will be to check if the id is still the same on each operation
     * on facade, or to maintain a useCount. We provide release if the manager
     * wants this.
     */
    
    /**
     * Return a new session object, based on the default
     * settings specified by this Manager's properties.  The session
     * id will be assigned by this method, and available via the getId()
     * method of the returned session.  If a new session cannot be created
     * for any reason, return <code>null</code>.
     *
     * @exception IllegalStateException if a new session cannot be
     *  instantiated for any reason
     */
    public HttpSession getNewSession();


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
    public HttpSession findSession(String id);


    /** Will mark the session lastAccess time.
     *  Will be called for each request that has a valid sessionId
     *   and a session.
     *
     *  Tomcat guarantees the session is the one created by the manager
     * and not null.
     */
    public void access(HttpSession s);
    
    /**
     *  Will be called when the servlet that used this session is out.
     *  A session shouldn't timeout while a servlet is still executing.
     *
     *  Tomcat guarantees the session is the one created by the manager
     * and not null.
     */
    public void release(HttpSession s);
    
    /* -------------------- Manager attributes - from web.xml -------------------- */
    /* The manager will be initialized in server.xml via setters ( or whatever mechanism
       it supports ), but it has to take web.xml informations from the context
    */
      
    /**
     * Used by context to configure the session manager's inactivity timeout.
     *
     * The SessionManager may have some default session time out, the
     * Context on the other hand has it's timeout set by the deployment
     * descriptor (web.xml). This method lets the Context configure the
     * session manager according to this value.
     *
     * @param minutes The session inactivity timeout in minutes.
     */
    public void setSessionTimeOut(int minutes);

    /** Pass the distributable info from Web.xml
     */
    public void setDistributable(boolean b);

    // -------------------- Control manager livecycle

    /** Start managing the sessions. Called after everything is set up to allow
     *  the manager to allocate the resources it needs
     */
    public void start();

    /** Release all resources. Called when the context was stoped.
     */
    public void stop();
}
