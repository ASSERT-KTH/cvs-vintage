/*
 * jBoss, the OpenSource EJB server
 *
 * Distributable under GPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.cluster;

/**
 *      
 *   @see <related>
 *   @author Rickard Öberg (rickard.oberg@telkel.com)
 *   @version $Revision: 1.1 $
 */
public interface ClusterMBean
{
   // Constants -----------------------------------------------------
    
   // Constructors --------------------------------------------------
   
   // Public --------------------------------------------------------
   public java.util.Iterator getNodes();
}

