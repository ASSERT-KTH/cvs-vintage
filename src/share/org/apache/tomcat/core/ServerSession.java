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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import org.apache.tomcat.util.buf.MessageBytes;
import org.apache.tomcat.util.buf.TimeStamp;


/**
 * Server representation of a Session.
 *
 *  - recyclable
 *  - serializable ( by external components )
 *
 * Components:
 *  - timestamp ( expire ) 
 *  - id
 *  - name/value repository
 *
 * @author Craig R. McClanahan
 * @author <a href="mailto:jon@latchkey.com">Jon S. Stevens</a>
 * @author Costin Manolache
 */
public class ServerSession {

    /** Session is new. Modules can do all the preparation
	work - set id, register it for expiration, etc
    */
    public static final int STATE_NEW=0;

    /** The session was accessed. Nothing big.
     */
    public static final int STATE_ACCESSED=1;

    /** If you set the server session object in the
	EXPIRED state, it'll do all the cleanup and
	be removed from the active sessions.

	You must make sure you recycle the object
	after setState(STATE_EXPIRED)
    */
    public static final int STATE_EXPIRED=2;

    public static final int STATE_INVALID=3;

    /** The session will be prepared for suspending -
	same as for reload. What can be preserved will be,
	or unbind events will be generated.
    */
    public static final int STATE_SUSPEND=4;

    /** After restart - or after reload
     */
    public static final int STATE_RESTORED=5;

    private int debug=0;
    private MessageBytes id = MessageBytes.newInstance();
    // XXX This must be replaced with a more efficient storage
    private Hashtable attributes = new Hashtable();

    TimeStamp ts=new TimeStamp();
    boolean distributable=false;
    Object manager;
    Context context;
    ContextManager contextM;
    private Object notes[]=new Object[ContextManager.MAX_NOTES];
    private int state=STATE_INVALID;
    Object facade;
    
    public ServerSession() {
    }

    /** The object that controls this server session. We don't
	care about the implementation details of the manager, but
	store a reference.
     */
    public void setManager( Object m ) {
	manager=m;
    }
    
    public Object getManager() {
	return manager;
    }

    /** The web application that creates this session.
     *  Don't relly on this, as we may have cross-context
     *  sessions in a future version. Used to get the session options,
     *  represents the webapp that creates the session.
     */
    public Context getContext() {
	return context;
    }

    public void setContext( Context ctx ) {
	context=ctx;
    }

    public void setContextManager( ContextManager cm ) {
	this.contextM=cm;
    }

    public Object getFacade() {
	return facade;
    }

    public void setFacade( Object o ) {
	facade=o;
    }
    
    public final int getState() {
	return state;
    }

    /** Change the state, call all hooks
     */
    public void setState( int state ) {
	if( context != null ) {
	    BaseInterceptor reqI[]=context.getContainer().
		getInterceptors(Container.H_sessionState);
	    for( int i=0; i< reqI.length; i++ ) {
		reqI[i].sessionState( null,
				      this,  state);
	    }
	}
	this.state=state;
    }

    protected void setState1( int state ) {
	this.state=state;
    }

    /** Change the state, call all hooks. The request that initiated
	the event is passed
     */
    public void setState( int state, Request req ) {
	if( context != null ) {
	    BaseInterceptor reqI[]=context.getContainer().
		getInterceptors(Container.H_sessionState);
	    for( int i=0; i< reqI.length; i++ ) {
		reqI[i].sessionState( req,
				      this,  state);
	    }
	}
	this.state=state;
    }
    
    // ----------------------------------------------------- Session Properties
    /** The time stamp associated with this session
     */
    public TimeStamp getTimeStamp() {
	return ts;
    }

    /**
     * Return the session identifier for this session.
     */
    public MessageBytes getId() {
	return id;
    }

    // -------------------- Attribute access --------------------

    public Object getAttribute(String name) {
	return attributes.get(name);
    }

    
    public Enumeration getAttributeNames() {
	return (attributes.keys());
    }

    public int getAttributeCount() {
	return attributes.size();
    }

    public void removeAllAttributes() {
	if( debug > 0 ) contextM.log("ServerSession:removeAllAttributes");
	Enumeration attrs = getAttributeNames();
	while (attrs.hasMoreElements()) {
	    String attr = (String) attrs.nextElement();
	    removeAttribute(attr);
	}
    }

    public void removeAttribute(String name) {
	if( debug > 0 ) contextM.log("ServerSession:removeAllAttribute "+ name);
	// Hashtable is already synchronized
	attributes.remove(name);
    }

    public void setAttribute(String name, Object value) {
	if( debug > 0 ) contextM.log("ServerSession:setAttribute "+ name);
	attributes.put(name, value);
    }

    /** Set the session access time
     */
    public void touch(long time ) {
	getTimeStamp().touch( time );
    }

    public void setValid( boolean b ) {
	getTimeStamp().setValid( b );
    }

    public boolean isValid() {
	return getTimeStamp().isValid();
    }

    /** Display debug messages. Set by the session creator - typically the
     *  session store ( SimpleSessionStore in 3.3 ).
     */
    public void setDebug(int d) {
	debug=d;
    }
    
    /**
     * Release all object references, and initialize instance variables, in
     * preparation for reuse of this object.
     */
    public void recycle() {
	// Reset the instance variables associated with this Session
	if( debug > 0 ) contextM.log("ServerSession:recycle ");
	facade=null;
	attributes.clear();
	ts.recycle();
    }

    
    // -------------------- Per-Request "notes" --------------------

    public final void setNote( int pos, Object value ) {
	notes[pos]=value;
    }

    public final Object getNote( int pos ) {
	return notes[pos];
    }

    public Object getNote( String name ) throws TomcatException {
	int id=context.getContextManager().
	    getNoteId( ContextManager.SESSION_NOTE,  name );
	return getNote( id );
    }

    public void setNote( String name, Object value ) throws TomcatException {
	int id=context.getContextManager().
	    getNoteId( ContextManager.SESSION_NOTE,name );
	setNote( id, value );
    }

}

