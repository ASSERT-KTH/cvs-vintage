
/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 *
 */

package org.jboss.ejb.plugins.local;

import java.lang.reflect.Method;
import javax.resource.spi.endpoint.MessageEndpoint;
import javax.transaction.Transaction;
import javax.transaction.xa.XAResource;



/**
 * MessageEndpointContext.java
 *
 *
 * Created: Sat May 31 20:35:49 2003
 *
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @version 1.0
 */
public class MessageEndpointContext
{

   public static final Method BEFORE_DELIVERY;
   public static final Method AFTER_DELIVERY;

   static
   {
      try
      {
         BEFORE_DELIVERY = MessageEndpoint.class.getMethod("beforeDelivery", new Class[] {Method.class});
         AFTER_DELIVERY = MessageEndpoint.class.getMethod("afterDelivery", new Class[] {});
      }
      catch (NoSuchMethodException e)
      {
         throw new RuntimeException("Missing method in java class: " + e.getMessage());
      } // end of try-catch
   }

   private XAResource xaResource;
   private Transaction sourceTransaction;
   private Transaction transaction;
   private boolean inDelivery;
   private boolean deliveryTransacted;

   public MessageEndpointContext()
   {

   } // MessageEndpointContext constructor


   /**
    * Get the XaResource value.
    * @return the XaResource value.
    */
   public XAResource getXAResource()
   {
      return xaResource;
   }

   /**
    * Set the XaResource value.
    * @param XaResource The  XaResource value.
    */
   public void setXAResource(XAResource xaResource)
   {
      this.xaResource = xaResource;
   }


   /**
    * Get the SourceTransaction value.
    * @return the SourceTransaction value.
    */
   public Transaction getSourceTransaction()
   {
      return sourceTransaction;
   }

   /**
    * Set the SourceTransaction value.
    * @param SourceTransaction The  SourceTransaction value.
    */
   public void setSourceTransaction(Transaction sourceTransaction)
   {
      this.sourceTransaction = sourceTransaction;
   }


   /**
    * Get the Transaction value.
    * @return the Transaction value.
    */
   public Transaction getTransaction()
   {
      return transaction;
   }

   /**
    * Set the Transaction value.
    * @param Transaction The  Transaction value.
    */
   public void setTransaction(Transaction transaction)
   {
      this.transaction = transaction;
   }

   /**
    * Get the InDelivery value.
    * @return the InDelivery value.
    */
   public boolean isInDelivery()
   {
      return inDelivery;
   }

   /**
    * Set the InDelivery value.
    * @param inDelivery The  inDelivery value.
    */
   public void setInDelivery(boolean inDelivery)
   {
      this.inDelivery = inDelivery;
   }

   /**
    * Get the DeliveryTransacted value.
    * @return the DeliveryTransacted value.
    */
   public boolean isDeliveryTransacted()
   {
      return deliveryTransacted;
   }

   /**
    * Set the DeliveryTransacted value.
    * @param deliveryTransacted The new DeliveryTransacted value.
    */
   public void setDeliveryTransacted(boolean deliveryTransacted)
   {
      this.deliveryTransacted = deliveryTransacted;
   }




} // MessageEndpointContext
