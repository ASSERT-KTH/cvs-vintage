/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.management.j2ee;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import javax.management.j2ee.JDBCDataSource;

/**
 * Root class of the JBoss JSR-77 implementation of
 * {@link javax.management.j2ee.JDBCConnectionPool JDBCConnectionPool}.
 *
 * @author  <a href="mailto:andreas@jboss.org">Andreas Schaefer</a>.
 * @version $Revision: 1.3 $
 *   
 * <p><b>Revisions:</b>
 *
 * <p><b>20011126 Andreas Schaefer:</b>
 * <ul>
 * <li> Adjustments to the JBoss Guidelines
 * </ul>
 **/
public class JDBCConnectionPool
   extends JDBCConnection
   implements javax.management.j2ee.JDBCConnectionPool
{
   // -------------------------------------------------------------------------
   // Members
   // -------------------------------------------------------------------------  

   // -------------------------------------------------------------------------
   // Constructors
   // -------------------------------------------------------------------------

   /**
   * @param pName Name of the J2EEManagement
   * @param pDriver JDBC Driver to be set
   *
   * @throws InvalidParameterException If given driver is null
   **/
   public JDBCConnectionPool( String pName, ObjectName pServer, JDBCDataSource pDriver )
      throws
         MalformedObjectNameException,
         InvalidParentException
   {
      super( "J2EEConnectionPool", pName, pServer, pDriver );
   }

   // -------------------------------------------------------------------------
   // Properties (Getters/Setters)
   // -------------------------------------------------------------------------  

   public String toString() {
      return "JDBCConnectionPool[ " + super.toString() +
         " ]";
   }

}
