/*   
 *  Copyright 1999-2004 The Apache Sofware Foundation.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.tomcat.core;

import java.util.Enumeration;
import java.util.Hashtable;

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

