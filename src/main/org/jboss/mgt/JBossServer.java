/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.mgt;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

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
public class JBossServer
   extends ServiceMBeanSupport
   implements JBossServerMBean
{

   // -------------------------------------------------------------------------
   // Constants
   // -------------------------------------------------------------------------  

   public static String JNDI_NAME = "j2eeserver:domain";
   public static String JMX_NAME = "j2eeserver";

   // -------------------------------------------------------------------------
   // Members
   // -------------------------------------------------------------------------  

   private MBeanServer mServer;
   private JBossServer mJBossServer;
   private String mName;
   private Collection mApplications = new ArrayList();

   // -------------------------------------------------------------------------
   // Constructors
   // -------------------------------------------------------------------------  

   /**
    * Default (no-args) Constructor
    *
    * @param pName Name of the MBean
    **/
   public JBossServer()
   {
      mName = null;
   }

   /**
    * Constructor with the necessary attributes to be set
    *
    * @param pName Name of the MBean
    **/
   public JBossServer( String pName )
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
   
   public JBossApplication getApplication(
      String pApplicationId
   ) {
      // Loop through the applications and find the application
      if( pApplicationId != null ) {
         Iterator i = getApplications().iterator();
         while( i.hasNext() ) {
            JBossApplication lTest = (JBossApplication) i.next();
            if( pApplicationId.equals( lTest.getId() ) ) {
               return lTest;
            }
         }
      }
      return null;
   }
   
   public Collection getApplications() {
      return mApplications;
   }
   
   public JBossApplication saveApplication(
      JBossApplication pApplication
   ) {
      JBossApplication lApplication = null;
      if( lApplication != null ) {
         lApplication = getApplication( pApplication.getId() );
         if( lApplication == null ) {
            // No application found -> add
            mApplications.add( lApplication );
         }
         else {
            // Application found -> replace
            mApplications.remove( lApplication );
            mApplications.add( pApplication );
         }
      }
      return pApplication;
   }
   
   public void removeApplication(
      String pApplicationId
   ) {
      if( pApplicationId != null ) {
         JBossApplication lApplication = getApplication( pApplicationId );
         if( lApplication != null ) {
            mApplications.remove( lApplication );
         }
      }
   }
   
   // -------------------------------------------------------------------------
   // ServiceMBean - Methods
   // -------------------------------------------------------------------------  

   protected void initService()
        throws Exception
   {
      mJBossServer = new JBossServer( mName );
   }
   
   protected void startService()
        throws Exception
   {
      bind( mJBossServer );
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

	private void bind( JBossServer pServer )
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
         JBossServer.class.getName(),
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
