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

package org.apache.tomcat.util.http;

import org.apache.tomcat.util.collections.*;
import org.apache.tomcat.util.MessageBytes;

import java.io.*;
import java.util.*;
import java.text.*;

// XXX many methods should be deprecated and removed after
// the core is changed. 

/**
 * 
 * @author dac@eng.sun.com
 * @author James Todd [gonzo@eng.sun.com]
 * @author Costin Manolache
 */
public class Headers extends MultiMap {
    
    /** Initial size - should be == average number of headers per request
     *  XXX  make it configurable ( fine-tuning of web-apps )
     */
    public static final int DEFAULT_HEADER_SIZE=8;
    
    /**
     * Creates a new MimeHeaders object using a default buffer size.
     */
    public Headers() {
	super( DEFAULT_HEADER_SIZE );
    }

    // Old names
    
    /**
     * Clears all header fields.
     */
    public void clear() {
	super.recycle();
    }

    /** Find the index of a header with the given name.
     */
    public int findHeader( String name, int starting ) {
	return super.findIgnoreCase( name, starting );
    }
    
    // -------------------- --------------------

    /**
     * Returns an enumeration of strings representing the header field names.
     * Field names may appear multiple times in this enumeration, indicating
     * that multiple fields with that name exist in this header.
     */
    public Enumeration names() {
	return new NamesEnumerator(this);
    }

    public Enumeration values(String name) {
	return new ValuesEnumerator(this, name);
    }

    // -------------------- Adding headers --------------------
    
    /** Create a new named header , return the MessageBytes
     *  container for the new value
     */
    public MessageBytes addValue( String name ) {
	int pos=addField();
	getName(pos).setString(name);
	return getValue(pos);
    }

    /** Create a new named header using un-translated byte[].
	The conversion to chars can be delayed until
	encoding is known.
     */
    public MessageBytes addValue(byte b[], int startN, int endN)
    {
	int pos=addField();
	getName(pos).setBytes(b, startN, endN);
	return getValue(pos);
    }

    /** Allow "set" operations - 
        return a MessageBytes container for the
	header value ( existing header or new
	if this .
    */
    public MessageBytes setValue( String name ) {
 	MessageBytes value=getValue(name);
	if( value == null ) {
	    value=addValue( name );
	}
	return value;
    }

    //-------------------- Getting headers --------------------
    /**
     * Finds and returns a header field with the given name.  If no such
     * field exists, null is returned.  If more than one such field is
     * in the header, an arbitrary one is returned.
     */
    public MessageBytes getValue(String name) {
        int pos=findIgnoreCase( name, 0 );
        if( pos <0 ) return null;
	return getValue( pos );
    }

    // bad shortcut - it'll convert to string ( too early probably,
    // encoding is guessed very late )
    public String getHeader(String name) {
	int pos=findIgnoreCase( name, 0 );
	if( pos <0 ) return null;
	MessageBytes mh = getValue(pos);
	return mh.toString();
    }

    /**
     * Removes a header field with the specified name.  Does nothing
     * if such a field could not be found.
     * @param name the name of the header field to be removed
     */
    public void removeHeader(String name) {
	int pos=0;
	while( pos>=0 ) {
	    // next header with this name
	    pos=findIgnoreCase( name, pos );
	    remove( pos );
	}
    }
}

/** Enumerate the distinct header names.
    Each nextElement() is O(n) ( a comparation is
    done with all previous elements ).

    This is less frequesnt than add() -
    we want to keep add O(1).
*/
class NamesEnumerator implements Enumeration {
    int pos;
    int size;
    String next;
    MultiMap headers;

    NamesEnumerator(MultiMap headers) {
	this.headers=headers;
	pos=0;
	size = headers.size();
	findNext();
    }

    private void findNext() {
	next=null;
	for(  ; pos< size; pos++ ) {
	    next=headers.getName( pos ).toString();
	    for( int j=0; j<pos ; j++ ) {
		if( headers.getName( j ).equalsIgnoreCase( next )) {
		    // duplicate.
		    next=null;
		    break;
		}
	    }
	    if( next!=null ) {
		// it's not a duplicate
		break;
	    }
	}
	// next time findNext is called it will try the
	// next element
	pos++;
    }
    
    public boolean hasMoreElements() {
	return next!=null;
    }

    public Object nextElement() {
	String current=next;
	findNext();
	return current;
    }
}

/** Enumerate the values for a (possibly ) multiple
    value element.
*/
class ValuesEnumerator implements Enumeration {
    int pos;
    int size;
    MessageBytes next;
    MultiMap headers;
    String name;

    ValuesEnumerator(MultiMap headers, String name) {
        this.name=name;
	this.headers=headers;
	pos=0;
	size = headers.size();
	findNext();
    }

    private void findNext() {
	next=null;
	for( ; pos< size; pos++ ) {
	    MessageBytes n1=headers.getName( pos );
	    if( n1.equalsIgnoreCase( name )) {
		next=headers.getValue( pos );
		break;
	    }
	}
	pos++;
    }
    
    public boolean hasMoreElements() {
	return next!=null;
    }

    public Object nextElement() {
	MessageBytes current=next;
	findNext();
	return current.toString();
    }
}

class MimeHeaderField {
    // multiple headers with same name - a linked list will
    // speed up name enumerations and search ( both cpu and
    // GC)
    MimeHeaderField next;
    MimeHeaderField prev; 
    
    protected final MessageBytes nameB = new MessageBytes();
    protected final MessageBytes valueB = new MessageBytes();

    /**
     * Creates a new, uninitialized header field.
     */
    public MimeHeaderField() {
    }

    public void recycle() {
	nameB.recycle();
	valueB.recycle();
	next=null;
    }

    public MessageBytes getName() {
	return nameB;
    }

    public MessageBytes getValue() {
	return valueB;
    }
}
