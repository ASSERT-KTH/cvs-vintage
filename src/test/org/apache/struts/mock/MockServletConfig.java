/*
 * $Header: /tmp/cvs-vintage/struts/src/test/org/apache/struts/mock/MockServletConfig.java,v 1.5 2004/03/14 06:23:52 sraeburn Exp $
 * $Revision: 1.5 $
 * $Date: 2004/03/14 06:23:52 $
 *
 * Copyright 1999-2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package org.apache.struts.mock;


import java.util.Enumeration;
import java.util.HashMap;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;


/**
 * <p>Mock <strong>ServletConfig</strong> object for low-level unit tests
 * of Struts controller components.  Coarser grained tests should be
 * implemented in terms of the Cactus framework, instead of the mock
 * object classes.</p>
 *
 * <p><strong>WARNING</strong> - Only the minimal set of methods needed to
 * create unit tests is provided, plus additional methods to configure this
 * object as necessary.  Methods for unsupported operations will throw
 * <code>UnsupportedOperationException</code>.</p>
 *
 * <p><strong>WARNING</strong> - Because unit tests operate in a single
 * threaded environment, no synchronization is performed.</p>
 *
 * @version $Revision: 1.5 $ $Date: 2004/03/14 06:23:52 $
 */

public class MockServletConfig implements ServletConfig {



    // ----------------------------------------------------------- Constructors


    public MockServletConfig() {
        super();
    }


    public MockServletConfig(ServletContext context) {
        super();
        setServletContext(context);
    }


    // ----------------------------------------------------- Instance Variables


    protected ServletContext context = null;
    protected HashMap parameters = new HashMap();


    // --------------------------------------------------------- Public Methods


    public void addInitParameter(String name, String value) {
        parameters.put(name, value);
    }


    public void setServletContext(ServletContext context) {
        this.context = context;
    }


    // ------------------------------------------------- ServletContext Methods


    public String getInitParameter(String name) {
        return ((String) parameters.get(name));
    }


    public Enumeration getInitParameterNames() {
        return (new MockEnumeration(parameters.keySet().iterator()));
    }


    public ServletContext getServletContext() {
        return (this.context);
    }


    public String getServletName() {
        return ("action");
    }


}
