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

import java.util.Hashtable;

import org.apache.tomcat.util.test.Header;
import org.apache.tomcat.util.test.Matcher;

/** Extract the session Id from the response
 */
public class SessionMatch extends Matcher {
    String id;
    String cookieName="JSESSIONID";
    String property;
    
    public SessionMatch() {
    }

    // -------------------- 

    public void setCookieName( String n ) {
	cookieName=n;
    }

    public void setId( String v ) {
	id=v;
    }
    
    public void setProperty(String prop) {
        property = prop;
    }
    
    public String getTestDescription() {
	return "Session extract";
    }

    // -------------------- Execute the request --------------------

    public void execute() {
        if( skipTest() )
            return;
	try {
	    extractSession();
	} catch(Exception ex ) {
	    ex.printStackTrace();
	    result=false;
	}
    }

    private void extractSession()
	throws Exception
    {
	Hashtable headers=response.getHeaders();

        Header resH=(Header)headers.get("Set-Cookie");
        if (null != resH) {
            String temp = resH.getValue();
            if (null != temp) {
                int begin = temp.indexOf("JSESSIONID");
                if (begin >= 0) {
                    if (null != property) {
                        int end = temp.indexOf(";",begin);
                        if (end >= 0) {
                            temp = temp.substring(begin,end);
                        } else {
                            temp = temp.substring(begin);
                        }
                        client.getProject().setUserProperty(property,temp);
                    }
                    result = true;
                }
            }
        }
    }
}
