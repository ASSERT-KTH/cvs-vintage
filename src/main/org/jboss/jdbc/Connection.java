/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.jdbc;

/**
 *   Just a fix for proxies not being created in java.* packages for security reasons...
 *      
 *   @see <related>
 *   @author <a href="mailto:rickard.oberg@telkel.com">Rickard Öberg</a>
 *   @version $Revision: 1.4 $
 */
public interface Connection
   extends java.sql.Connection
{
   // Constants -----------------------------------------------------
    
   // Public --------------------------------------------------------
}
