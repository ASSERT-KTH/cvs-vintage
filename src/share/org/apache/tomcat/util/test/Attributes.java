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
 *  A trivial inter-object communication mechanism, part of the first step
 *  in modularizing and enhancing GTest.
 *
 *  A more generic and extensible system is needed ( JNDI ? )
 * 
 *  ( Part of GTest.)
 */
public class Attributes {

    static Hashtable attributeCollections=new Hashtable();

    private Attributes() {}

    public static Hashtable getAttributes( String collection ) {
	Hashtable att=(Hashtable)attributeCollections.get( collection );
	if( att==null ) {
	    att=new Hashtable();
	    attributeCollections.put( collection, att );
	}
	return att;
    }
}
