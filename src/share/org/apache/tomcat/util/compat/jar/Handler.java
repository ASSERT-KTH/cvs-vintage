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

package org.apache.tomcat.util.compat.jar;

import java.io.IOException;
import java.net.URL;


/** Jar: protocol handler for JDK1.1 ( where it is not present ).
 *  Only minimal functionality is implemented right now, to get
 *  URL objects created.
 */
public class Handler extends java.net.URLStreamHandler {

    protected java.net.URLConnection openConnection(URL u)
	throws IOException
    {
	// Not implemented.
	return null;
    }
}
