/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.resource.security;

import java.security.Principal;

import javax.security.auth.Subject;

import javax.resource.spi.ManagedConnectionFactory;

import org.jboss.logging.Log;
import org.jboss.resource.RARMetaData;

/**
 *   Interface for classes that implement a mapping from caller
 *   principal to resource principal.
 *
 *   <p> The <code>set...</code> methods <strong>must</strong> be
 *   called before <code>createSubject</code>.
 *
 *   @see org.jboss.resource.ConnectionManagerImpl
 *   @author Toby Allsopp (toby.allsopp@peace.com)
 *   @version $Revision: 1.1 $
 */
public interface PrincipalMapping
{
   // Constants -----------------------------------------------------

   // Public --------------------------------------------------------

   /**
    * Sets the <code>Log</code> to which to log.
    */
   void setLog(Log log);

   /**
    * Sets the managed connection factory for which principals will be
    * mapped.
    */
   void setManagedConnectionFactory(ManagedConnectionFactory mcf);

   /**
    * Sets the meta-data that describes the resource adapter for which
    * principals will be mapped.
    */
   void setRARMetaData(RARMetaData metadata);

   /**
    * Sets configuration information for a particular implementation
    * of this interface. The format of this information is specific to
    * each implementation, but it is intended that a sequence of
    * name-value pairs in <code>Properties.load</code> format will be
    * used.
    *
    * @see java.util.Properties#load
    */
   void setProperties(String properties);

   /**
    * Creates a <code>Subject</code> that contains the resource
    * principal and its credentials obtained from the principal
    * mapping implementation.
    *
    * @param callerPrincipal the identity under which the request for
    *                        a connection has been made, i.e. the
    *                        principal the requesting component is
    *                        running under.
    *
    * @return a new <code>Subject</code> instance containing a single
    *         principal, the mapped resource principal, and whatever
    *         credentials are required for EIS sign-on
    */
   Subject createSubject(Principal callerPrincipal);
}
