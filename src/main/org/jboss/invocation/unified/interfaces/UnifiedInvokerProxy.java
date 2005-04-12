/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.invocation.unified.interfaces;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.StreamCorruptedException;
import java.rmi.MarshalledObject;
import java.rmi.RemoteException;
import java.rmi.ServerException;
import org.jboss.invocation.Invocation;
import org.jboss.invocation.Invoker;
import org.jboss.invocation.unified.marshall.InvocationMarshaller;
import org.jboss.invocation.unified.marshall.InvocationUnMarshaller;
import org.jboss.logging.Logger;
import org.jboss.remoting.Client;
import org.jboss.remoting.InvokerLocator;

/**
 * This represents the client side of the EJB invoker.  This invoker uses
 * the remoting framework for making invocations.
 *
 * @author <a href="mailto:tom.elrod@jboss.com">Tom Elrod</a>
 */
public class UnifiedInvokerProxy implements Invoker, Externalizable
{
   static final long serialVersionUID = -1108158470271861548L;

   private transient Client client;
   private InvokerLocator locator;

   protected final Logger log = Logger.getLogger(getClass());

   static final int VERSION_5_0 = 500;
   static final int CURRENT_VERSION = VERSION_5_0;


   public UnifiedInvokerProxy()
   {
      super();
   }

   public UnifiedInvokerProxy(InvokerLocator locator)
   {
      this.locator = locator;
      init(locator);

   }

   private void init(InvokerLocator locator)
   {
      if(client == null)
      {
         try
         {
            client = new Client(locator, "invoker");
            client.connect();

            //TODO: -TME (JBREM-51) Is not good that this is hardcoded.
            client.getInvoker().setMarshaller(new InvocationMarshaller());
            client.getInvoker().setUnMarshaller(new InvocationUnMarshaller());
         }
         catch(Exception e)
         {
            e.printStackTrace();
         }
      }
   }


   /**
    * A free form String identifier for this delegate invoker, can be clustered or target node
    * This should evolve in a more advanced meta-inf object.
    * <p/>
    * This will return the host supplied by the invoker locator if locator is not null.  Otherwise, if the locator is null, will
    * return null.
    */
   public String getServerHostName() throws Exception
   {
      if(locator != null)
      {
         return locator.getHost();
      }
      else
      {
         return null;
      }
   }

   /**
    * @param invocation A pointer to the invocation object
    * @return Return value of method invocation.
    * @throws Exception Failed to invoke method.
    */
   public Object invoke(Invocation invocation) throws Exception
   {
      Object response = null;

      try
      {
         response = client.invoke(invocation, null);

         if(response instanceof Exception)
         {
            throw ((Exception) response);
         }
         if(response instanceof MarshalledObject)
         {
            return ((MarshalledObject) response).get();
         }
         return response;

      }
      catch(RemoteException aex)
      {
         // per Jira issue JBREM-61
         throw new ServerException(aex.getMessage(), aex);
      }
      catch(Throwable throwable)
      {
         // this is somewhat of a hack as remoting throws throwable,
         // so will let Exception types bubble up, but if Throwable type,
         // then have to wrap in new Exception, as this is the signature
         // of this invoke method.
         if(throwable instanceof Exception)
         {
            throw (Exception) throwable;
         }
         throw new Exception(throwable);
      }
   }

   /**
    * Externalize this instance and handle obtaining the remoteInvoker stub
    */
   public void writeExternal(final ObjectOutput out)
         throws IOException
   {
      out.writeInt(CURRENT_VERSION);

      out.writeObject(locator);
   }

   /**
    * Un-externalize this instance.
    */
   public void readExternal(final ObjectInput in)
         throws IOException, ClassNotFoundException
   {
      int version = in.readInt();
      // Read in and map the version of the serialized data seen
      switch(version)
      {
         case VERSION_5_0:
            {
               locator = (InvokerLocator) in.readObject();
               init(locator);
            }
         default:
            throw new StreamCorruptedException("Unknown version seen: " + version);
      }
   }

}
