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

import org.jboss.metadata.BeanMetaData;
import org.jboss.metadata.SessionMetaData;

/**
 * Root class of the JBoss JSR-77 implementation of
 * {@link javax.management.j2ee.EJB EJB}.
 *
 * @author  <a href="mailto:andreas@jboss.org">Andreas Schaefer</a>.
 * @version $Revision: 1.2 $
 *   
 * <p><b>Revisions:</b>
 *
 * <p><b>20011126 Andreas Schaefer:</b>
 * <ul>
 * <li> Creation
 * </ul>
 **/
public abstract class EJB
   extends J2EEManagedObject
   implements javax.management.j2ee.EJB
{
   // Constants -----------------------------------------------------
   
   // Attributes ----------------------------------------------------
   
   // Static --------------------------------------------------------
   
   private static final String[] sTypes = new String[] {
                                             "EntityBean",
                                             "StatefulSessionBean",
                                             "StatelessSessionBean",
                                             "MessageDrivenBean"
                                          };
   
   public static ObjectName create( MBeanServer pServer, String pEjbModule, BeanMetaData pBeanMeta ) {
      try {
         int lType =
            pBeanMeta.isSession() ?
               ( ( (SessionMetaData) pBeanMeta ).isStateless() ? 2 : 1 ) :
               ( pBeanMeta.isMessageDriven() ? 3 : 0 );
         // Now create the J2EEApplication
         return pServer.createMBean(
            "org.jboss.management.j2ee." + sTypes[ lType ],
            null,
            new Object[] {
               pBeanMeta.getJndiName(),
               new ObjectName( pEjbModule )
            },
            new String[] {
               String.class.getName(),
               ObjectName.class.getName()
            }
         ).getObjectName();
      }
      catch( Exception e ) {
//         e.printStackTrace();
         return null;
      }
   }
   
   public static void destroy( MBeanServer pServer, String pEJBName ) {
      try {
         // Now remove the EJB
         pServer.unregisterMBean( new ObjectName( pEJBName ) );
      }
      catch( Exception e ) {
//         e.printStackTrace();
      }
   }
   
   // Constructors --------------------------------------------------
   
   /**
    * @param pName Name of the EntityBean
    *
    * @throws InvalidParameterException If list of nodes or ports was null or empty
    **/
   public EJB( String pType, String pName, ObjectName pEJBModule )
      throws
         MalformedObjectNameException,
         InvalidParentException
   {
      super( pType, pName, pEJBModule );
   }

   // -------------------------------------------------------------------------
   // Properties (Getters/Setters)
   // -------------------------------------------------------------------------  

   public String toString() {
      return "EJB { " + super.toString() + " } []";
   }
}
