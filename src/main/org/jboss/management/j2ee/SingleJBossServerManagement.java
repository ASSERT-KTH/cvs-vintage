package org.jboss.management.j2ee;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;

import javax.management.JMException;
import javax.management.MalformedObjectNameException;
import javax.management.MBeanServer;
import javax.management.ObjectName;

/**
* Represents the single JBoss server management domain
*
* @author <a href="mailto:andreas@jboss.org">Andreas Schaefer</a>
**/
public class SingleJBossServerManagement
   extends J2EEManagement
{

   // -------------------------------------------------------------------------
   // Constructors
   // -------------------------------------------------------------------------
   
   public SingleJBossServerManagement()
      throws
         MalformedObjectNameException,
         InvalidParentException
   {
      super( "SingleJBoss" );
   }
   
   // -------------------------------------------------------------------------
   // Properties (Getters/Setters)
   // -------------------------------------------------------------------------
   
   public void postRegister( java.lang.Boolean pRegistrationDone ) {
      super.postRegister( pRegistrationDone );
      if( pRegistrationDone.booleanValue() ) {
         // Create Server Component
         System.out.println( "SingleJBossServerManagement.getObjectName(), name: " + getObjectName() );
         try {
            System.out.println( "SingleJBossServerManagement.getObjectName(), create J2EEServer instance" );
            ObjectName lServer = getServer().createMBean(
               "org.jboss.management.j2ee.J2EEServer",
               null,
               new Object[] { "Single", getObjectName() },
               new String[] { String.class.getName(), ObjectName.class.getName() }
            ).getObjectName();
   /*
            pServer.invoke(
               lServer,
               "start",
               new Object[] {},
               new String[] {}
            );
   */
         }
         catch( JMException jme ) {
            jme.printStackTrace();
         }
         catch( Exception e ) {
            e.printStackTrace();
         }
      }
   }
   
   public String toString() {
      return "SingleJBossServerManagement { " + super.toString() + " } []";
   }
   
}
