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
 * {@link javax.management.j2ee.JCAResource JCAResource}.
 *
 * @author  <a href="mailto:mclaugs@comcast.com">Scott McLaughlin</a>.
 * @version $Revision: 1.1 $
 *   
 * <p><b>Revisions:</b>
 *
 * <p><b>20020303 Scott McLaughlin:</b>
 * <ul>
 * <li> Creation
 * </ul>
 **/
public class JCAResource extends J2EEResource implements JCAResourceMBean
{
   // Constants -----------------------------------------------------
   
   // Attributes ----------------------------------------------------
   
   private long mStartTime = -1;
   private int mState = ServiceMBean.STOPPED;
   private ObjectName mService;
   
   private List mConnectionFactories = new ArrayList();
   
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
      Logger lLog = Logger.getLogger( JCAResource.class );
      ObjectName lServer = null;
      try {
         lServer = (ObjectName) pServer.queryNames(
             new ObjectName( J2EEManagedObject.getDomainName() + ":type=J2EEServer,*" ),
             null
         ).iterator().next();
      }
      catch( Exception e ) {
         lLog.error( "Could not create JSR-77 JCAResource", e );
         return null;
      }
      try {
         // Now create the JNDI Representant
         return pServer.createMBean(
            "org.jboss.management.j2ee.JCAResource",
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
         lLog.error( "Could not create JSR-77 JCAResource", e );
         return null;
      }
   }
   
   public static void destroy( MBeanServer pServer, String pName ) {
      Logger lLog = Logger.getLogger( JCAResource.class );
      try {
         // Find the Object to be destroyed
         ObjectName lSearch = new ObjectName(
            J2EEManagedObject.getDomainName() + ":type=JCAResource,name=" + pName + ",*"
         );
         Set lNames = pServer.queryNames(
            lSearch,
            null
         );
         if( !lNames.isEmpty() ) {
            ObjectName lJCAResource = (ObjectName) lNames.iterator().next();
            // Now check if the JCAResource does not contains another Connection Factory
            ObjectName[] lConnectionFactories = (ObjectName[]) pServer.getAttribute(
               lJCAResource,
               "ConnectionFactories"
            );
            if( lConnectionFactories.length == 0 ) {
               // Remove it because it does not reference any JDBC DataSources
               pServer.unregisterMBean( lJCAResource );
            }
         }
      }
      catch( Exception e ) {
       lLog.error( "Could not destroy JSR-77 JCAResource", e );
      }
   }
   
   // Constructors --------------------------------------------------
   
   /**
    * @param pName Name of the JDBC
    *
    * @throws InvalidParameterException If list of nodes or ports was null or empty
    **/
   public JCAResource(String pName, ObjectName pServer) throws MalformedObjectNameException, InvalidParentException
   {
      super( "JCAResource", pName, pServer );
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
            "JCAResource starting"
         )
      );
      mState = ServiceMBean.STARTED;
      sendNotification(
         new Notification(
            sTypes[ 5 ],
            getName(),
            2,
            System.currentTimeMillis(),
            "JCAResource started"
         )
      );
   }

   public void startRecursive() {
      // No recursive start here
      start();
      Iterator i = mConnectionFactories.iterator();
      ObjectName lJCAResource = null;
      while( i.hasNext() ) {
         lJCAResource = (ObjectName) i.next();
         try {
            getServer().invoke(
               lJCAResource,
               "start",
               new Object[] {},
               new String[] {}
            );
         }
         catch( JMException jme ) {
            getLog().error( "Could not stop JSR-77 JCAResource: " + lJCAResource, jme );
         }
      }
   }

   public void stopService() {
      Iterator i = mConnectionFactories.iterator();
      while( i.hasNext() ) {
         ObjectName lJCAResource = (ObjectName) i.next();
         try {
            getServer().invoke(
               lJCAResource,
               "stop",
               new Object[] {},
               new String[] {}
            );
         }
         catch( JMException jme ) {
            getLog().error( "Could not stop JSR-77 JCAResource: " + lJCAResource, jme );
         }
      }
      mState = ServiceMBean.STOPPING;
      sendNotification(
         new Notification(
            sTypes[ 2 ],
            getName(),
            1,
            System.currentTimeMillis(),
            "JCAResource stopping"
         )
      );
      mState = ServiceMBean.STOPPED;
      sendNotification(
         new Notification(
            sTypes[ 3 ],
            getName(),
            2,
            System.currentTimeMillis(),
            "JCAResource stopped"
         )
      );
   }
   
   // org.jboss.ServiceMBean overrides ------------------------------------
   
   public void postCreation() {
      sendNotification(
         new Notification(
            sTypes[ 0 ],
            getName(),
            1,
            System.currentTimeMillis(),
            "JCA Resource created"
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
            "JCA Resource deleted"
         )
      );
   }
   
   // javax.management.j2ee.JDBC implementation ---------------------
   
   public ObjectName[] getConnectionFactories() {
      return (ObjectName[]) mConnectionFactories.toArray( new ObjectName[ mConnectionFactories.size() ] );
   }
   
   public ObjectName getConnectionFactory( int pIndex ) {
      if( pIndex >= 0 && pIndex < mConnectionFactories.size() ) {
         return (ObjectName) mConnectionFactories.get( pIndex );
      }
      else {
         return null;
      }
   }
   
   // J2EEManagedObjectMBean implementation -------------------------
   
   public void addChild( ObjectName pChild ) {
      String lType = J2EEManagedObject.getType( pChild );
      if( "JCAConnectionFactory".equals( lType ) ) {
         mConnectionFactories.add( pChild );
      }
   }
   
   public void removeChild( ObjectName pChild ) {
      String lType = J2EEManagedObject.getType( pChild );
      if( "JCAConnectionFactory".equals( lType ) ) {
         mConnectionFactories.remove( pChild );
      }
   }

   // java.lang.Object overrides ------------------------------------
   
   public String toString() {
      return "JCAResource { " + super.toString() + " } [ " +
         "Datasources: " + mConnectionFactories +
         " ]";
   }

   // Package protected ---------------------------------------------
   
   // Protected -----------------------------------------------------
   
   // Private -------------------------------------------------------
   
   // Inner classes -------------------------------------------------
   

}
