/*
 * $Header: /tmp/cvs-vintage/tomcat/src/share/org/apache/tomcat/shell/deployment/Attic/ServerConfig.java,v 1.2 1999/12/31 01:18:37 craigmcc Exp $
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

import org.apache.tomcat.core.LifecycleInterceptor;
import org.apache.tomcat.core.ServiceInterceptor;
import org.apache.tomcat.shell.Constants;
import org.apache.tomcat.util.XMLTree;
import java.util.Vector;
import java.util.Enumeration;

/**
 *
 * @author James Todd [gonzo@eng.sun.com]
 */

public class ServerConfig {
    private int adminPort = Constants.Default.ADMIN_PORT;
    private String workDir = Constants.Default.WORK_DIR;
    private Vector contextManagers = new Vector();

    ServerConfig(XMLTree config) {
	if (config.getName().equals(Constants.Element.SERVER)) {
	    setAdminPort(
                (String)config.getAttribute(Constants.Attribute.AdminPort));
	    setWorkDir(
	        (String)config.getAttribute(Constants.Attribute.WorkDir));

	    setContextManagers(getContextManagerConfigs(config));
	}
    }

    public int getAdminPort() {
      return this.adminPort;
    }

    void setAdminPort(String adminPort) {
       try {
	    setAdminPort(Integer.parseInt(adminPort));
        } catch (NumberFormatException nfe) {
        }
    }

    void setAdminPort(int adminPort) {
        this.adminPort = adminPort;
    }

    public String getWorkDir() {
        return this.workDir;
    }

    void setWorkDir(String workDir) {
        this.workDir = workDir;
    }

    public Enumeration getContextManagers() {
        return this.contextManagers.elements();
    }

    void setContextManagers(Vector contextManagers) {
        this.contextManagers = contextManagers;
    }

    private Vector getContextManagerConfigs(XMLTree config) {
        Vector contextManagers  = new Vector();
	Enumeration enum = config.elements();

	while (enum.hasMoreElements()) {
	    XMLTree contextManager = (XMLTree)enum.nextElement();

	    if (contextManager.getName().equals(
	        Constants.Element.CONTEXT_MANAGER)) { 
	        ContextManagerConfig contextManagerConfig =
		    new ContextManagerConfig();

		contextManagerConfig.setPort(
		    (String)contextManager.getAttribute(
                        Constants.Attribute.Port));
		contextManagerConfig.setHostName(
                    (String)contextManager.getAttribute(
                        Constants.Attribute.HostName));
		contextManagerConfig.setINet(
                    (String)contextManager.getAttribute(
                        Constants.Attribute.INet));

                Enumeration contexts = contextManager.getElements(
		        Constants.Element.CONTEXT).elements();

		while (contexts.hasMoreElements()) {
		    XMLTree context = (XMLTree)contexts.nextElement();
		    ContextConfig contextConfig = new ContextConfig();

		    contextConfig.setPath((String)context.getAttribute(
			Constants.Attribute.Path));
		    contextConfig.setDocumentBase(
                        (String)context.getAttribute(
			    Constants.Attribute.DocumentBase));
		    contextConfig.setDefaultSessionTimeOut(
			(String)context.getAttribute(
			    Constants.Attribute.DefaultSessionTimeOut));
		    contextConfig.setIsWARExpanded(
                        (String)context.getAttribute(
			    Constants.Attribute.IsWARExpanded));
		    contextConfig.setIsWARValidated(
		        (String)context.getAttribute(
		           Constants.Attribute.IsWARValidated));
		    contextConfig.setIsInvokerEnabled(
		        (String)context.getAttribute(
		           Constants.Attribute.IsInvokerEnabled));
		    contextConfig.setIsWorkDirPersistent(
		        (String)context.getAttribute(
		           Constants.Attribute.IsWorkDirPersistent));

		    contextManagerConfig.addContextConfig(contextConfig);
		}

		// [Arkin] This reads the interceptors information from the
		//         server configuration file, creates an instance
		//         for each interceptor based on it's class name and
		//         registers it with all contexts that match the same
		//         document base URL.
		Enumeration interceptors = contextManager.getElements(
		    Constants.Element.INTERCEPTOR).elements();
		
		while (interceptors.hasMoreElements()) {
		    XMLTree interceptor = (XMLTree)interceptors.nextElement();
		    String className = (String)interceptor.getAttribute(Constants.Attribute.CLASS_NAME);
		    System.out.println("Adding Global Interceptor: " + className);
		    try {
			Object instance = Class.forName(className).newInstance();
			String docBase=(String)interceptor.getAttribute(
			    Constants.Attribute.DocumentBase);
			for(Enumeration e=contextManagerConfig.getContextConfigs();
			    e.hasMoreElements();) {
			    ContextConfig contextConfig=(ContextConfig)e.nextElement();
			    if(contextConfig.getDocumentBase().equals(docBase)||
			       (docBase.endsWith("*") &&
				contextConfig.getDocumentBase().toString().startsWith(
				 docBase.substring(0,docBase.length()-1)))) {
				if(instance instanceof ServiceInterceptor)
				    contextConfig.addServiceInterceptor((ServiceInterceptor)instance);
				else if(instance instanceof LifecycleInterceptor)
				    contextConfig.addLifecycleInterceptor((LifecycleInterceptor)instance);
			    }
			}
		    } catch ( Throwable except ) {
			System.out.println("Error creating interceptor " + className + ": " + except);
		    }
		}

                Enumeration connectors = contextManager.getElements(
		    Constants.Element.CONNECTOR).elements();

		while (connectors.hasMoreElements()) {
		    XMLTree connector = (XMLTree)connectors.nextElement();

		    ConnectorConfig connectorConfig = new ConnectorConfig();

		    connectorConfig.setClassName(
		        (String)connector.getAttribute(
			    Constants.Attribute.CLASS_NAME));

		    Enumeration parameters = connector.getElements(
			Constants.Element.PARAMETER).elements();

		    while (parameters.hasMoreElements()) {
		        XMLTree parameter =
			    (XMLTree)parameters.nextElement();

			ParameterConfig parameterConfig =
			    new ParameterConfig();

			parameterConfig.setName(
			    (String)parameter.getAttribute(
			        Constants.Attribute.PARAMETER_NAME));
			parameterConfig.setValue(
			    (String)parameter.getAttribute(
			        Constants.Attribute.PARAMETER_VALUE));

			connectorConfig.addParameter(parameterConfig);
		    }

		    contextManagerConfig.addConnectorConfig(connectorConfig);
		}

		contextManagers.addElement(contextManagerConfig);
	    }
	}
	
	return contextManagers;
    }
}
