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

package org.apache.tomcat.util.xml;

import java.util.StringTokenizer;

/** micro-XPath match - match a path
 */
class PathMatch implements XmlMatch {
    String names[]=new String[6];
    int pos=0;
    
    public PathMatch( String tagName ) {
	StringTokenizer st=new StringTokenizer( tagName, "/" );
	while( st.hasMoreTokens() ) {
	    names[pos]=st.nextToken();
	    pos++;
	}
    }
    
    public boolean match( SaxContext ctx ) {
	int depth=ctx.getTagCount();

	for( int j=pos-1; j>=0; j--) {
	    if( depth<1) {
		//		System.out.println("Pattern too long ");
		return false;
	    }
	    String tag=ctx.getTag(depth-1);
	    if( ! names[j].equals( tag ) ) {
		//		System.out.println("XXX" + names[j] + " " + tag);
		return false;
	    }
	    depth--;
	}

	
	return true;
    }

    public String toString() {
	return "Tag("+names+")";
    }

}

