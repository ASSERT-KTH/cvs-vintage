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
package org.apache.tomcat.util.test;

import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.Hashtable;

public class Report  {
    // Defaults
    static PrintWriter defaultOutput=new PrintWriter(System.out);
    static String defaultOutType="text";

    PrintWriter out=null;
    String outType=null;
    HttpClient httpClient;
    HttpRequest httpRequest;
    String description;
    String failueMessage;

    public static void setDefaultWriter( PrintWriter pw ) {
        defaultOutput=pw;
    }

    public static void setDefaultOutput( String s ) {
	defaultOutType=s;
    }

    public void setWriter( PrintWriter pw ) {
        out=pw;
    }

    /** text, xml, html
     */
    public void setOutput( String t ) {
        outType=t;
    }

    public void setDescription( String desc ) {
        description = desc;
    }

    public void setHttpClient( HttpClient client ) {
        httpClient = client;
    }

    public void setHttpRequest( HttpRequest req ) {
        httpRequest = req;
    }

    public void setFailureMsg( String msg ) {
        failueMessage = msg;
    }

    public void writeReport() {
        if( out==null) out=defaultOutput;
        if( outType==null) outType=defaultOutType;

        if( "text".equals(outType) )
            textReport();
        if( "html".equals(outType) )
            htmlReport();
    }

    private void textReport() {
        String msg=null;
        if(  "".equals( httpClient.getComment() )) 
            msg=" (" + httpRequest.getRequestLine() + ")";
        else
            msg=httpClient.getComment() + " (" + httpRequest.getRequestLine() + ")";

        if( httpClient.getResult() ) 
            out.println("OK " +  msg );
        else {
            out.println("FAIL " + msg );
            out.println("Message: " + httpClient.getFailureMessage());
        }
        out.flush();
    }

    private void htmlReport() {
        String uri=httpRequest.getURI();
        boolean result=httpClient.getResult();
        if( uri!=null )
            out.println("<a href='" + uri + "'>");
        if( result )
            out.println( "OK " );
        else
            out.println("<font color='red'>FAIL ");
        if( uri!=null )
            out.println("</a>");

        String msg=null;
        if(  "".equals( httpClient.getComment() )) 
            msg=" (" + httpRequest.getRequestLine() + ")";
        else
            msg=httpClient.getComment() + " (" + httpRequest.getRequestLine() + ")";

        out.println( msg );

        if( ! result )
            out.println("</font>");
        
        out.println("<br>");

        if( ! result ) {
            out.println("<b>Message:</b><pre>");
            out.println( httpClient.getFailureMessage() );
            out.println("</pre>");
        }

        if( ! result && httpClient.getDebug() > 0 ) {
            out.println("<b>Request: </b><pre>" + httpRequest.getFullRequest());
            out.println("</pre><b>Response:</b> " +
                        httpRequest.getHttpResponse().getResponseLine());
            out.println("<br><b>Response headers:</b><br>");
            Hashtable headerH=httpRequest.getHttpResponse().getHeaders();
            Enumeration hE=headerH.elements();
            while( hE.hasMoreElements() ) {
                Header h=(Header) hE.nextElement();
                out.println("<b>" + h.getName() + ":</b>" +
                            h.getValue() + "<br>");
            }
            out.println("<b>Response body:</b><pre> ");
            out.println(httpRequest.getHttpResponse().getResponseBody());
            out.println("</pre>");
        }

        Throwable ex=httpRequest.getHttpResponse().getThrowable();
        if( ex!=null) {
            out.println("<b>Exception</b><pre>");
            ex.printStackTrace(out);
            out.println("</pre><br>");
        }
        out.flush();
    }
}