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
import javax.transaction.xa.Xid;
import org.jboss.invocation.Invocation;
import org.jboss.invocation.InvocationKey;
import org.jboss.invocation.MarshalledInvocation;
import org.jboss.invocation.PayloadKey;
import org.jboss.logging.Logger;

/**
 * This is the request message that will be sent to a client
 *
 * @author    <a href="mailto:hiram.chirino@jboss.org">Hiram Chirino</a>
 */
public class TrunkRequest
{
   final static private Logger log = Logger.getLogger(TrunkRequest.class);

   final static byte REQUEST_INVOKE = 1;
   final static byte REQUEST_GET_SERVER_HOST_NAME = 2;
   final static byte REQUEST_PREPARE = 3;
   final static byte REQUEST_COMMIT_1P = 4;
   final static byte REQUEST_COMMIT_2P = 5;
   final static byte REQUEST_ROLLBACK = 6;
   final static byte REQUEST_FORGET = 7;
   final static byte REQUEST_RECOVER_STARTRSCAN = 8;
   final static byte REQUEST_RECOVER_TMNOFLAGS = 9;
   final static byte REQUEST_RECOVER_ENDRSCAN = 10;

   private static int lastRequestId = 0;
   private static ClassLoader classLoader = TrunkRequest.class.getClassLoader();

   public byte operation;
   public Integer requestId;
   public Invocation invocation;
   public Xid xid;
   public int transactionTimeout;
   

   public TrunkRequest()
   {
   }

   public void setOpInvoke(Invocation invocation, Xid xid, int transactionTimeout)
   {
      this.operation = REQUEST_INVOKE;
      this.requestId = new Integer(lastRequestId++);
      this.invocation = invocation;
      this.xid = xid;
      this.transactionTimeout = transactionTimeout;
   }

   public void setOpServerHostName()
   {
      this.operation = REQUEST_GET_SERVER_HOST_NAME;
      this.requestId = new Integer(lastRequestId++);
      this.invocation = null;
   }

   public void setOpTxOp(byte op, Xid xid, int transactionTimeout)
   {
      if (op < REQUEST_PREPARE || op > REQUEST_RECOVER_ENDRSCAN)
      {
         throw new IllegalArgumentException("Must use a tx op, not: " + op);
      } // end of if ()
      
      this.operation = op;
      this.requestId = new Integer(lastRequestId++);
      this.xid = xid;
      this.invocation = null;
      this.transactionTimeout = transactionTimeout;
   }


   public byte[] serialize() throws IOException
   {
      ByteArrayOutputStream baos = new ByteArrayOutputStream(1000);
      ObjectOutputStream out = new CustomObjectOutputStream(baos);

      out.writeByte(operation);
      out.writeInt(requestId.intValue());
      switch (operation)
      {
         case REQUEST_INVOKE :
            // We are going to go through a Remote invocation, switch to a Marshalled Invocation
            MarshalledInvocation mi = new MarshalledInvocation(invocation);
            //set the xid from our context.  Perhaps this could be AS_IS key.
            mi.setValue(InvocationKey.XID, xid, PayloadKey.PAYLOAD);
            mi.setValue(InvocationKey.TX_TIMEOUT, new Integer(transactionTimeout), PayloadKey.AS_IS);
            mi.writeExternal(out);
            break;
         case REQUEST_GET_SERVER_HOST_NAME :
           break;
         case REQUEST_PREPARE:
         case REQUEST_COMMIT_1P:
         case REQUEST_COMMIT_2P:
         case REQUEST_ROLLBACK:
         case REQUEST_FORGET:
            out.writeObject(xid);
            out.writeInt(transactionTimeout);
            break;
      case REQUEST_RECOVER_STARTRSCAN:
      case REQUEST_RECOVER_TMNOFLAGS:
      case REQUEST_RECOVER_ENDRSCAN:
            out.writeInt(transactionTimeout);
            break;
      default:
         throw new IOException("Invalid op: " + operation);
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
            xid = (Xid)invocation.getPayloadValue(InvocationKey.XID);
            transactionTimeout = ((Integer)invocation.getValue(InvocationKey.TX_TIMEOUT)).intValue();
            break;
         case REQUEST_GET_SERVER_HOST_NAME :
            break;

         case REQUEST_PREPARE:
         case REQUEST_COMMIT_1P:
         case REQUEST_COMMIT_2P:
         case REQUEST_ROLLBACK:
         case REQUEST_FORGET:
            xid = (Xid)in.readObject();
            transactionTimeout = in.readInt();
            break;
      case REQUEST_RECOVER_STARTRSCAN:
      case REQUEST_RECOVER_TMNOFLAGS:
      case REQUEST_RECOVER_ENDRSCAN:
            transactionTimeout = in.readInt();
            break;
      default:
            throw new IOException("Invalid op: " + operation);

      }
   }

   public String toString()
   {
      return "[operation:" + operation + ",requestId:" + requestId + ",invocation:" + invocation + "]";
   }
}
