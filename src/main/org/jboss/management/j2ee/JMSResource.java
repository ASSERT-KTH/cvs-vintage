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
 * {@link javax.management.j2ee.JMS JMS}.  *
 * @author  <a href="mailto:mclaugs@comcast.net">Scott McLaughlin</a>.
 * @version $Revision: 1.3 $
 *
 * <p><b>Revisions:</b>
 *
 * <p><b>20020301 Scott McLaughlin</b>
 * <ul>
 * <li> Creation 
 * </ul>
 *
 * @jmx:mbean extends="org.jboss.management.j2ee.StateManageable,org.jboss.management.j2ee.J2EEResourceMBean"
 **/
public class JMSResource
   extends J2EEResource
   implements JMSResourceMBean
{
   
   // Constants -----------------------------------------------------
   
   public static final String J2EE_TYPE = "JMSResource";
   
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
      Logger lLog = Logger.getLogger( JMSResource.class );
      ObjectName lServer = null;
      try {
         lServer = (ObjectName) pServer.queryNames(
            new ObjectName(
               J2EEManagedObject.getDomainName() + ":" +
               J2EEManagedObject.TYPE + "=" + J2EEServer.J2EE_TYPE + "," +
               "*"
            ),
            null
         ).iterator().next();
      }
      catch( Exception e ) {
         lLog.error( "Could not create JSR-77 Server", e );
         return null;
      }
      try {
         // Now create the JMS Representant
         return pServer.createMBean(
            "org.jboss.management.j2ee.JMSResource",
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
         lLog.error( "Could not create JSR-77 JMS Resouce", e );
         return null;
      }
   }
   
   public static void destroy( MBeanServer pServer, String pName ) {
      Logger lLog = Logger.getLogger( JMSResource.class );
      try {
         // Find the Object to be destroyed
         ObjectName lSearch = new ObjectName(
            J2EEManagedObject.getDomainName() + ":" +
            J2EEManagedObject.TYPE + "=" + JMSResource.J2EE_TYPE + "," +
            "name=" + pName + "," +
            "*"
         );
         ObjectName lJMSResource = (ObjectName) pServer.queryNames(
            lSearch,
            null
         ).iterator().next();
         // Now remove the J2EEApplication
         pServer.unregisterMBean( lJMSResource );
      }
      catch( Exception e ) {
       lLog.error( "Could not destroy JSR-77 JMSResource Resource", e );
      }
   }
   
   // -------------------------------------------------------------------------
   // Constructors
   // -------------------------------------------------------------------------

   /**
    * @param pName Name of the JMSResource
    *
    * @throws InvalidParameterException If list of nodes or ports was null or empty
    **/
   public JMSResource( String pName, ObjectName pServer, ObjectName pService )
      throws
         MalformedObjectNameException,
         InvalidParentException
   {
      super( J2EE_TYPE, pName, pServer );
      mService = pService;
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
            "JMSResource Resource created"
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
            "JMSResource Resource deleted"
         )
      );
   }
   
   // javax.managment.j2ee.EventProvider implementation -------------
   
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
      mejbStart();
   }
   
   public void mejbStop() {
      stop();
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
      return "JMSResource { " + super.toString() + " } [ " +
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

