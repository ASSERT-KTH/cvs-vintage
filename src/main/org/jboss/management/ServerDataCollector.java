/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.management;

import java.beans.PropertyEditor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.MalformedObjectNameException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.Name;
import javax.naming.NamingException;
import javax.naming.NameNotFoundException;
import javax.naming.Reference;
import javax.naming.StringRefAddr;

import org.jboss.logging.Log;
import org.jboss.naming.NonSerializableFactory;
import org.jboss.util.ServiceMBeanSupport;

import management.EJB;
import management.J2EEApplication;
import management.StatisticsProvider;
import management.Stats;

/**
 * JBoss Management MBean Wrapper
 *
 * @author <a href="mailto:marc.fleury@jboss.org">Marc Fleury</a>
 * @author <a href="mailto:andreas.schaefer@madplanet.com">Andreas Schaefer</a>
 * @version $Revision: 1.3 $
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
   private Boolean mRefresh = new Boolean( true );
   private RefreshWorker mWorker;
   private int mRefreshSleep = 2000;

   private Map mCollectors = new Hashtable();

   private Map mApplications = new Hashtable();
   private Collection mResources = new ArrayList();
   private Collection mNodes = new ArrayList();

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
     this( null );
   }

   /**
    * Constructor with the necessary attributes to be set
    *
    * @param pName Name of the MBean
    **/
   public ServerDataCollector( String pName )
   {
      mName = pName;
      //AS Later on load this dynamically
      mCollectors.put( "EJB", new EJBDataCollector() );
      mCollectors.put( "JDBC", new JDBCDataCollector() );
      mCollectors.put( "JNDI", new JNDIDataCollector() );
      mCollectors.put( "Mail", new MailDataCollector() );
      mCollectors.put( "Node", new NodeDataCollector() );
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

   public void refreshNow() {
      synchronized( mRefresh ) {
         mWorker.doRefresh();
         mRefresh = new Boolean( false );
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
   
   public J2EEApplication getApplication(
      String pApplicationId
   ) {
      // Loop through the applications and find the application
      if( pApplicationId != null ) {
         return (J2EEApplication) mApplications.get( pApplicationId );
      }
      return null;
   }
   
   public Collection getApplications() {
      return new ArrayList( mApplications.values() );
   }

   public Collection getResources() {
      return mResources;
   }

   public Collection getNodes() {
      return mNodes;
   }

/* AS Keep it but mostly likely it will removed later
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
    ** /
   public void removeModule(
      String pApplicationId,
      int pModuleId
   ) {
      Application lApplication = getApplication( pApplicationId );
      if( lApplication != null ) {
         lApplication.removeModule( pModuleId );
      }
   }
*/
  public Stats getStatistics( StatisticsProvider pProvider ) {
    if( pProvider instanceof EJB ) {
      return ( (DataCollector) mCollectors.get( "EJB" ) ).getStatistics( pProvider, getServer() );
    }
    return null;
  }
  
  public void resetStatistics( StatisticsProvider pProvider ) {
    ( (DataCollector) mCollectors.get( "EJB" ) ).resetStatistics( pProvider, getServer() );
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
            mApplications = new Hashtable();
            Collection lApplications = ( (DataCollector) mCollectors.get( "EJB" ) ).refresh( mServer );
            Iterator i = lApplications.iterator();
            while( i.hasNext() ) {
               J2EEApplication lApplication = (J2EEApplication) i.next();
               mApplications.put(
                  lApplication.getName(),
                  lApplication
               );
            }
         }
         catch( Exception e ) {
            e.printStackTrace();
         }
         mResources = new ArrayList();
         try {
            // Get the info about JDBCs
            mResources.addAll( ( (DataCollector) mCollectors.get( "JDBC" ) ).refresh( mServer ) );
         }
         catch( Exception e ) {
            e.printStackTrace();
         }
         try {
            // Get the info about Mail
            mResources.addAll( ( (DataCollector) mCollectors.get( "Mail" ) ).refresh( mServer ) );
         }
         catch( Exception e ) {
            e.printStackTrace();
         }
         try {
            // Get the info about JNDI
            mResources.addAll( ( (DataCollector) mCollectors.get( "JNDI" ) ).refresh( mServer ) );
         }
         catch( Exception e ) {
            e.printStackTrace();
         }
         try {
            // Get the info about current nodes
            mNodes = ( (DataCollector) mCollectors.get( "Node" ) ).refresh( mServer );
         }
         catch( Exception e ) {
            e.printStackTrace();
         }
      }
   }

}
