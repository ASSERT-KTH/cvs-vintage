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

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.Vector;

// XXX TODO Use the util.http

/**
 *  Part of GTest
 * 
 */
public class Header {
    String name;
    String value;
    
    public Header() {}

    public Header( String n, String v ) {
	setName(n);
	setValue(v);
    }

    
    public String getName() {
	return name;
    }
    
    public void setName(String  v) {
	this.name = v;
    }

    public String getValue() {
	return value;
    }
    
    public void setValue(String  v) {
	this.value = v;
    }

    public String toString() {
	return value;
    }
    
    // -------------------- Utils --------------------

    // Code from JSERV !!!
    /**
     * Parse the incoming HTTP request headers, and set the corresponding
     * request properties.
     *
     *
     * @exception IOException if an input/output error occurs
     */
    public static Hashtable parseHeaders(InputStream is) throws IOException {
	Hashtable headers=new Hashtable();
	while (true) {
	    // Read the next header line
	    String line = HttpClient.read(is);
	    if ((line == null) || (line.length() < 1)) {
		break;
	    }

	    Header h=new Header();
	    h.parseHeaderLine( line );
	    if( h.getName() == null ) {
		System.out.println("ERROR: Wrong Header Line: " +  line );
	    } else {
		headers.put( h.getName(), h );
	    }
	    //	    if( debug>0) System.out.println("HEADER: " +line +"X" );

	}

	return headers;
    }

    public void parseHeaderLine(String line) {
	// Parse the header name and value
	int colon = line.indexOf(":");
	if (colon < 0) {
	    return;
	}
	name = line.substring(0, colon).trim();
	value = line.substring(colon + 1).trim();
    }


    public static void parseHeadersAsString( String s, Vector headers ) {
	StringTokenizer st=new StringTokenizer( s, "|");
	while( st.hasMoreTokens() ) {
	    String tok=st.nextToken();
	    Header h=new Header();
	    h.parseHeaderLine( tok );
	    if( h.getName() !=null )
		headers.addElement( h );
	}
    }

    public static String toString( Hashtable headers ) {
	StringBuffer sb=new StringBuffer();
	sb.append("{");
	Enumeration eH=headers.keys();
	while(eH.hasMoreElements() ) {
	    String k=(String)eH.nextElement();
	    sb.append( k ).append("=");
	    sb.append( ((Header)headers.get(k)).getValue());
	    if( eH.hasMoreElements()) sb.append(",");
	}
	sb.append("}");
	return sb.toString();
    }

    public void execute()
    {
    }

}
