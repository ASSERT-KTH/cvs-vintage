/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.ejb;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import javax.ejb.CreateException;
import javax.ejb.EJBHome;
import javax.ejb.EJBMetaData;
import javax.ejb.EJBObject;
import javax.ejb.Handle;
import javax.ejb.HomeHandle;
import javax.ejb.RemoveException;
import javax.ejb.TimedObject;
import javax.ejb.Timer;
import javax.management.AttributeNotFoundException;
import javax.management.MBeanException;
import javax.management.ReflectionException;
import javax.resource.spi.endpoint.MessageEndpoint;
import javax.resource.spi.endpoint.MessageEndpointFactory;
import javax.resource.spi.endpoint.UnavailableException;
import javax.transaction.xa.XAResource;
import org.jboss.ejb.plugins.TxSupport;
import org.jboss.invocation.Invocation;
import org.jboss.invocation.InvocationResponse;
import org.jboss.invocation.InvocationType;
import org.jboss.metadata.ConfigurationMetaData;

/**
 * The container for <em>MessageDriven</em> beans.
 * @todo WARNING currently methods are put directly in the invocation,
 * without using the method hashing.
 *
 * @author <a href="mailto:peter.antman@tim.se">Peter Antman</a>.
 * @author <a href="mailto:rickard.oberg@telkel.com">Rickard Öberg</a>
 * @author <a href="mailto:marc.fleury@telkel.com">Marc Fleury</a>
 * @author <a href="mailto:docodan@mvcsoft.com">Daniel OConnor</a>
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @author <a href="mailto:Scott.Stark@jboss.org">Scott Stark</a>
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @version $Revision: 1.32 $
 */
public class MessageDrivenContainer extends Container
{
   /**
    * These are the mappings between the remote interface methods
    * and the bean methods.
    */
   protected Map beanMapping;

   /**
    * EJBProxyFactoryContainer - not needed, should we skip inherit this
    * or just throw Error??
    */
   public Class getHomeClass()
   {
      //throw new Error("HomeClass not valid for MessageDriven beans");
      return null;
   }

   public Class getRemoteClass()
   {
      //throw new Error("RemoteClass not valid for MessageDriven beans");
      return null;
   }
   //localClass actually exists now...
   public Class getLocalHomeClass()
   {
      //throw new Error("LocalHomeClass not valid for MessageDriven beans");
      return null;
   }

   protected void typeSpecificCreate()  throws Exception
   {
      setupBeanMapping();
      ConfigurationMetaData conf = getBeanMetaData().getContainerConfiguration();
      setInstancePool( createInstancePool( conf, getClassLoader() ) );
   }


   /**
    * The <code>getMessageEndpointFactory</code> method returns a
    * MessageEndpointFactory for this MessageDrivenBean.
    *
    * @return a <code>MessageEndpointFactory</code> value
    *
    * @jmx.managed-attribute
    */
   public MessageEndpointFactory getMessageEndpointFactory()
   {
      return new JBossMessageEndpointFactory();
   }

   public Object getAttribute(String attribute)
      throws AttributeNotFoundException,
             MBeanException,
             ReflectionException
   {
      if ("MessageEndpointFactory".equals(attribute))
      {
         return getMessageEndpointFactory();
      }
      return super.getAttribute(attribute);
   }

   // Container implementation - overridden here ----------------------

   protected void setupBeanMapping() throws Exception
   {
      //we intercept all non-bean methods, so who needs the map?
      // Map the bean methods
      Map map = new HashMap();
      if( TimedObject.class.isAssignableFrom( beanClass ) ) {
         // Map ejbTimeout
         map.put(
            TimedObject.class.getMethod( "ejbTimeout", new Class[] { Timer.class } ),
            beanClass.getMethod( "ejbTimeout", new Class[] { Timer.class } )
            );
      }
      beanMapping = map;
   }
   /**
    * @throws UnsupportedOperationException Not valid for MDB
    */
   public Object invokeHome(Invocation mi) throws Exception
   {
      throw new UnsupportedOperationException(
            "invokeHome not valid for MessageDriven beans");
   }

   // EJBHome implementation ----------------------------------------

   public EJBObject createHome()
      throws java.rmi.RemoteException, CreateException
   {
      throw new Error("createHome not valid for MessageDriven beans");
   }


   public void removeHome(Handle handle)
      throws java.rmi.RemoteException, RemoveException
   {
      throw new Error("removeHome not valid for MessageDriven beans");
      // TODO
   }

   public void removeHome(Object primaryKey)
      throws java.rmi.RemoteException, RemoveException
   {
      throw new Error("removeHome not valid for MessageDriven beans");
      // TODO
   }

   public EJBMetaData getEJBMetaDataHome()
      throws java.rmi.RemoteException
   {
      // TODO
      //return null;
      throw new Error("getEJBMetaDataHome not valid for MessageDriven beans");
   }

   public HomeHandle getHomeHandleHome()
      throws java.rmi.RemoteException
   {
      // TODO
      //return null;
      throw new Error("getHomeHandleHome not valid for MessageDriven beans");
   }

   Interceptor createContainerInterceptor()
   {
      return new ContainerInterceptor();
   }




   /**
    * The <code>isDeliveryTransacted</code> method is part of the
    * MessageEndpointFactory interface, and is also used by the
    * MessageEndpointInterceptor.
    *
    * @param m a <code>Method</code> value
    * @return a <code>boolean</code> value
    */
   public boolean isDeliveryTransacted(Method m)
   {
      boolean cmt = getBeanMetaData().isContainerManagedTx();
      if (cmt)
      {
         return TxSupport.REQUIRED == getBeanMetaData().getMethodTransactionType(m.getName(),
                                                                   m.getParameterTypes(),
                                                                   InvocationType.LOCAL);
      } // end of if ()
      return false;
   }

   /**
    * This is the last step before invocation - all interceptors are done
    */
   class ContainerInterceptor extends AbstractContainerInterceptor
   {
      /**
       * FIXME Design problem, who will do the acknowledging for
       * beans with bean managed transaction?? Probably best done in the
       * listener "proxys"
       *
       * NO! (david jencks 3/15/2003)
       *
       * According to the jca 1.5 spec pfd 2 12.5.7 p. 164, the
       * resource adapter delivering the message relies on successful
       * return from message delivery to determine that delivery took
       * place and is responsible for notifications and
       * acknowledgements.
       */
      public InvocationResponse invoke(Invocation mi) throws Exception
      {
         EnterpriseContext ctx = (EnterpriseContext)mi.getEnterpriseContext();


         // Get method and instance to invoke upon
         Method m = mi.getMethod();//(Method)beanMapping.get(mi.getMethod());

         // we have a method that needs to be done by a bean instance
         try {
            return new InvocationResponse(m.invoke(ctx.getInstance(), mi.getArguments()));
         }
         catch (Exception e) {
            rethrow(e);
         }

         // We will never get this far, but the compiler does not know that
         throw new org.jboss.util.UnreachableStatementException();
      }
   }


   public class JBossMessageEndpointFactory
   implements MessageEndpointFactory {

      // Implementation of javax.resource.spi.endpoint.MessageEndpointFactory

      /**
       * The <code>createEndpoint</code> method
       *
       * @param XAResource a <code>XAResource</code> value
       * @return a <code>MessageEndpoint</code> value
       * @exception UnavailableException if an error occurs
       */
      public MessageEndpoint createEndpoint(XAResource xaResource)
         throws UnavailableException
      {
         return getLocalProxyFactory().getMessageEndpoint(xaResource);
      }

      /**
       * The <code>isDeliveryTransacted</code> method
       *
       * @param method a <code>Method</code> value
       * @return a <code>boolean</code> value
       */
      public boolean isDeliveryTransacted(Method method)
      {
         return MessageDrivenContainer.this.isDeliveryTransacted(method);
      }

   }
}
