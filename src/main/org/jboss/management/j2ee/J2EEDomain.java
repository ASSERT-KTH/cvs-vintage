/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.management.j2ee;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

/**
 * Root class of the JBoss JSR-77 implementation of
 * {@link javax.management.j2ee.J2EEDomain J2EEDomain}.
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
 *
 * @jmx:mbean extends="org.jboss.management.j2ee.J2EEManagedObjectMBean"
 **/
public class J2EEDomain
   extends J2EEManagedObject
   implements J2EEDomainMBean
{
   
   // Constants -----------------------------------------------------
   
   public static final String J2EE_TYPE = "J2EEDomain";
   
   // Attributes ----------------------------------------------------
   
   private List mServers = new ArrayList();
   
   // Static --------------------------------------------------------
   
   // Constructors --------------------------------------------------
   
   public J2EEDomain( String pDomainName )
      throws
         MalformedObjectNameException,
         InvalidParentException
   {
      super( pDomainName, J2EE_TYPE, "Manager" );
   }
   
   // Public --------------------------------------------------------
   
   /**
    * @jmx:managed-attribute
    **/
   public ObjectName[] getServers() {
      return (ObjectName[]) mServers.toArray( new ObjectName[ 0 ] );
   }

   /**
    * @jmx:managed-operation
    **/
   public ObjectName getServer( int pIndex ) {
      if( pIndex >= 0 && pIndex < mServers.size() ) {
         return (ObjectName) mServers.get( pIndex );
      }
      return null;
   }
   
   // J2EEManagedObject implementation ----------------------------------------------
   
   public void addChild( ObjectName pChild ) {
      String lType = J2EEManagedObject.getType( pChild );
      if( J2EEServer.J2EE_TYPE.equals( lType ) ) {
         mServers.add( pChild );
      }
   }
   
   public void removeChild( ObjectName pChild ) {
      String lType = J2EEManagedObject.getType( pChild );
      if( J2EEServer.J2EE_TYPE.equals( lType ) ) {
         mServers.remove( pChild );
      }
   }
   // Object overrides ---------------------------------------------------
   
   public String toString() {
      return "J2EEDomain { " + super.toString() + " } [ " +
         ", servers: " + mServers +
         " ]";
   }
   
   // Package protected ---------------------------------------------
   
   // Protected -----------------------------------------------------
   
   // Private -------------------------------------------------------
   
   // Inner classes -------------------------------------------------
}
