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
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import org.apache.tomcat.util.*;
import javax.servlet.http.*;

/**
 * Server representation of a Session.
 *
 *  - recyclable
 *  - serializable ( by external components )
 *
 * @author Craig R. McClanahan
 * @author <a href="mailto:jon@latchkey.com">Jon S. Stevens</a>
 * @author Costin Manolache
 */
public final class ServerSession  implements  Serializable {
    private static StringManager sm =
        StringManager.getManager("org.apache.tomcat.resources");

    private MessageBytes id = new MessageBytes();
    // XXX This must be replaced with a more efficient storage
    private Hashtable attributes = new Hashtable();

    TimeStamp ts=new TimeStamp();
    boolean distributable=false;
    
    public ServerSession() {
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

    public boolean isDistributable() {
	return distributable;
    }

    public void setDistributable( boolean b ) {
	distributable=b;
    }
    
    // --------------------

    /**
     * Perform the internal processing required to invalidate this session,
     * without triggering an exception if the session has already expired.
     */
    public void expire() {

	// Remove this session from our manager's active sessions
// 	if (manager != null) 
// 	    manager.remove(this);

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
	ts.setValid(false);

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

    public void removeAttribute(String name) {
	synchronized (attributes) {
	    Object object = attributes.get(name);
	    if (object == null)
		return;
	    attributes.remove(name);
	    //	    System.out.println( "Removing attribute " + name );
// 	    if (object instanceof HttpSessionBindingListener) {
// 		((HttpSessionBindingListener) object).valueUnbound
// 		    (new HttpSessionBindingEvent((HttpSession) this, name));
// 	    }
	}
    }

    public void setAttribute(String name, Object value) {
	synchronized (attributes) {
	    removeAttribute(name);
	    attributes.put(name, value);
// 	    if (value instanceof HttpSessionBindingListener)
// 		((HttpSessionBindingListener) value).valueBound
// 		    (new HttpSessionBindingEvent((HttpSession) this, name));
	}
    }



    /** Normal serialization can be used for this object, but before
	serializing you _must_ call prepareSerialize() to remove all
	non-serializable attributes and notify about their removal.
    */
    private void prepareSerialize()
    {
	for (Enumeration e = attributes.keys(); e.hasMoreElements() ; ) {
	    String key = (String) e.nextElement();
	    Object value = attributes.get(key);
	    if (! ( value instanceof Serializable)) {
		if (value instanceof HttpSessionBindingListener ) {
// 		    try {
// 			((HttpSessionBindingListener)value)
// 			    .valueUnbound(new
// 				HttpSessionBindingEvent(this, key));
// 		    } catch (Exception f) {
// 			// ignored
// 		    }
		}
		attributes.remove( key );
	    }
	}
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
}

