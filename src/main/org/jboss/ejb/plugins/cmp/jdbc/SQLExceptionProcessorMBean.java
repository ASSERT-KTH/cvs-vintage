/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins.cmp.jdbc;

/**
 * MBean interface.
 */
public interface SQLExceptionProcessorMBean {

   /**
    * Return true if the exception indicates that an operation failed due to a unique constraint violation. This could be from any unique constraint not just the primary key.
    * @param e the SQLException to process
    * @return true if it was caused by a unique constraint violation
    */
  boolean isDuplicateKey(java.sql.SQLException e) ;

}
