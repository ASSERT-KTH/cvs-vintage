/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.management;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.jboss.ejb.Container;
import org.jboss.ejb.StatefulSessionContainer;

import org.jboss.management.JBossEjbModule;
import org.jboss.management.JBossEntityBean;
import org.jboss.management.JBossJ2EEApplication;
import org.jboss.management.JBossMessageDrivenBean;
import org.jboss.management.JBossStatefulSessionBean;
import org.jboss.management.JBossStatelessSessionBean;

import management.EjbModule;
import management.EJB;
import management.J2EEApplication;
import management.J2EEModule;

/**
 * JDBC Data Collector
 *
 * @author Marc Fleury
 **/
public class EJBDataCollector
   implements DataCollector
{

   // -------------------------------------------------------------------------
   // Constants
   // -------------------------------------------------------------------------  

   // -------------------------------------------------------------------------
   // Members
   // -------------------------------------------------------------------------  

   // -------------------------------------------------------------------------
   // Constructors
   // -------------------------------------------------------------------------  

   /**
    * Default (no-args) Constructor
    **/
   public EJBDataCollector() {
   }

   // -------------------------------------------------------------------------
   // Methods
   // -------------------------------------------------------------------------  

   public Collection refresh( MBeanServer pServer ) {
      Collection lReturn = new ArrayList();
      try {
         // Look up all the registered Containers for the EJB Module and loop through
         Hashtable lApplications = new Hashtable();
         Iterator i = pServer.queryNames( new ObjectName( "Management:*" ), null ).iterator();
         while( i.hasNext() ) {
            ObjectName lName = (ObjectName) i.next();
            if( lName.getKeyProperty( "container" ) == null ) {
               continue;
            }
            Container lContainer = (Container) pServer.getAttribute( lName, "Container" );
            Collection lBeans = null;
            // Check if application name already exists
            String lApplicationName = lContainer.getApplication().getName();
            if( lApplications.containsKey( lApplicationName ) ) {
               lBeans = (Collection) lApplications.get( lApplicationName );
            }
            else {
               lBeans = new ArrayList();
               lApplications.put( lApplicationName, lBeans );
            }
            // Add EJB Info
            if( lContainer.getBeanMetaData().isSession() ) {
               if( lContainer instanceof StatefulSessionContainer ) {
                  lBeans.add( new JBossStatefulSessionBean( lContainer.getBeanMetaData().getEjbName() ) );
               }
               else {
                  lBeans.add( new JBossStatelessSessionBean( lContainer.getBeanMetaData().getEjbName() ) );
               }
            }
            if( lContainer.getBeanMetaData().isEntity() ) {
               lBeans.add( new JBossEntityBean( lContainer.getBeanMetaData().getEjbName() ) );
            }
            if( lContainer.getBeanMetaData().isMessageDriven() ) {
               lBeans.add( new JBossMessageDrivenBean( lContainer.getBeanMetaData().getEjbName() ) );
            }
         }
         i = lApplications.keySet().iterator();
         while( i.hasNext() ) {
            String lApplicationName = (String) i.next();
            ArrayList lBeans = (ArrayList) lApplications.get( lApplicationName );
            EjbModule lModule = new JBossEjbModule(
               "EjbModule",
               (EJB[]) lBeans.toArray( new EJB[ 0 ] )
            );
            lReturn.add(
               new JBossJ2EEApplication(
                  lApplicationName,
                  null,
                  new J2EEModule[] { lModule }
               )
            );
         }
      }
      catch( Exception e ) {
         e.printStackTrace();
      }
      return lReturn;
   }
}
