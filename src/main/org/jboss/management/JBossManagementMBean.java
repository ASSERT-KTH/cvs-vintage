/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.management;

import java.net.URL;
import java.security.InvalidParameterException;

import org.jboss.util.ServiceMBean;

import javax.management.j2ee.J2EEManagement;
import javax.management.j2ee.StatisticsProvider;

/**
 * This interface defines the manageable interface for the JBossManagement
 * object to be used as a JMX MBean.
 *
 * @author Marc Fleury
 **/
public interface JBossManagementMBean
   extends ServiceMBean
{
   // -------------------------------------------------------------------------
   // Constants
   // -------------------------------------------------------------------------  

   public static final String OBJECT_NAME = "J2EEManagement:name=Main";

   // -------------------------------------------------------------------------
   // Methods
   // -------------------------------------------------------------------------

   /**
    * @return JNDI Name for this MBean
    **/
   public String getJNDIName();

   /**
    * Sets the URL to the JNDI properties files to be used to look up the
    * JNDI server.
    *
    * @param pJNDIPropertiesFileURL URL to the JNDI Properties file
    **/
   public void setJNDIPropertiesFileURL( String pJNDIPropertiesFileURL )
      throws
         InvalidParameterException;

   /**
    * Refreshs the management values
    */
   public void refresh();
   
   /**
    * Refreshs the statistics
    *
    * @param pProvider StatisticProvider which value has to be refreshed
    */
   public void refreshStatistic( StatisticsProvider pProvider );
   
   /**
    * @return J2EE Management INstance
    **/
   public J2EEManagement getJ2EEManagement();
   
}
