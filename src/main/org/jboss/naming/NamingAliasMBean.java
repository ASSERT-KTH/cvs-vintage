/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.naming;

/**
 * MBean interface.
 */
public interface NamingAliasMBean extends org.jboss.system.ServiceMBean {

   /**
    * Get the from name of the alias. This is the location where the LinkRef is bound under JNDI.
    * @return the location of the LinkRef    */
  java.lang.String getFromName() ;

   /**
    * Set the from name of the alias. This is the location where the LinkRef is bound under JNDI.
    * @param name, the location where the LinkRef will be bound    */
  void setFromName(java.lang.String name) throws javax.naming.NamingException;

   /**
    * Get the to name of the alias. This is the target name to which the LinkRef refers. The name is a URL, or a name to be resolved relative to the initial context, or if the first character of the name is ".", the name is relative to the context in which the link is bound.
    * @return the target JNDI name of the alias.    */
  java.lang.String getToName() ;

   /**
    * Set the to name of the alias. This is the target name to which the LinkRef refers. The name is a URL, or a name to be resolved relative to the initial context, or if the first character of the name is ".", the name is relative to the context in which the link is bound.
    * @param name, the target JNDI name of the alias.    */
  void setToName(java.lang.String name) throws javax.naming.NamingException;

}
