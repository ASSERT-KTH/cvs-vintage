/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.management.j2ee;

import java.net.URL;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

import javax.management.AttributeChangeNotification;
import javax.management.MalformedObjectNameException;
import javax.management.MBeanServer;
import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.ObjectName;

import java.security.InvalidParameterException;

import org.jboss.logging.Logger;
import org.jboss.system.ServiceMBean;

/**
 * Root class of the JBoss JSR-77 implementation of
 * {@link javax.management.j2ee.ResourceAdapterModule ResourceAdapterModule}.
 *
 * @author  <a href="mailto:mclaugs@comcast.net">Scott McLaughlin</a>.
 * @version $Revision: 1.3 $
 *   
 * <p><b>Revisions:</b>
 *
 * <p><b>20020301 Scott McLaughlin:</b>
 * <ul>
 * <li>
 *      Creation
 * </ul>
 * <p><b>20020301 Andreas Schaefer:</b>
 * <ul>
 * <li>
 *      Convertion to mejb...() methods and using XDoclet to generate MBean interface
 * </ul>
 *
 * @jmx:mbean extends="org.jboss.management.j2ee.StateManageable,org.jboss.management.j2ee.J2EEModuleMBean"
 **/
public class ResourceAdapterModule
  extends J2EEModule
  implements ResourceAdapterModuleMBean
{

   // Constants -----------------------------------------------------
   
   public static final String J2EE_TYPE = "ResourceAdapterModule";
   
   // Attributes ----------------------------------------------------
   
   private List mResourceAdapters = new ArrayList();
   private long mStartTime = -1;
   private int mState = ServiceMBean.STOPPED;
   private ObjectName mService;
   
   // Static --------------------------------------------------------
   
   private static final String[] sTypes = new String[] {
                                             "j2ee.object.created",
                                             "j2ee.object.deleted",
                                             "state.stopped",
                                             "state.stopping",
                                             "state.starting",
                                             "state.running",
                                             "state.failed"
                                          };
   /**
    * @todo Now support for JMVs right now !
    **/
   public static ObjectName create( MBeanServer pServer, String pApplicationName, String pName, URL pURL, ObjectName pService) {
      Logger lLog = Logger.getLogger( ResourceAdapterModule.class );
      String lDD = null;
      ObjectName lParent = null;
      try {
         ObjectName lServer = (ObjectName) pServer.queryNames(
             new ObjectName(
               J2EEManagedObject.getDomainName() + ":" +
               J2EEManagedObject.TYPE  + "=" + J2EEServer.J2EE_TYPE + "," +
               "*"
             ),
             null
         ).iterator().next();
         String lServerName = lServer.getKeyPropertyList().get( J2EEManagedObject.TYPE ) + "=" +
                              lServer.getKeyPropertyList().get( "name" );
         lLog.debug( "ResourceAdapterModule.create(), server name: " + lServerName );
         if(pApplicationName != null)
         {
            lParent = (ObjectName) pServer.queryNames(
               new ObjectName(
                  J2EEManagedObject.getDomainName() + ":" +
                  J2EEManagedObject.TYPE + "=" + J2EEApplication.J2EE_TYPE + "," +
                  "name=" + pApplicationName + "," +
                  lServerName + "," +
                  "*"
                ),
                null
            ).iterator().next();
         }
         else 
         {
            lParent = lServer;
         }
         // First get the deployement descriptor
         lDD = J2EEDeployedObject.getDeploymentDescriptor( pURL, J2EEDeployedObject.RAR );

      }
      catch( Exception e ) {
         lLog.error( "Could not create JSR-77 ResourceAdapterModule: " + pApplicationName, e );
         return null;
      }
      try {
         // Now create the ResourceAdapterModule
         lLog.debug(
            "Create ResourceAdapterModule, name: " + pName +
            ", application: " + lParent +
            ", dd: " + lDD
         );
         return pServer.createMBean(
            "org.jboss.management.j2ee.ResourceAdapterModule",
            null,
            new Object[] {
               pName,
               lParent,
               null,
               lDD,
               pService
            },
            new String[] {
               String.class.getName(),
               ObjectName.class.getName(),
               ObjectName[].class.getName(),
               String.class.getName(),
               ObjectName.class.getName()
            }
         ).getObjectName();
      }
      catch( Exception e ) {
         lLog.error( "Could not create JSR-77 ResourceAdapterModule: " + pApplicationName, e );
         return null;
      }
   }
   
   public static void destroy( MBeanServer pServer, String pModuleName ) {
      Logger lLog = Logger.getLogger( EjbModule.class );
      try {
         // Find the Object to be destroyed
         ObjectName lSearch = new ObjectName(
            J2EEManagedObject.getDomainName() + ":" +
            J2EEManagedObject.TYPE + "=" + ResourceAdapterModule.J2EE_TYPE + "," +
            "name=" + pModuleName + "," +
            "*"
         );
         ObjectName lResourceAdapterModule = (ObjectName) pServer.queryNames(
            lSearch,
            null
         ).iterator().next();
         // Now remove the ResourceAdapterModule
         pServer.unregisterMBean( lResourceAdapterModule );
      }
      catch( Exception e ) {
         lLog.error( "Could not destory JSR-77 ResourceAdapterModule: " + pModuleName, e );
      }
   }
   
   // Constructors --------------------------------------------------
   
   /**
   * Constructor taking the Name of this Object
   *
   * @param pName Name to be set which must not be null
   * @param pDeploymentDescriptor
   *
   * @throws InvalidParameterException If the given Name is null
   **/
   public ResourceAdapterModule( String pName, ObjectName pApplication, ObjectName[] pJVMs, String pDeploymentDescriptor, ObjectName pService )
      throws
         MalformedObjectNameException,
         InvalidParentException
   {
      super( J2EE_TYPE, pName, pApplication, pJVMs, pDeploymentDescriptor );
      mService = pService;
   }

   // Public --------------------------------------------------------
   
   // EjbModule implementation --------------------------------------
   
   /**
    * @jmx:managed-attribute
    **/
   public ObjectName[] getResourceAdapters() {
      return (ObjectName[]) mResourceAdapters.toArray( new ObjectName[ 0 ] );
   }
   
   /**
    * @jmx:managed-operation
    **/
   public ObjectName getResourceAdapter( int pIndex ) {
      if( pIndex >= 0 && pIndex < mResourceAdapters.size() )
      {
         return (ObjectName) mResourceAdapters.get( pIndex );
      }
      else
      {
         return null;
      }
   }
   
   // J2EEManagedObjectMBean implementation -------------------------
   
   public void addChild( ObjectName pChild ) {
      String lType = J2EEManagedObject.getType( pChild );
      if( ResourceAdapter.J2EE_TYPE.equals( lType ))
      {
         mResourceAdapters.add( pChild );
      }
   }
   
   public void removeChild( ObjectName pChild ) {
      String lType = J2EEManagedObject.getType( pChild );
      if( ResourceAdapter.J2EE_TYPE.equals( lType )) 
      {
         mResourceAdapters.remove( pChild );
      }
   }

   // org.jboss.ServiceMBean overrides ------------------------------------

   public void postCreation() {
      sendNotification(
         new Notification(
            sTypes[ 0 ],
            getName(),
            1,
            System.currentTimeMillis(),
            "Resource Adapter Module created"
         )
      );
   }

   public void preDestruction() {
      sendNotification(
         new Notification(
            sTypes[ 1 ],
            getName(),
            1,
            System.currentTimeMillis(),
            "Resource Adapter Module deleted"
         )
      );
   }
   
   // org.jboss.managment.j2ee.EventProvider implementation -------------
   
   public String[] getEventTypes() {
      return sTypes;
   }
   
   public String getEventType( int pIndex ) {
      if( pIndex >= 0 && pIndex < sTypes.length ) {
         return sTypes[ pIndex ];
      } else {
         return null;
      }
   }
   
   // javax.management.j2ee.StateManageable implementation ----------
   
   public long getStartTime() {
      return mStartTime;
   }
   
   public int getState() {
      return mState;
   }
   
   public void mejbStart()
   {
      try {
         start();
      }
      catch( Exception e ) {
         getLog().error( "start failed", e );
      }
   }
   
   public void mejbStartRecursive() {
      // No recursive start here
      try {
         start();
      }
      catch( Exception e ) {
         getLog().error( "start failed", e );
      }
   }
   
   public void mejbStop() {
      stop();
   }
   
   // Object overrides ---------------------------------------------------
   
   public String toString() {
      return "ResourceAdapterModule[ " + super.toString() +
         "ResourceAdapters: " + mResourceAdapters +
         " ]";
   }
   
   // Package protected ---------------------------------------------
   
   // Protected -----------------------------------------------------
   
   // Private -------------------------------------------------------
   
   // Inner classes -------------------------------------------------
   
   private class Listener implements NotificationListener {
      
      public void handleNotification( Notification pNotification, Object pHandback )
      {
         if( pNotification instanceof AttributeChangeNotification ) {
            AttributeChangeNotification lChange = (AttributeChangeNotification) pNotification;
            if( "State".equals( lChange.getAttributeName() ) )
            {
               mState = ( (Integer) lChange.getNewValue() ).intValue();
               if( mState == ServiceMBean.STARTED ) {
                  mStartTime = lChange.getTimeStamp();
               } else {
                  mStartTime = -1;
               }
               // Now send the event to the JSR-77 listeners
               sendNotification(
                  new Notification(
                     sTypes[ getState() + 2 ],
                     getName(),
                     1,
                     System.currentTimeMillis(),
                     "State changed"
                  )
               );
            }
         }
      }
      
   }
}

