/***************************************
 *                                     *
 *  JBoss: The OpenSource J2EE WebOS   *
 *                                     *
 *  Distributable under LGPL license.  *
 *  See terms of license at gnu.org.   *
 *                                     *
 ***************************************/

package org.jboss.invocation.trunk.client;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;

import org.jboss.logging.Logger;

import EDU.oswego.cs.dl.util.concurrent.Channel;
import EDU.oswego.cs.dl.util.concurrent.LinkedQueue;
import EDU.oswego.cs.dl.util.concurrent.PooledExecutor;
import EDU.oswego.cs.dl.util.concurrent.Slot;
import EDU.oswego.cs.dl.util.concurrent.ThreadFactory;

/**
 * The CommTrunkRamp acts like the on and off ramps on a highway.  It merges the Invocation traffic from 
 * multiple threads and places it on one communications "Trunk".  It manages breaking
 * down an Invocation into it's composit TrunkRequest message (sent to the server) and the 
 * TrunkResponse message (sent to the client).  Once the request message is sent to the server,
 * the client thread block waiting for the correlated response to come back from the server.
 * While the client is blocked other threads can still send messages to the server and also block
 * waiting for the response.
 * 
 * Amoung the blocking clients that are waiting for a response, one will be able to slip into the 
 * "pump" which is in charge of getting messages from ITrunk and delivering it to the thread that is
 * waiting for the response.
 *
 * @author    <a href="mailto:hiram.chirino@jboss.org">Hiram Chirino</a>
 */
public final class CommTrunkRamp implements java.lang.Cloneable
{
   final static private Logger log = Logger.getLogger(CommTrunkRamp.class);

   /**
    * If the socket handler is currently pumping messages.
    */
   private volatile boolean pumpingData = false;

   /**
    * Requst create slots to wait for responses,
    * those slots are stored in this hashmap.
    * 
    * This field uses copy on write semantics.
    */
   volatile HashMap responseSlots = new HashMap();

   /**
    * Used to guard write access to the responseSlots object
    */
   Object responseSlotsMutex = new Object();

   /**
    * The request listner is notified of new requests
    * and of asyncronous IO errors.
    */
   ITrunkListner trunkListner;

   /**
    * The Trunk that this ramp is attached to.
    */
   ICommTrunk trunk;

   /**
    * The thread pool used to service incoming requests..
    */
   static PooledExecutor pool;

   /**
    * The number of pool threads created so far
    */
   private static int poolCounter = 0;

   /**
    * Constructor for the OILServerIL object
    *
    * @param a     Description of Parameter
    * @param port  Description of Parameter
    */
   public CommTrunkRamp(ICommTrunk trunk)
   {
      this.trunk = trunk;
      synchronized (CommTrunkRamp.class)
      {
         if (pool == null)
         {

            pool = new PooledExecutor(Integer.MAX_VALUE);
            pool.setMinimumPoolSize(0);
            pool.setKeepAliveTime(1000 * 60);
            pool.setThreadFactory(new ThreadFactory()
            {
               public Thread newThread(Runnable r)
               {
                  return new Thread(
                     ConnectionManager.oiThreadGroup,
                     r,
                     "Optimized Invoker Pool Thread-" + (poolCounter++));
               }
            });
         }
      }
   }

   /**
    * #Description of the Method
    */
   private void registerResponseSlot(TunkRequest request, Slot responseSlot) throws IOException
   {
      synchronized (responseSlotsMutex)
      {
         HashMap newMap = (HashMap) responseSlots.clone();
         newMap.put(request.requestId, responseSlot);
         responseSlots = newMap;
      }

   }

   /**
    * #Description of the Method
    */
   public void setTrunkListner(ITrunkListner requestListner)
   {
      this.trunkListner = requestListner;
   }

   public TrunkResponse synchRequest(TunkRequest request)
      throws IOException, InterruptedException, ClassNotFoundException
   {

      if (log.isTraceEnabled())
         log.trace("Sending request: " + request);

      Slot slot = new Slot();
      registerResponseSlot(request, slot);
      trunk.sendRequest(request);

      return (TrunkResponse)slot.take();
   }

   public void exceptionEvent(Exception e)
   {
      trunkListner.exceptionEvent(trunk, e);
   }


   /*
    * The Trunk should deliver reponses to the Ramp via this 
    * method.
    */
   public void deliverTrunkResponse(TrunkResponse response) throws InterruptedException
   {
      boolean tracing = log.isTraceEnabled();
      
      // Response received... find the response slot
      if (tracing)
         log.trace("Delivering Response Message");

      // No reponse id to response to..
      if (response.correlationRequestId == null) {
         if (tracing)
            log.trace("No correlation id found, cannot deliver response.");
         return;
      }

      Slot slot;
      synchronized (responseSlotsMutex)
      {
         HashMap newMap = (HashMap) responseSlots.clone();
         slot = (Slot) newMap.remove(response.correlationRequestId);
         responseSlots = newMap;
      }

      if (slot != null)
      {
         slot.put(response);
      }
      else
      {
         // This should not happen...
         log.warn("No slot registered for: " + response);
      }      
   }
   
   /*
    * The Trunk should deliver requests to the Ramp via this 
    * method.
    */
   public void deliverTrunkRequest(TunkRequest request) throws InterruptedException
   {
      pool.execute(new RequestRunner(request));
   }
   

   public class RequestRunner implements Runnable
   {
      TunkRequest request;
      RequestRunner(TunkRequest request)
      {
         this.request = request;
      }
      public void run()
      {
         trunkListner.requestEvent(trunk, request);
      }
   }

}