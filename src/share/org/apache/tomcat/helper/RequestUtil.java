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

import org.apache.tomcat.core.*;
import org.apache.tomcat.util.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.text.*;

/**
 * Usefull methods for request processing. Used to be in ServerRequest or Request,
 * but most are usefull in other adapters. 
 * 
 * @author James Duncan Davidson [duncan@eng.sun.com]
 * @author James Todd [gonzo@eng.sun.com]
 * @author Jason Hunter [jch@eng.sun.com]
 * @author Harish Prabandham
 * @author costin@eng.sun.com
 */
public class RequestUtil {
    protected static StringManager sm =
        StringManager.getManager("org.apache.tomcat.resources");
    

    /** Combine 2 hashtables into a new one.
     *  XXX Will move to the MimeHeaders equivalent for params.
     */
    public static Hashtable mergeParameters(Hashtable one, Hashtable two) {
	// Try some shortcuts
	if (one.size() == 0) {
	    return two;
	}

	if (two.size() == 0) {
	    return one;
	}

	Hashtable combined = (Hashtable) one.clone();

        Enumeration e = two.keys();

	while (e.hasMoreElements()) {
	    String name = (String) e.nextElement();
	    String[] oneValue = (String[]) one.get(name);
	    String[] twoValue = (String[]) two.get(name);
	    String[] combinedValue;

	    if (oneValue == null) {
		combinedValue = twoValue;
	    }

	    else {
		combinedValue = new String[oneValue.length + twoValue.length];

	        System.arraycopy(oneValue, 0, combinedValue, 0,
                    oneValue.length);
	        System.arraycopy(twoValue, 0, combinedValue,
                    oneValue.length, twoValue.length);
	    }

	    combined.put(name, combinedValue);
	}

	return combined;
    }
    
    /**
     * This method decodes the given urlencoded string.
     *
     * @param  str the url-encoded string
     * @return the decoded string
     * @exception IllegalArgumentException If a '%' is not
     * followed by a valid 2-digit hex number.
     *
     * @author: cut & paste from JServ, much faster that previous tomcat impl 
     */
    public final static String URLDecode(String str)
	throws NumberFormatException, StringIndexOutOfBoundsException
    {
        if (str == null)  return  null;

        StringBuffer dec = new StringBuffer();    // decoded string output
        int strPos = 0;
        int strLen = str.length();

        dec.ensureCapacity(str.length());
        while (strPos < strLen) {
            int laPos;        // lookahead position

            // look ahead to next URLencoded metacharacter, if any
            for (laPos = strPos; laPos < strLen; laPos++) {
                char laChar = str.charAt(laPos);
                if ((laChar == '+') || (laChar == '%')) {
                    break;
                }
            }

            // if there were non-metacharacters, copy them all as a block
            if (laPos > strPos) {
                dec.append(str.substring(strPos,laPos));
                strPos = laPos;
            }

            // shortcut out of here if we're at the end of the string
            if (strPos >= strLen) {
                break;
            }

            // process next metacharacter
            char metaChar = str.charAt(strPos);
            if (metaChar == '+') {
                dec.append(' ');
                strPos++;
                continue;
            } else if (metaChar == '%') {
		// We throw the original exception - the super will deal with
		// it
		//                try {
		dec.append((char)Integer.
			   parseInt(str.substring(strPos + 1, strPos + 3),16));
                strPos += 3;
            }
        }

        return dec.toString();
    }

    /** Decode a URL-encoded string. Inefficient.
     */
    public static String unUrlDecode(String data) {
	StringBuffer buf = new StringBuffer();
	for (int i = 0; i < data.length(); i++) {
	    char c = data.charAt(i);
	    switch (c) {
	    case '+':
		buf.append(' ');
		break;
	    case '%':
		// XXX XXX 
		try {
		    buf.append((char) Integer.parseInt(data.substring(i+1,
                        i+3), 16));
		    i += 2;
		} catch (NumberFormatException e) {
                    String msg = "Decode error ";
		    // XXX no need to add sm just for that
		    // sm.getString("serverRequest.urlDecode.nfe", data);

		    throw new IllegalArgumentException(msg);
		} catch (StringIndexOutOfBoundsException e) {
		    String rest  = data.substring(i);
		    buf.append(rest);
		    if (rest.length()==2)
			i++;
		}
		
		break;
	    default:
		buf.append(c);
		break;
	    }
	}
	return buf.toString();
    }           
	

    // Basically return everything after ";charset="
    // If no charset specified, use the HTTP default (ASCII) character set.
    public static String getCharsetFromContentType(String type) {
        if (type == null) {
            return null;
        }
        int semi = type.indexOf(";");
        if (semi == -1) {
            return null;
        }
        String afterSemi = type.substring(semi + 1);
        int charsetLocation = afterSemi.indexOf("charset=");
        if (charsetLocation == -1) {
            return null;
        }
        String afterCharset = afterSemi.substring(charsetLocation + 8);
        String encoding = afterCharset.trim();
        return encoding;
    }

    /** Utility method for parsing the mime type and setting
     *  the encoding to locale. Also, convert from java Locale to mime
     * encodings
     */
    public static String constructLocalizedContentType(String type,
							Locale loc) {
        // Cut off everything after the semicolon
        int semi = type.indexOf(";");
        if (semi != -1) {
            type = type.substring(0, semi);
        }

        // Append the appropriate charset, based on the locale
        String charset = LocaleToCharsetMap.getCharset(loc);
        if (charset != null) {
            type = type + "; charset=" + charset;
        }

        return type;
    }


    
    public static Locale getLocale(Request req) {
    	String acceptLanguage = req.getHeader("Accept-Language");
	    if( acceptLanguage == null ) return Locale.getDefault();

        Hashtable languages = new Hashtable();
        Vector quality=new Vector();
        processAcceptLanguage(acceptLanguage, languages,quality);

        if (languages.size() == 0) return Locale.getDefault();

        Vector l = new Vector();
        extractLocales( languages,quality, l);

        return (Locale)l.elementAt(0);
    }

    public static Enumeration getLocales(Request req) {
	String acceptLanguage = req.getHeader("Accept-Language");
    	// Short circuit with an empty enumeration if null header
        if (acceptLanguage == null) {
            Vector v = new Vector();
            v.addElement(Locale.getDefault());
            return v.elements();
        }

        Hashtable languages = new Hashtable();
        Vector quality=new Vector();
    	processAcceptLanguage(acceptLanguage, languages , quality);

        if (languages.size() == 0) {
            Vector v = new Vector();
            v.addElement(Locale.getDefault());
            return v.elements();
        }
    	Vector l = new Vector();
    	extractLocales( languages, quality , l);
    	return l.elements();
    }

    public static void processAcceptLanguage( String acceptLanguage,
					      Hashtable languages, Vector q)
    {
        StringTokenizer languageTokenizer =
            new StringTokenizer(acceptLanguage, ",");

        while (languageTokenizer.hasMoreTokens()) {
            String language = languageTokenizer.nextToken().trim();
            int qValueIndex = language.indexOf(';');
            int qIndex = language.indexOf('q');
            int equalIndex = language.indexOf('=');
            Double qValue = new Double(1);

            if (qValueIndex > -1 &&
                    qValueIndex < qIndex &&
                    qIndex < equalIndex) {
    	        String qValueStr = language.substring(qValueIndex + 1);
                language = language.substring(0, qValueIndex);
                qValueStr = qValueStr.trim().toLowerCase();
                qValueIndex = qValueStr.indexOf('=');
                qValue = new Double(0);
                if (qValueStr.startsWith("q") &&
                    qValueIndex > -1) {
                    qValueStr = qValueStr.substring(qValueIndex + 1);
                    try {
                        qValue = new Double(qValueStr.trim());
                    } catch (NumberFormatException nfe) {
                    }
                }
            }

            // XXX
            // may need to handle "*" at some point in time

            if (! language.equals("*")) {
                String key = qValue.toString();
                Vector v;
                if (languages.containsKey(key)) {
                    v = (Vector)languages.get(key) ;
                } else {
                    v= new Vector();
                    q.addElement(qValue);
                }
                v.addElement(language);
                languages.put(key, v);
            }
        }
    }

    public static void extractLocales(Hashtable languages, Vector q,Vector l)
    {
        // XXX We will need to order by q value Vector in the Future ?
        Enumeration e = q.elements();
        while (e.hasMoreElements()) {
            Vector v =
                (Vector)languages.get(((Double)e.nextElement()).toString());
            Enumeration le = v.elements();
            while (le.hasMoreElements()) {
    	        String language = (String)le.nextElement();
	        	String country = "";
        		int countryIndex = language.indexOf("-");
                if (countryIndex > -1) {
                    country = language.substring(countryIndex + 1).trim();
                    language = language.substring(0, countryIndex).trim();
                }
                l.addElement(new Locale(language, country));
            }
        }
    }

    static String st_200=null;
    static String st_302=null;
    static String st_400=null;
    static String st_404=null;
    
    /** Get the status string associated with a status code.
     *  No I18N - return the messages defined in the HTTP spec.
     *  ( the user isn't supposed to see them, this is the last
     *  thing to translate)
     *
     *  Common messages are cached.
     *
     */
    public static String getMessage( int status ) {
	// method from Response.
	
	// Does HTTP requires/allow international messages or
	// are pre-defined? The user doesn't see them most of the time
	switch( status ) {
	case 200:
	    if( st_200==null ) st_200=sm.getString( "sc.200");
	    return st_200;
	case 302:
	    if( st_302==null ) st_302=sm.getString( "sc.302");
	    return st_302;
	case 400:
	    if( st_400==null ) st_400=sm.getString( "sc.400");
	    return st_400;
	case 404:
	    if( st_404==null ) st_404=sm.getString( "sc.404");
	    return st_404;
	}
	return sm.getString("sc."+ status);
    }

    

    /* -------------------- From HttpDate -------------------- */
    // Parse date - XXX This code is _very_ slow ( 3 parsers, GregorianCalendar,
    // etc ). It was moved out to avoid creating 1 Calendar instance ( and
    // a associated parsing ) per header ( the Calendar was created in HttpDate
    // which was created for each HeaderField ).
    // This also avoid passing HttpHeaders - which was required to access
    // HttpHeaderFiled to access HttpDate to access the parsing code.

    // we force our locale here as all http dates are in english
    private final static Locale loc = Locale.US;

    // all http dates are expressed as time at GMT
    private final static TimeZone zone = TimeZone.getTimeZone("GMT");

    // format for RFC 1123 date string -- "Sun, 06 Nov 1994 08:49:37 GMT"
    private final static String rfc1123Pattern ="EEE, dd MMM yyyyy HH:mm:ss z";

    // format for RFC 1036 date string -- "Sunday, 06-Nov-94 08:49:37 GMT"
    private final static String rfc1036Pattern ="EEEEEEEEE, dd-MMM-yy HH:mm:ss z";

    // format for C asctime() date string -- "Sun Nov  6 08:49:37 1994"
    private final static String asctimePattern ="EEE MMM d HH:mm:ss yyyyy";

    private final static SimpleDateFormat rfc1123Format =
	new SimpleDateFormat(rfc1123Pattern, loc);

    private final static SimpleDateFormat rfc1036Format =
	new SimpleDateFormat(rfc1036Pattern, loc);

    private final static SimpleDateFormat asctimeFormat =
	new SimpleDateFormat(asctimePattern, loc);

    public static long toDate( String dateString ) {
	// XXX
	Date date=null;
	try {
            date = rfc1123Format.parse(dateString);
	} catch (ParseException e) { }
	
        if( date==null)
	    try {
		date = rfc1036Format.parse(dateString);
	    } catch (ParseException e) { }
	
        if( date==null)
	    try {
		date = asctimeFormat.parse(dateString);
	    } catch (ParseException pe) {
	    }

	if(date==null) {
	    return -1;
	}

	// Original code was: 
	//	Calendar calendar = new GregorianCalendar(zone, loc);
	//calendar.setTime(date);
	// calendar.getTime().getTime();
	return date.getTime();
    }

    // -------------------- Parameter processing --------------------
    
    public static void processFormData(String data, Hashtable parameters) {
        // XXX
        // there's got to be a faster way of doing this.
	if( data==null ) return; // no parameters
        StringTokenizer tok = new StringTokenizer(data, "&", false);
        while (tok.hasMoreTokens()) {
            String pair = tok.nextToken();
	    int pos = pair.indexOf('=');
	    if (pos != -1) {
		String key = unUrlDecode(pair.substring(0, pos));
		String value = unUrlDecode(pair.substring(pos+1,
							  pair.length()));
		String values[];
		if (parameters.containsKey(key)) {
		    String oldValues[] = (String[])parameters.get(key);
		    values = new String[oldValues.length + 1];
		    for (int i = 0; i < oldValues.length; i++) {
			values[i] = oldValues[i];
		    }
		    values[oldValues.length] = value;
		} else {
		    values = new String[1];
		    values[0] = value;
		}
		parameters.put(key, values);
	    } else {
		// we don't have a valid chunk of form data, ignore
	    }
        }
    }

    /** Process the POST data from a request.
     */
    public static Hashtable readFormData( Request request ) {

        String contentType=request.getContentType();
	if (contentType != null) {
            if (contentType.indexOf(";")>0)
                contentType=contentType.substring(0,contentType.indexOf(";"));
            contentType = contentType.toLowerCase().trim();
        }

	int contentLength=request.getContentLength();

	if (contentType != null &&
            contentType.startsWith("application/x-www-form-urlencoded")) {
	    try {
                Hashtable postParameters =  new Hashtable();

		if( contentLength <=0 ) return postParameters;

		// based on HttpUtils, very ,very slow and bad
		byte[] formData = new byte [contentLength];
		int readLen=readBody( request, formData, contentLength );
		// XXX if readLen != len
		String postedBody = new String(formData, 0, readLen,
					       "8859_1");

		processFormData( postedBody, postParameters);
		return postParameters;
	    }
	    catch (IOException e) {
		// nothing
		// XXX at least warn ?
	    }
        }
	return null;
    }


    public static int readBody(Request req, byte body[], int len)
	throws IOException
    {
	int offset = 0;
	
	do {
	    int inputLen = req.doRead(body, offset, len - offset);
	    if (inputLen <= 0) {
		return offset;
	    }
	    offset += inputLen;
	} while ((len - offset) > 0);
	return len;
    }
    
    public static int readData(InputStream in, byte buf[], int length) {
        int read = 0;
        try {
            do {
                read += in.read(buf, read, length - read);
            } while (read < length && read != -1);
        } catch (IOException e) {
            
        }
	return read;
    }


}
