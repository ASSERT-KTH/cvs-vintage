/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins;

/**
 * MBean interface.
 */
public interface StatefulSessionFilePersistenceManagerMBean extends org.jboss.system.ServiceMBean {

   /**
    * Set the sub-directory name under the server data directory where session data will be stored. <p> This value will be appened to the value of <tt><em>jboss-server-data-dir</em></tt>. <p> This value is only used during creation and will not dynamically change the store directory when set after the create step has finished.
    * @param dirName A sub-directory name.    */
  void setStoreDirectoryName(java.lang.String dirName) ;

}
