/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.management;

import java.net.URL;
import java.net.MalformedURLException;
import java.security.InvalidParameterException;
import java.util.Properties;

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

import javax.management.j2ee.J2EEManagement;
import javax.management.j2ee.StatisticsProvider;

/**
 * JBoss Management MBean Wrapper
 *
 * @author Marc Fleury
 **/
public class JBossManagement
   extends ServiceMBeanSupport
   implements JBossManagementMBean
{

   // -------------------------------------------------------------------------
   // Constants
   // -------------------------------------------------------------------------  

   public static String JNDI_NAME = "j2eemanagement:domain";
   public static String JMX_NAME = "j2eemanagement";

   // -------------------------------------------------------------------------
   // Members
   // -------------------------------------------------------------------------  

   private MBeanServer mServer;
   private JBossJ2EEManagement mJ2EEManagement;
   private String mName;
   private URL mJNDIPropertiesFileURL;

   // -------------------------------------------------------------------------
   // Constructors
   // -------------------------------------------------------------------------  

   /**
    * Default (no-args) Constructor
    *
    * @param pName Name of the MBean
    **/
   public JBossManagement()
   {
      mName = null;
   }

   /**
    * Constructor with the necessary attributes to be set
    *
    * @param pName Name of the MBean
    **/
   public JBossManagement( String pName )
   {
      mName = pName;
   }

   /**
    * Constructor with the necessary attributes to be set
    *
    * @param pName Name of the MBean
    * @param pJNDIPropertiesFileURL JNDI Properties File URL
    *
    * @throws InvalidParameterException When the given URL to the JNDI property
    *                                   file is an invalid URL
    **/
   public JBossManagement( String pName, String pJNDIPropertiesFileURL )
      throws
         InvalidParameterException
   {
      mName = pName;
      setJNDIPropertiesFileURL( pJNDIPropertiesFileURL );
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
   
   public String getName() {
      return "JBoss J2EE Management MBean";
   }
   
   public String getJNDIName() {
      if( mName != null ) {
         return JMX_NAME + ":" + mName;
      }
      else {
         return JMX_NAME;
      }
   }
   
   public void setJNDIPropertiesFileURL( String pJNDIPropertiesFileURL )
      throws
         InvalidParameterException
   {
      try {
         mJNDIPropertiesFileURL = new URL( pJNDIPropertiesFileURL );
      }
      catch( MalformedURLException mue ) {
         throw new InvalidParameterException( "Given JNDI Properties File URL is invalid" );
      }
   }
   
   public void refresh() {
      mJ2EEManagement.refresh();
   }

   public void refreshStatistic( StatisticsProvider pProvider )
   {
     
   }
   public J2EEManagement getJ2EEManagement() {
      return mJ2EEManagement;
   }
   
   // -------------------------------------------------------------------------
   // ServiceMBean - Methods
   // -------------------------------------------------------------------------  

   protected void initService()
        throws Exception
   {
      Properties lJNDIProperties = null;
      if( mJNDIPropertiesFileURL != null ) {
         lJNDIProperties.load( mJNDIPropertiesFileURL.openStream() );
      }
      mJ2EEManagement = new JBossJ2EEManagement( mName, mServer, lJNDIProperties );
   }
   
   protected void startService()
        throws Exception
   {
      bind( mJ2EEManagement );
      refresh();
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

	private void bind( JBossJ2EEManagement pJ2EEManagement )
      throws
         NamingException
   {
		Context lContext = new InitialContext();
		String lJNDIName = getJNDIName();

		// Ah ! J2EE Management isn't serializable, so we use a helper class
		NonSerializableFactory.bind( lJNDIName, pJ2EEManagement );

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
         JBossJ2EEManagement.class.getName(),
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
