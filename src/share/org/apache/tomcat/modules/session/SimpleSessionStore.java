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
package org.apache.tomcat.modules.session;

import java.io.*;
import java.util.Random;
import org.apache.tomcat.util.*;
import org.apache.tomcat.util.compat.*;
import org.apache.tomcat.util.threads.*;
import org.apache.tomcat.core.*;
import java.util.*;
import org.apache.tomcat.util.collections.SimplePool;
import org.apache.tomcat.util.log.*;
import org.apache.tomcat.util.buf.*;
import java.security.*;


/**
 * A simple session store plugin. It will create, store and maintain
 * session objects using a simple in-memory pool.
 *
 * It must be inserted after SessionId, which does common
 * session stuff ( cookie, rewrite, etc)
 *
 * @author costin@eng.sun.com
 * @author hans@gefionsoftware.com
 * @author pfrieden@dChain.com
 * @author Shai Fultheim [shai@brm.com]
 */
public final class SimpleSessionStore  extends BaseInterceptor {
    int manager_note;
    int maxActiveSessions = -1;
    int size=16;
    int max=256;
    static final String SESSIONS_RELOAD = "tomcat.sessions.reload";
    
    public SimpleSessionStore() {
    }

    // -------------------- Configuration properties --------------------

    public void setMaxActiveSessions( int count ) {
	maxActiveSessions=count;
    }

    public void setInitialPool( int initial ) {
	size=initial;
    }

    public void setMaxPool( int max ) {
	this.max=max;
    }
    
    // -------------------- Tomcat request events --------------------
    public void engineInit( ContextManager cm ) throws TomcatException {
	// set-up a per/container note for StandardManager
	manager_note = cm.getNoteId( ContextManager.CONTAINER_NOTE,
				     "tomcat.standardManager");
    }

    
    public void copyContext(Request req, Context oldC, Context newC)
	throws TomcatException {
	contextInit(newC);
	ClassLoader oldLoader = oldC.getClassLoader();
	SimpleSessionManager sM = getManager( oldC );    
	SimpleSessionManager sMnew = getManager( newC );

	// remove all non-serializable objects from session
	Enumeration sessionEnum=sM.getSessions();
	while( sessionEnum.hasMoreElements() ) {
	    ServerSession session = (ServerSession)sessionEnum.nextElement();
	    ServerSession newS = sMnew.cloneSession(req, newC, 
						    session.getId().toString());
	    Enumeration e = session.getAttributeNames();
	    while( e.hasMoreElements() ) {
		String key = (String) e.nextElement();
		Object value = session.getAttribute(key);
		newS.setAttribute(key, value);
	    }
	}
	newC.getContainer().setNote(SESSIONS_RELOAD, req);
    }

    private void processSession(ServerSession session, 
				ClassLoader oldCL, ClassLoader newCL)
	throws TomcatException {

	Hashtable newSession=new Hashtable();
	Enumeration e = session.getAttributeNames();
	while( e.hasMoreElements() )   {
	    String key = (String) e.nextElement();
	    Object value = session.getAttribute(key);
	    if ( value instanceof Serializable ) {
		Object newValue =
		    ObjectSerializer.doSerialization( newCL,
						      value);
		newSession.put( key, newValue );
	    } 
	    else if( value.getClass().getClassLoader() != oldCL ) {
		// it's loaded by the parent loader, no need to reload
		newSession.put( key, value );
	    } 
	}
	// If saving back to the same session.
	// Remove all objects we know how to handle
	e=newSession.keys();
	while( e.hasMoreElements() )   {
	    String key = (String) e.nextElement();
	    session.removeAttribute(key);
	}

	if( debug > 0 ) log("Prepare for reloading, SUSPEND " + session );
	// If anyone can save the rest of the attributes or at least notify
	// the owner...
	session.setState( ServerSession.STATE_SUSPEND, null );
	    
	if( debug > 0 ) log("After reloading, RESTORED " + session );
	session.setState( ServerSession.STATE_RESTORED, null );

	/* now put back all attributes */
	e=newSession.keys();
	while(e.hasMoreElements() ) {
	    String key = (String) e.nextElement();
	    Object value=newSession.get(key );
	    session.setAttribute( key, value );
	}
    }

    public void reload( Request req, Context ctx ) throws TomcatException {
	ClassLoader newLoader = ctx.getClassLoader();
	ClassLoader oldLoader=(ClassLoader)ctx.getContainer().
	    getNote("oldLoader");
	SimpleSessionManager sM = getManager( ctx );    

	// remove all non-serializable objects from session
	Enumeration sessionEnum=sM.getSessions();
	while( sessionEnum.hasMoreElements() ) {
	    ServerSession session = (ServerSession)sessionEnum.nextElement();
	    processSession(session,  oldLoader, newLoader);
	}
    }

    /** The session store hook
     */
    public ServerSession findSession( Request request,
				      String sessionId, boolean create)
    {
	Context ctx=request.getContext();
	if( ctx==null ) return null;
	
	SimpleSessionManager sM = getManager( ctx );    
	
	ServerSession sess=sM.findSession( sessionId );
	if( sess!= null ) return sess;

	if( ! create ) return null; // not found, don't create

	if ((maxActiveSessions >= 0) &&
	    (sM.getSessionCount() >= maxActiveSessions)) {
	    log( "Too many sessions " + maxActiveSessions );
	    return null;
	}

	ServerSession newS=sM.getNewSession(request, ctx);
	if( newS==null ) {
	    log( "Create session failed " );
	    return null;
	}
	
	return newS;
    }

    //--------------------  Tomcat context events --------------------


    /** Init session management stuff for this context. 
     */
    public void contextInit(Context ctx) throws TomcatException {
	// Defaults !!
	SimpleSessionManager sm= getManager( ctx );

	if( sm == null ) {
	    sm=new SimpleSessionManager();
	    sm.setDebug( debug );
	    sm.setModule( this );
	    ctx.getContainer().setNote( manager_note, sm );
	}
	if(ctx.getContainer().getNote(SESSIONS_RELOAD) != null ) {
	    Request req = (Request)ctx.getContainer().getNote(SESSIONS_RELOAD);
	    reload(req, ctx);
	    // Dump for GC.
	    ctx.getContainer().setNote(SESSIONS_RELOAD,null);
	}
		
    }

    /** Notification of context shutdown.
     *  We should clean up any resources that are used by our
     *  session management code. 
     */
    public void contextShutdown( Context ctx )
	throws TomcatException
    {
	if( debug > 0 )
	    log("Removing sessions from " + ctx );

	SimpleSessionManager sm=getManager(ctx);
	Enumeration ids = sm.getSessionIds();
	while (ids.hasMoreElements()) {
	    String id = (String) ids.nextElement();
	    ServerSession session = sm.findSession(id);
	    if (!session.getTimeStamp().isValid())
		continue;
	    if( debug > 0 )
		log( "Shuting down " + id );
	    session.setState( ServerSession.STATE_SUSPEND );
	    session.setState( ServerSession.STATE_EXPIRED );
	    session.setState( ServerSession.STATE_INVALID );
	}
    }

    public int sessionState( Request req, ServerSession session, int state ) {
	TimeStamp ts=session.getTimeStamp();

	if( state==ServerSession.STATE_INVALID ) {
	    // session moved to expire state - remove all attributes from
	    // storage
	    SimpleSessionManager ssm=(SimpleSessionManager)session.getManager();
	    ssm.removeSession( session );
	}
	return state;
    }

    // -------------------- State Info -------------------- 
    public Enumeration getSessionIds(Context ctx) {
	SimpleSessionManager sm= getManager( ctx );
	return sm.getSessionIds();
    }
    
    public Enumeration getSessions(Context ctx) {
	SimpleSessionManager sm= getManager( ctx );
	return sm.getSessions();
    }
    
    public int getSessionCount(Context ctx) {
	SimpleSessionManager sm= getManager( ctx );
	return sm.getSessionCount();
    }
    
    public int getRecycledCount(Context ctx) {
	SimpleSessionManager sm= getManager( ctx );
	return sm.getRecycledCount();
    }

    public ServerSession findSession( Context ctx, String sessionId)
    {
	SimpleSessionManager sM = getManager( ctx );    
	return sM.findSession( sessionId );
    }

    
    // -------------------- Internal methods --------------------

    
    private SimpleSessionManager getManager( Context ctx ) {
	return (SimpleSessionManager)ctx.getContainer().getNote(manager_note);
    }

    /**
     * The actual "simple" manager
     * 
     */
    public static class SimpleSessionManager  
    {
	private int debug=0;
	private BaseInterceptor mod;
	/** The set of previously recycled Sessions for this Manager.
	 */
	protected SimplePool recycled = new SimplePool();
	
	/**
	 * The set of currently active Sessions for this Manager, keyed by
	 * session identifier.
	 */
	protected Hashtable sessions = new Hashtable();

	public SimpleSessionManager() {
	}

	public void setDebug( int l ) {
	    debug=l;
	}

	public void setModule( BaseInterceptor bi ) {
	    mod=bi;
	}

	// --------------------------------------------- Public Methods

	public Enumeration getSessionIds() {
	    return sessions.keys();
	}

	public Enumeration getSessions() {
	    return sessions.elements();
	}

	public int getSessionCount() {
	    return sessions.size();
	}

	public int getRecycledCount() {
	    return recycled.getCount();
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
	    if( debug>0 ) mod.log( "removeSession " + session );
	    sessions.remove(session.getId().toString());
	    recycled.put(session);
	    session.setValid(false);
	    // Do not recycle it yet - whoever expires it should also recyle.
	    // Otherwise we may miss something
	    // session.recycle();
	    //	    session.removeAllAttributes();
	}

	public ServerSession getNewSession(Request req, Context ctx) {
	    // Recycle or create a Session instance
	    ServerSession session = (ServerSession)recycled.get();
	    if (session == null) {
		session = ctx.getContextManager().createServerSession();
		session.setManager( this );
		session.setDebug( debug );
	    }
	    session.setContext( ctx );

	    session.setState( ServerSession.STATE_NEW, req );
	    
	    // The id will be set by one of the modules
	    String newId=session.getId().toString();
	    
//XXXXX - the following is a temporary fix only!  Underlying problem
//        is:  Why is the newId==null?

	    newId=(newId==null)?"null":newId;
	    
	    // What if the newId belongs to an existing session ?
	    // This shouldn't happen ( maybe we can try again ? )
	    ServerSession oldS=findSession( newId );
	    if( oldS!=null) {
		// that's what the original code did
		oldS.setState( ServerSession.STATE_EXPIRED );
		oldS.recycle();
		oldS.setState( ServerSession.STATE_INVALID );
	    }
	    sessions.put( newId, session );
	    return (session);
	}
	public ServerSession cloneSession(Request req, Context ctx, String oldS) {
	    // Recycle or create a Session instance
	    ServerSession session = (ServerSession)recycled.get();
	    if (session == null) {
		session = ctx.getContextManager().createServerSession();
		session.setManager( this );
		session.setDebug( debug );
	    }
	    session.setContext( ctx );

	    session.setState( ServerSession.STATE_NEW, req );
	    
	    session.getId().setString(oldS);

	    // The id will be set by one of the modules
	    String newId=session.getId().toString();
	    
//XXXXX - the following is a temporary fix only!  Underlying problem
//        is:  Why is the newId==null?

	    newId=(newId==null)?"null":newId;
	    
	    sessions.put( newId, session );
	    return (session);
	}

    }
}
