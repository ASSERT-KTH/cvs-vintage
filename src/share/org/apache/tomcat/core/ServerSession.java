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
import java.io.Serializable;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import org.apache.tomcat.util.*;
import org.apache.tomcat.util.threads.*;

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
public class ServerSession  implements  Serializable {

    public static final int STATE_NEW=0;

    public static final int STATE_ACCESSED=1;

    public static final int STATE_EXPIRED=2;

    public static final int STATE_INVALID=3;

    public static final int STATE_SUSPEND=4;

    public static final int STATE_RESTORED=5;


    private MessageBytes id = new MessageBytes();
    // XXX This must be replaced with a more efficient storage
    private Hashtable attributes = new Hashtable();

    TimeStamp ts=new TimeStamp();
    boolean distributable=false;
    Object manager;
    Context context;
    private Object notes[]=new Object[ContextManager.MAX_NOTES];
    private Counters cntr=new Counters(ContextManager.MAX_NOTES);
    private int state=STATE_NEW;
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

    /** The web application that creates this session
     */
    public Context getContext() {
	return context;
    }

    public void setContext( Context ctx ) {
	context=ctx;
    }

    public Object getFacade() {
	return facade;
    }

    public void setFacade( Object o ) {
	facade=o;
    }
    
    public int getState() {
	return state;
    }

    public void setState( int state ) {
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
	Enumeration attrs = getAttributeNames();
	while (attrs.hasMoreElements()) {
	    String attr = (String) attrs.nextElement();
	    removeAttribute(attr);
	}
    }

    public void removeAttribute(String name) {
	// Hashtable is already synchronized
	attributes.remove(name);
    }

    public void setAttribute(String name, Object value) {
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
    
    /**
     * Release all object references, and initialize instance variables, in
     * preparation for reuse of this object.
     */
    public void recycle() {
	// Reset the instance variables associated with this Session
	attributes.clear();
	ts.recycle();
	id.recycle();
    }

    
    // -------------------- Per-Request "notes" --------------------

    public final void setNote( int pos, Object value ) {
	notes[pos]=value;
    }

    public final Object getNote( int pos ) {
	return notes[pos];
    }

    public final Counters getCounters() {
	return cntr;
    }
    
}

