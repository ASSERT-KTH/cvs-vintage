package org.jboss.management.j2ee;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

/**
* Is the access point to management information for a single J2EE Server
* Core implementation representing a logical core server of one instance
* of a J2EE platform product.
*
* @author <a href="mailto:andreas@jboss.org">Andreas Schaefer</a>
**/
public class J2EEServer
  extends J2EEManagedObject
  implements J2EEServerMBean
{
   // -------------------------------------------------------------------------
   // Members
   // -------------------------------------------------------------------------
   
   private List mApplications = new ArrayList();
   
   private List mResources = new ArrayList();
   
   private List mNodes = new ArrayList();
   
   private List mPorts = new ArrayList();
   
   private List mJVMs = new ArrayList();
   
   private String mJ2eeVendor = null;
   
   // -------------------------------------------------------------------------
   // Constructors
   // -------------------------------------------------------------------------
   
   public J2EEServer( String pName, ObjectName pDomain )
      throws
         MalformedObjectNameException,
         InvalidParentException
   {
      super( "J2EEServer", pName, pDomain );
   }
   
   public J2EEServer(
      String pName,
      ObjectName pDomain,
      ObjectName[] pApplications,
      ObjectName[] pResources,
      ObjectName[] pNodes,
      ObjectName[] pPorts,
      ObjectName[] pJVMs,
      String pJ2eeVendor
   )
      throws
         MalformedObjectNameException,
         InvalidParentException
   {
      super( "J2EEServer", pName, pDomain );
      mApplications.addAll( Arrays.asList( pApplications ) );
      mResources.addAll( Arrays.asList( pResources ) );
      mNodes.addAll( Arrays.asList( pNodes ) );
      mPorts.addAll( Arrays.asList( pPorts ) );
      mJVMs.addAll( Arrays.asList( pJVMs ) );
      mJ2eeVendor = pJ2eeVendor;
   }
   
   // -------------------------------------------------------------------------
   // Properties (Getters/Setters)
   // -------------------------------------------------------------------------  

   /**
   * @return The actual list of Applications deployed on this server. The
   *          list is never null but maybe empty.
   **/
   public ObjectName[] getApplications() {
      return (ObjectName[]) mApplications.toArray( new ObjectName[ 0 ] );
   }

   /**
   * Looks up an application with the given index
   *
   * @param pIndex Index of the requested application
   *
   * @return Application found for the given index or null
   *         if index is out of bounds.
   **/
   public ObjectName getApplication( int pIndex ) {
      if( pIndex >= 0 && pIndex < mApplications.size() ) {
         return (ObjectName) mApplications.get( pIndex );
      }
      return null;
   }

   /**
   * @return The actual list of Ports which is never null
   **/
   public ObjectName[] getPorts() {
      return (ObjectName[]) mPorts.toArray( new ObjectName[ 0 ] );
   }

   /**
   * Looks up an Port with the given index
   *
   * @param pIndex Index of the requested Port
   *
   * @return Port found for the given index or null
   *         if index is out of bounds.
   **/
   public ObjectName getPort( int pIndex ) {
      if( pIndex >= 0 && pIndex < mPorts.size() ) {
         return (ObjectName) mPorts.get( pIndex );
      }
      return null;
   }

   /**
   * @return The actual list of Nodes which is never null
   **/
   public ObjectName[] getNodes() {
      return (ObjectName[]) mNodes.toArray( new ObjectName[ 0 ] );
   }

   /**
   * Looks up an Node with the given index
   *
   * @param pIndex Index of the requested Node
   *
   * @return Node found for the given index or null
   *         if index is out of bounds.
   **/
   public ObjectName getNode( int pIndex ) {
      if( pIndex >= 0 && pIndex < mNodes.size() ) {
         return (ObjectName) mNodes.get( pIndex );
      }
      return null;
   }

   /**
   * @return The actual list of Resources which is never null
   *
   **/
   public ObjectName[] getResources() {
      return (ObjectName[]) mResources.toArray( new ObjectName[ 0 ] );
   }

   /**
   * Looks up an Resource with the given index
   *
   * @param pIndex Index of the requested Resource
   *
   * @return Resource found for the given index or null
   *         if index is out of bounds.
   **/
   public ObjectName getResource( int pIndex ) {
      if( pIndex >= 0 && pIndex < mResources.size() ) {
         return (ObjectName) mResources.get( pIndex );
      }
      return null;
   }

   /**
   * @return The actual list of JavaVMs which is never empty
   **/
   public ObjectName[] getJavaVMs() {
      return (ObjectName[]) mJVMs.toArray( new ObjectName[ 0 ] );
   }

   /**
   * Looks up an JVM with the given index
   *
   * @param pIndex Index of the requested JVM
   *
   * @return JVM found for the given index or null
   *         if index is out of bounds.
   **/
   public ObjectName getJavaVM( int pIndex ) {
      if( pIndex >= 0 && pIndex < mJVMs.size() ) {
         return (ObjectName) mJVMs.get( pIndex );
      }
      return null;
   }

   /**
   * @return The Indentifications of the J2EE plattform vendor or
   *         this server.
   **/
   public String getJ2eeVendor() {
      return mJ2eeVendor;
   }
   
   public String toString() {
      return "J2EEServer { " + super.toString() + " } [ " +
         "applications: " + mApplications +
         ", resources: " + mResources +
         ", nodes: " + mNodes +
         ", ports: " + mPorts +
         ", JVMs: " + mJVMs +
         ", J2EE vendor: " + mJ2eeVendor +
         " ]";
   }

}
