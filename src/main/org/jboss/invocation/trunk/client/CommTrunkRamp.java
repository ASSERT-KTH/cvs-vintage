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
    * Pump mutex
    */
   private Object pumpMutex = new Object();

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
    * The that new request get placed into when they arrived.
    */
   LinkedQueue requestQueue = new LinkedQueue();

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
            pool.setKeepAliveTime(1000 * 20);
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

   /**
    *  Pumps messages from the input stream.
    *  
    *  If the request object is not null, then the target message is 
    *  the response object for the request argument.  The target
    *  message is returned.
    * 
    *  If the request object is null, then the target message is 
    *  the first new request that is encountered.  The new request 
    *  messag is returned.
    * 
    *  All message received before the target message are pumped.
    *  A pumped message is placed in either Response Slots or
    *  the Request Queue depending on if the message is a response
    *  or requests.
    * 
    * @param request The request object that is waiting for a response.
    * @return the request or reponse object that this method was looking for
    * @exception  IOException  Description of Exception
    */
   private Object pumpMessages(TunkRequest request, Channel mySlot)
      throws IOException, ClassNotFoundException, InterruptedException
   {

      synchronized (pumpMutex)
      {
         // Is somebody else pumping data??
         if (pumpingData)
            return null;
         else
            pumpingData = true;
      }

      try
      {
         while (true)
         {
            if (mySlot != null)
            {
               // Do we have our response sitting in our slot allready??
               Object o;
               while ((o = mySlot.peek()) != null)
               {
                  o = mySlot.take();
                  if (o != this)
                  {
                     return o;
                  }
               }
            }

            boolean tracing = log.isTraceEnabled();
            Object o = trunk.getNextMessage();
            if (o instanceof TunkRequest)
            {
               TunkRequest newRequest = (TunkRequest) o;

               // Are we looking for a request??
               if (request == null)
               {
                  if (tracing)
                     log.trace("Target message arrvied: returning the new request: " + newRequest);
                  return newRequest;
               }
               else
               {
                  if (tracing)
                     log.trace("Not the target message: queueing the new request: " + newRequest);
                  requestQueue.put(newRequest);
               }

            }
            else
            {

               // Response received... find the response slot
               if (tracing)
                  log.trace("Reading Response Message");

               TrunkResponse response = (TrunkResponse) o;

               // No reponse id to response to..
               if (response.correlationRequestId == null)
                  continue;

               // Is this the response object we are looking for
               if (request != null && request.requestId.equals(response.correlationRequestId))
               {
                  if (tracing)
                     log.trace("Target message arrvied: returning the response: " + response);
                  return response;
               }
               else
               {
                  if (tracing)
                     log.trace("Not the target message: Sending to request slot: " + response);

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
            }
         } // while         
      }
      finally
      {
         synchronized (pumpMutex)
         {
            pumpingData = false;
         }

         // We are done, let somebody know that they can 
         // start pumping us again.         
         HashMap snapShot = responseSlots;
         if (snapShot.size() > 0)
         {
            Iterator i = snapShot.values().iterator();
            while (i.hasNext())
            {
               Slot s = (Slot) i.next();
               if (s != mySlot)
                  s.offer(this, 0);
            }
         }

         // Only notify the request waiter if we are not
         // giving him a message on this method call.
         if (request != null)
         {
            requestQueue.put(this);
         }
      }
   }

   public TrunkResponse synchRequest(TunkRequest request)
      throws IOException, InterruptedException, ClassNotFoundException
   {

      if (log.isTraceEnabled())
         log.trace("Sending request: " + request);

      Slot slot = new Slot();
      registerResponseSlot(request, slot);
      trunk.sendRequest(request);

      Object o = null;
      while (true)
      {
         // Do we have something in our queue??
         if (o != null)
         {
            // was is a request message??
            if (o != this)
            {
               if (log.isTraceEnabled())
                  log.trace("Got response: " + o);
               return (TrunkResponse) o;
            }
            // See if we have another message in the queue.
            o = slot.peek();
            if (o != null)
               o = slot.take();
         }
         else
         {
            // We did not have any messages in the slot,
            // so we have to go pumping..
            o = pumpMessages(request, slot);
            if (o == null)
            {
               // Somebody else is in the pump, wait till we 
               // are notified to get in.
               o = slot.take();
            }
         }
      } // end while
   }

   public void exceptionEvent(Exception e)
   {
      trunkListner.exceptionEvent(trunk, e);
   }

   /*
    * The blocking trunk's connection thread uses this method 
    * to ensure that new requests are serviced.  This method returns
    * when the trunk disconnects.
    */
   public void pumpRequest() throws IOException, InterruptedException, ClassNotFoundException
   {
      Object o = null;
      while (trunk.isConnected())
      {
         // Do we have something in our queue??
         if (o != null)
         {
            // was is a request message??
            if (o != this)
            {
               deliver((TunkRequest) o);
               return;
            }
            // See if we have another message in the queue.
            o = requestQueue.peek();
            if (o != null)
               o = requestQueue.take();
         }
         else
         {
            // We did not have any messages in the queue,
            // so we have to go pumping..
            o = pumpMessages(null, requestQueue);
            if (o == null)
            {
               // Somebody else is in the pump, wait till we 
               // are notified to get in.
               o = requestQueue.take();
            }
         }
      } // end while
   }

   /*
    * The non-blocking trunk's connection thread uses method 
    * to deliver new requests.  There is no need to have a thread
    * dedicated to powering the pumpRequest() methods because
    * the non-blocking trunk gets events when new requests 
    * arrive.
    */
   public void deliver(TunkRequest request) throws InterruptedException
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