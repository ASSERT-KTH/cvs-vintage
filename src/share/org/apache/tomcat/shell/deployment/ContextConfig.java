/*
 * $Header: /tmp/cvs-vintage/tomcat/src/share/org/apache/tomcat/shell/deployment/Attic/ContextConfig.java,v 1.2 1999/12/31 01:18:37 craigmcc Exp $
 * $Revision: 1.2 $
 * $Date: 1999/12/31 01:18:37 $
 *
 * ====================================================================
 *
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 1999 The Apache Software Foundation.  All rights 
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer. 
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:  
 *       "This product includes software developed by the 
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "The Jakarta Project", "Tomcat", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written 
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Group.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 * [Additional notices, if required by prior licensing conditions]
 *
 */ 


package org.apache.tomcat.shell.deployment;

import java.util.Enumeration;
import java.util.Vector;
import org.apache.tomcat.core.LifecycleInterceptor;
import org.apache.tomcat.core.ServiceInterceptor;
import org.apache.tomcat.shell.Constants;
import org.apache.tomcat.util.URLUtil;
import java.net.URL;
import java.net.MalformedURLException;

/**
 *
 * @author James Todd [gonzo@eng.sun.com]
 */

public class ContextConfig {
    private String path = "";
    private URL documentBase = null;
    private int defaultSessionTimeOut = Constants.Default.SessionTimeOut;
    private boolean isWARExpanded = Constants.Default.IS_WAR_EXPANDED;
    private boolean isWARValidated = Constants.Default.IS_WAR_VALIDATED;
    private boolean isInvokerEnabled = Constants.Default.IS_INVOKER_ENABLED;
    private boolean isWorkDirPersistent =
        Constants.Default.IS_WORK_DIR_PERSISTENT;
    private Vector lifecycleInterceptors = new Vector();
    private Vector serviceInterceptors = new Vector();

    ContextConfig() {
    }

    public String getPath() {
        return this.path;
    }

    void setPath(String path) {
        String p = path;

        if (p.equals("") ||
            p.equals("/")) {
            p = org.apache.tomcat.core.Constants.Context.Default.Path;
        }

        this.path = p;
    }

    public URL getDocumentBase() {
        return this.documentBase;
    }

    void setDocumentBase(String documentBase) {
        URL db = null;

        try {
            db = URLUtil.resolve(documentBase);
        } catch (MalformedURLException mue) {
        }

        setDocumentBase(db);
    }

    void setDocumentBase(URL documentBase) {
        URL db = null;

        try {
            db = URLUtil.resolve(documentBase.toString());
        } catch (MalformedURLException mue) {
        }

        this.documentBase = db;
    }

    public int getDefaultSessionTimeOut() {
        return this.defaultSessionTimeOut;
    }

    void setDefaultSessionTimeOut(String defaultSessionTimeOut) {
        try {
            setDefaultSessionTimeOut(
                Integer.parseInt(defaultSessionTimeOut));
        } catch (NumberFormatException nfe) {
        }
    }

    void setDefaultSessionTimeOut(int defaultSessionTimeOut) {
        this.defaultSessionTimeOut = defaultSessionTimeOut;
    }

    public boolean isWARExpanded() {
        return this.isWARExpanded;
    }

    void setIsWARExpanded(String isWARExpanded) {
        setIsWARExpanded(
            (boolean)Boolean.valueOf(isWARExpanded).booleanValue());
    }

    void setIsWARExpanded(boolean isWARExpanded) {
        this.isWARExpanded = isWARExpanded;
    }

    public boolean isWARValidated() {
        return this.isWARValidated;
    }

    void setIsWARValidated(String isWARValidated) {
        setIsWARValidated(
            (boolean)Boolean.valueOf(isWARValidated).booleanValue());
    }

    void setIsWARValidated(boolean isWARValidated) {
        this.isWARValidated = isWARValidated;
    }

    public boolean isInvokerEnabled() {
        return this.isInvokerEnabled;
    }

    void setIsInvokerEnabled(String isInvokerEnabled) {
        setIsInvokerEnabled(
            (boolean)Boolean.valueOf(isInvokerEnabled).booleanValue());
    }

    void setIsInvokerEnabled(boolean isInvokerEnabled) {
        this.isInvokerEnabled = isInvokerEnabled;
    }

    public boolean isWorkDirPersistent() {
        return this.isWorkDirPersistent;
    }

    void setIsWorkDirPersistent(String isWorkDirPersistent) {
        setIsWorkDirPersistent(
            (boolean)Boolean.valueOf(isWorkDirPersistent).booleanValue());
    }

    void setIsWorkDirPersistent(boolean isWorkDirPersistent) {
        this.isWorkDirPersistent = isWorkDirPersistent;
    }

    public void addLifecycleInterceptor(LifecycleInterceptor interceptor) {
	lifecycleInterceptors.addElement(interceptor);
    }

    public Enumeration getLifecycleInterceptors() {
	return lifecycleInterceptors.elements();
    }

    public void addServiceInterceptor(ServiceInterceptor interceptor) {
	serviceInterceptors.addElement(interceptor);
    }

    public Enumeration getServiceInterceptors() {
	return serviceInterceptors.elements();
    }

}
