/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.management.j2ee;

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
 * {@link javax.management.j2ee.JNDI JNDI}.
 *
 * @author  <a href="mailto:andreas@jboss.org">Andreas Schaefer</a>.
 * @version $Revision: 1.6 $
 *   
 * <p><b>Revisions:</b>
 *
 * <p><b>20011126 Andreas Schaefer:</b>
 * <ul>
 * <li> Adjustments to the JBoss Guidelines
 * </ul>
 * <p><b>20011202 Andreas Schaefer:</b>
 * <ul>
 * <li> Added state handling (except event notification)
 * </ul>
 **/
public class JNDI
   extends J2EEResource
   implements JNDIMBean
{
   // Constants -----------------------------------------------------
   
   // Attributes ----------------------------------------------------
   
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
   
   public static ObjectName create( MBeanServer pServer, String pName, ObjectName pService ) {
      Logger lLog = Logger.getLogger( JNDI.class );
      ObjectName lServer = null;
      try {
         lServer = (ObjectName) pServer.queryNames(
             new ObjectName( J2EEManagedObject.getDomainName() + ":type=J2EEServer,*" ),
             null
         ).iterator().next();
      }
      catch( Exception e ) {
//AS         lLog.error( "Could not create JSR-77 JNDI: " + pName, e );
         return null;
      }
      try {
         // Now create the JNDI Representant
         return pServer.createMBean(
            "org.jboss.management.j2ee.JNDI",
            null,
            new Object[] {
               pName,
               lServer,
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
//AS         lLog.error( "Could not create JSR-77 JNDI: " + pName, e );
         return null;
      }
   }
   
   public static void destroy( MBeanServer pServer, String pName ) {
      Logger lLog = Logger.getLogger( JNDI.class );
      try {
         // Find the Object to be destroyed
         ObjectName lSearch = new ObjectName(
            J2EEManagedObject.getDomainName() + ":type=JNDI,name=" + pName + ",*"
         );
         ObjectName lJNDI = (ObjectName) pServer.queryNames(
            lSearch,
            null
         ).iterator().next();
         // Now remove the J2EEApplication
         pServer.unregisterMBean( lJNDI );
      }
      catch( Exception e ) {
//AS         lLog.error( "Could not destroy JSR-77 JNDI: " + pName, e );
      }
   }
   
   // Constructors --------------------------------------------------
   
   /**
    * @param pName Name of the JNDI
    *
    * @throws InvalidParameterException If list of nodes or ports was null or empty
    **/
   public JNDI( String pName, ObjectName pServer, ObjectName pService )
      throws
         MalformedObjectNameException,
         InvalidParentException
   {
      super( "JNDI", pName, pServer );
      getLog().info( "Service name: " + pService );
      mService = pService;
   }
   
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

   public void startRecursive() {
      // No recursive start here
      start();
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
         //AS ToDo: later on we have to define what happens when service could not be started
         jme.printStackTrace();
      }
   }
   
   // org.jboss.ServiceMBean overrides ------------------------------------
   
   public void postRegister( Boolean pRegisterationDone ) {
      super.postRegister( pRegisterationDone );
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
         new J2EEManagementEvent(
            sTypes[ 0 ],
            getName(),
            1,
            System.currentTimeMillis(),
            "JNDI Resource created"
         ).getNotification()
      );
   }
   
   public void preDeregister() {
      sendNotification(
         new J2EEManagementEvent(
            sTypes[ 1 ],
            getName(),
            1,
            System.currentTimeMillis(),
            "JNDI Resource deleted"
         ).getNotification()
      );
   }
   
   // java.lang.Object overrides ------------------------------------
   
   public String toString() {
      return "JNDI { " + super.toString() + " } [ " +
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
                  new J2EEManagementEvent(
                     sTypes[ getState() + 2 ],
                     getName(),
                     1,
                     System.currentTimeMillis(),
                     "State changed"
                  ).getNotification()
               );
            }
         }
      }
      
   }
   
}
