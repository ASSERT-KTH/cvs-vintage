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

import org.jboss.logging.Logger;

/**
 * Root class of the JBoss JSR-77 implementation of
 * {@link javax.management.j2ee.JNDI JNDI}.
 *
 * @author  <a href="mailto:andreas@jboss.org">Andreas Schaefer</a>.
 * @version $Revision: 1.4 $
 *   
 * <p><b>Revisions:</b>
 *
 * <p><b>20011126 Andreas Schaefer:</b>
 * <ul>
 * <li> Adjustments to the JBoss Guidelines
 * </ul>
 **/
public class JNDI
   extends J2EEResource
   implements JNDIMBean
{
   // Constants -----------------------------------------------------
   
   // Attributes ----------------------------------------------------
   
   // Static --------------------------------------------------------
   
   private static final String[] sTypes = new String[] {
                                             "j2ee.object.created",
                                             "j2ee.object.deleted",
                                             "state.starting",
                                             "state.running",
                                             "state.stopping",
                                             "state.stopped",
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
         lLog.error( "Could not create JSR-77 JNDI: " + pName, e );
         return null;
      }
      try {
         // Now create the J2EEApplication
         return pServer.createMBean(
            "org.jboss.management.j2ee.JNDI",
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
         lLog.error( "Could not create JSR-77 JNDI: " + pName, e );
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
         lLog.error( "Could not destroy JSR-77 JNDI: " + pName, e );
      }
   }
   
   // Constructors --------------------------------------------------
   
   /**
    * @param pName Name of the JNDI
    *
    * @throws InvalidParameterException If list of nodes or ports was null or empty
    **/
   public JNDI( String pName, ObjectName pServer )
      throws
         MalformedObjectNameException,
         InvalidParentException
   {
      super( "JNDI", pName, pServer );
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
      return 0;
   }
   
   public int getState() {
      return 0;
   }

   public void start() {
   }

   public void startRecursive() {
   }

   public void stop() {
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
}
