/*
 * $Header: /tmp/cvs-vintage/tomcat/src/share/org/apache/tomcat/core/Attic/RequestUtil.java,v 1.2 1999/10/28 05:15:25 costin Exp $
 * $Revision: 1.2 $
 * $Date: 1999/10/28 05:15:25 $
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


package org.apache.tomcat.core;

import org.apache.tomcat.core.*;
import org.apache.tomcat.util.*;
import java.io.*;
import java.net.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;

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

    public static Hashtable readFormData( Request request ) {
	String contentType=request.getContentType();
	int contentLength=request.getContentLength();

	if (contentType != null &&
            contentType.equals("application/x-www-form-urlencoded")) {

	    try {
		ServletInputStream is=request.getInputStream();
                Hashtable postParameters =  HttpUtils.parsePostData(contentLength, is);
		return postParameters;
	    }
	    catch (IOException e) {
		// nothing
		// XXX at least warn ?
	    }
        }
	return null;
    }

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

    public static BufferedReader getReader(Request request) throws IOException {
        // XXX
	// this won't work in keep alive scenarios. We need to provide
	// a buffered reader that won't try to read in the stream
	// past the content length -- if we don't, the buffered reader
	// will probably try to read into the next request... bad!
        String encoding = request.getCharacterEncoding();
        if (encoding == null) {
            // Set a default of Latin-1 even for non-Latin-1 systems
            encoding = "ISO-8859-1";
        }
	InputStreamReader r =
            new InputStreamReader(request.getInputStream(), encoding);
	return new BufferedReader(r);
    }

    public static void processCookies( Request request, Vector cookies ) {
	// XXX bug in original RequestImpl - might not work if multiple
	// cookie headers.
	//
	// XXX need to use the cookies hint in RequestAdapter
    	String cookieString = request.getHeader("cookie");
	
	if (cookieString != null) {
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
                    Cookie cookie = new Cookie(name, value);
                    cookies.addElement(cookie);
                } else {
                    // we have a bad cookie.... just let it go
                }
            }
        }
    }
    
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
		// We throw the original exception - the super will deal with it
		//                try {
		dec.append((char) Integer.parseInt(
						   str.substring(strPos + 1, strPos + 3), 16));
		//                } catch (NumberFormatException e) {
		//                    throw new IllegalArgumentException("invalid hexadecimal "
		//                    + str.substring(strPos + 1, strPos + 3)
		//                    + " in URLencoded string (illegal unescaped '%'?)" );
		//                } catch (StringIndexOutOfBoundsException e) {
		//                    throw new IllegalArgumentException("illegal unescaped '%' "
		//                    + " in URLencoded string" );
		//                }
                strPos += 3;
            }
        }

        return dec.toString();
    }

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
	

    // XXX This method is duplicated in core/Response.java
    public static String getCharsetFromContentType(String type) {
        // Basically return everything after ";charset="
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

}
