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


//XXX rename it to Text

/**
 *  Part of GTest. Simple representation of a request/response body.
 *  The only trick is addText - it allows to use it in ant in a nice way.
 * 
 */
public class Body {
    String body;
    
    public Body() {}

    public Body(String s ) {
	addText(s);
    }

    public void addText(String s ) {
	body=s;
    }

    public void setBody(String s ) {
	body=s;
    }

    public String getBody() {
	return body;
    }

    public String getText() {
	return body;
    }
	
    public void execute()
    {
    }

}
