/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.management.j2ee;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
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
 * {@link javax.management.j2ee.JDBC JDBC}.
 *
 * @author  <a href="mailto:andreas@jboss.org">Andreas Schaefer</a>.
 * @version $Revision: 1.8 $
 *   
 * <p><b>Revisions:</b>
 *
 * <p><b>20011126 Andreas Schaefer:</b>
 * <ul>
 * <li> Creation
 * </ul>
 *
 * @todo This resource should not implement state manageable because it
 *       has no MBean/Service associated but codes stays.
 **/
public class JDBC
   extends J2EEResource
   implements JDBCMBean
{
   // Constants -----------------------------------------------------
   
   // Attributes ----------------------------------------------------
   
   private long mStartTime = -1;
   private int mState = ServiceMBean.STOPPED;
   private ObjectName mService;
   private Listener mListener;
   
   private List mDatasources = new ArrayList();
   
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
   
   public static ObjectName create( MBeanServer pServer, String pName ) {
      Logger lLog = Logger.getLogger( JDBC.class );
      ObjectName lServer = null;
      try {
         lServer = (ObjectName) pServer.queryNames(
             new ObjectName( J2EEManagedObject.getDomainName() + ":type=J2EEServer,*" ),
             null
         ).iterator().next();
      }
      catch( Exception e ) {
         lLog.error( "Could not find parent J2EEServer", e );
         return null;
      }
      try {
         // Now create the JDBC Representant
         return pServer.createMBean(
            "org.jboss.management.j2ee.JDBC",
            null,
            new Object[] {
               pName,
               lServer
            },
            new String[] {
               String.class.getName(),
               ObjectName.class.getName()
            }
         ).getObjectName();
      }
      catch( Exception e ) {
         lLog.error( "Could not create JSR-77 JDBC Manager", e );
         return null;
      }
   }
   
   public static void destroy( MBeanServer pServer, String pName ) {
      Logger lLog = Logger.getLogger( JNDI.class );
      try {
         // Find the Object to be destroyed
         ObjectName lSearch = new ObjectName(
            J2EEManagedObject.getDomainName() + ":type=JDBC,name=" + pName + ",*"
         );
         Set lNames = pServer.queryNames(
            lSearch,
            null
         );
         if( !lNames.isEmpty() ) {
            ObjectName lJDBC = (ObjectName) lNames.iterator().next();
            // Now check if the JDBC Manager does not contains another DataSources
            ObjectName[] lDataSources = (ObjectName[]) pServer.getAttribute(
               lJDBC,
               "JdbcDataSources"
            );
            if( lDataSources.length == 0 ) {
               // Remove it because it does not reference any JDBC DataSources
               pServer.unregisterMBean( lJDBC );
            }
         }
      }
      catch( Exception e ) {
       lLog.error( "Could not destroy JSR-77 JDBC Manager", e );
      }
   }
   
   // Constructors --------------------------------------------------
   
   /**
    * @param pName Name of the JDBC
    *
    * @throws InvalidParameterException If list of nodes or ports was null or empty
    **/
   public JDBC( String pName, ObjectName pServer )
      throws
         MalformedObjectNameException,
         InvalidParentException
   {
      super( "JDBC", pName, pServer );
//AS      mService = pService;
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

   public void startService() {
      mState = ServiceMBean.STARTING;
      sendNotification(
         new Notification(
            sTypes[ 4 ],
            getName(),
            1,
            System.currentTimeMillis(),
            "JDBC Manager starting"
         )
      );
      mState = ServiceMBean.STARTED;
      sendNotification(
         new Notification(
            sTypes[ 5 ],
            getName(),
            2,
            System.currentTimeMillis(),
            "JDBC Manager started"
         )
      );
   }

   public void startRecursive() {
      start();
      Iterator i = mDatasources.iterator();
      ObjectName lDataSource = null;
      while( i.hasNext() ) {
         lDataSource = (ObjectName) i.next();
         try {
            getServer().invoke(
               lDataSource,
               "start",
               new Object[] {},
               new String[] {}
            );
         }
         catch( JMException jme ) {
            getLog().error( "Could not start JSR-77 JDBC-DataSource: " + lDataSource, jme );
         }
      }
   }

   public void stopService() {
      Iterator i = mDatasources.iterator();
      mState = ServiceMBean.STOPPING;
      sendNotification(
         new Notification(
            sTypes[ 2 ],
            getName(),
            1,
            System.currentTimeMillis(),
            "JDBC Manager stopping"
         )
      );
      while( i.hasNext() ) {
         ObjectName lDataSource = (ObjectName) i.next();
         try {
            getServer().invoke(
               lDataSource,
               "stop",
               new Object[] {},
               new String[] {}
            );
         }
         catch( JMException jme ) {
            getLog().error( "Could not stop JSR-77 JDBC-DataSource: " + lDataSource, jme );
         }
      }
      mState = ServiceMBean.STOPPED;
      sendNotification(
         new Notification(
            sTypes[ 3 ],
            getName(),
            2,
            System.currentTimeMillis(),
            "JDBC Manager stopped"
         )
      );
   }
   
   /**
    * @todo Listener cannot be used right now because there is no MBean associated
    *       to it and therefore no state management possible but currently it stays
    *       StateManageable to save the code.
    **/
   public void postCreation() {
/*AS 
      try {
         mListener = new Listener();
         getServer().addNotificationListener( mService, mListener, null, null );
      }
      catch( JMException jme ) {
         //AS ToDo: later on we have to define what happens when service is null or
         //AS ToDo: not found.
         getLog().error( "Could not add listener at target service", jme );
      }
*/
      sendNotification(
         new Notification(
            sTypes[ 0 ],
            getName(),
            1,
            System.currentTimeMillis(),
            "JDBC Resource created"
         )
      );
   }
   
   /**
    * @todo Listener cannot be used right now because there is no MBean associated
    *       to it and therefore no state management possible but currently it stays
    *       StateManageable to save the code.
    **/
   public void preDestruction() {
      sendNotification(
         new Notification(
            sTypes[ 1 ],
            getName(),
            1,
            System.currentTimeMillis(),
            "JDBC Resource deleted"
         )
      );
/*AS
      // Remove the listener of the target MBean
      try {
         getServer().removeNotificationListener( mService, mListener );
      }
      catch( JMException jme ) {
         //AS ToDo: later on we have to define what happens when service is null or
         //AS ToDo: not found.
         jme.printStackTrace();
      }
*/
   }
   
   // javax.management.j2ee.JDBC implementation ---------------------
   
   public ObjectName[] getJdbcDataSources() {
      return (ObjectName[]) mDatasources.toArray( new ObjectName[ mDatasources.size() ] );
   }
   
   public ObjectName getJdbcDataSource( int pIndex ) {
      if( pIndex >= 0 && pIndex < mDatasources.size() ) {
         return (ObjectName) mDatasources.get( pIndex );
      }
      else {
         return null;
      }
   }
   
   // J2EEManagedObjectMBean implementation -------------------------
   
   public void addChild( ObjectName pChild ) {
      String lType = J2EEManagedObject.getType( pChild );
      if( "JDBCDataSource".equals( lType ) ) {
         mDatasources.add( pChild );
      }
   }
   
   public void removeChild( ObjectName pChild ) {
      String lType = J2EEManagedObject.getType( pChild );
      if( "JDBCDataSource".equals( lType ) ) {
         mDatasources.remove( pChild );
      }
   }

   // java.lang.Object overrides ------------------------------------
   
   public String toString() {
      return "JDBC { " + super.toString() + " } [ " +
         "Datasources: " + mDatasources +
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
