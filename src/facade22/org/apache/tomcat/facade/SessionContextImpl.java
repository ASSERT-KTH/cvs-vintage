/*
 * $Header: /tmp/cvs-vintage/tomcat/src/facade22/org/apache/tomcat/facade/SessionContextImpl.java,v 1.4 2004/02/23 06:06:13 billbarker Exp $
 * $Revision: 1.4 $
 * $Date: 2004/02/23 06:06:13 $
 *
 *  Copyright 1999-2004 The Apache Software Foundation
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

package org.apache.tomcat.facade;

import java.util.Enumeration;
import java.util.Vector;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionContext;

/**
 * 
 * @author duncan@eng.sun.com
 */
final class SessionContextImpl implements HttpSessionContext {

    /**
     *
     * @deprecated
     */
    
    public HttpSession getSession(String sessionId) {
        return null;
    }

    /**
     *
     * @deprecated
     */

    public Enumeration getIds() {
        // cheap hack to get an empty enum
        Vector v = new Vector();

        return v.elements();
    }
}
