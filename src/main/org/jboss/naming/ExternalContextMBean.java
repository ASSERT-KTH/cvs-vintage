/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.naming;

/**
 * MBean interface.
 * @see org.jboss.naming.NonSerializableFactory
 */
public interface ExternalContextMBean extends org.jboss.system.ServiceMBean {

   /**
    * Set the jndi name under which the external context is bound.
    */
  java.lang.String getJndiName() ;

   /**
    * Set the jndi name under which the external context is bound.
    */
  void setJndiName(java.lang.String jndiName) throws javax.naming.NamingException;

  boolean getRemoteAccess() ;

  void setRemoteAccess(boolean remoteAccess) ;

  boolean getCacheContext() ;

  void setCacheContext(boolean cacheContext) ;

   /**
    * Get the class name of the InitialContext implementation to use. Should be one of: <ul> <li>javax.naming.InitialContext <li>javax.naming.directory.InitialDirContext <li>javax.naming.ldap.InitialLdapContext </ul>
    * @return the classname of the InitialContext to use    */
  java.lang.String getInitialContext() ;

   /**
    * Set the class name of the InitialContext implementation to use. Should be one of: <ul> <li>javax.naming.InitialContext <li>javax.naming.directory.InitialDirContext <li>javax.naming.ldap.InitialLdapContext </ul>
    * @param contextClass, the classname of the InitialContext to use    */
  void setInitialContext(java.lang.String className) throws java.lang.ClassNotFoundException;

   /**
    * Set the InitialContex class environment properties from the given URL.
    */
  void setPropertiesURL(java.lang.String contextPropsURL) throws java.io.IOException;

   /**
    * Set the InitialContex class environment properties.
    */
  void setProperties(java.util.Properties props) throws java.io.IOException;

   /**
    * Get the InitialContex class environment properties.
    */
  java.util.Properties getProperties() throws java.io.IOException;

}
