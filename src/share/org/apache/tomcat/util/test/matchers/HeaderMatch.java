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

package org.apache.tomcat.util.test.matchers;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import org.apache.tomcat.util.test.Header;
import org.apache.tomcat.util.test.Matcher;

/** Check if the response has ( or has not ) some headers
 */
public class HeaderMatch extends Matcher {
    String name;
    String value;
    
    // the response should include the following headers
    Vector headerVector=new Vector(); // workaround for introspection problems
    Hashtable expectHeaders=new Hashtable();

    public HeaderMatch() {
    }

    // -------------------- 

    public void setName( String n ) {
	name=n;
    }

    public void setValue( String v ) {
	value=v;
    }

    // Multiple headers ?
    public void addHeader( Header rh ) {
	headerVector.addElement( rh );
    }

    /** Verify that response includes the expected headers.
     *  The value is a "|" separated list of headers to expect.
     *  ?? Do we need that ?
     */
    public void setExpectHeaders( String s ) {
       Header.parseHeadersAsString( s, headerVector );
    }

    public Hashtable getExpectHeaders() {
	if( name!=null ) {
	    headerVector.addElement( new Header( name, value ));
	}
	if( headerVector.size() > 0 ) {
	    Enumeration en=headerVector.elements();
	    while( en.hasMoreElements()) {
		Header rh=(Header)en.nextElement();
		expectHeaders.put( rh.getName(), rh );
	    }
	    headerVector=new Vector();
	}
	return expectHeaders;
    }
    
    public String getTestDescription() {
	StringBuffer desc=new StringBuffer();
	boolean needAND=false;
	
	if( getExpectHeaders().size() > 0 ) {
	    Enumeration e=expectHeaders.keys();
	    while( e.hasMoreElements()) {
		if( needAND ) desc.append( " && " );
		needAND=true;
		String key=(String)e.nextElement();
		Header h=(Header)expectHeaders.get(key);
		desc.append("( responseHeader '" + h.getName() +
			    ": " + h.getValue() + "' ) ");
	    }
	}

	desc.append( " == " ).append( magnitude );
	return desc.toString();
    }

    // -------------------- Execute the request --------------------

    public void execute() {
        if( skipTest() )
           return;
	try {
	    result=checkResponse( magnitude );
	} catch(Exception ex ) {
	    ex.printStackTrace();
	    result=false;
	}
    }

    private boolean checkResponse(boolean testCondition)
	throws Exception
    {
	String responseLine=response.getResponseLine();
	Hashtable headers=response.getHeaders();
	
        boolean responseStatus = true;
	
	getExpectHeaders();
	if( expectHeaders.size() > 0 ) {
	    // Check if we got the expected headers
	    if(headers==null) {
		log("ERROR no response header, expecting header");
	    }
	    Enumeration e=expectHeaders.keys();
	    while( e.hasMoreElements()) {
		String key=(String)e.nextElement();
		Header expH=(Header)expectHeaders.get(key);
		String value=expH.getValue();
		Header resH=(Header)headers.get(key);
		String respValue=(resH==null)? "": resH.getValue();
		if( respValue==null || respValue.indexOf( value ) <0 ) {
		    log("ERROR expecting header " + key + ":" +
			value + " \r\nGOT: " + respValue+ " HEADERS(" +
			Header.toString(headers) + ")");
		    
		    return false;
		}
	    }

	}
	
	return responseStatus;
    }
    
}
