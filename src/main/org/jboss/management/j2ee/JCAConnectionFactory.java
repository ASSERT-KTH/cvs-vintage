/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.management.j2ee;

import java.util.Set;

import javax.management.AttributeChangeNotification;
import javax.management.JMException;
import javax.management.MalformedObjectNameException;
import javax.management.MBeanServer;
import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.ObjectName;

import org.jboss.logging.Logger;
import org.jboss.system.ServiceMBean;

/**
 * Root class of the JBoss JSR-77 implementation of
 * {@link javax.management.j2ee.JCAConnectionFactory JCAConnectionFactory}.
 *
 * @author  <a href="mailto:mclaugs@comcast.net">Scott McLaughlin</a>.
 * @version $Revision: 1.1 $
 *   
 * <p><b>Revisions:</b>
 *
 * <p><b>20020303 Scott McLaughlin:</b>
 * <ul>
 * <li> Finishing first real implementation
 * </ul>
 **/
public class JCAConnectionFactory extends J2EEManagedObject implements JCAConnectionFactoryMBean
{
   // Constants -----------------------------------------------------
   
   // Attributes ----------------------------------------------------
   
   private long mStartTime = -1;
   private int mState = ServiceMBean.STOPPED;
   private ObjectName mService;
   private ObjectName mManagedConnectionFactory;
   
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
   
   public static ObjectName create( MBeanServer pServer, String pName,ObjectName pService ) {
      Logger lLog = Logger.getLogger( JCAConnectionFactory.class );
      ObjectName lServer = null;
      try {
         lServer = (ObjectName) pServer.queryNames(
             new ObjectName( J2EEManagedObject.getDomainName() + ":type=J2EEServer,*" ),
             null
         ).iterator().next();
      }
      catch( Exception e ) {
         lLog.error( "Could not locate JSR-77 Server: " + pName, e );
         return null;
      }
      // First create its parent, the JCA resource
      ObjectName lJCAResource = null;
      try {
         // Check if the JCA Resource exists and if not create one
         Set lNames = pServer.queryNames(
             new ObjectName( J2EEManagedObject.getDomainName() + ":type=JCAResource,*" ),
             null
         );
         if( lNames.isEmpty() ) {
            // Now create the JCA resource
            lJCAResource = JCAResource.create( pServer, "JCA" );
         } else {
            lJCAResource = (ObjectName) lNames.iterator().next();
         }
      }
      catch( Exception e ) {
         lLog.error( "Could not create JSR-77 JCA resource", e );
         return null;
      }
      
      try {
         return pServer.createMBean(
            "org.jboss.management.j2ee.JCAConnectionFactory",
            null,
            new Object[] {
               pName,
               lJCAResource,
               pService
            },
            new String[] {
               String.class.getName(),
               ObjectName.class.getName(),
               ObjectName.class.getName()
            }
         ).getObjectName();
         
      }
      catch( Exception e ) {
         lLog.error( "Could not create JSR-77 JCAConnectionFactory: " + pName, e );
         return null;
      }
   }
   
   public static void destroy( MBeanServer pServer, String pName ) {
      Logger lLog = Logger.getLogger( JCAConnectionFactory.class );
      try {
         // Find the Object to be destroyed
         ObjectName lSearch = new ObjectName(
            J2EEManagedObject.getDomainName() + ":type=JCAConnectionFactory,name=" + pName + ",*"
         );
         ObjectName lJCAConnectionFactory = (ObjectName) pServer.queryNames(
            lSearch,
            null
         ).iterator().next();
         // Now remove the JCA Connection Factory  
         pServer.unregisterMBean( lJCAConnectionFactory );
         // Now let us try to destroy the JDBC Manager
         JCAResource.destroy( pServer, "JCA" );
      }
      catch( Exception e ) {
         lLog.error( "Could not destroy JSR-77 JCAConnectionFactory: " + pName, e );
      }
   }
   
   // Constructors --------------------------------------------------
   
   /**
   * @param pName Name of the JCAConnectionFactory 
   *
   * @throws InvalidParameterException If list of nodes or ports was null or empty
   **/
   public JCAConnectionFactory(String pName, ObjectName pServer, ObjectName pService) throws MalformedObjectNameException, InvalidParentException
   {
      super( "JCAConnectionFactory", pName, pServer );
      mService = pService;
   }
   
   // Public --------------------------------------------------------
   
   // javax.managment.j2ee.EventProvider implementation -------------
   
   public String[] getTypes() {
      return sTypes;
   }
   
   public String getType( int pIndex ) {
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
   
   /**
    * This method is only overwriten because to catch the exception
    * which is not specified in {@link javax.management.j2ee.StateManageable
    * StateManageable} interface.
    **/
   public void start()
   {
      try {
         super.start();
      }
      catch( Exception e ) {
         getLog().error( "start failed", e );
      }
   }
   
   public void startRecursive() {
      // No recursive start here
      try {
         start();
      }
      catch( Exception e ) {
         getLog().error( "start failed", e );
      }
   }

   // javax.management.j2ee.JCAConnectionFactory implementation -----------------
   
   public ObjectName getManagedConnectionFactory()
   {
      return mManagedConnectionFactory;
   }
   
   // org.jboss.ServiceMBean overrides ------------------------------------
   
   public void postCreation() {
      // If set then register for its events
      try {
         getServer().addNotificationListener( mService, new Listener(), null, null );
      }
      catch( JMException jme ) {
         //AS ToDo: later on we have to define what happens when service is null or
         //AS ToDo: not found.
         jme.printStackTrace();
      }
      sendNotification(
         new Notification(
            sTypes[ 0 ],
            getName(),
            1,
            System.currentTimeMillis(),
            "JCA Connection Factory created"
         )
      );
   }
   
   public void preDestruction() {
      Logger lLog = getLog();
      if( lLog.isInfoEnabled() ) {
         lLog.info( "JCAConnectionFactory.preDeregister(): " + getName() );
      }
      sendNotification(
         new Notification(
            sTypes[ 1 ],
            getName(),
            1,
            System.currentTimeMillis(),
            "JCA Connection Factory deleted"
         )
      );
   }
   
   // ServiceMBeanSupport overrides ---------------------------------
   
   public void startService() {
      try {
         getServer().invoke(
            mService,
            "start",
            new Object[] {},
            new String[] {}
         );
      }
      catch( JMException jme ) {
         //AS ToDo: later on we have to define what happens when service could not be started
         jme.printStackTrace();
      }
   }
   
   public void stopService() {
      try {
         getServer().invoke(
            mService,
            "stop",
            new Object[] {},
            new String[] {}
         );
      }
      catch( JMException jme ) {
         //AS ToDo: later on we have to define what happens when service could not be stopped
         jme.printStackTrace();
      }
   }
   
   // java.lang.Object overrides ------------------------------------
   
   public String toString() {
      return "JCAConnectionFactory { " + super.toString() + " } [ " +
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
