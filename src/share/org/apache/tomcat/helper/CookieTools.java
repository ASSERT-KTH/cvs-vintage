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

package org.apache.tomcat.helper;
import  org.apache.tomcat.util.*;
import  org.apache.tomcat.util.http.*;
import  org.apache.tomcat.core.*;
import  org.apache.tomcat.session.*;
import java.text.*;
import java.util.*;

// XXX use only one Date instance/request, reuse it.

/**
 * Cookie utils - generate cookie header, etc
 *
 * @author Original Author Unknown
 * @author duncan@eng.sun.com
 */
public class CookieTools {

    // -------------------- Cookie parsing tools
    /** Process all Cookie headers of a request, setting them
     *  in a cookie vector
     */
    public static void processCookies( Request request ) {
	// XXX bug in original RequestImpl - might not work if multiple
	// cookie headers.
	//
	// XXX need to use the cookies hint in RequestAdapter
    	MimeHeaders mh=request.getMimeHeaders();
	MessageBytes cookieMB= mh.getValue( "cookie" );

	if( cookieMB==null ) return;
	if( cookieMB.isNull() ) return;

	// XXX XXX XXX TODO TODO TODO
	// byte b[]=cookieMB.getBytes();
	// 	if( b!=null ) {
	// 	    // this is a byte header 
	// 	    parseCookie( request, b, cookieMB.getLength());
	// 	    return;
	// 	}

	// normal cookie, with a string value.
	// This is the original code, un-optimized - it shouldn't
	// happen in normal case
	String cookieString=cookieMB.toString();
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
		ServerCookie cookie = new ServerCookie();
		cookie.getName().setString(name);
		cookie.getValue().setString(value);
		request.addCookie( cookie );
	    } else {
		// we have a bad cookie.... just let it go
	    }
	}
    }

    /** Parse a cookie from a byte[]
     */
    public void parseCookie( Request req, byte b[], int len ) {
	
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
    
    
//     /** Return the header name to set the cookie, based on cookie
//      *  version
//      */
//     public static String getCookieHeaderName(int version) {
//         if (version == 1) {
// 	    return "Set-Cookie2";
//         } else {
//             return "Set-Cookie";
//         }
//     }

//     /** Return the header value used to set this cookie
//      *  @deprecated Use StringBuffer version
//      */
//     public static String getCookieHeaderValue(ServerCookie cookie) {
//         StringBuffer buf = new StringBuffer();
// 	getCookieHeaderValue( cookie, buf );
// 	return buf.toString();
//     }

//     /** Return the header value used to set this cookie
//      */
//     public static void getCookieHeaderValue(ServerCookie cookie,
// 					    StringBuffer buf) {
//         int version = cookie.getVersion();

//         // this part is the same for all cookies

//         buf.append(cookie.getName());
//         buf.append("=");
//         maybeQuote(version, buf, cookie.getValue().toString());

//  	// add version 1 specific information
// 	if (version == 1) {
// 	    // Version=1 ... required
// 	    buf.append ("; Version=1");

// 	    // Comment=comment
// 	    if (cookie.getComment() != null) {
// 		buf.append ("; Comment=");
// 		maybeQuote (version, buf, cookie.getComment().toString());
// 	    }
// 	}

// 	// add domain information, if present

// 	if (!cookie.getDomain().isNull()) {
// 	    buf.append("; Domain=");
// 	    maybeQuote (version, buf, cookie.getDomain().toString());
// 	}

// 	// Max-Age=secs/Discard ... or use old "Expires" format
// 	if (cookie.getMaxAge() >= 0) {
// 	    if (version == 0) {
// 		buf.append ("; Expires=");
// 		DateTool.oldCookieFormat.format(new Date( System.currentTimeMillis() + cookie.getMaxAge() *1000L) ,buf,
// 						new FieldPosition(0));

// 	    } else {
// 		buf.append ("; Max-Age=");
// 		buf.append (cookie.getMaxAge());
// 	    }
// 	} else if (version == 1)
// 	  buf.append ("; Discard");

// 	// Path=path
// 	if (! cookie.getPath().isNull()) {
// 	    buf.append ("; Path=");
// 	    maybeQuote (version, buf, cookie.getPath().toString());
// 	}

// 	// Secure
// 	if (cookie.getSecure()) {
// 	  buf.append ("; Secure");
// 	}
//     }

//     public static void maybeQuote (int version, StringBuffer buf,
//                                     String value)
//     {
// 	if (version == 0 || isToken (value))
// 	  buf.append (value);
// 	else {
// 	    buf.append ('"');
// 	    buf.append (value);
// 	    buf.append ('"');
// 	}
//     }

//         //
//     // from RFC 2068, token special case characters
//     //
//     private static final String tspecials = "()<>@,;:\\\"/[]?={} \t";

//     /*
//      * Return true iff the string counts as an HTTP/1.1 "token".
//      */
//     private static boolean isToken (String value) {
// 	int len = value.length ();

// 	for (int i = 0; i < len; i++) {
// 	    char c = value.charAt (i);

// 	    if (c < 0x20 || c >= 0x7f || tspecials.indexOf (c) != -1)
// 	      return false;
// 	}
// 	return true;
//     }


}
