/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb;


import org.jboss.monitor.StatisticsProvider;

/**
 * Defines the model for a EnterpriseContext instance pool.
 *
 * @author <a href="mailto:rickard.oberg@telkel.com">Rickard Öberg</a>
 * @version $Revision: 1.10 $
 *
 * <p><b>Revisions:</b>
 * <p><b>20010718 andreas schaefer:</b>
 * <ul>
 * <li>- Marked the InstancePool as Statistics Provider
 * </ul>
 * <p><b>20011208 Vincent Harcq:</b>
 * <ul>
 * <li> Added a Pool Feeder (Thread that pre-create bean instances to avoid
 *      setContext overhead.
 * </ul>
 */
public interface InstancePool
   extends ContainerPlugin, StatisticsProvider
{
   /**
    *  Add an instance in the Pool.
    *  Is used by the TimedInstancePoolFeeder thread to create instances ready for use by
    *  the interceptor.
    *
    * @exception Exception when an Instance can not be instantiated
    */
   public void add()
         throws Exception;

   /**
    * Get an instance without identity.
    *
    * <p>Can be used by finders and create-methods, or stateless beans
    *
    * @return    Context/w instance
    *
    * @throws Exception    RemoteException
    */
   EnterpriseContext get() throws Exception;

   /**
    * Return an anonymous instance after invocation.
    *
    * @param ctx    The context to free.
    */
   void free(EnterpriseContext ctx);

   /**
    * Discard an anonymous instance after invocation.
    * This is called if the instance should not be reused, perhaps due to some
    * exception being thrown from it.
    *
    * @param ctx    The context to discard.
    */
   void discard(EnterpriseContext ctx);

   /**
    * Return the size of the pool.
    *
    * @return the size of the pool.
    */
   int getCurrentSize();

   /**
    * Get the maximum size of the pool.
    *
    * @return the size of the pool.
    */
   public int getMaxSize();

}

