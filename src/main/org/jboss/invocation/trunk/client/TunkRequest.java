/***************************************
 *                                     *
 *  JBoss: The OpenSource J2EE WebOS   *
 *                                     *
 *  Distributable under LGPL license.  *
 *  See terms of license at gnu.org.   *
 *                                     *
 ***************************************/
package org.jboss.invocation.trunk.client;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.jboss.invocation.Invocation;
import org.jboss.invocation.MarshalledInvocation;
import org.jboss.logging.Logger;

/**
 * This is the request message that will be sent to a client
 *
 * @author    <a href="mailto:hiram.chirino@jboss.org">Hiram Chirino</a>
 */
public class TunkRequest
{
   final static private Logger log = Logger.getLogger(TunkRequest.class);

   final static byte REQUEST_INVOKE = 1;
   final static byte REQUEST_GET_SERVER_HOST_NAME = 2;

   private static int lastRequestId = 0;
   private static ClassLoader classLoader = TunkRequest.class.getClassLoader();

   public byte operation;
   public Integer requestId;
   public Invocation invocation;
   

   public TunkRequest()
   {
   }

   public void setOpInvoke(Invocation invocation)
   {
      this.operation = REQUEST_INVOKE;
      this.requestId = new Integer(lastRequestId++);
      this.invocation = invocation;
   }

   public void setOpServerHostName()
   {
      this.operation = REQUEST_GET_SERVER_HOST_NAME;
      this.requestId = new Integer(lastRequestId++);
      this.invocation = null;
   }

   public byte[] serialize() throws IOException
   {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      ObjectOutputStream out = new CustomObjectOutputStream(baos);

      out.writeByte(operation);
      out.writeInt(requestId.intValue());
      switch (operation)
      {
         case REQUEST_INVOKE :
            // We are going to go through a Remote invocation, switch to a Marshalled Invocation
            MarshalledInvocation mi = new MarshalledInvocation(invocation);

            // Set the transaction propagation context
            //  @todo: MOVE TO TRANSACTION
            //mi.setTransactionPropagationContext(getTransactionPropagationContext());
            mi.writeExternal(out);
            break;
         case REQUEST_GET_SERVER_HOST_NAME :
            break;
      }

      out.close();
      return baos.toByteArray();

   }

   public void deserialize(byte data[]) throws IOException, ClassNotFoundException
   {
      ByteArrayInputStream bais = new ByteArrayInputStream(data);
      ObjectInputStream in = new CustomObjectInputStreamWithClassloader(bais, classLoader);

      operation = in.readByte();
      requestId = new Integer(in.readInt());
      switch (operation)
      {
         case REQUEST_INVOKE :
            MarshalledInvocation mi = new MarshalledInvocation();
            mi.readExternal(in);
            invocation = mi;
            break;
         case REQUEST_GET_SERVER_HOST_NAME :
            break;
      }
   }

   public String toString()
   {
      return "[operation:" + operation + ",requestId:" + requestId + ",invocation:" + invocation + "]";
   }
}
