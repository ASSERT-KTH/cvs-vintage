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
import org.apache.tomcat.util.MimeHeaders;
import org.apache.tomcat.util.ServerCookie;
import org.apache.tomcat.util.DateTool;

import java.io.*;
import java.util.*;
import java.text.*;

/**
 * A collection of cookies - reusable and tuned for server side performance.
 * 
 * @author Costin Manolache
 */
public final class Cookies { // extends MultiMap {

    // expected average number of cookies per request
    public static final int INITIAL_SIZE=4; 
    ServerCookie scookies[]=new ServerCookie[INITIAL_SIZE];
    int cookieCount=-1; // -1 = cookies not processed yet

    MimeHeaders headers;
    
    /**
     * 
     */
    public Cookies() {
    }

    public void recycle() {
    	for( int i=0; i< cookieCount; i++ ) {
	    if( scookies[i]!=null )
		scookies[i].recycle();
	}
	cookieCount=-1;
    }

    public ServerCookie getCookie( int idx ) {
	if( cookieCount == -1 ) {
	    getCookieCount(); // will also update the cookies
	}
	return scookies[idx];
    }

    public int getCookieCount() {
	if( cookieCount == -1 ) {
	    cookieCount=0;
	    // compute cookies
	    processCookies(headers);
	}
	return cookieCount;
    }

    public ServerCookie addCookie() {
	if( cookieCount >= scookies.length  ) {
	    ServerCookie scookiesTmp[]=new ServerCookie[2*cookieCount];
	    System.arraycopy( scookies, 0, scookiesTmp, 0, cookieCount);
	    scookies=scookiesTmp;
	}
	
	ServerCookie c = scookies[cookieCount];
	if( c==null ) {
	    c= new ServerCookie();
	    scookies[cookieCount]=c;
	}
	cookieCount++;
	return c;
    }


    // -------------------- Static methods ( used to be CookieTools )

    /** Process all Cookie headers of a request, setting them
     *  in a cookie vector
     */
    public  void processCookies( MimeHeaders headers ) {
	// process each "cookie" header
	int pos=0;
	while( pos>=0 ) {
	    pos=headers.findHeader( "Cookie", pos );
	    // no more cookie headers headers
	    if( pos<0 ) break;

	    MessageBytes cookieValue=headers.getValue( pos );
	    if( cookieValue==null || cookieValue.isNull() ) continue;
	    if( cookieValue.getType() == MessageBytes.T_BYTES ) {
		processCookieHeader( cookieValue.getBytes(),
				     cookieValue.getOffset(),
				     cookieValue.getLength());
	    } else {
		processCookieHeader( cookieValue.toString() );
	    }
	}
    }

    private  void processCookieHeader(  byte bytes[], int off, int len )
    {
	if( len<=0 || bytes==null ) return;
	int end=off+len;
	int pos=off;

	while( true ) {
	    // [ skip_spaces name skip_spaces "=" skip_spaces value EXTRA ; ] *
	    
	    int startName=skipSpaces(bytes, pos, end);
	    if( pos>=end )
		return; // only spaces
	    
	    boolean isSpecial=false;
	    if(bytes[pos]=='$') { pos++; isSpecial=true; }
	    
	    int endName= findDelim1( bytes, startName, end); // " =;,"
	    if(endName >= end )
		return; // invalid
	
	    // current = "=" or " " 
	    pos= skipSpaces( bytes, endName, end );
	    if(endName >= end )
		return; // invalid

	    // cookie without value
	    if( bytes[pos] == ';' || bytes[pos]==',' ) {
		// add cookie

		// we may have more cookies
		continue;
	    }

	    if( bytes[pos] != '=' ) {
		// syntax error - ignore the rest
		// ( we could also skip to the next ';' )
		return;
	    }
	
	    // we must have "="
	    pos++;
	    int startValue=skipSpaces( bytes, pos, end);
	    int endValue=startValue;
	    if( bytes[pos]== '\'' || bytes[pos]=='"' ) {
		startValue++;
		endValue=indexOf( bytes, startValue, end, bytes[startValue] );
 	    } else {
		endValue=findDelim2( bytes, startValue, end );
	    }

	    // process $Version, etc
	    if( ! isSpecial ) {
		ServerCookie sc=addCookie();
		sc.getName().setBytes( bytes, startName, endName );
		sc.getValue().setBytes( bytes, startValue, endValue );
		continue;
	    }
	    // special - Path, Version, Domain
	    // XXX TODO
	}
    }

    // -------------------- Utils --------------------
    public static int skipSpaces(  byte bytes[], int off, int end ) {
	while( off < end ) {
	    byte b=bytes[off];
	    if( b!= ' ' ) return off;
	    off ++;
	}
	return off;
    }

    public static int findDelim1( byte bytes[], int off, int end )
    {
	while( off < end ) {
	    byte b=bytes[off];
	    if( b==' ' || b=='=' || b==';' || b==',' )
		return off;
	    off++;
	}
	return off;
    }

    public static int findDelim2( byte bytes[], int off, int end )
    {
	while( off < end ) {
	    byte b=bytes[off];
	    if( b==' ' || b==';' || b==',' )
		return off;
	    off++;
	}
	return off;
    }

    public static int indexOf( byte bytes[], int off, int end, byte qq )
    {
	while( off < end ) {
	    byte b=bytes[off];
	    if( b==qq )
		return off;
	    off++;
	}
	return off;
    }

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
    

    // ---------------------------------------------------------
    // -------------------- DEPRECATED, OLD --------------------
    
    private void processCookieHeader(  String cookieString )
    {
	
	// normal cookie, with a string value.
	// This is the original code, un-optimized - it shouldn't
	// happen in normal case

	StringTokenizer tok = new StringTokenizer(cookieString,
						  ";", false);
	while (tok.hasMoreTokens()) {
	    String token = tok.nextToken();
	    int i = token.indexOf("=");
	    if (i > -1) {
		
		// XXX
		// the trims here are a *hack* -- this should
		// be more properly fixed to be spec compliant
		
		String name = token.substring(0, i).trim();
		String value = token.substring(i+1, token.length()).trim();
		// RFC 2109 and bug 
		value=stripQuote( value );
		ServerCookie cookie = addCookie();
		
		cookie.getName().setString(name);
		cookie.getValue().setString(value);
	    } else {
		// we have a bad cookie.... just let it go
	    }
	}
    }

    /**
     *
     * Strips quotes from the start and end of the cookie string
     * This conforms to RFC 2109
     * 
     * @param value            a <code>String</code> specifying the cookie 
     *                         value (possibly quoted).
     *
     * @see #setValue
     *
     */
    private static String stripQuote( String value )
    {
	//	log("Strip quote from " + value );
	if (((value.startsWith("\"")) && (value.endsWith("\""))) ||
	    ((value.startsWith("'") && (value.endsWith("'"))))) {
	    try {
		return value.substring(1,value.length()-1);
	    } catch (Exception ex) { 
	    }
	}
	return value;
    }  



}
