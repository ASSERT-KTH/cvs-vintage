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

import  org.apache.tomcat.util.*;
import  org.apache.tomcat.util.collections.*;
import java.io.*;
import java.util.*;
import java.text.*;

/**
 * 
 * @author Costin Manolache
 */
public final class Parameters extends MultiMap {
    public static final int INITIAL_SIZE=4;

    private boolean isSet=false;
    private boolean isFormBased=false;
    
    /**
     * 
     */
    public Parameters() {
	super( INITIAL_SIZE );
    }

    public void recycle() {
	super.recycle();
	isSet=false;
	isFormBased=false;
    }
    // XXX need better name
    public boolean isEvaluated() {
	return isSet;
    }
    public void setEvaluated( boolean b ) {
	isSet=b;
    }
    // XXX need better name
    public boolean hasFormData() {
	return isFormBased;
    }
    public void setFormData(boolean b ) {
	isFormBased=b;
    }
    
    // duplicated
    public static int indexOf( byte bytes[], int off, int end, char qq )
    {
	while( off < end ) {
	    byte b=bytes[off];
	    if( b==qq )
		return off;
	    off++;
	}
	return off;
    }

    public static int indexOf( char chars[], int off, int end, char qq )
    {
	while( off < end ) {
	    char b=chars[off];
	    if( b==qq )
		return off;
	    off++;
	}
	return off;
    }

    public void processParameters( byte bytes[], int start, int len ) {
	int end=start+len;
	int pos=start;
	
        do {
	    int nameStart=pos;
	    int nameEnd=indexOf(bytes, nameStart, end, '=' );
	    int valStart=nameEnd+1;
	    int valEnd=indexOf(bytes, valStart, end, '&');
	    
	    pos=valEnd+1;
	    
	    if( nameEnd<=nameStart ) {
		continue;
		// invalid chunk - it's better to ignore
		// XXX log it ?
	    }
	    
	    int field=this.addField();
	    this.getName( field ).setBytes( bytes,
					    nameStart, nameEnd );
	    this.getValue( field ).setBytes( bytes,
					     valStart, valEnd );
	} while( pos<end );
    }

    public void processParameters( char chars[], int start, int len ) {
	int end=start+len;
	int pos=start;
	
        do {
	    int nameStart=pos;
	    int nameEnd=indexOf(chars, nameStart, end, '=' );
	    int valStart=nameEnd+1;
	    int valEnd=indexOf(chars, valStart, end, '&');
	    
	    pos=valEnd+1;
	    
	    if( nameEnd<=nameStart ) {
		continue;
		// invalid chunk - it's better to ignore
		// XXX log it ?
	    }
	    
	    int field=this.addField();
	    this.getName( field ).setChars( chars,
					    nameStart, nameEnd );
	    this.getValue( field ).setChars( chars,
					     valStart, valEnd );
	} while( pos<end );
    }

    
    public void processParameters( MessageBytes data ) {
	if( data==null || data.getLength() <= 0 ) return;

	if( data.getType() == MessageBytes.T_BYTES ) {
	    processParameters( data.getBytes(), data.getOffset(),
			       data.getLength());
	} else {
	    processParameters( data.getChars(), data.getOffset(),
			       data.getLength());
	}
    }


    public void mergeParameters( Parameters extra ) {
	
    }
    
}
