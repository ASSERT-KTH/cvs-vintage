/*
 * JBoss, the OpenSource J2EE WebOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.naming;

import org.jboss.system.ServiceMBean;

/**     
 * The management interface for the link ref pair service.
 * 
 * @author Adrian Brock (adrian@jboss.com)
 * @version $Revision: 1.1 $
 */
public interface LinkRefPairServiceMBean extends ServiceMBean
{
   // Constants -----------------------------------------------------
    
   // Public --------------------------------------------------------

   /**
    * The jndi name where the link ref pair is bound
    * 
    * @return the jndi name
    */
   public String getJndiName();

   /**
    * Set the jndi name where the link ref pair is bound
    * 
    * @param jndiName the jndi name
    */
   public void setJndiName(String jndiName);

   /**
    * The jndi name of the remote binding
    * 
    * @return the jndi name
    */
   public String getRemoteJndiName();

   /**
    * Set the jndi name of the remote binding
    * 
    * @param jndiName the jndi name
    */
   public void setRemoteJndiName(String jndiName);

   /**
    * The jndi name of the local binding
    * 
    * @return the jndi name
    */
   public String getLocalJndiName();

   /**
    * Set the jndi name of the local binding
    * 
    * @param jndiName the jndi name
    */
   public void setLocalJndiName(String jndiName);
}