/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.jmx.connector;

import java.util.Iterator;
import java.util.Hashtable;

import javax.management.DynamicMBean;
import javax.management.ObjectName;
import javax.management.MBeanServer;

import javax.naming.InitialContext;

import org.jboss.jmx.connector.RemoteMBeanServer;
import org.jboss.system.ServiceMBeanSupport;

/**
 * Factory delivering a list of servers and its available protocol connectors
 * and after selected to initiate the connection
 *
 * This is just the (incomplete) interface of it
 *
 * @author <A href="mailto:andreas.schaefer@madplanet.com">Andreas &quot;Mad&quot; Schaefer</A>
 */
public class ConnectorFactoryService
   extends ServiceMBeanSupport
   implements ConnectorFactoryServiceMBean
{
   private static final String JNDI_NAME = "jxm:connector:factory";

   /** Connector Factory instance **/
   private ConnectorFactoryImpl	mFactory;
   private int mNotificationType;
   private String mJMSName;
   private String mEJBAdaptorName;

   public ConnectorFactoryService() {
   }
	
   public int getNotificationType() {
      return mNotificationType;
   }
   
   public void setNotificationType( int pNotificationType ) {
      mNotificationType = pNotificationType;
   }
   
   public String getJMSName() {
      return mJMSName;
   }
   
   public void setJMSName( String pName ) {
      mJMSName = pName;
   }
   
   public String getEJBAdaptorName() {
      return mEJBAdaptorName;
   }
   
   public void setEJBAdaptorName( String pName ) {
      if( pName == null ) {
         mEJBAdaptorName = "ejb/jmx/ejb/adaptor";
      } else {
         mEJBAdaptorName = pName;
      }
   }

   public Iterator getConnectors( Hashtable pProperties, ConnectorFactoryImpl.IConnectorTester pTester )
   {
      return mFactory.getConnectors( pProperties, pTester );
   }

   public RemoteMBeanServer createConnection(ConnectorFactoryImpl.ConnectorName pConnector)
   {
      return mFactory.createConnection( pConnector );
   }

   public void removeConnection(ConnectorFactoryImpl.ConnectorName pConnector)
   {
		mFactory.removeConnection( pConnector );
   }

   public ObjectName getObjectName(MBeanServer pServer, ObjectName pName)
      throws javax.management.MalformedObjectNameException
   {
      log.debug(
         "ConnectorFactoryService.getObjectName(), server: " + server +
         ", object name: " + OBJECT_NAME +
         ", instance: " + OBJECT_NAME
         );
      return OBJECT_NAME;
   }
	
   protected void startService() throws Exception 
   {
      log.debug( "Init Connector Factory mNotificationTypeService: " +
                 "NT: " + mNotificationType + ", JMS: " + mJMSName
                 );
      mFactory = new ConnectorFactoryImpl( server, mNotificationType, mJMSName, mEJBAdaptorName );
   }
	
}
