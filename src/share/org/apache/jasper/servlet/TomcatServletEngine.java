/*
 * $Header: /tmp/cvs-vintage/tomcat/src/share/org/apache/jasper/servlet/TomcatServletEngine.java,v 1.3 2004/02/23 03:28:28 billbarker Exp $
 * $Revision: 1.3 $
 * $Date: 2004/02/23 03:28:28 $
 *
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
 *  See the License for the specific language 
 */

package org.apache.jasper.servlet;

import javax.servlet.ServletContext;

/**
 * Implementation of Servlet Engine that is used when the JSP engine
 * is running with Tomcat. 
 *
 * @author Anil K. Vijendran
 */
public class TomcatServletEngine extends ServletEngine {
    public ClassLoader getClassLoader(ServletContext ctx) {
        return null;// XXX (ClassLoader)((ServletContextFacade) ctx).getRealContext().getLoader();
    }
}
