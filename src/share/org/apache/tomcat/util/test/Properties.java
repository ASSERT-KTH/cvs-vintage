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

import java.util.Hashtable;


/**
 *  Part of GTest
 * 
 */
public class Properties {
    Hashtable keys=new Hashtable();
    
    public Properties() {}


    
    /** Replace ${NAME} with the property value
     *  Reproduced from ant, without dependencies on Project. Should be
     *  part of a top-level tool set.
     */
    public static String replaceProperties(String value, Hashtable keys )
    {
        StringBuffer sb=new StringBuffer();
        int i=0;
        int prev=0;
        if( value==null ) return null;
        int pos;
        while( (pos=value.indexOf( "$", prev )) >= 0 ) {
            if(pos>0) {
                sb.append( value.substring( prev, pos ) );
            }
            if( pos == (value.length() - 1)) {
                sb.append('$');
                prev = pos + 1;
            }
            else if (value.charAt( pos + 1 ) != '{' ) {
                sb.append( value.charAt( pos + 1 ) );
                prev=pos+2; 
            } else {
                int endName=value.indexOf( '}', pos );
                if( endName < 0 ) {
		    // it's not a property..
		    sb.append( value.substring( pos ));
		    pos=value.length() -1;
                }
                String n=value.substring( pos+2, endName );
                String v = (keys.containsKey(n)) ?
		    (String) keys.get(n) :
		    "${"+n+"}"; 
                
                sb.append( v );
                prev=endName+1;
            }
        }
        if( prev < value.length() ) sb.append( value.substring( prev ) );
        return sb.toString();
    }

}
