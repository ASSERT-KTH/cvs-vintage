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


package org.apache.tomcat.session;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpSession;
import org.apache.tomcat.util.*;
import org.apache.tomcat.util.threads.*;
import org.apache.tomcat.helper.*;
import org.apache.tomcat.core.Request;

/**
 *
 * 
 */
public final class ServerSessionManager  implements ThreadPoolRunnable
{
    // ----------------------------------------------------- Instance Variables
    /**
     * The distributable flag for Sessions created by this Manager.  If this
     * flag is set to <code>true</code>, any user attributes added to a
     * session controlled by this Manager must be Serializable.
     */
    protected boolean distributable;

    /**
     * The default maximum inactive interval for Sessions created by
     * this Manager.
     */
    protected int maxInactiveInterval = 60;

    /** The set of previously recycled Sessions for this Manager.
     */
    protected SimplePool recycled = new SimplePool();

    /**
     * The set of currently active Sessions for this Manager, keyed by
     * session identifier.
     */
    protected Hashtable sessions = new Hashtable();

    /**
     * The interval (in seconds) between checks for expired sessions.
     */
    private int checkInterval = 60;

    /**
     * The maximum number of active Sessions allowed, or -1 for no limit.
     */
    protected int maxActiveSessions = -1;

    /**
     * The string manager for this package.
     */
    private static StringManager sm =
        StringManager.getManager("org.apache.tomcat.resources");

    protected Reaper reaper;

    public ServerSessionManager() {
    }

    // ------------------------------------------------------------- Properties
    /**
     * Return the distributable flag for the sessions supported by
     * this Manager.
     */
    public boolean getDistributable() {
	return (this.distributable);
    }

    /**
     * Set the distributable flag for the sessions supported by this
     * Manager.  If this flag is set, all user data objects added to
     * sessions associated with this manager must implement Serializable.
     *
     * @param distributable The new distributable flag
     */
    public void setDistributable(boolean distributable) {
	this.distributable = distributable;
    }

    /**
     * Return the default maximum inactive interval (in seconds)
     * for Sessions created by this Manager.
     */
    public int getMaxInactiveInterval() {
	return (this.maxInactiveInterval);
    }

    /**
     * Set the default maximum inactive interval (in seconds)
     * for Sessions created by this Manager.
     *
     * @param interval The new default value
     */
    public void setMaxInactiveInterval(int interval) {
	this.maxInactiveInterval = interval;
    }

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
    public void setSessionTimeOut(int minutes) {
        if(-1 != minutes) {
            // The manager works with seconds...
            setMaxInactiveInterval(minutes * 60);
        }
    }

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

    public int getMaxActiveSessions() {
	return maxActiveSessions;
    }

    public void setMaxActiveSessions(int max) {
	maxActiveSessions = max;
    }

    // --------------------------------------------------------- Public Methods

    public Hashtable getSessions() {
	return this.sessions;
    }

    public ServerSession findSession(String id) {
	if (id == null) return null;
	return (ServerSession)sessions.get(id);
    }

    public ServerSession getNewSession() {
	if ((maxActiveSessions >= 0) &&
	    (sessions.size() >= maxActiveSessions))
	    return null;

	// Recycle or create a Session instance
	ServerSession session = (ServerSession)recycled.get();
	if (session == null) {
	    session = new ServerSession();
	    recycled.put( session );
	}

	// XXX can return MessageBytes !!!
	String newId=SessionUtil.generateSessionId();

	// What if the newId belongs to an existing session ?
	// This shouldn't happen ( maybe we can try again ? )
	ServerSession oldS=findSession( newId );
	if( oldS!=null) {
	    // that's what the original code did
	    remove( oldS );
	}

	// Initialize the properties of the new session and return it
	session.getId().setString( newId );
	
	session.getTimeStamp().setNew(true);
	session.getTimeStamp().setValid(true);
	session.getTimeStamp().setCreationTime(System.currentTimeMillis());
	session.getTimeStamp().setMaxInactiveInterval(maxInactiveInterval);

	//	System.out.println("New session: " + newId );
	sessions.put( newId, session );

	return (session);
    }

    public void handleReload(Request req, ClassLoader newLoader) {
	sessions = SessionSerializer.doSerialization( newLoader, sessions);
    }

    public void start() {
	// Start the background reaper thread
	reaper=new Reaper("StandardManager");
	reaper.addCallback( this, checkInterval * 1000 );
	reaper.startReaper();
    }

    public void stop() {
	reaper.stopReaper();

	// expire all active sessions
	Enumeration ids = sessions.keys();
	while (ids.hasMoreElements()) {
	    String id = (String) ids.nextElement();
	    ServerSession session = (ServerSession) sessions.get(id);
	    if (!session.getTimeStamp().isValid())
		continue;
	    session.expire();
	}
    }

    /**
     * Remove this Session from the active Sessions for this Manager.
     *
     * @param session Session to be removed
     */
    void remove(ServerSession session) {
	sessions.remove(session.getId().toString());
	recycled.put(session);
    }


    // -------------------------------------------------------- Private Methods

    // ThreadPoolRunnable impl

    public Object[] getInitData() {
	return null;
    }

    public void runIt( Object td[] ) {
	//	System.out.println("Expiring " + this);
	long timeNow = System.currentTimeMillis();
	Enumeration ids = sessions.keys();
	while (ids.hasMoreElements()) {
	    String id = (String) ids.nextElement();
	    ServerSession session = (ServerSession) sessions.get(id);
	    TimeStamp ts=session.getTimeStamp();
	    
	    if (!ts.isValid())
		continue;
	    
	    int maxInactiveInterval = ts.getMaxInactiveInterval();
	    if (maxInactiveInterval < 0)
		continue;
	    
	    int timeIdle = // Truncate, do not round up
		(int) ((timeNow - ts.getLastAccessedTime()) / 1000L);

	    if (timeIdle >= maxInactiveInterval)
		session.expire();
	}
    }

}
