/***************************************
 *                                     *
 *  JBoss: The OpenSource J2EE WebOS   *
 *                                     *
 *  Distributable under LGPL license.  *
 *  See terms of license at gnu.org.   *
 *                                     *
 ***************************************/

package org.jboss.invocation.trunk.client.nbio;

import java.io.IOException;
import java.net.SocketException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.Set;

import org.jboss.logging.Logger;

/**
 * The SelectorManager will manage one Selector and the thread that checks 
 * the selector.
 * 
 * We may need to consider running more than one thread to check the selector
 * if servicing the selector takes too long.
 *
 * @author    <a href="mailto:hiram.chirino@jboss.org">Hiram Chirino</a>
 */
public class SelectorManager implements Runnable
{

   static final private Logger log = Logger.getLogger(SelectorManager.class);
   static private SelectorManager instance;
   /**
    * The running flag that all worker and server
    * threads check to determine if the service should
    * be stopped.
    */
   private volatile boolean running;

   /**
    * The selector used to wait for non-blocking events.
    */
   private Selector selector;

   /**
    * The groupd that threads created by this class will be a member of.
    */
   private ThreadGroup threadGroup;

   /**
    * how many times we have been started ++ and stoped --
    */
   private int startCounter;

   protected SelectorManager(ThreadGroup threadGroup) throws IOException
   {
      this.threadGroup = threadGroup;
      selector = Selector.open();
   }

   public synchronized static SelectorManager getInstance(ThreadGroup threadGroup) throws IOException
   {
      if (instance == null)
         instance = new SelectorManager(threadGroup);
      return instance;
   }

   /**
    * Main processing method for the OILServerILService object
    */
   public void run()
   {
      try
      {

         while (running)
         {

            if (log.isInfoEnabled())
               log.trace("Waiting for selector to return");
            // This call blocks until there is activity on one of the 
            // registered channels. This is the key method in non-blocking I/O.
            selector.select();

            // Get a java.util.Set containing the SelectionKey objects for
            // all channels that are ready for I/O.
            Set keys = selector.selectedKeys();

            if (log.isInfoEnabled())
               log.trace("We have " + keys.size() + " keys to process.");

            // Use a java.util.Iterator to loop through the selected keys
            for (Iterator i = keys.iterator(); i.hasNext();)
            {
               SelectionKey key = (SelectionKey) i.next();
               i.remove(); // Remove the key from the set of selected keys
                ((SelectionAction) key.attachment()).service(key);
            }
         }
      }
      catch (SocketException e)
      {
         // There is no easy way (other than string comparison) to
         // determine if the socket exception is caused by connection
         // reset by peer. In this case, it's okay to ignore both
         // SocketException and IOException.
         log.warn("SocketException occured (Connection reset by peer?).");
      }
      catch (IOException e)
      {
         log.warn("IOException occured. Cannot initialize the OILServerILService.");
      }
   }

   synchronized public void start()
   {
      startCounter++;
      if (startCounter == 1)
      {
         log.debug("Starting a Selector Work thread.");
         running = true;
         new Thread(threadGroup, (Runnable) this, "Selector Worker").start();
      }
   }

   synchronized public void stop()
   {
      startCounter--;
      if (startCounter == 0)
      {
         log.debug("Stopping a Selector Work thread.");
         running = false;
      }
   }

   public Selector getSelector()
   {
      return selector;
   }

}
