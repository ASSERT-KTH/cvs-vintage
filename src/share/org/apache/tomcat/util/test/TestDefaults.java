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

import org.apache.tools.ant.Project;

/** Set default values for HttpClient and GTest.
 */
public class TestDefaults {

    public TestDefaults() {
    }

    public void setHost( String s ) {
	// I know, I'll add setters...
	HttpRequest.defaultHost=s;
    }

    public void setPort(int port ) {
	HttpRequest.defaultPort=port;
    }

    public void setProtocol( String proto ) {
	HttpRequest.defaultProtocol=proto;
    }

    public void setDebug( int debug ) {
	GTest.setDefaultDebug( debug );
    }

    public void setOutputType(String t  ) {
	Report.setDefaultOutput(t);
    }

    public void setProject(Project p ) {
    }

    public void execute() {
    }
}
