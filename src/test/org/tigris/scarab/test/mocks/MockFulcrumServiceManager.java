package org.tigris.scarab.test.mocks;

/* ================================================================
 * Copyright (c) 2000-2003 CollabNet.  All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 * 
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * 
 * 3. The end-user documentation included with the redistribution, if
 * any, must include the following acknowlegement: "This product includes
 * software developed by CollabNet <http://www.collab.net/>."
 * Alternately, this acknowlegement may appear in the software itself, if
 * and wherever such third-party acknowlegements normally appear.
 * 
 * 4. The hosted project names must not be used to endorse or promote
 * products derived from this software without prior written
 * permission. For written permission, please contact info@collab.net.
 * 
 * 5. Products derived from this software may not use the "Tigris" or 
 * "Scarab" names nor may "Tigris" or "Scarab" appear in their names without 
 * prior written permission of CollabNet.
 * 
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL COLLAB.NET OR ITS CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE
 * GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER
 * IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * ====================================================================
 * 
 * This software consists of voluntary contributions made by many
 * individuals on behalf of CollabNet.
 */ 

import org.apache.commons.configuration.Configuration;
import org.apache.fulcrum.InitializationException;
import org.apache.fulcrum.InstantiationException;
import org.apache.fulcrum.Service;
import org.apache.fulcrum.ServiceManager;
import org.apache.fulcrum.security.SecurityService;
import org.apache.log4j.Category;
import org.tigris.scarab.services.security.ScarabSecurity;

/**
 * @author Eric Pugh
 *
 */
public class MockFulcrumServiceManager implements ServiceManager {

    public MockFulcrumServiceManager() {
        super();
        // TODO Auto-generated constructor stub
    }

    public void init() throws InitializationException {
        // TODO Auto-generated method stub

    }

    public Configuration getConfiguration() {
        // TODO Auto-generated method stub
        return null;
    }

    public void setConfiguration(Configuration configuration) {
        // TODO Auto-generated method stub

    }

    public void setApplicationRoot(String applicationRoot) {
        // TODO Auto-generated method stub

    }

    public boolean isRegistered(String serviceName) {
        // TODO Auto-generated method stub
        return false;
    }

    public void initService(String name) throws InitializationException {
        // TODO Auto-generated method stub

    }

    public void shutdownService(String name) {
        // TODO Auto-generated method stub

    }

    public void shutdownServices() {
        // TODO Auto-generated method stub

    }

    public Service getService(String name) throws InstantiationException {
        if(name.equals(ScarabSecurity.SERVICE_NAME)){
            return new MockScarabSecurity();            
        }
        else if (name.equals(SecurityService.SERVICE_NAME)){
            return new MockSecurityService();
        }
        throw new InstantiationException("Couldn't create mock version of " + name);
    }

    public Configuration getConfiguration(String name) {
        // TODO Auto-generated method stub
        return null;
    }

    public void setServiceObject(String key, Object value) {
        // TODO Auto-generated method stub

    }

    public Object getServiceObject(String key) {
        // TODO Auto-generated method stub
        return null;
    }

    public String getRealPath(String path) {
        // TODO Auto-generated method stub
        return null;
    }

    public String getApplicationRoot() {
        // TODO Auto-generated method stub
        return null;
    }

    public Category getCategory() {
        // TODO Auto-generated method stub
        return null;
    }

}
