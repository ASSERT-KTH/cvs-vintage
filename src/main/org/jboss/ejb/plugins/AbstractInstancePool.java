/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.ejb.plugins;

import java.rmi.RemoteException;
import java.util.LinkedList;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;
import java.lang.reflect.Constructor;

import javax.ejb.EJBException;

import org.jboss.ejb.Container;
import org.jboss.ejb.InstancePool;
import org.jboss.ejb.EnterpriseContext;
import org.jboss.ejb.InstancePoolFeeder;

import org.jboss.deployment.DeploymentException;
import org.jboss.metadata.MetaData;
import org.jboss.metadata.XmlLoadable;

//import org.jboss.management.j2ee.CountStatistic;
import org.jboss.management.j2ee.SampleData;

import org.w3c.dom.Element;

import org.jboss.system.ServiceMBean;
import org.jboss.system.ServiceMBeanSupport;

/**
 * Abstract Instance Pool class containing the basic logic to create
 * an EJB Instance Pool.
 *
 * @author <a href="mailto:rickard.oberg@telkel.com">Rickard Öberg</a>
 * @author <a href="mailto:marc.fleury@jboss.org">Marc Fleury</a>
 * @author <a href="mailto:andreas.schaefer@madplanet.com">Andreas Schaefer</a>
 * @author <a href="mailto:sacha.labourey@cogito-info.ch">Sacha Labourey</a>
 * @version $Revision: 1.29 $
 *
 * <p><b>Revisions:</b>
 * <p><b>20010704 marcf:</b>
 * <ul>
 * <li>- Pools if used, do not reuse but restock the pile with fresh instances
 * </ul>
 * <p><b>20010709 andreas schaefer:</b>
 * <ul>
 * <li>- Added statistics gathering
 * </ul>
 * <p><b>20010920 Sacha Labourey:</b>
 * <ul>
 * <li>- Pooling made optional and only activated in concrete subclasses for SLSB and MDB
 * </ul>
 * <p><b>20011208 Vincent Harcq:</b>
 * <ul>
 * <li>- A TimedInstancePoolFeeder thread is started at first use of the pool
 *       and will populate the pool with new instances at a regular period.
 * </ul>
 */
public abstract class AbstractInstancePool
   extends ServiceMBeanSupport
   implements InstancePool, XmlLoadable, ServiceMBean
{
   protected Container container;

   protected LinkedList pool = new LinkedList();

   /** The maximum number of instances allowed in the pool */
   protected int maxSize = 30;

   /** determine if we reuse EnterpriseContext objects i.e. if we actually do pooling */
   protected boolean reclaim = false;

   protected InstancePoolFeeder poolFeeder;
   protected boolean useFeeder = false;
   
   /** Counter of all the Bean instantiated within the Pool **/
   protected SampleData mInstantiate = new SampleData( "InstantiationCount", SampleData.COUNT );
//   protected CountStatistic mInstantiate = new CountStatistic( "Instantiation", "", "Beans instantiated in Pool" );

   /** Counter of all the Bean destroyed within the Pool **/
   protected SampleData mDestroy = new SampleData( "InstantiationCount", SampleData.COUNT );
//   protected CountStatistic mDestroy = new CountStatistic( "Destroy", "", "Beans destroyed in Pool" );

   /** Counter of all the ready Beans within the Pool (which are not used now) **/
   protected SampleData mReadyBean = new SampleData( "InstantiationCount", SampleData.COUNT );
//   protected CountStatistic mReadyBean = new CountStatistic( "ReadyBean", "", "Numbers of ready Bean Pool" );

   /**
    * Set the callback to the container. This is for initialization.
    * The IM may extract the configuration from the container.
    *
    * @param   c
    */
   public void setContainer(final Container c)
   {
      this.container = c;
   }

   /**
    * @return Callback to the container which can be null if not set proviously
    */
   public Container getContainer()
   {
      return container;
   }

   protected void stopService() throws Exception
   {
      if (useFeeder && poolFeeder.isStarted())
      {
         poolFeeder.stop();
      }
   }
   
   protected void destroyService() throws Exception
   {
      if (useFeeder && poolFeeder.isStarted()) {
         throw new IllegalStateException("Instance pool destroyed before stopped");
      }
   
      freeAll();
      this.container = null;
   }

   /**
    * A pool is reclaim if it push back its dirty instances in its stack.
    */
   public boolean getReclaim()
   {
      return reclaim;
   }

   public void setReclaim(final boolean reclaim)
   {
      this.reclaim = reclaim;
   }

   /**
    * Add a instance in the pool
    */
   public void add() throws Exception
   {
      if (container == null) {
         throw new IllegalStateException("Attempt to add an instance to a destroyed pool");
      }
      
      EnterpriseContext ctx = create(container.createBeanClassInstance());
      if( log.isTraceEnabled() )
         log.trace("Add instance "+this+"#"+ctx);
      synchronized (pool)
      {
         pool.addFirst(ctx);
      }
   }

   /**
    * Get an instance without identity.
    * Can be used by finders,create-methods, and activation
    *
    * @return     Context /w instance
    * 
    * @throws Exception
    */
   public EnterpriseContext get()
      throws Exception
   {
      if( log.isTraceEnabled() )
         log.trace("Get instance "+this+"#"+pool.isEmpty()+"#"+getContainer().getBeanClass());

      EnterpriseContext ctx = null;
      synchronized (pool)
      {
         if (!pool.isEmpty())
         {
            mReadyBean.add();
            return (EnterpriseContext) pool.removeFirst();
         }
      }
      // pool is empty
      // The Pool feeder should avoid this
      if (useFeeder && poolFeeder.isStarted() && log.isDebugEnabled())
      {
         log.debug("The Pool for " + container.getBeanClass().getName()
            + " has been overloaded.  You should change pool parameters.");
      }
      try
      {
         synchronized (this)
         {
            if (useFeeder && ! poolFeeder.isStarted())
            {
               poolFeeder.start();
            }
         }
         return create(container.createBeanClassInstance());
      }
      catch (InstantiationException e)
      {
         throw new EJBException("Could not instantiate bean", e);
      }
      catch (IllegalAccessException e)
      {
         throw new EJBException("Could not instantiate bean", e);
      }
   }

   /**
    * Return an instance after invocation.
    *
    * Called in 2 cases:
    * a) Done with finder method
    * b) Just removed
    *
    * @param   ctx
    */
   public void free(EnterpriseContext ctx)
   {
      if( log.isTraceEnabled() )
      {
         String msg = maxSize+" Free instance:"+this+"#"+ctx.getId()
            +"#"+ctx.getTransaction()
            +"#"+reclaim
            +"#"+getContainer().getBeanClass();
         log.trace(msg);
      }
      ctx.clear();

      // If (!reclaim), we do not reuse but create a brand new instance simplifies the design
      try {
         mReadyBean.remove();
         if (this.reclaim)
         {
            // Add the unused context back into the pool
            synchronized (pool)
            {
               if (pool.size() < maxSize) 
               {
                  pool.addFirst(ctx);
                  return;
               } // end of if ()
            }
         }
         else
         {
            // Discard the context
            discard (ctx);
         }
      } catch (Exception ignored) {}
   }

   public int getMaxSize()
   {
      return this.maxSize;
   }

   public void discard(EnterpriseContext ctx)
   {
      // Throw away, unsetContext()
      try
      {
         mDestroy.add();
         ctx.discard();
      }
      catch (RemoteException e)
      {
         log.error("failed to discard", e);
      }
   }

   public int getCurrentSize()
   {
      synchronized (pool)
      {
         return this.pool.size();
      }
   }

   /**
    * XmlLoadable implementation
    */
   public void importXml(Element element) throws DeploymentException
   {
      String maximumSize = MetaData.getElementContent(MetaData.getUniqueChild(element, "MaximumSize"));
      try
      {
         this.maxSize = Integer.parseInt(maximumSize);
      }
      catch (NumberFormatException e)
      {
         throw new DeploymentException("Invalid MaximumSize value for instance pool configuration");
      }

      String feederPolicy = MetaData.getElementContent(MetaData.getOptionalChild(element, "feeder-policy"));
      if (feederPolicy != null)
      {
         useFeeder = true;
         try
         {
            Class cls = Thread.currentThread().getContextClassLoader().loadClass(feederPolicy);
            Constructor ctor = cls.getConstructor(new Class[] {});
            this.poolFeeder = (InstancePoolFeeder)ctor.newInstance(new Class[] {});
            this.poolFeeder.setInstancePool(this);
            this.poolFeeder.importXml(element);
         }
         catch (Exception x)
         {
            throw new DeploymentException("Can't create instance pool feeder", x);
         }
      }
      else
      {
         useFeeder = false;
      }
   }

   public Map retrieveStatistic()
   {
      Map lStatistics = new HashMap();
      lStatistics.put( mInstantiate.getName(), mInstantiate );
      lStatistics.put( mDestroy.getName(), mDestroy );
      lStatistics.put( mReadyBean.getName(), mReadyBean );
      return lStatistics;
   }

   public void resetStatistic()
   {
      mInstantiate.reset();
      mDestroy.reset();
      mReadyBean.reset();
   }

   protected abstract EnterpriseContext create(Object instance)
      throws Exception;

   /**
    * At undeployment we want to free completely the pool.
    */
   private void freeAll()
   {
      LinkedList clone = (LinkedList)pool.clone();
      for (Iterator i = clone.iterator(); i.hasNext(); )
      {
         EnterpriseContext ec = (EnterpriseContext)i.next();
         // Clear TX so that still TX entity pools get killed as well
         ec.clear();
         discard(ec);
      }
      pool.clear();
   }
}
