/*
 * Created on Nov 20, 2004
 *
 */
package org.tigris.scarab.test.mocks;

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
