
/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 *
 */

package org.jboss.invocation.remoting;

import java.io.ObjectStreamException;
import java.io.Serializable;
import javax.management.ObjectName;
import org.jboss.invocation.Invocation;
import org.jboss.invocation.InvocationResponse;
import org.jboss.invocation.Invoker;
import org.jboss.invocation.MarshalledInvocation;
import org.jboss.invocation.ServerID;
import org.jboss.remoting.Client;
import org.jboss.remoting.InvokerLocator;
import org.jboss.remoting.ident.Identity;
import org.jboss.system.ServiceMBean;
import org.jboss.system.client.ClientServiceMBeanSupport;
import org.jboss.util.jmx.ObjectNameFactory;



/**
 * RemotingAdapter.java
 *
 *
 * Created: Tue Apr 22 23:49:03 2003
 *
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @version 1.0
 *
 * @jmx.mbean extends="org.jboss.system.ServiceMBean"
 */
public class RemotingAdapter
   extends ClientServiceMBeanSupport
   implements Invoker, RemotingAdapterMBean, Serializable
{

   /**
    * The field <code>connectorName</code> links to the remoting
    * connector, used primarily on the server side to discover what
    * our locatorURI is.
    *
    */
   private transient ObjectName connectorName;

   /**
    * The field <code>locatorURI</code> holds the locatorURI that is
    * used on the client to connect to the appropriate server using
    * the appropriate transport.
    *
    */
   private String locatorURI;

   /**
    * The field <code>client</code> holds the remoting client used as
    * the client side endpoint of the transport/subsystem combination.
    *
    */
   private transient Client client;

   /**
    * The field <code>identity</code> holds the remoting identity of
    * the server we are connecting to.  It is used to name the client
    * side mbean and to determine if we are on the client or server.
    *
    */
   private Identity identity;

   public RemotingAdapter()
   {

   } // RemotingAdapter constructor

   protected void startService() throws Exception
   {
      //don't overwrite deserialized identity
      if (identity == null)
      {
         identity = Identity.get(getServer());
      } // end of if ()
      if (locatorURI == null)
      {
         locatorURI = (String)getServer().getAttribute(connectorName, "InvokerLocator");
      } // end of if ()

      InvokerLocator locator = new InvokerLocator(locatorURI);
      client = new Client(locator, "EJB");
   }

   protected void stopService() throws Exception
   {
      client = null;
   }

   /**
    * Get the ConnectorName value.
    * @return the ConnectorName value.
    *
    * @jmx.managed-attribute
    */
   public ObjectName getConnectorName()
   {
      return connectorName;
   }

   /**
    * Set the ConnectorName value.
    * @param connectorName The new ConnectorName value.
    *
    * @jmx.managed-attribute
    */
   public void setConnectorName(ObjectName connectorName)
   {
      this.connectorName = connectorName;
   }




   // Implementation of org.jboss.invocation.Invoker

   /**
    * The <code>getServerID</code> method is obsolete and not used with remoting
    *
    * @return a <code>ServerID</code> value
    * @exception Exception if an error occurs
    */
   public ServerID getServerID() throws Exception
   {
      return null;
   }

   /**
    * The <code>getIdentity</code> method returns the remoting server
    * identity, used to identify all parts of the invoker chain for
    * ejb invocation.
    *
    * @return an <code>Identity</code> value
    */
   public Identity getIdentity()
   {
      return identity;
   }


   /**
    * The <code>invoke</code> method translates the ejb invocation
    * object into a remoting framework invocation.  I suggest these
    * could become better aligned to eliminate any translation.
    *
    * @param invocation an <code>Invocation</code> value
    * @return an <code>InvocationResponse</code> value
    * @exception Exception if an error occurs
    */
   public InvocationResponse invoke(Invocation invocation) throws Throwable
   {

      Object result = client.invoke("",
                                    new MarshalledInvocation(invocation),
                                    null);
      return (InvocationResponse)result;
   }

   /**
    * The <code>getIdentityNameClause</code> method converts the
    * remoting identity into two name-value pairs for inclusion in the
    * client-side object name of this invoker.
    *
    * @return a <code>String</code> value
    */
   private String getIdentityNameClause()
   {
      return "domain=" + identity.getDomain() + ",instanceid=" + identity.getInstanceId();
   }

   /**
    * The <code>internalSetServiceName</code> method sets the client
    * side object name based on the remoting identity object.
    *
    * @exception Exception if an error occurs
    */
   protected void internalSetServiceName() throws Exception
   {
      serviceName = ObjectNameFactory.create("jboss.client:service=RemotingAdapter," + getIdentityNameClause());
      getLog().info("internalSetServiceName called: " + serviceName);
   }

   private Object readResolve() throws ObjectStreamException
   {
      return internalReadResolve();
   }

   /**
    * The <code>internalSetup</code> method method registers this as a
    * client-side mbean, then starts this.
    *
    * @exception Exception if an error occurs
    */
   protected void internalSetup() throws Exception
   {
      //register ourselves
      if (!server.isRegistered(getServiceName()))
      {
         super.internalSetup();
      } // end of if ()
      else
      {
         log.info("Got to internal setup even though already registered");
      } // end of else

      startService();
   }


} // RemotingAdapter
