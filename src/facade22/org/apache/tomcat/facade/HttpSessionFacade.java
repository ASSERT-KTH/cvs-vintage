/*
 *  Copyright 1999-2004 The Apache Software Foundation
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
 *  See the License for the specific language 
 */


package org.apache.tomcat.facade;

import java.util.Enumeration;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionBindingListener;
import javax.servlet.http.HttpSessionContext;

import org.apache.tomcat.core.ServerSession;
import org.apache.tomcat.util.res.StringManager;

/**
 * Facade for http session. Used to prevent servlets to access
 * internal tomcat objects.
 *
 * This is a "special" facade - since session management is
 * (more or less) orthogonal to request processing, it is
 * indpendent of tomcat architecture. It will provide a
 * HttpSession implementation ( but it's not guaranteed
 * in any way it is "safe" ), and HttpSessionFacade will
 * act as a "guard" to make sure only servlet API public
 * methods are exposed.
 *
 * Another thing to note is that this object will be recycled
 * and will allways be set in a request. The "real" session
 * object will determine if the request is part of a session.
 *
 * @author James Duncan Davidson [duncan@eng.sun.com]
 * @author Jason Hunter [jch@eng.sun.com]
 * @author James Todd [gonzo@eng.sun.com]
 * @author costin@eng.sun.com
 */
public final class HttpSessionFacade implements HttpSession {
    private static StringManager sm =
        StringManager.getManager("org.apache.tomcat.resources");
    ServerSession realSession;
    //  We need to keep the Id, since it may change in realSession.
    private String sessionId;
    private boolean isValid = false;
    
    HttpSessionFacade() {
    }

    /** Package-level method - accessible only by core
     */
    void setRealSession(ServerSession s) {
 	realSession=s;
	realSession.setFacade( this );
	sessionId = realSession.getId().toString();
	isValid = true;
     }

    /** Package-level method - accessible only by core
     */
    void recycle() {
	isValid = false;
	//	realSession=null;
    }

    // -------------------- public facade --------------------

    public String getId() {
	return sessionId;
    }

    /**
     * Return the time when this session was created, in milliseconds since
     * midnight, January 1, 1970 GMT.
     *
     * @exception IllegalStateException if this method is called on an
     *  invalidated session
     */
    public long getCreationTime() {
	checkValid();
	return realSession.getTimeStamp().getCreationTime();
    }
    
    /**
     * We return our own "disabled" SessionContext -
     * regardless of what the real session returns.
     *
     * @deprecated
     */
    public HttpSessionContext getSessionContext() {
	return new SessionContextImpl();
    }
    
    public long getLastAccessedTime() {
	return realSession.getTimeStamp().getLastAccessedTime();
    }

    /**
     * Invalidates this session and unbinds any objects bound to it.
     *
     * @exception IllegalStateException if this method is called on
     *  an invalidated session
     */
    public void invalidate() {
	checkValid();
 	realSession.getTimeStamp().setValid( false );
	// remove all attributes
	if( dL > 0 ) d("Invalidate " + realSession.getId());
	realSession.setState(ServerSession.STATE_EXPIRED);
	realSession.recycle();
	realSession.setState(ServerSession.STATE_INVALID);
    }

    /**
     * Return <code>true</code> if the client does not yet know about the
     * session, or if the client chooses not to join the session.  For
     * example, if the server used only cookie-based sessions, and the client
     * has disabled the use of cookies, then a session would be new on each
     * request.
     *
     * @exception IllegalStateException if this method is called on an
     *  invalidated session
     */
    public boolean isNew() {
	checkValid();
	return realSession.getTimeStamp().isNew();
    }
    
    /**
     * @deprecated
     */
    public void putValue(String name, Object value) {
	setAttribute(name, value);
    }

    public void setAttribute(String name, Object value) {
        checkValid();
        Object oldValue;
        if (value instanceof HttpSessionBindingListener) {
	    synchronized( this ) {
		oldValue=realSession.getAttribute( name) ;
		if (oldValue!=null) {
		    removeAttribute(name);
		}
                try{
                    ((HttpSessionBindingListener) value).valueBound
                        (new HttpSessionBindingEvent( this, name));
                } catch ( Throwable th ) {
                }
		realSession.setAttribute( name, value );
	    }
	} else {
	    oldValue=realSession.getAttribute( name) ;
	    if (oldValue!=null) {
		removeAttribute(name);
	    }
	    // no sync overhead
	    realSession.setAttribute( name, value );
	}

    }

    /**
     * @deprecated
     */
    public Object getValue(String name) {
	return getAttribute(name);
    }

    public Object getAttribute(String name) {
	checkValid();
	return realSession.getAttribute(name);
    }
    
    /**
     * @deprecated
     */
    public String[] getValueNames() {
	checkValid();
	
	Enumeration attrs = getAttributeNames();
	String names[] = new String[realSession.getAttributeCount()];
	for (int i = 0; i < names.length; i++)
	    names[i] = (String)attrs.nextElement();
	return names;
    }

    /**
     * Return an <code>Enumeration</code> of <code>String</code> objects
     * containing the names of the objects bound to this session.
     *
     * @exception IllegalStateException if this method is called on an
     *  invalidated session
     */
    public Enumeration getAttributeNames() {
	checkValid();
	return realSession.getAttributeNames();
    }

    /**
     * @deprecated
     */
    public void removeValue(String name) {
	removeAttribute(name);
    }

    /**
     * Remove the object bound with the specified name from this session.  If
     * the session does not have an object bound with this name, this method
     * does nothing.
     * <p>
     * After this method executes, and if the object implements
     * <code>HttpSessionBindingListener</code>, the container calls
     * <code>valueUnbound()</code> on the object.
     *
     * @param name Name of the object to remove from this session.
     *
     * @exception IllegalStateException if this method is called on an
     *  invalidated session
     */
    public void removeAttribute(String name) {
	checkValid();
	Object object=realSession.getAttribute( name );
	if (object instanceof HttpSessionBindingListener) {
	    synchronized( this ) {
		// double check ( probably not needed since setAttribute calls
		// remove if it detects a value
		object=realSession.getAttribute( name );
		if( object != null ) {
		    realSession.removeAttribute(name);
                    try {
                        ((HttpSessionBindingListener) object).valueUnbound
                            (new HttpSessionBindingEvent( this, name));
                    } catch ( Throwable th ) {
                    }
		}
	    }
	} else {
	    // Regular object, no sync overhead
	    realSession.removeAttribute(name);
	}

    }

    public void setMaxInactiveInterval(int interval) {
	realSession.getTimeStamp().setMaxInactiveInterval( interval * 1000 );
    }

    public int getMaxInactiveInterval() {
	// We use long because it's better to do /1000 here than
	// every time the internal code does expire
	return (int)realSession.getTimeStamp().getMaxInactiveInterval()/1000;
    }

    // duplicated code, private
    private void checkValid() {
	if (! (realSession.getTimeStamp().isValid() && isValid )) {
	    throw new IllegalStateException
		(sm.getString("standardSession.getAttributeNames.ise"));
	}
    }

    private static final int dL=0;
    private void d(String s ) {
	System.err.println( "HttpSessionFacade: " + s );
    }


}
