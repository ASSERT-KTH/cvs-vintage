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
import java.util.Map;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.jboss.ejb.Container;
import org.jboss.ejb.EntityContainer;
import org.jboss.ejb.StatefulSessionContainer;
import org.jboss.monitor.StatisticsProvider;

import org.jboss.management.JBossEjbModule;
import org.jboss.management.JBossEntityBean;
import org.jboss.management.JBossJ2EEApplication;
import org.jboss.management.JBossMessageDrivenBean;
import org.jboss.management.JBossStatefulSessionBean;
import org.jboss.management.JBossStatelessSessionBean;

import management.CountStatistic;
import management.EjbModule;
import management.EJB;
import management.EntityBean;
import management.EntityBeanStats;
import management.J2EEApplication;
import management.J2EEModule;
import management.Statistic;
import management.Stats;
import management.TimeStatistic;

/**
* EJB Data Collector collects the management data about EJBs.
*
* @author <a href="mailto:marc.fleury@jboss.org">Marc Fleury</a>
* @author <a href="mailto:andreas.schaefer@madplanet.com">Andreas Schaefer</a>
* @version $Revision: 1.4 $
*
*  <p><b>Revisions:</b>
*  <p><b>20010718 andreas schaefer:</b>
*  <ul>
*  <li>- Create the data collector
*  <li>- Added Statistics Gathering
*  </ul>
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
            if( lName.getKeyProperty( "jndiName" ) == null ) {
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
            // Only to test the Statistics Gathering
            if( lContainer instanceof EntityContainer ) {
               Map lStatistics = ( (EntityContainer) lContainer ).retrieveStatistic();
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

  public Stats getStatistics( management.StatisticsProvider pProvider, MBeanServer pServer )
  {
    try {
      if( pProvider instanceof EJB )
      {
        if( pProvider instanceof EntityBean ) {
          EntityBean lBean = (EntityBean) pProvider;
          Iterator i = pServer.queryNames( new ObjectName( "Management:container" + lBean.getName() ), null ).iterator();
          if( i.hasNext() )
          {
            ObjectName lName = (ObjectName) i.next();
            StatisticsProvider lContainer = (StatisticsProvider) pServer.getAttribute(
              lName,
              "Container"
            );
            Map lStatistics = lContainer.retrieveStatistic();
            return new EntityBeanStats(
              (CountStatistic) lStatistics.get( "ReadyBeanCount" ),
              (CountStatistic) lStatistics.get( "BeanPoolSize" ),
              (CountStatistic) lStatistics.get( "InstatiationCount" ),
              (CountStatistic) lStatistics.get( "DestroyCount" ),
              (CountStatistic) lStatistics.get( "CreateCount" ),
              (CountStatistic) lStatistics.get( "RemoveCount" ),
              (TimeStatistic[]) lStatistics.get( "MethodStatistics" ),
              (TimeStatistic) lStatistics.get( "LoadTime" ),
              (TimeStatistic) lStatistics.get( "StoreTime" ),
              (CountStatistic) lStatistics.get( "ActiveBeanCount" ),
              (TimeStatistic) lStatistics.get( "ActivationTime" ),
              (TimeStatistic) lStatistics.get( "PassivationTime" )
            );
          }
        }
      }
    }
    catch( Exception e )
    {
      e.printStackTrace();
    }
    return null;
  }
  
  public void resetStatistics( management.StatisticsProvider pProvider, MBeanServer pServer )
  {
  }
}
