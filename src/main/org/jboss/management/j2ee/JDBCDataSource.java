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
 * {@link javax.management.j2ee.JDBCDataSource JDBCDataSource}.
 *
 * @author  <a href="mailto:andreas@jboss.org">Andreas Schaefer</a>.
 * @version $Revision: 1.10 $
 *   
 * <p><b>Revisions:</b>
 *
 * <p><b>20011126 Andreas Schaefer:</b>
 * <ul>
 * <li> Adjustments to the JBoss Guidelines
 * </ul>
 * <p><b>20011206 Andreas Schaefer:</b>
 * <ul>
 * <li> Finishing first real implementation
 * </ul>
 **/
public class JDBCDataSource
   extends J2EEManagedObject
   implements JDBCDataSourceMBean
{
   // Constants -----------------------------------------------------
   
   // Attributes ----------------------------------------------------
   
   private long mStartTime = -1;
   private int mState = ServiceMBean.STOPPED;
   private ObjectName mService;
   private ObjectName mJdbcDriver;
   private Listener mListener;
   
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
      Logger lLog = Logger.getLogger( JDBCDataSource.class );
      ObjectName lServer = null;
      try {
         lServer = (ObjectName) pServer.queryNames(
             new ObjectName( J2EEManagedObject.getDomainName() + ":type=J2EEServer,*" ),
             null
         ).iterator().next();
      }
      catch( Exception e ) {
         lLog.error( "Could not locate JSR-77 Server: " + pName, e );
         // Return because without the JDBC manager go on does not work
         return null;
      }
      // First create its parent the JDBC resource
      ObjectName lJDBC = null;
      try {
         // Check if the JDBC Manager exists and if not create one
         Set lNames = pServer.queryNames(
             new ObjectName( J2EEManagedObject.getDomainName() + ":type=JDBC,*" ),
             null
         );
         if( lNames.isEmpty() ) {
            // Now create the JDBC Manager
            lJDBC = JDBC.create( pServer, "JDBC" );
         } else {
            lJDBC = (ObjectName) lNames.iterator().next();
         }
      }
      catch( Exception e ) {
         lLog.error( "Could not create JSR-77 JDBC Manager", e );
         // Return because without the JDBC manager go on does not work
         return null;
      }
      
      try {
         //AS ToDo: Replace any ':' by '~' do avoid ObjectName conflicts for now
         //AS FixMe: look for a solution
         pName = pName.replace( ':', '~' );
         // Now create the JNDI Representant
         return pServer.createMBean(
            "org.jboss.management.j2ee.JDBCDataSource",
            null,
            new Object[] {
               pName,
               lJDBC,
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
         lLog.error( "Could not create JSR-77 JDBC DataSource: " + pName, e );
         return null;
      }
   }
   
   public static void destroy( MBeanServer pServer, String pName ) {
      Logger lLog = Logger.getLogger( JDBCDataSource.class );
      try {
         // Find the Object to be destroyed
         ObjectName lSearch = new ObjectName(
            J2EEManagedObject.getDomainName() + ":type=JDBCDataSource,name=" + pName + ",*"
         );
         ObjectName lJNDI = (ObjectName) pServer.queryNames(
            lSearch,
            null
         ).iterator().next();
         // Now remove the J2EEApplication
         pServer.unregisterMBean( lJNDI );
         // Now let us try to destroy the JDBC Manager
         JDBC.destroy( pServer, "JDBC" );
      }
      catch( Exception e ) {
         lLog.error( "Could not destroy JSR-77 JDBC DataSource: " + pName, e );
      }
   }
   
   // Constructors --------------------------------------------------
   
   /**
   * @param pName Name of the JDBCDataSource
   *
   * @throws InvalidParameterException If list of nodes or ports was null or empty
   **/
   public JDBCDataSource( String pName, ObjectName pServer, ObjectName pService )
      throws
         MalformedObjectNameException,
         InvalidParentException
   {
      super( "JDBCDataSource", pName, pServer );
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
   
   public void postCreation() {
      try {
         mListener = new Listener();
         getServer().addNotificationListener( mService, mListener, null, null );
      }
      catch( JMException jme ) {
         //AS ToDo: later on we have to define what happens when service is null or
         //AS ToDo: not found.
         getLog().error( "Could not add listener at target service", jme );
      }
      sendNotification(
         new Notification(
            sTypes[ 0 ],
            getName(),
            1,
            System.currentTimeMillis(),
            "JDBC DataSource Resource created"
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
            "JDBC DataSource Resource deleted"
         )
      );
      // Remove the listener of the target MBean
      try {
         getServer().removeNotificationListener( mService, mListener );
      }
      catch( JMException jme ) {
         //AS ToDo: later on we have to define what happens when service is null or
         //AS ToDo: not found.
         jme.printStackTrace();
      }
   }
   
   // javax.management.j2ee.JDBCDataSource implementation -----------------
   
   public ObjectName getJdbcDriver()
   {
      return mJdbcDriver;
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
      return "JDBCDatasource { " + super.toString() + " } [ " +
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
