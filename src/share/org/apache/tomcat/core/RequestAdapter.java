/*
 * $Header: /tmp/cvs-vintage/tomcat/src/share/org/apache/tomcat/core/Attic/RequestAdapter.java,v 1.1 1999/10/24 17:21:20 costin Exp $
 * $Revision: 1.1 $
 * $Date: 1999/10/24 17:21:20 $
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

import org.apache.tomcat.util.*;
import java.io.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;

/* XXX
   - add comments
   - more clean-up
   - make it minimal!
*/

/**
 * Low-level representation of a Request in a server adapter ( connector ).
 * 
 * @author James Duncan Davidson [duncan@eng.sun.com]
 * @author Jason Hunter [jch@eng.sun.com]
 * @author James Todd [gonzo@eng.sun.com]
 * @author Harish Prabandham
 * @author costin@eng.sun.com
 */

public interface RequestAdapter {

    // Required - basic request fields

    public String getScheme();
    
    public String getMethod();
    
    public String getRequestURI();

    public String getProtocol();
    
    public  Enumeration getHeaderNames();

    public  String getHeader(String name);
    
    public  ServletInputStream getInputStream() throws IOException;
    

    // Required - connection info 
    public String getServerName();
    
    public int getServerPort();

    public String getRemoteAddr();
    
    public String getRemoteHost();


    // -------------------- "cooked" info --------------------
    // Hints = return null if you don't know,
    // and Tom will find the value. You can also use the static
    // methods in RequestImpl

    /** Return the cookies
     */
    public String[] getCookieHeaders();

    // server may have it pre-calculated - return null if
    // it doesn't
    public String getContextPath();

    // What's between context path and servlet name ( /servlet )
    // A smart server may use arbitrary prefixes and rewriting
    public String getServletPrefix();

    // Servlet name ( a smart server may use aliases and rewriting !!! )
    public String getServletName();

    // What's after Servlet name, before "?"
    public String getPathInfo();

    public String getQueryString();

    // Parameters - if the server can parse parameters faster
    // Note: there is a tricky requirement in Servlet API
    // regarding POST parameters ( you can't read the body until you are
    // asked for the first parameter ). It's easy if you use callbacks (
    // this method will be called when needed, but don't read until you are
    // asked for params )
    public Enumeration getParameterNames();

    public String[] getParameterValues(String name);

    // You probably know the session ID if you are in a distributed engine, or
    // if you use a non-standard session identifier ( to integrate with "legacy"
    // apps ?)
    public String getRequestedSessionId();

    // Authentication - carefull :-)
    public String getAuthType();

    public String getRemoteUser();

    // Headers and special Headers. You may want to use a "fast" route for those,
    // since it's almost sure will be needed ( for example get "other" headers in
    // a callback.). Again - if you don't know, return null.
    
    public String getCharacterEncoding();

    public int getContentLength();
    
    public String getContentType();

    // You're no longer needed, go away
    public  void recycle(); 
}
