/*
 * jBoss, the OpenSource EJB server
 *
 * Distributable under GPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.cluster;

import java.net.InetAddress;

/**
 *      
 *   @see <related>
 *   @author Rickard Öberg (rickard.oberg@telkel.com)
 *   @version $Revision: 1.1 $
 */
public class ClusterInfo
   implements java.io.Serializable
{
   // Constants -----------------------------------------------------

   // Attributes ----------------------------------------------------
   ClusterRemote node;
   String name;
   
   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------
   public ClusterInfo(ClusterRemote node, String name)
   {
      this.node = node;
      this.name = name;
   }
   
   // Public --------------------------------------------------------
   public ClusterRemote getNode() { return node; }
   public String getName() { return name; }
   
   public String toString()
   {
      return name+":"+node.toString();
   }
}

