/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.jdbc;

/**
 *   Just a fix for proxies not being created in java.* packages for security reasons...
 *      
 *   @see <related>
 *   @author Rickard Öberg (rickard.oberg@telkel.com)
 *   @version $Revision: 1.2 $
 */
public interface Connection
   extends java.sql.Connection
{
   // Constants -----------------------------------------------------
    
   // Public --------------------------------------------------------
}
