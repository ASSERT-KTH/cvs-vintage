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
 * JBoss implementation of the JSR-77 {@link javax.management.j2ee.J2EEServer
 * J2EEServer}.
 *
 * @author <a href="mailto:andreas@jboss.org">Andreas Schaefer</a>
 * @version $Revision: 1.7 $
 *   
 * <p><b>Revisions:</b>
 *
 * <p><b>20011123 Andreas Schaefer:</b>
 * <ul>
 * <li> Adjustments to the JBoss Guidelines and adding of the
 *      {@link #removeChild removeChild()} implementation.
 * </ul>
 **/
public class J2EEServer
  extends J2EEManagedObject
  implements J2EEServerMBean
{
   // Constants -----------------------------------------------------
   
   // Attributes ----------------------------------------------------
   
   private List mDeployedObjects = new ArrayList();
   
   private List mResources = new ArrayList();
   
   private List mNodes = new ArrayList();
   
   private List mJVMs = new ArrayList();
   
   private String mServerVendor = null;
   
   // Static --------------------------------------------------------
   
   // Constructors --------------------------------------------------
   
   public J2EEServer( String pName, ObjectName pDomain, String pServerVendor )
      throws
         MalformedObjectNameException,
         InvalidParentException
   {
      super( "J2EEServer", pName, pDomain );
      mServerVendor = pServerVendor;
   }
   
   // Public --------------------------------------------------------
   
   public ObjectName[] getDeployedObjects()
   {
      return (ObjectName[]) mDeployedObjects.toArray( new ObjectName[ 0 ] );
   }
   
   public ObjectName getDeployedObject( int pIndex )
   {
      if( pIndex >= 0 && pIndex < mDeployedObjects.size() ) {
         return (ObjectName) mDeployedObjects.get( pIndex );
      }
      return null;
   }
   
   public ObjectName[] getResources() {
      return (ObjectName[]) mResources.toArray( new ObjectName[ 0 ] );
   }
   
   public ObjectName getResource( int pIndex ) {
      if( pIndex >= 0 && pIndex < mResources.size() ) {
         return (ObjectName) mResources.get( pIndex );
      }
      return null;
   }
   
   public ObjectName[] getNodes() {
      return (ObjectName[]) mNodes.toArray( new ObjectName[ 0 ] );
   }
   
   public ObjectName getNode( int pIndex ) {
      if( pIndex >= 0 && pIndex < mNodes.size() ) {
         return (ObjectName) mNodes.get( pIndex );
      }
      return null;
   }
   
   public ObjectName[] getJavaVMs() {
      return (ObjectName[]) mJVMs.toArray( new ObjectName[ 0 ] );
   }
   
   public ObjectName getJavaVM( int pIndex ) {
      if( pIndex >= 0 && pIndex < mJVMs.size() ) {
         return (ObjectName) mJVMs.get( pIndex );
      }
      return null;
   }
   
   public String getServerVendor() {
      return mServerVendor;
   }
   
   public void addChild( ObjectName pChild ) {
      String lType = J2EEManagedObject.getType( pChild );
      if(
         "J2EEApplication".equals( lType ) ||
         "J2EEModule".equals( lType ) ||
         "EjbModule".equals( lType ) ||
         "ConnectorModule".equals( lType ) ||
         "WebModule".equals( lType )
      ) {
         mDeployedObjects.add( pChild );
      } else if( "Node".equals( lType ) ) {
         mNodes.add( pChild );
      } else if( "JVM".equals( lType ) ) {
         mJVMs.add( pChild );
      } else
      if( "JNDI".equals( lType ) ||
         "JMS".equals( lType ) ||
         "URL".equals( lType ) ||
         "JTA".equals( lType ) ||
         "JavaMail".equals( lType ) ||
         "JDBC".equals( lType ) ||
         "RMI IIOP".equals( lType )
      ) {
         mResources.add( pChild );
      }
   }
   
   public void removeChild( ObjectName pChild ) {
      String lType = J2EEManagedObject.getType( pChild );
      if(
         "J2EEApplication".equals( lType ) ||
         "J2EEModule".equals( lType ) ||
         "EjbModule".equals( lType ) ||
         "ConnectorModule".equals( lType ) ||
         "WebModule".equals( lType )
      ) {
         mDeployedObjects.remove( pChild );
      } else if( "Node".equals( lType ) ) {
         mNodes.remove( pChild );
      } else if( "JVM".equals( lType ) ) {
         mJVMs.remove( pChild );
      } else
      if( "JNDI".equals( lType ) ||
         "JMS".equals( lType ) ||
         "URL".equals( lType ) ||
         "JTA".equals( lType ) ||
         "JavaMail".equals( lType ) ||
         "JDBC".equals( lType ) ||
         "RMI IIOP".equals( lType )
      ) {
         mResources.remove( pChild );
      }
   }

   public String toString() {
      return "J2EEServer { " + super.toString() + " } [ " +
         "depoyed objects: " + mDeployedObjects +
         ", resources: " + mResources +
         ", nodes: " + mNodes +
         ", JVMs: " + mJVMs +
         ", J2EE vendor: " + mServerVendor +
         " ]";
   }

   // Z implementation ----------------------------------------------
   
   // Y overrides ---------------------------------------------------
   
   // Package protected ---------------------------------------------
   
   // Protected -----------------------------------------------------
   
   // Private -------------------------------------------------------
   
   // Inner classes -------------------------------------------------
}
