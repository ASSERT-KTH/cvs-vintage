/*   
 *  Copyright 1999-2004 The Apache Sofware Foundation.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
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
