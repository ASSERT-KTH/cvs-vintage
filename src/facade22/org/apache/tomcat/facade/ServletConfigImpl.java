/*
 * $Header: /tmp/cvs-vintage/tomcat/src/facade22/org/apache/tomcat/facade/ServletConfigImpl.java,v 1.5 2004/02/23 02:08:54 billbarker Exp $
 * $Revision: 1.5 $
 * $Date: 2004/02/23 02:08:54 $
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
 
package org.apache.tomcat.facade;

import java.util.Enumeration;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;

/**
 * 
 * @author James Davidson [duncan@eng.sun.com]
 * @author James Todd [gonzo@eng.sun.com]
 */
final class ServletConfigImpl implements ServletConfig {

    ServletInfo servletW;
    
    ServletConfigImpl( ServletInfo sw) {
	servletW=sw;
    }

    // -------------------- public facade -------------------- 
    
    public ServletContext getServletContext() {
	return (ServletContext)servletW.getContext().getFacade();
    }

    public String getInitParameter(String name) {
        return servletW.getInitParameter( name );
    }

    public Enumeration getInitParameterNames() {
	return servletW.getInitParameterNames();
    }

    public String getServletName() {
	return servletW.getServletName();
    }

}
