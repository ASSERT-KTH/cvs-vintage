/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.management.j2ee;

import javax.management.MalformedObjectNameException;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.Notification;

import org.jboss.logging.Logger;

/**
 * Root class of the JBoss JSR-77 implementation of
 * {@link javax.management.j2ee.JCAManagedConnectionFactory JCAManagedConnectionFactory}.
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
public class JCAManagedConnectionFactory extends J2EEManagedObject implements JCAManagedConnectionFactoryMBean
{
   // Constants -----------------------------------------------------
   
   // Attributes ----------------------------------------------------
   
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
                                          
   public static ObjectName create( MBeanServer pServer, String pName, ObjectName pParent ) {
      Logger lLog = Logger.getLogger( JCAManagedConnectionFactory.class );
      
      try {
         // Now create the JCAManagedConnectionFactory Representant
         return pServer.createMBean(
            "org.jboss.management.j2ee.JCAManagedConnectionFactory",
            null,
            new Object[] {
               pName,
               pParent
            },
            new String[] {
               String.class.getName(),
               ObjectName.class.getName()
            }
         ).getObjectName();
      }
      catch( Exception e ) {
         lLog.error( "Could not create JSR-77 JCAManagedConnectionFactory: " + pName, e );
         return null;
      }
   }
   
   public static void destroy( MBeanServer pServer, String pName ) {
      Logger lLog = Logger.getLogger( JCAManagedConnectionFactory.class );
      try {
         // Find the Object to be destroyed
         ObjectName lSearch = new ObjectName(
            J2EEManagedObject.getDomainName() + ":type=JCAManagedConnectionFactory,name=" + pName + ",*"
         );
         ObjectName lJCAManagedConnectionFactory = (ObjectName) pServer.queryNames(
            lSearch,
            null
         ).iterator().next();
         // Now remove the JCAManagedConnectionFactory
         pServer.unregisterMBean( lJCAManagedConnectionFactory );
      }
      catch( Exception e ) {
         lLog.error( "Could not destroy JSR-77 JCAManagedConnectionFactory: " + pName, e );
      }
   }
   
   // Constructors --------------------------------------------------
   
   /**
   * @param pName Name of the JCAManagedConnectionFactory 
   *
   * @throws InvalidParameterException If list of nodes or ports was null or empty
   **/
   public JCAManagedConnectionFactory(String pName, ObjectName pServer) throws MalformedObjectNameException, InvalidParentException
   {
      super( "JCAManagedConnectionFactory", pName, pServer );
   }
   
   // Public --------------------------------------------------------
   
    // org.jboss.ServiceMBean overrides ------------------------------------
   
   public void postCreation() {
      sendNotification(
         new Notification(
            sTypes[ 0 ],
            getName(),
            1,
            System.currentTimeMillis(),
            "Managed Connection Factory created"
         )
      );
   }
   
   public void preDestruction() {
      Logger lLog = getLog();
      if( lLog.isInfoEnabled() ) {
         lLog.info( "JCAManagedConnectionFactory.preDeregister(): " + getName() );
      }
      sendNotification(
         new Notification(
            sTypes[ 1 ],
            getName(),
            1,
            System.currentTimeMillis(),
            "Managed Connection Factory deleted"
         )
      );
   }
   // org.jboss.ServiceMBean overrides ------------------------------------
   
   // java.lang.Object overrides ------------------------------------
   
   public String toString() {
      return "JCAManagedConnectionFactory { " + super.toString() + " } [ " +
         " ]";
   }
   
   // Package protected ---------------------------------------------
   
   // Protected -----------------------------------------------------
   
   // Private -------------------------------------------------------
   
   // Inner classes -------------------------------------------------
   
}
