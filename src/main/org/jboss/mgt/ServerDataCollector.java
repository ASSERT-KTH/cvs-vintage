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
      return "JBoss Server MBean";
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
//      refresh();
   }
   
   protected void stopService() {
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

}
