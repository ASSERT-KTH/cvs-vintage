/*
* JBoss, the OpenSource J2EE webOS
*
* Distributable under LGPL license.
* See terms of license at gnu.org.
*/
package org.jboss.jmx.adaptor.xml;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.Name;
import javax.naming.NamingException;
import javax.naming.NameNotFoundException;
import javax.naming.Reference;
import javax.naming.StringRefAddr;

// import org.jboss.logging.Log;
import org.jboss.naming.NonSerializableFactory;

import org.jboss.system.ServiceMBeanSupport;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
* MBean Wrapper for the XML Adaptor.
*
* @author Andreas Schaefer (andreas.schaefer@madplanet.com)
* @created June 22, 2001
* @version $Revision: 1.3 $
*/
public class XMLAdaptorService
  extends ServiceMBeanSupport
  implements XMLAdaptorServiceMBean
{
  // Attributes ----------------------------------------------------
  XMLAdaptorImpl mAdaptor;

  // Constants -----------------------------------------------------
  public static String JNDI_NAME = "jmx:xml";


  // Static --------------------------------------------------------

  // Constructors --------------------------------------------------

  // Public --------------------------------------------------------

  public Object[] invokeXML( Document pJmxOperations ) {
    return mAdaptor.invokeXML( pJmxOperations );
  }

  public Object invokeXML( Element pJmxOperation ) {
    return mAdaptor.invokeXML( pJmxOperation );
  }

  public ObjectName getObjectName( MBeanServer pServer, ObjectName pName )
    throws
      javax.management.MalformedObjectNameException
  {
    return pName;
  }

  public String getName() {
    return "JMX XML-Adaptor";
  }

  // Protected -----------------------------------------------------

  protected void startService()
    throws
      Exception
  {
    mAdaptor = new XMLAdaptorImpl( getServer() );
    bind( mAdaptor );
  }

  protected void stopService() {
    try
    {
      unbind();
    }
    catch( Exception e )
    {
// AS ToDo
//      log.exception( e );
    }
  }
	private void bind( XMLAdaptorImpl pImplementation )
      throws
         NamingException
   {
		Context lContext = new InitialContext();

		// Ah ! JBoss Server isn't serializable, so we use a helper class
		NonSerializableFactory.bind( JNDI_NAME, pImplementation );

      //AS Don't ask me what I am doing here
		Name lName = lContext.getNameParser("").parse( JNDI_NAME );
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
		StringRefAddr lAddress = new StringRefAddr( "nns", JNDI_NAME );
		Reference lReference = new Reference(
         XMLAdaptorImpl.class.getName(),
         lAddress,
         NonSerializableFactory.class.getName(),
         null
      );
		lContext.bind( lName.get( 0 ), lReference );

// AS ToDo
//		log.log( "JBoss XML Adaptor Service '" + JNDI_NAME + "' bound to " + JNDI_NAME );
	}

	private void unbind() throws NamingException
	{
      new InitialContext().unbind( JNDI_NAME );
      NonSerializableFactory.unbind( JNDI_NAME );
// AS ToDo
//      log.log("JBoss XML Adaptor service '" + JNDI_NAME + "' removed from JNDI" );
	}

}

