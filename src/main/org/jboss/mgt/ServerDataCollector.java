/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.mgt;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import javax.management.MalformedObjectNameException;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.Name;
import javax.naming.NamingException;
import javax.naming.NameNotFoundException;
import javax.naming.Reference;
import javax.naming.StringRefAddr;

import org.jboss.ejb.Container;
import org.jboss.logging.Log;
import org.jboss.naming.NonSerializableFactory;
import org.jboss.util.ServiceMBeanSupport;

/**
 * JBoss Management MBean Wrapper
 *
 * @author Marc Fleury
 **/
public class ServerDataCollector
   extends ServiceMBeanSupport
   implements ServerDataCollectorMBean
{

   // -------------------------------------------------------------------------
   // Constants
   // -------------------------------------------------------------------------  

   public static String JNDI_NAME = "servercollector:domain";
   public static String JMX_NAME = "servercollector";

   // -------------------------------------------------------------------------
   // Members
   // -------------------------------------------------------------------------  

   private MBeanServer mServer;
   private String mName;
   private Map mApplications = new Hashtable();
   private Boolean mRefresh = new Boolean( true );
   private Thread mWorker;
   private int mRefreshSleep = 2000;

   // -------------------------------------------------------------------------
   // Constructors
   // -------------------------------------------------------------------------  

   /**
    * Default (no-args) Constructor
    *
    * @param pName Name of the MBean
    **/
   public ServerDataCollector()
   {
      mName = null;
   }

   /**
    * Constructor with the necessary attributes to be set
    *
    * @param pName Name of the MBean
    **/
   public ServerDataCollector( String pName )
   {
      mName = pName;
   }

   // -------------------------------------------------------------------------
   // Methods
   // -------------------------------------------------------------------------  

   public int getRefreshSleep() {
      return mRefreshSleep;
   }
   
   public void setRefreshSleep( int pSleep ) {
      if( pSleep > 0 ) {
         mRefreshSleep = pSleep;
      }
   }
   
   public void refresh() {
      // Mark it to be refreshed
      synchronized( mRefresh ) {
         mRefresh = new Boolean( true );
      }
   }

   public ObjectName getObjectName(
      MBeanServer server,
      ObjectName name
   )
      throws MalformedObjectNameException
   {
      mServer = server;
      return new ObjectName( OBJECT_NAME );
   }
   
   public String getJNDIName() {
      if( mName != null ) {
         return JMX_NAME + ":" + mName;
      }
      else {
         return JMX_NAME;
      }
   }
   
   public String getName() {
      return "JBoss Server Data Collector MBean";
   }
   
   public Application getApplication(
      String pApplicationId
   ) {
      // Loop through the applications and find the application
      if( pApplicationId != null ) {
         return (Application) mApplications.get( pApplicationId );
      }
      return null;
   }
   
   public Collection getApplications() {
      return new ArrayList( mApplications.values() );
   }
   
   public void saveApplication(
      String pApplicationId,
      Application pApplication
   ) {
      if( pApplicationId != null ) {
         mApplications.put( pApplicationId, pApplication ); 
      }
   }
   
   public void removeApplication(
      String pApplicationId
   ) {
      if( pApplicationId != null ) {
         mApplications.remove( pApplicationId );
      }
   }

   public void saveModule(
      String pApplicationId,
      int pModuleId,
      Module pModule
   ) {
      Application lApplication = getApplication( pApplicationId );
      System.out.println( "ServerDataCollector.saveModule(), App. Id: " + pApplicationId + ", application: " + lApplication );
      if( lApplication != null ) {
         lApplication.saveModule( pModuleId, pModule );
      }
   }

   /**
    * Removes the registered Module if found
    *
    * @param pApplicationId Id of the Application the Module is part of
    * @param pModuleId Id of the Module to be removed
    **/
   public void removeModule(
      String pApplicationId,
      int pModuleId
   ) {
      Application lApplication = getApplication( pApplicationId );
      if( lApplication != null ) {
         lApplication.removeModule( pModuleId );
      }
   }

   // -------------------------------------------------------------------------
   // ServiceMBean - Methods
   // -------------------------------------------------------------------------  

   protected void initService()
        throws Exception
   {
//      mJBossServer = new JBossServer( mName );
   }
   
   protected void startService()
        throws Exception
   {
      bind( this );
      mWorker = new RefreshWorker( log );
      mWorker.start();
   }
   
   protected void stopService() {
      mWorker.stop();
      try {
         unbind();
      }
      catch( Exception e ) {
         log.exception( e );
      }
   }

   // -------------------------------------------------------------------------
   // Helper methods to bind/unbind the Management class
   // -------------------------------------------------------------------------

	private void bind( ServerDataCollector pServer )
      throws
         NamingException
   {
		Context lContext = new InitialContext();
		String lJNDIName = getJNDIName();

		// Ah ! JBoss Server isn't serializable, so we use a helper class
		NonSerializableFactory.bind( lJNDIName, pServer );

      //AS Don't ask me what I am doing here
		Name lName = lContext.getNameParser("").parse( lJNDIName );
		while( lName.size() > 1 ) {
			String lContextName = lName.get( 0 );
			try {
				lContext = (Context) lContext.lookup(lContextName);
			}
			catch( NameNotFoundException e )	{
				lContext = lContext.createSubcontext(lContextName);
			}
			lName = lName.getSuffix( 1 );
		}

		// The helper class NonSerializableFactory uses address type nns, we go on to
		// use the helper class to bind the javax.mail.Session object in JNDI
		StringRefAddr lAddress = new StringRefAddr( "nns", lJNDIName );
		Reference lReference = new Reference(
         ServerDataCollector.class.getName(),
         lAddress,
         NonSerializableFactory.class.getName(),
         null
      );
		lContext.bind( lName.get( 0 ), lReference );

		log.log( "JBoss Management Service '" + getJNDIName() + "' bound to " + lJNDIName );
	}

	private void unbind() throws NamingException
	{
      String lJNDIName = getJNDIName();

      new InitialContext().unbind( lJNDIName );
      NonSerializableFactory.unbind( lJNDIName );
      log.log("JBoss Management service '" + lJNDIName + "' removed from JNDI" );
	}
   
   /**
    * Worker class to perform the refresh of the data
    **/
   private class RefreshWorker
      extends Thread
   {
      private Log mLog;
      
      public RefreshWorker( Log pLog ) {
         mLog = pLog;
      }
      
      public void run() {
         while( true ) {
            try {
               synchronized( mRefresh ) {
                  if( mRefresh.booleanValue() ) {
                     doRefresh();
                     mRefresh = new Boolean( false );
                  }
               }
               Thread.sleep( mRefreshSleep );
            }
            catch( InterruptedException e ) {
            }
         }
      }
      
      private void doRefresh() {
         try {
            // Drop the actual info
            mApplications = new Hashtable();
            // Look up all the registered Containers for the EJB Module and loop through
            Iterator i = mServer.queryNames( new ObjectName( "Management:*" ), null ).iterator();
            while( i.hasNext() ) {
               ObjectName lName = (ObjectName) i.next();
               if( lName.getKeyProperty( "container" ) == null ) {
                  continue;
               }
               Container lContainer = (Container) mServer.getAttribute( lName, "Container" );
               // Check if application name already exists
               String lApplicationName = lContainer.getApplication().getName();
               Application lApplication = null;
               if( mApplications.containsKey( lApplicationName ) ) {
                  lApplication = (Application) mApplications.get( lApplicationName );
               }
               else {
                  lApplication = new Application( lApplicationName, "DD:Fix it later" );
                  mApplications.put( lApplicationName, lApplication );
               }
               // Check if the EJB module is there
               Module lModule = lApplication.getModule( Application.EJBS );
               if( lModule == null ) {
                  lModule = new Module( "EJB", "DD:Fix it later" );
                  lApplication.saveModule( Application.EJBS, lModule );
               }
               // Add EJB Info
               int lType = 0;
               if( lContainer.getBeanMetaData().isSession() ) {
                  lType = EJB.SESSION;
               }
               if( lContainer.getBeanMetaData().isEntity() ) {
                  lType = EJB.ENTITY;
               }
               if( lContainer.getBeanMetaData().isMessageDriven() ) {
                  lType = EJB.MESSAGE_DRIVEN;
               }
               String lEjbName = lContainer.getBeanMetaData().getEjbName();
               EJB lEjb = new EJB( lEjbName, lType, true );
               lModule.addItem( lEjb );
            }
         }
         catch( Exception e ) {
            e.printStackTrace();
         }
      }
   }

}
