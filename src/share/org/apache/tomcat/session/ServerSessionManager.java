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
import org.apache.tomcat.core.*;

/**
 *
 * 
 */
public final class ServerSessionManager  
{
    /** The set of previously recycled Sessions for this Manager.
     */
    protected SimplePool recycled = new SimplePool();

    /**
     * The set of currently active Sessions for this Manager, keyed by
     * session identifier.
     */
    protected Hashtable sessions = new Hashtable();

    protected Expirer expirer;
    /**
     * The interval (in seconds) between checks for expired sessions.
     */
    private int checkInterval = 60;

    /**
     * The maximum number of active Sessions allowed, or -1 for no limit.
     */
    protected int maxActiveSessions = -1;

    long maxInactiveInterval;
    
    protected Reaper reaper;

    public ServerSessionManager() {
    }

    public void setExpirer( Expirer ex ) {
	expirer = ex;
    }

    public Expirer getExpirer() {
	return expirer;
    }
    
    // ------------------------------------------------------------- Properties
    public int getMaxActiveSessions() {
	return maxActiveSessions;
    }

    public void setMaxActiveSessions(int max) {
	maxActiveSessions = max;
    }

    // --------------------------------------------------------- Public Methods

    public void setMaxInactiveInterval( long l ) {
	maxInactiveInterval=l;
    }
    
    /**
     * Return the default maximum inactive interval (in miliseconds)
     * for Sessions created by this Manager. We use miliseconds
     * because that's how the time is expressed, avoid /1000
     * in the common code
     */
    public long getMaxInactiveInterval() {
	return maxInactiveInterval;
    }


    Hashtable getSessions() {
	return sessions;
    }
    
    void setSessions(Hashtable s) {
	sessions=s;
    }

    public ServerSession findSession(String id) {
	if (id == null) return null;
	return (ServerSession)sessions.get(id);
    }

    /**
     * Remove this Session from the active Sessions for this Manager.
     *
     * @param session Session to be removed
     */
    public void removeSession(ServerSession session) {
	sessions.remove(session.getId().toString());
	recycled.put(session);
	session.removeAllAttributes();
	expirer.removeManagedObject( session.getTimeStamp());
	session.getTimeStamp().setValid(false);
    }

    public ServerSession getNewSession() {
	if ((maxActiveSessions >= 0) &&
	    (sessions.size() >= maxActiveSessions))
	    return null;

	// Recycle or create a Session instance
	ServerSession session = (ServerSession)recycled.get();
	if (session == null) {
	    session = new ServerSession(this);
	    recycled.put( session );
	}
	
	// XXX can return MessageBytes !!!
	String newId=SessionUtil.generateSessionId();

	// What if the newId belongs to an existing session ?
	// This shouldn't happen ( maybe we can try again ? )
	ServerSession oldS=findSession( newId );
	if( oldS!=null) {
	    // that's what the original code did
	    removeSession( oldS );
	}

	// Initialize the properties of the new session and return it
	session.getId().setString( newId );

	TimeStamp ts=session.getTimeStamp();
	ts.setNew(true);
	ts.setValid(true);

	ts.setCreationTime(System.currentTimeMillis());
	ts.setMaxInactiveInterval(getMaxInactiveInterval());
	session.getTimeStamp().setParent( session );

	//	System.out.println("New session: " + newId );
	sessions.put( newId, session );
	expirer.addManagedObject( session.getTimeStamp());
	return (session);
    }

    public void removeAllSessions() {
	Enumeration ids = sessions.keys();
	while (ids.hasMoreElements()) {
	    String id = (String) ids.nextElement();
	    ServerSession session = (ServerSession) sessions.get(id);
	    if (!session.getTimeStamp().isValid())
		continue;
	    removeSession( session );
	}
    }

}
