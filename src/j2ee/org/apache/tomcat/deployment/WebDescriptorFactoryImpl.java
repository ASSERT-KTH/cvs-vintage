/*
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


package org.apache.tomcat.deployment;

import java.lang.IllegalStateException;
import java.util.Hashtable;
    
/**
 * @author James Todd [gonzo@xphylz.eng.sun.com]
 */

public class WebDescriptorFactoryImpl
implements WebDescriptorFactory {
    private Hashtable interfaceToImplementationMap = new Hashtable();
    
    public WebDescriptorFactoryImpl() {
	this.interfaceToImplementationMap.put(
            WebApplicationDescriptor.class,
            WebApplicationDescriptorImpl.class);
	this.interfaceToImplementationMap.put(MimeMapping.class,
            MimeMappingImpl.class);
	this.interfaceToImplementationMap.put(
            InitializationParameter.class,
            InitializationParameterImpl.class);
	this.interfaceToImplementationMap.put(
            LocalizedContentDescriptor.class,
            LocalizedContentDescriptorImpl.class);
	this.interfaceToImplementationMap.put(
            ServletDescriptor.class, ServletDescriptorImpl.class);
	this.interfaceToImplementationMap.put(JspDescriptor.class,
            JspDescriptorImpl.class);
	this.interfaceToImplementationMap.put(EjbReference.class,
            EjbReferenceImpl.class);
	this.interfaceToImplementationMap.put(ContextParameter.class,
            ContextParameterImpl.class);
	this.interfaceToImplementationMap.put(ErrorPageDescriptor.class,
            ErrorPageDescriptorImpl.class);
	this.interfaceToImplementationMap.put(SecurityRole.class,
            SecurityRoleImpl.class);
	this.interfaceToImplementationMap.put(SecurityRoleReference.class,
            SecurityRoleReferenceImpl.class);
	this.interfaceToImplementationMap.put(EnvironmentEntry.class,
            EnvironmentEntryImpl.class);
	this.interfaceToImplementationMap.put(LoginConfiguration.class,
            LoginConfigurationImpl.class);
	this.interfaceToImplementationMap.put(SecurityConstraint.class,
            SecurityConstraintImpl.class);
	this.interfaceToImplementationMap.put(
            AuthorizationConstraint.class,
            AuthorizationConstraintImpl.class);
	this.interfaceToImplementationMap.put(
            UserDataConstraint.class, UserDataConstraintImpl.class);
	this.interfaceToImplementationMap.put(
            WebResourceCollection.class, WebResourceCollectionImpl.class);
	this.interfaceToImplementationMap.put(ResourceReference.class,
            ResourceReferenceImpl.class);

        this.interfaceToImplementationMap.put(TagLibConfig.class, 
                                              TagLibConfigImpl.class);
    }

    public Object createDescriptor(Class interfaceType) {
	Class implementationClass =
            (Class)this.interfaceToImplementationMap.get(interfaceType);
	if (implementationClass == null) {
	    throw new RuntimeException(interfaceType +
                " has no mapped implementation");
	}

	Object toReturn = null;
	try {
	    toReturn = implementationClass.newInstance();
	} catch (Throwable t) {
	    t.printStackTrace();
	}

	return toReturn;
    }
}
