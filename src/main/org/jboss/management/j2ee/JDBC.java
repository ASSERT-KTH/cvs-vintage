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
 * @version $Revision: 1.5 $
 *   
 * <p><b>Revisions:</b>
 *
 * <p><b>20011126 Andreas Schaefer:</b>
 * <ul>
 * <li> Creation
 * </ul>
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
      Logger lLog = Logger.getLogger( JNDI.class );
      ObjectName lServer = null;
      try {
         lServer = (ObjectName) pServer.queryNames(
             new ObjectName( J2EEManagedObject.getDomainName() + ":type=J2EEServer,*" ),
             null
         ).iterator().next();
      }
      catch( Exception e ) {
         lLog.error( "Could not create JSR-77 JDBC Manager", e );
         return null;
      }
      try {
         // Now create the JNDI Representant
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
               "DataSources"
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
      // No recursive start here
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
            getLog().error( "Could not stop JSR-77 JDBC-DataSource: " + lDataSource, jme );
         }
      }
   }

   public void stopService() {
      Iterator i = mDatasources.iterator();
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
   
   // org.jboss.ServiceMBean overrides ------------------------------------
   
   public void postRegister( Boolean pRegisterationDone ) {
      super.postRegister( pRegisterationDone );
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
   
   public void preDeregister() {
      sendNotification(
         new Notification(
            sTypes[ 1 ],
            getName(),
            1,
            System.currentTimeMillis(),
            "JDBC Resource deleted"
         )
      );
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
}
