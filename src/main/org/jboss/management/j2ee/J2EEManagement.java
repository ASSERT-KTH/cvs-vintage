package org.jboss.management.j2ee;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

/**
* Is the entry point of all managed objects in the management
* domain.
*
* @author <a href="mailto:marc.fleury@jboss.org">Marc Fleury</a>
* @author <a href="mailto:andreas@jboss.org">Andreas Schaefer</a>
**/
public class J2EEManagement
   extends J2EEManagedObject
   implements J2EEManagementMBean
{
   // -------------------------------------------------------------------------
   // Members
   // -------------------------------------------------------------------------
   
   private List mApplications = new ArrayList();
   
   private List mDeployments = new ArrayList();
   
   private List mServers = new ArrayList();
   
   // -------------------------------------------------------------------------
   // Constructors
   // -------------------------------------------------------------------------
   
   public J2EEManagement( String pDomainName )
      throws
         MalformedObjectNameException,
         InvalidParentException
   {
      super( pDomainName, "J2EEManagement", "Manager" );
   }
   
   // -------------------------------------------------------------------------
   // Properties (Getters/Setters)
   // -------------------------------------------------------------------------
   
   public ObjectName[] getApplications() {
      return (ObjectName[]) mApplications.toArray( new ObjectName[ 0 ] );
   }
   
   public ObjectName getApplication( int pIndex ) {
      if( pIndex >= 0 && pIndex < mApplications.size() ) {
         return (ObjectName) mApplications.get( pIndex );
      }
      return null;
   }

   public ObjectName[] getDeployments() {
      return (ObjectName[]) mDeployments.toArray( new ObjectName[ 0 ] );
   }
   
   public ObjectName getDeployment( int pIndex ) {
      if( pIndex >= 0 && pIndex < mDeployments.size() ) {
         return (ObjectName) mDeployments.get( pIndex );
      }
      return null;
   }

   public ObjectName[] getServers() {
      return (ObjectName[]) mServers.toArray( new ObjectName[ 0 ] );
   }

   public ObjectName getServer( int pIndex ) {
      if( pIndex >= 0 && pIndex < mServers.size() ) {
         return (ObjectName) mServers.get( pIndex );
      }
      return null;
   }
   
   public String toString() {
      return "J2EEManagement { " + super.toString() + " } [ " +
         "applications: " + mApplications +
         ", deployements: " + mDeployments +
         ", servers: " + mServers +
         " ]";
   }
   
   public void addChild( ObjectName pChild ) {
      Hashtable lProperties = pChild.getKeyPropertyList();
      String lType = lProperties.get( "type" ) + "";
      if( "J2EEApplication".equals( lType ) ) {
         mApplications.add( pChild );
      } else if( "J2EEDeployments".equals( lType ) ) {
         mDeployments.add( pChild );
      } else if( "J2EEServer".equals( lType ) ) {
         mServers.add( pChild );
      }
   }
   
   public void removeChild( ObjectName pChild ) {
      //AS ToDo
   }
}
