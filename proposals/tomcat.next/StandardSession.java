/*
 * $Header: /tmp/cvs-vintage/tomcat/proposals/tomcat.next/Attic/StandardSession.java,v 1.1 2000/01/08 03:54:03 craigmcc Exp $
 * $Revision: 1.1 $
 * $Date: 2000/01/08 03:54:03 $
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


// package org.apache.tomcat.session.standard;


import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import javax.servlet.ServletException;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionBindingListener;
import javax.servlet.http.HttpSessionContext;


/**
 * Standard implementation of the <b>Session</b> interface.  This object is
 * serializable, so that it can be stored in persistent storage or transferred
 * to a different JVM for distributable session support.
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.1 $ $Date: 2000/01/08 03:54:03 $
 */

final class StandardSession
    implements HttpSession, Session {


    // ----------------------------------------------------------- Constructors


    /**
     * Construct a new Session associated with the specified Manager.
     *
     * @param manager The manager with which this Session is associated
     */
    public StandardSession(Manager manager) {

	super();
	this.manager = manager;

    }


    // ----------------------------------------------------- Instance Variables


    /**
     * The collection of user data attributes associated with this Session.
     */
    private Hashtable attributes = new Hashtable();


    /**
     * The time this session was created, in milliseconds since midnight,
     * January 1, 1970 GMT.
     */
    private long creationTime = 0L;


    /**
     * The session identifier of this Session.
     */
    private String id = null;


    /**
     * Descriptive information describing this Session implementation.
     */
    private static final String info = "StandardSession/1.0";


    /**
     * The last accessed time for this Session.
     */
    private long lastAccessedTime = 0L;


    /**
     * The Manager with which this Session is associated.
     */
    private Manager manager = null;


    /**
     * The maximum time interval, in seconds, between client requests before
     * the servlet container may invalidate this session.  A negative time
     * indicates that the session should never time out.
     */
    private int maxInactiveInterval = -1;


    /**
     * Flag indicating whether this session is new or not.
     */
    private boolean isNew = false;


    /**
     * Flag indicating whether this session is valid or not.
     */
    private boolean isValid = false;


    // ----------------------------------------------------- Session Properties


    /**
     * Return the session identifier for this session.
     */
    public String getId() {

	return (this.id);

    }


    /**
     * Set the session identifier for this session.
     *
     * @param id The new session identifier
     */
    public void setId(String id) {

	if ((this.id != null) && (manager != null) &&
	  (manager instanceof ManagerBase))
	    ((ManagerBase) manager).remove(this);

	this.id = id;

	if ((manager != null) && (manager instanceof ManagerBase))
	    ((ManagerBase) manager).add(this);

    }


    /**
     * Return descriptive information about this Session implementation and
     * the corresponding version number, in the format
     * <code>&lt;description&gt;/&lt;version&gt;</code>.
     */
    public String getInfo() {

	return (this.info);

    }


    /**
     * Return the last time the client sent a request associated with this
     * session, as the number of milliseconds since midnight, January 1, 1970
     * GMT.  Actions that your application takes, such as getting or setting
     * a value associated with the session, do not affect the access time.
     */
    public long getLastAccessedTime() {

	return (this.lastAccessedTime);

    }


    /**
     * Set the last time the client sent a request associated with this
     * session, as the number of milliseconds since midnight, January 1, 1970
     * GMT.
     *
     * @param time The new last accessed time
     */
    public void setLastAccessedTime(long time) {

	this.lastAccessedTime = time;

    }


    /**
     * Return the Manager within which this Session is valid.
     */
    public Manager getManager() {

	return (this.manager);

    }


    /**
     * Set the Manager within which this Session is valid.
     *
     * @param manager The new Manager
     */
    public void setManager(Manager manager) {

	this.manager = manager;

    }


    /**
     * Return the maximum time interval, in seconds, between client requests
     * before the servlet container will invalidate the session.  A negative
     * time indicates that the session should never time out.
     */
    public int getMaxInactiveInterval() {

	return (this.maxInactiveInterval);

    }


    /**
     * Set the maximum time interval, in seconds, between client requests
     * before the servlet container will invalidate the session.  A negative
     * time indicates that the session should never time out.
     *
     * @param interval The new maximum interval
     */
    public void setMaxInactiveInterval(int interval) {

	this.maxInactiveInterval = interval;

    }


    /**
     * Return the <code>HttpSession</code> for which this object
     * is the facade.
     */
    public HttpSession getSession() {

	return ((HttpSession) this);

    }


    // ------------------------------------------------- Session Public Methods


    /**
     * Perform the internal processing required to invalidate this session,
     * without triggering an exception if the session has already expired.
     */
    public void expire() {

	// Remove this session from our manager's active sessions
	if ((manager != null) && (manager instanceof ManagerBase))
	    ((ManagerBase) manager).remove(this);

	// Unbind any objects associated with this session
	Vector results = new Vector();
	Enumeration attrs = getAttributeNames();
	while (attrs.hasMoreElements()) {
	    String attr = (String) attrs.nextElement();
	    results.addElement(attr);
	}
	Enumeration names = results.elements();
	while (names.hasMoreElements()) {
	    String name = (String) names.nextElement();
	    removeAttribute(name);
	}

	// Mark this session as invalid
	setValid(false);

    }


    /**
     * Release all object references, and initialize instance variables, in
     * preparation for reuse of this object.
     */
    public void recycle() {

	// Reset the instance variables associated with this Session
	attributes.clear();
	creationTime = 0L;
	id = null;
	lastAccessedTime = 0L;
	manager = null;
	maxInactiveInterval = -1;
	isNew = false;
	isValid = false;

	// Tell our Manager that this Session has been recycled
	if ((manager != null) && (manager instanceof ManagerBase))
	    ((ManagerBase) manager).recycle(this);

    }


    // ------------------------------------------------ Session Package Methods


    /**
     * Return the <code>isValid</code> flag for this session.
     */
    boolean isValid() {

	return (this.isValid);

    }


    /**
     * Set the session creation time of this session, in milliseconds since
     * midnight, January 1, 1970 GMT.
     *
     * @param time The session creation time
     */
    void setCreationTime(long time) {

	this.creationTime = time;

    }


    /**
     * Set the <code>isNew</code> flag for this session.
     *
     * @param isNew The new value for the <code>isNew</code> flag
     */
    void setNew(boolean isNew) {

	this.isNew = isNew;

    }


    /**
     * Set the <code>isValid</code> flag for this session.
     *
     * @param isValid The new value for the <code>isValid</code> flag
     */
    void setValid(boolean isValid) {

	this.isValid = isValid;
    }


    // ------------------------------------------------- HttpSession Properties


    /**
     * Return the time when this session was created, in milliseconds since
     * midnight, January 1, 1970 GMT.
     */
    public long getCreationTime() {

	return (this.creationTime);

    }


    /**
     * Return the session context with which this session is associated.
     *
     * @deprecated As of Version 2.1, this method is deprecated and has no
     *  replacement.  It will be removed in a future version of the
     *  Java Servlet API.
     */
    public HttpSessionContext getSessionContext() {

	return (null);

    }


    // ----------------------------------------------HttpSession Public Methods


    /**
     * Return the object bound with the specified name in this session, or
     * <code>null</code> if no object is bound with that name.
     *
     * @param name Name of the attribute to be returned
     */
    public Object getAttribute(String name) {

	return (attributes.get(name));

    }


    /**
     * Return an <code>Enumeration</code> of <code>String</code> objects
     * containing the names of the objects bound to this session.
     */
    public Enumeration getAttributeNames() {

	return (attributes.keys());

    }


    /**
     * Return the object bound with the specified name in this session, or
     * <code>null</code> if no object is bound with that name.
     *
     * @param name Name of the value to be returned
     *
     * @deprecated As of Version 2.2, this method is replaced by
     *  <code>getAttribute()</code>
     */
    public Object getValue(String name) {

	return (getAttribute(name));

    }


    /**
     * Return the set of names of objects bound to this session.  If there
     * are no such objects, a zero-length array is returned.
     *
     * @deprecated As of Version 2.2, this method is replaced by
     *  <code>getAttributeNames()</code>
     */
    public String[] getValueNames() {

	Vector results = new Vector();
	Enumeration attrs = getAttributeNames();
	while (attrs.hasMoreElements()) {
	    String attr = (String) attrs.nextElement();
	    results.addElement(attr);
	}
	String names[] = new String[results.size()];
	for (int i = 0; i < names.length; i++)
	    names[i] = (String) results.elementAt(i);
	return (names);

    }


    /**
     * Invalidates this session and unbinds any objects bound to it.
     *
     * @exception IllegalStateException if this method is called on
     *  an invalidated session
     */
    public void invalidate() {

	// FIXME: Use a localized string
	if (!isValid())
	    throw new IllegalStateException("isNew:  Invalidated session");

	// Cause this session to expire
	expire();

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

	// FIXME: Use a localized string
	if (!isValid())
	    throw new IllegalStateException("isNew:  Invalidated session");

	return (this.isNew);

    }


    /**
     * Bind an object to this session, using the specified name.  If an object
     * of the same name is already bound to this session, the object is
     * replaced.
     * <p>
     * After this method executes, and if the object implements
     * <code>HttpSessionBindingListener</code>, the container calls
     * <code>valueBound()</code> on the object.
     *
     * @param name Name to which the object is bound, cannot be null
     * @param value Object to be bound, cannot be null
     *
     * @deprecated As of Version 2.2, this method is replaced by
     *  <code>setAttribute()</code>
     */
    public void putValue(String name, Object value) {

	setAttribute(name, value);

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

	// FIXME: Use a localized string
	if (!isValid())
	    throw new IllegalStateException("removeAttribute:  Invalidated session");

	synchronized (attributes) {
	    Object object = attributes.get(name);
	    if (object == null)
		return;
	    attributes.remove(name);
	    if (object instanceof HttpSessionBindingListener)
		((HttpSessionBindingListener) object).valueUnbound
		    (new HttpSessionBindingEvent((HttpSession) this, name));
	}

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
     *
     * @deprecated As of Version 2.2, this method is replaced by
     *  <code>removeAttribute()</code>
     */
    public void removeValue(String name) {

	removeAttribute(name);

    }


    /**
     * Bind an object to this session, using the specified name.  If an object
     * of the same name is already bound to this session, the object is
     * replaced.
     * <p>
     * After this method executes, and if the object implements
     * <code>HttpSessionBindingListener</code>, the container calls
     * <code>valueBound()</code> on the object.
     *
     * @param name Name to which the object is bound, cannot be null
     * @param value Object to be bound, cannot be null
     *
     * @excpetion IllegalArgumentException if an attempt is made to add a
     *  non-serializable object in an environment marked distributable.
     * @exception IllegalStateException if this method is called on an
     *  invalidated session
     */
    public void setAttribute(String name, Object value) {

	// FIXME: Use a localized string
	if (!isValid())
	    throw new IllegalStateException("setAttribute:  Invalidated session");

	// FIXME: Use a localized string
	if ((manager != null) && manager.getDistributable() &&
	  !(value instanceof Serializable))
	    throw new IllegalArgumentException("setAttribute:  Non-serializable object");

	synchronized (attributes) {
	    removeAttribute(name);
	    attributes.put(name, value);
	    if (value instanceof HttpSessionBindingListener)
		((HttpSessionBindingListener) value).valueBound
		    (new HttpSessionBindingEvent((HttpSession) this, name));
	}

    }


    // -------------------------------------------- HttpSession Private Methods


    /**
     * Read a serialized version of this session object from the specified
     * object input stream.
     * <p>
     * <b>IMPLEMENTATION NOTE</b>:  The reference to the owning Manager
     * is not restored by this method, and must be set explicitly.
     *
     * @param stream The input stream to read from
     *
     * @exception ClassNotFoundException if an unknown class is specified
     * @exception IOException if an input/output error occurs
     */
    private void readObject(ObjectInputStream stream)
	throws ClassNotFoundException, IOException {

	// Deserialize the scalar instance variables (except Manager)
	creationTime = ((Long) stream.readObject()).longValue();
	id = (String) stream.readObject();
	lastAccessedTime = ((Long) stream.readObject()).longValue();
	maxInactiveInterval = ((Integer) stream.readObject()).intValue();
	isNew = ((Boolean) stream.readObject()).booleanValue();
	isValid = ((Boolean) stream.readObject()).booleanValue();

	// Deserialize the attribute count and attribute values
	int n = ((Integer) stream.readObject()).intValue();
	for (int i = 0; i < n; i++) {
	    String name = (String) stream.readObject();
	    Object value = (Object) stream.readObject();
	    attributes.put(name, value);
	}

    }


    /**
     * Write a serialized version of this session object to the specified
     * object output stream.
     * <p>
     * <b>IMPLEMENTATION NOTE</b>:  The owning Manager will not be stored
     * in the serialized representation of this Session.  After calling
     * <code>readObject()</code>, you must set the associated Manager
     * explicitly.
     * <p>
     * <b>IMPLEMENTATION NOTE</b>:  Any attribute that is not Serializable
     * will be silently ignored.  If you do not want any such attributes,
     * be sure the <code>distributable</code> property of our associated
     * Manager is set to <code>true</code>.
     *
     * @param stream The output stream to write to
     *
     * @exception IOException if an input/output error occurs
     */
    private void writeObject(ObjectOutputStream stream) throws IOException {

	// Write the scalar instance variables (except Manager)
	stream.writeObject(new Long(creationTime));
	stream.writeObject(id);
	stream.writeObject(new Long(lastAccessedTime));
	stream.writeObject(new Integer(maxInactiveInterval));
	stream.writeObject(new Boolean(isNew));
	stream.writeObject(new Boolean(isValid));

	// Accumulate the names of serializable attributes
	Vector results = new Vector();
	Enumeration attrs = getAttributeNames();
	while (attrs.hasMoreElements()) {
	    String attr = (String) attrs.nextElement();
	    Object value = attributes.get(attr);
	    if (value instanceof Serializable)
		results.addElement(attr);
	}

	// Serialize the attribute count and the  attribute values
	stream.writeObject(new Integer(results.size()));
	Enumeration names = results.elements();
	while (names.hasMoreElements()) {
	    String name = (String) names.nextElement();
	    stream.writeObject(name);
	    stream.writeObject(attributes.get(name));
	}


    }


}
