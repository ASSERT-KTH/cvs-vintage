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
 * @version $Revision: 1.2 $
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
   // -------------------------------------------------------------------------
   // Members
   // -------------------------------------------------------------------------
   
   private List mDeployedObjects = new ArrayList();
   
   private List mServers = new ArrayList();
   
   // -------------------------------------------------------------------------
   // Constructors
   // -------------------------------------------------------------------------
   
   public J2EEDomain( String pDomainName )
      throws
         MalformedObjectNameException,
         InvalidParentException
   {
      super( pDomainName, "J2EEDomain", "Manager" );
   }
   
   // -------------------------------------------------------------------------
   // Properties (Getters/Setters)
   // -------------------------------------------------------------------------
   
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
   
   public String toString() {
      return "J2EEDomain { " + super.toString() + " } [ " +
         "deployed objects: " + mDeployedObjects +
         ", servers: " + mServers +
         " ]";
   }
   
   public void addChild( ObjectName pChild ) {
      Hashtable lProperties = pChild.getKeyPropertyList();
      String lType = lProperties.get( "j2eeType" ) + "";
      if(
         "J2EEApplication".equals( lType ) ||
         "J2EEModule".equals( lType )
      ) {
         mDeployedObjects.add( pChild );
      } else if( "J2EEServer".equals( lType ) ) {
         mServers.add( pChild );
      }
   }
   
   public void removeChild( ObjectName pChild ) {
      //AS ToDo
   }
}
