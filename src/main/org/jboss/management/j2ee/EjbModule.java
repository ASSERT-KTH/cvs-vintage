/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.management.j2ee;





import java.net.URL;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.management.AttributeChangeNotification;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.ObjectName;
import javax.management.j2ee.EJB;
import javax.management.j2ee.J2EEApplication;
import javax.management.j2ee.J2EEServer;
import javax.management.j2ee.JVM;
import org.jboss.logging.Logger;
import org.jboss.system.ServiceMBean;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Root class of the JBoss JSR-77 implementation of
 * {@link javax.management.j2ee.EJBModule EJBModule}.
 *
 * @author  <a href="mailto:andreas@jboss.org">Andreas Schaefer</a>.
 * @version $Revision: 1.13 $
 *   
 * <p><b>Revisions:</b>
 *
 * <p><b>20011123 Andreas Schaefer:</b>
 * <ul>
 * <li> Adjustments to the JBoss Guidelines and implementing of the
 *      the create() and destroy() helper method
 * </ul>
 **/
public class EjbModule
  extends J2EEModule
  implements EjbModuleMBean
{

   // Constants -----------------------------------------------------
   
   // Attributes ----------------------------------------------------

   private List mEJBs = new ArrayList();
   private long mStartTime = -1;
   private int mState = ServiceMBean.STOPPED;
   //used to see if we should remove our parent when we are destroyed.
   private static final Map iCreatedParent = new HashMap();

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
   
   public static ObjectName create( MBeanServer pServer, String pApplicationName, String pName, URL pURL ) {
      Logger lLog = Logger.getLogger( EjbModule.class );
      String lDD = null;
      ObjectName lApplication = null;
      boolean fakeParent = false;
      try {
         ObjectName serverQuery = new ObjectName( J2EEManagedObject.getDomainName() + ":type=J2EEServer,*" );
         Set servers = pServer.queryNames(serverQuery, null);
         if (servers.size() != 1) 
         {
            lLog.error("Wrong number of servers found, should be 1: " + servers.size());
            return null; 
         } // end of if ()
         
         ObjectName lServer = (ObjectName)servers.iterator().next();

         String lServerName = lServer.getKeyPropertyList().get( "type" ) + "=" +
                              lServer.getKeyPropertyList().get( "name" );

         lLog.debug( "EjbModule.create(), server name: " + lServerName );

         ObjectName parentAppQuery =  new ObjectName( J2EEManagedObject.getDomainName() + 
                                                      ":type=J2EEApplication" +
                                                      ",name=" + pApplicationName + 
                                                      "," + lServerName + ",*");

         Set parentApps =  pServer.queryNames(parentAppQuery, null);

         if (parentApps.size() == 0) 
         {
            lApplication = org.jboss.management.j2ee.J2EEApplication.create(pServer,
                                                  pApplicationName,
                                                  null);
            fakeParent = true;
            
         } // end of if ()
         else if (parentApps.size() == 1) 
         {
            lApplication = (ObjectName)parentApps.iterator().next();
         } // end of if ()
         else
         {
            lLog.error("more than one parent app for this ejb-module: " + parentApps.size());
            return null;
         } // end of else
         
         // First get the deployement descriptor
         lDD = J2EEDeployedObject.getDeploymentDescriptor( pURL, J2EEDeployedObject.EJB );
      }
      catch( Exception e ) {
         lLog.error( "Could not create JSR-77 EjbModule: " + pApplicationName, e );
         return null;
      }
      try {
         // Now create the J2EE EJB module
         lLog.debug(
            "Create EJB-Module, name: " + pName +
            ", application: " + lApplication +
            ", dd: " + lDD
         );
         ObjectName ejbModule = pServer.createMBean(
            "org.jboss.management.j2ee.EjbModule",
            null,
            new Object[] {
               pName,
               lApplication,
               lDD
            },
            new String[] {
               String.class.getName(),
               ObjectName.class.getName(),
               String.class.getName()
            }
         ).getObjectName();
         //remember if we created our parent, if we did we have to kill it on destroy.
         if (fakeParent) 
         {
            iCreatedParent.put(ejbModule, lApplication);
         } // end of if ()
         return ejbModule;
         
      }
      catch( Exception e ) {
         lLog.error( "Could not create JSR-77 EjbModule: " + pApplicationName, e );
         return null;
      }
   }
   
   public static void destroy( MBeanServer pServer, String pModuleName ) {
      Logger lLog = Logger.getLogger( EjbModule.class );
      try {
         ObjectName name = new ObjectName(pModuleName);
         // Now remove the EjbModule
         pServer.unregisterMBean(name);
         ObjectName parent = (ObjectName)iCreatedParent.get(name);
         if (parent != null) 
         {
            lLog.info( "Remove fake JSR-77 parent Application: " + parent.toString() );
            org.jboss.management.j2ee.J2EEApplication.destroy(pServer, parent.toString());
            
         } // end of if ()
         
      }
      catch( Exception e ) {
         lLog.error( "Could not destory JSR-77 EjbModule: " + pModuleName, e );
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
   public EjbModule( String pName, ObjectName pApplication, String pDeploymentDescriptor )
      throws
         MalformedObjectNameException,
         InvalidParentException
   {
      super( "EjbModule", pName, pApplication, pDeploymentDescriptor );
   }

   // Public --------------------------------------------------------
   
   // EjbModule implementation --------------------------------------
   
   public ObjectName[] getEjbs() {
      return (ObjectName[]) mEJBs.toArray( new ObjectName[ 0 ] );
   }
   
   public ObjectName getEjb( int pIndex ) {
      if( pIndex >= 0 && pIndex < mEJBs.size() )
      {
         return (ObjectName) mEJBs.get( pIndex );
      }
      else
      {
         return null;
      }
   }
   
   // J2EEManagedObjectMBean implementation -------------------------
   
   public void addChild( ObjectName pChild ) {
      String lType = J2EEManagedObject.getType( pChild );
      if( "EntityBean".equals( lType ) ||
         "StatelessSessionBean".equals( lType ) ||
         "StatefulSessionBean".equals( lType ) ||
         "MessageDrivenBean".equals( lType )
      ) {
         mEJBs.add( pChild );
      }
   }
   
   public void removeChild( ObjectName pChild ) {
      String lType = J2EEManagedObject.getType( pChild );
      if( "EntityBean".equals( lType ) ||
         "StatelessSessionBean".equals( lType ) ||
         "StatefulSessionBean".equals( lType ) ||
         "MessageDrivenBean".equals( lType )
      ) {
         mEJBs.remove( pChild );
      }
   }

   // org.jboss.ServiceMBean overrides ------------------------------------

   public void postRegister( Boolean pRegisterationDone ) {
      super.postRegister( pRegisterationDone );
      // If set then register for its events
/*
      try {
         getServer().addNotificationListener( mService, new Listener(), null, null );
      }
      catch( JMException jme ) {
         //AS ToDo: later on we have to define what happens when service is null or
         //AS ToDo: not found.
         jme.printStackTrace();
      }
*/
      sendNotification(
         new Notification(
            sTypes[ 0 ],
            getName(),
            1,
            System.currentTimeMillis(),
            "EJB Module created"
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
            "EJB Module deleted"
         )
      );
   }
   
   // Object overrides ---------------------------------------------------
   
   public String toString() {
      return "EjbModule[ " + super.toString() +
         "EJBs: " + mEJBs +
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
