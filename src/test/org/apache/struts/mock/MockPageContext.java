/*
 * $Header: /tmp/cvs-vintage/struts/src/test/org/apache/struts/mock/MockPageContext.java,v 1.5 2004/03/14 06:23:52 sraeburn Exp $
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


import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;

import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.BodyContent;


/**
 * <p>Mock <strong>ServletContext</strong> object for low-level unit tests
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

public class MockPageContext extends PageContext {



    // ----------------------------------------------------------- Constructors


    public MockPageContext() {
        super();
    }


    public MockPageContext(ServletConfig config,
                           ServletRequest request,
                           ServletResponse response) {
        super();
        setValues(config, request, response);
    }


    // ----------------------------------------------------- Instance Variables


    protected ServletContext application = null;
    protected HashMap attributes = new HashMap();    // Page scope attributes
    protected ServletConfig config = null;
    protected ServletRequest request = null;
    protected ServletResponse response = null;
    protected HttpSession session = null;


    // --------------------------------------------------------- Public Methods


    public void setValues(ServletConfig config,
                          ServletRequest request,
                          ServletResponse response) {
        this.config = config;
        if (config != null) {
            this.application = config.getServletContext();
        } else {
            this.application = null;
        }
        this.request = request;
        this.response = response;
        if (request != null) {
            session = ((HttpServletRequest) request).getSession(false);
        } else {
            this.session = null;
        }
    }



    // ---------------------------------------------------- PageContext Methods


    public Object findAttribute(String name) {
        Object value = getAttribute(name, PageContext.PAGE_SCOPE);
        if (value == null) {
            value = getAttribute(name, PageContext.REQUEST_SCOPE);
        }
        if (value == null) {
            value = getAttribute(name, PageContext.SESSION_SCOPE);
        }
        if (value == null) {
            value = getAttribute(name, PageContext.APPLICATION_SCOPE);
        }
        return (value);
    }


    public void forward(String path) {
        throw new UnsupportedOperationException();
    }


    public Object getAttribute(String name) {
        return (getAttribute(name, PageContext.PAGE_SCOPE));
    }


    public Object getAttribute(String name, int scope) {
        if (scope == PageContext.PAGE_SCOPE) {
            return (attributes.get(name));
        } else if (scope == PageContext.REQUEST_SCOPE) {
            if (request != null) {
                return (request.getAttribute(name));
            } else {
                return (null);
            }
        } else if (scope == PageContext.SESSION_SCOPE) {
            if (session != null) {
                return (session.getAttribute(name));
            } else {
                return (null);
            }
        } else if (scope == PageContext.APPLICATION_SCOPE) {
            if (application != null) {
                return (application.getAttribute(name));
            } else {
                return (null);
            }
        } else {
            throw new IllegalArgumentException("Invalid scope " + scope);
        }
    }


    public Enumeration getAttributeNamesInScope(int scope) {
        if (scope == PageContext.PAGE_SCOPE) {
            return (new MockEnumeration(attributes.keySet().iterator()));
        } else if (scope == PageContext.REQUEST_SCOPE) {
            if (request != null) {
                return (request.getAttributeNames());
            } else {
                return
                    (new MockEnumeration(Collections.EMPTY_LIST.iterator()));
            }
        } else if (scope == PageContext.SESSION_SCOPE) {
            if (session != null) {
                return (session.getAttributeNames());
            } else {
                return
                    (new MockEnumeration(Collections.EMPTY_LIST.iterator()));
            }
        } else if (scope == PageContext.APPLICATION_SCOPE) {
            if (application != null) {
                return (application.getAttributeNames());
            } else {
                return
                    (new MockEnumeration(Collections.EMPTY_LIST.iterator()));
            }
        } else {
            throw new IllegalArgumentException("Invalid scope " + scope);
        }
    }


    public int getAttributesScope(String name) {
        if (attributes.get(name) != null) {
            return (PageContext.PAGE_SCOPE);
        } else if ((request != null) &&
                   (request.getAttribute(name) != null)) {
            return (PageContext.REQUEST_SCOPE);
        } else if ((session != null) &&
                   (session.getAttribute(name) != null)) {
            return (PageContext.SESSION_SCOPE);
        } else if ((application != null) &&
                   (application.getAttribute(name) != null)) {
            return (PageContext.APPLICATION_SCOPE);
        } else {
            return (0);
        }
    }


    public Exception getException() {
        throw new UnsupportedOperationException();
    }


    public JspWriter getOut() {
        throw new UnsupportedOperationException();
    }


    public Object getPage() {
        throw new UnsupportedOperationException();
    }


    public ServletRequest getRequest() {
        return (this.request);
    }


    public ServletResponse getResponse() {
        return (this.response);
    }


    public ServletConfig getServletConfig() {
        return (this.config);
    }


    public ServletContext getServletContext() {
        return (this.application);
    }


    public HttpSession getSession() {
        return (this.session);
    }


    public void handlePageException(Exception e) {
        throw new UnsupportedOperationException();
    }


    public void handlePageException(Throwable t) {
        throw new UnsupportedOperationException();
    }


    public void include(String path) {
        throw new UnsupportedOperationException();
    }


    public void initialize(Servlet servlet, ServletRequest request,
                           ServletResponse response, String errorPageURL,
                           boolean needsSession, int bufferSize,
                           boolean autoFlush) {
        throw new UnsupportedOperationException();
    }


    public JspWriter popBody() {
        throw new UnsupportedOperationException();
    }


    public BodyContent pushBody() {
        throw new UnsupportedOperationException();
    }


    public void release() {
        throw new UnsupportedOperationException();
    }


    public void removeAttribute(String name) {
        int scope = getAttributesScope(name);
        if (scope != 0) {
            removeAttribute(name, scope);
        }
    }


    public void removeAttribute(String name, int scope) {
        if (scope == PageContext.PAGE_SCOPE) {
            attributes.remove(name);
        } else if (scope == PageContext.REQUEST_SCOPE) {
            if (request != null) {
                request.removeAttribute(name);
            }
        } else if (scope == PageContext.SESSION_SCOPE) {
            if (session != null) {
                session.removeAttribute(name);
            }
        } else if (scope == PageContext.APPLICATION_SCOPE) {
            if (application != null) {
                application.removeAttribute(name);
            }
        } else {
            throw new IllegalArgumentException("Invalid scope " + scope);
        }
    }


    public void setAttribute(String name, Object value) {
        setAttribute(name, value, PageContext.PAGE_SCOPE);
    }


    public void setAttribute(String name, Object value, int scope) {
        if (scope == PageContext.PAGE_SCOPE) {
            attributes.put(name, value);
        } else if (scope == PageContext.REQUEST_SCOPE) {
            if (request != null) {
                request.setAttribute(name, value);
            }
        } else if (scope == PageContext.SESSION_SCOPE) {
            if (session != null) {
                session.setAttribute(name, value);
            }
        } else if (scope == PageContext.APPLICATION_SCOPE) {
            if (application != null) {
                application.setAttribute(name, value);
            }
        } else {
            throw new IllegalArgumentException("Invalid scope " + scope);
        }
    }


}
