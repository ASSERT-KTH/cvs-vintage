/*
 * $Header: /tmp/cvs-vintage/tomcat/src/share/org/apache/tomcat/session/Attic/StandardManager.java,v 1.3 2000/02/14 04:59:41 costin Exp $
 * $Revision: 1.3 $
 * $Date: 2000/02/14 04:59:41 $
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


package org.apache.tomcat.session;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import org.apache.tomcat.catalina.*;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpSession;
import org.apache.tomcat.util.StringManager;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;


/**
 * Standard implementation of the <b>Manager</b> interface that provides
 * no session persistence or distributable capabilities, but does support
 * an optional, configurable, maximum number of active sessions allowed.
 * <p>
 * Lifecycle configuration of this component assumes an XML node
 * in the following format:
 * <code>
 *     &lt;Manager className="org.apache.tomcat.session.StandardManager"
 *              checkInterval="60" maxActiveSessions="-1"
 *              maxInactiveInterval="-1" />
 * </code>
 * where you can adjust the following parameters, with default values
 * in square brackets:
 * <ul>
 * <li><b>checkInterval</b> - The interval (in seconds) between background
 *     thread checks for expired sessions.  [60]
 * <li><b>maxActiveSessions</b> - The maximum number of sessions allowed to
 *     be active at once, or -1 for no limit.  [-1]
 * <li><b>maxInactiveInterval</b> - The default maximum number of seconds of
 *     inactivity before which the servlet container is allowed to time out
 *     a session, or -1 for no limit.  This value should be overridden from
 *     the default session timeout specified in the web application deployment
 *     descriptor, if any.  [-1]
 * </ul>
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.3 $ $Date: 2000/02/14 04:59:41 $
 */

public final class StandardManager
    extends ManagerBase
    implements Lifecycle, Runnable {


    // ----------------------------------------------------- Instance Variables


    /**
     * The interval (in seconds) between checks for expired sessions.
     */
    private int checkInterval = 60;


    /**
     * Has this component been configured yet?
     */
    private boolean configured = false;


    /**
     * The descriptive information about this implementation.
     */
    private static final String info = "StandardManager/1.0";


    /**
     * The maximum number of active Sessions allowed, or -1 for no limit.
     */
    protected int maxActiveSessions = -1;


    /**
     * The string manager for this package.
     */
    private StringManager sm =
        StringManager.getManager("org.apache.tomcat.session");


    /**
     * Has this component been started yet?
     */
    private boolean started = false;


    /**
     * The background thread.
     */
    private Thread thread = null;


    /**
     * The background thread completion semaphore.
     */
    private boolean threadDone = false;


    /**
     * Name to register for the background thread.
     */
    private String threadName = "StandardManager";


    // ------------------------------------------------------------- Properties


    /**
     * Return the check interval (in seconds) for this Manager.
     */
    public int getCheckInterval() {

	return (this.checkInterval);

    }


    /**
     * Set the check interval (in seconds) for this Manager.
     *
     * @param checkInterval The new check interval
     */
    public void setCheckInterval(int checkInterval) {

	this.checkInterval = checkInterval;

    }


    /**
     * Return descriptive information about this Manager implementation and
     * the corresponding version number, in the format
     * <code>&lt;description&gt;/&lt;version&gt;</code>.
     */
    public String getInfo() {

	return (this.info);

    }


    /**
     * Return the maximum number of active Sessions allowed, or -1 for
     * no limit.
     */
    public int getMaxActiveSessions() {

	return (this.maxActiveSessions);

    }


    /**
     * Set the maximum number of actives Sessions allowed, or -1 for
     * no limit.
     *
     * @param max The new maximum number of sessions
     */
    public void setMaxActiveSessions(int max) {

	this.maxActiveSessions = max;

    }


    // --------------------------------------------------------- Public Methods


    /**
     * Construct and return a new session object, based on the default
     * settings specified by this Manager's properties.  The session
     * id will be assigned by this method, and available via the getId()
     * method of the returned session.  If a new session cannot be created
     * for any reason, return <code>null</code>.
     *
     * @exception IllegalStateException if a new session cannot be
     *  instantiated for any reason
     */
    public Session createSession() {

	if ((maxActiveSessions >= 0) &&
	  (sessions.size() >= maxActiveSessions))
	    throw new IllegalStateException
		(sm.getString("standardManager.createSession.ise"));

	return (super.createSession());

    }


    // ------------------------------------------------------ Lifecycle Methods


    /**
     * Configure this component, based on the specified configuration
     * parameters.  This method should be called immediately after the
     * component instance is created, and before <code>start()</code>
     * is called.
     *
     * @param parameters Configuration parameters for this component
     *  (<B>FIXME: What object type should this really be?)
     *
     * @exception IllegalStateException if this component has already been
     *  configured and/or started
     * @exception LifecycleException if this component detects a fatal error
     *  in the configuration parameters it was given
     */
    public void configure(Node parameters)
	throws LifecycleException {

	// Validate and update our current component state
	if (configured)
	    throw new LifecycleException
		(sm.getString("standardManager.alreadyConfigured"));
	configured = true;
	if (parameters == null)
	    return;

	// Parse and process our configuration parameters
	if (!("Manager".equals(parameters.getNodeName())))
	    return;
	NamedNodeMap attributes = parameters.getAttributes();
	Node node = null;

	node = attributes.getNamedItem("checkInterval");
	if (node != null) {
	    try {
		setCheckInterval(Integer.parseInt(node.getNodeValue()));
	    } catch (Throwable t) {
		;	// XXX - Throw exception?
	    }
	}

	node = attributes.getNamedItem("maxActiveSessions");
	if (node != null) {
	    try {
		setMaxActiveSessions(Integer.parseInt(node.getNodeValue()));
	    } catch (Throwable t) {
		;	// XXX - Throw exception?
	    }
	}

	node = attributes.getNamedItem("maxInactiveInterval");
	if (node != null) {
	    try {
		setMaxInactiveInterval(Integer.parseInt(node.getNodeValue()));
	    } catch (Throwable t) {
		;	// XXX - Throw exception?
	    }
	}

    }


    /**
     * Prepare for the beginning of active use of the public methods of this
     * component.  This method should be called after <code>configure()</code>,
     * and before any of the public methods of the component are utilized.
     *
     * @exception IllegalStateException if this component has not yet been
     *  configured (if required for this component)
     * @exception IllegalStateException if this component has already been
     *  started
     * @exception LifecycleException if this component detects a fatal error
     *  that prevents this component from being used
     */
    public void start() throws LifecycleException {

	// Validate and update our current component state
	if (!configured)
	    throw new LifecycleException
		(sm.getString("standardManager.notConfigured"));
	if (started)
	    throw new LifecycleException
		(sm.getString("standardManager.alreadyStarted"));
	started = true;

	// Start the background reaper thread
	threadStart();

    }


    /**
     * Gracefully terminate the active use of the public methods of this
     * component.  This method should be the last one called on a given
     * instance of this component.
     *
     * @exception IllegalStateException if this component has not been started
     * @exception IllegalStateException if this component has already
     *  been stopped
     * @exception LifecycleException if this component detects a fatal error
     *  that needs to be reported
     */
    public void stop() throws LifecycleException {

	// Validate and update our current component state
	if (!started)
	    throw new LifecycleException
		(sm.getString("standardManager.notStarted"));
	started = false;

	// Stop the background reaper thread
	threadStop();

	// Expire all active sessions
	Session sessions[] = findSessions();
	for (int i = 0; i < sessions.length; i++) {
	    StandardSession session = (StandardSession) sessions[i];
	    if (!session.isValid())
		continue;
	    session.expire();
	}

    }


    // -------------------------------------------------------- Private Methods


    /**
     * Invalidate all sessions that have expired.
     */
    private void processExpires() {

	long timeNow = System.currentTimeMillis();
	Session sessions[] = findSessions();

	for (int i = 0; i < sessions.length; i++) {
	    StandardSession session = (StandardSession) sessions[i];
	    if (!session.isValid())
		continue;
	    int maxInactiveInterval = session.getMaxInactiveInterval();
	    if (maxInactiveInterval < 0)
		continue;
	    int timeIdle = // Truncate, do not round up
		(int) ((timeNow - session.getLastAccessedTime()) / 1000L);
	    if (timeIdle >= maxInactiveInterval)
		session.expire();
	}
    }


    /**
     * Sleep for the duration specified by the <code>checkInterval</code>
     * property.
     */
    private void threadSleep() {

	try {
	    Thread.sleep(checkInterval * 1000L);
	} catch (InterruptedException e) {
	    ;
	}

    }


    /**
     * Start the background thread that will periodically check for
     * session timeouts.
     */
    private void threadStart() {

	if (thread != null)
	    return;

	threadDone = false;
	thread = new Thread(this, threadName);
	thread.setDaemon(true);
	thread.start();

    }


    /**
     * Stop the background thread that is periodically checking for
     * session timeouts.
     */
    private void threadStop() {

	if (thread == null)
	    return;

	threadDone = true;
	thread.interrupt();
	try {
	    thread.join();
	} catch (InterruptedException e) {
	    ;
	}

	thread = null;

    }


    // ------------------------------------------------------ Background Thread


    /**
     * The background thread that checks for session timeouts and shutdown.
     */
    public void run() {

	// Loop until the termination semaphore is set
	while (!threadDone) {
	    threadSleep();
	    processExpires();
	}

    }


}
