/*
* JBoss, the OpenSource J2EE webOS
*
* Distributable under LGPL license.
* See terms of license at gnu.org.
*/
package org.jboss.management.j2ee;

/**
*
*
* @author <a href="mailto:marc.fleury@jboss.org">Marc Fleury</a>
* @author <a href="mailto:andreas@jboss.com">Andreas Schaefer</a>
* @version $Revision: 1.1 $
**/
public class JDBCDataSource
   extends J2EEManagedObject
   implements javax.management.j2ee.JDBCDataSource
{
   // -------------------------------------------------------------------------
   // Members
   // -------------------------------------------------------------------------  

   // -------------------------------------------------------------------------
   // Constructors
   // -------------------------------------------------------------------------

   /**
   * @param pName Name of the JDBCDataSource
   *
   * @throws InvalidParameterException If list of nodes or ports was null or empty
   **/
   public JDBCDataSource( String pName ) {
      super( pName );
   }

   // -------------------------------------------------------------------------
   // Properties (Getters/Setters)
   // -------------------------------------------------------------------------  

   public String toString() {
      return "JDBCDatasource[ " + super.toString() +
         " ]";
   }

}
