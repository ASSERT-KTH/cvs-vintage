/*
 * $Header: /tmp/cvs-vintage/tomcat/src/share/org/apache/tomcat/util/Attic/CookieTools.java,v 1.3 2000/04/06 22:31:04 craigmcc Exp $
 * $Revision: 1.3 $
 * $Date: 2000/04/06 22:31:04 $
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


package org.apache.tomcat.util;
import java.text.*;
import java.util.*;

import javax.servlet.http.Cookie;

/**
 *
 *
 * @author Original Author Unknown
 * @author duncan@eng.sun.com
 */

public class CookieTools {

    public static String getCookieHeaderName(Cookie cookie) {
        int version = cookie.getVersion();
	
        if (version == 1) {
	    return "Set-Cookie2";
        } else {
            return "Set-Cookie";
        }
    }

    public static String getCookieHeaderValue(Cookie cookie) {
        StringBuffer buf = new StringBuffer();
        int version = cookie.getVersion();

        // this part is the same for all cookies
        
        buf.append(cookie.getName());
        buf.append("=");
        maybeQuote(version, buf, cookie.getValue());

 	// add version 1 specific information 
	if (version == 1) {
	    // Version=1 ... required 
	    buf.append (";Version=1");

	    // Comment=comment
	    if (cookie.getComment() != null) {
		buf.append (";Comment=");
		maybeQuote (version, buf, cookie.getComment());
	    }
	}

	// add domain information, if present

	if (cookie.getDomain() != null) {
	    buf.append(";Domain=");
	    maybeQuote (version, buf, cookie.getDomain());
	}

	// Max-Age=secs/Discard ... or use old "Expires" format
	if (cookie.getMaxAge() >= 0) {
	    if (version == 0) {
		buf.append (";Expires=");
		new OldCookieExpiry (cookie.getMaxAge()).append (buf);
	    } else {
		buf.append (";MaxAge=");
		buf.append (cookie.getMaxAge());
	    }
	} else if (version == 1)
	  buf.append (";Discard");

	// Path=path
	if (cookie.getPath() != null) {
	    buf.append (";Path=");
	    maybeQuote (version, buf, cookie.getPath());
	}

	// Secure
	if (cookie.getSecure()) {
	  buf.append (";Secure");
	}

	return buf.toString();
    }

    static void maybeQuote (int version, StringBuffer buf,
                                    String value)
    {
	if (version == 0 || isToken (value))
	  buf.append (value);
	else {
	    buf.append ('"');
	    buf.append (value);
	    buf.append ('"');
	}
    }

        //
    // from RFC 2068, token special case characters
    //
    private static final String tspecials = "()<>@,;:\\\"/[]?={} \t";

    /*
     * Return true iff the string counts as an HTTP/1.1 "token".
     */
    private static boolean isToken (String value) {
	int len = value.length ();

	for (int i = 0; i < len; i++) {
	    char c = value.charAt (i);

	    if (c < 0x20 || c >= 0x7f || tspecials.indexOf (c) != -1)
	      return false;
	}
	return true;
    }    


    
        /*
     * The original Netscape cookie spec had a funky string format
     * for dates ... RFC 1123 GMT format, but dashes in two places
     * where spaces would normally live.  RFC 2109 simplified that,
     * deleting date parsing entirely.
     */
    static class OldCookieExpiry extends HttpDate {
	OldCookieExpiry (long maxAge)  {
	    super();
	    setTime(getCurrentTime() + maxAge * 1000);
	}
	// Wdy, DD-Mon-YYYY HH:MM:SS GMT
	void append (StringBuffer buf) {
	    String pattern = "EEE, dd-MMM-yyyy HH:mm:ss z";
	    Locale loc = Locale.US;
	    SimpleDateFormat df = new SimpleDateFormat(pattern, loc);
	    TimeZone zone = TimeZone.getTimeZone("GMT");
	    df.setTimeZone(zone);
	    String str = df.format(calendar.getTime());
	    buf.append(str);
	}
    }
}
