/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.resource;

import javax.resource.spi.ManagedConnectionFactory;
import javax.security.auth.Subject;

/**
 *   Looks up a Subject for a particular connector request.  The implementation
 *   may perform authentication translation, so that user "foo" logged into
 *   the EJB container is authenticated as user "bar" for the Resource
 *   Adapter (aka ManagedConnectionFactory).
 *
 *   @author Toby Allsopp (toby.allsopp@peace.com)
 *   @author Aaron Mulder <ammulder@alumni.princeton.edu>
 *   @version $Revision: 1.1 $
 */
public interface ResourceSubjectFactory {
    /**
     * Gets a subject for the curent request.
     * @param factory The factory that the request will authenticate to.
     * @param name A name for the factory, which corresponds to the name
     *             passed in to the ConnectionManagerFactory at deployment
     *             time if any, or an auto-generated name otherwise.  Just a
     *             convenience in case it's easier to use than the factory
     *             itself.
     * @return The Subject to use to authenticate to the ManagedConnectionFactory.
     */
    public Subject getSubject(ManagedConnectionFactory factory, String name);
}