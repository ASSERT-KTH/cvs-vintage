/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins;

import java.rmi.RemoteException;
import java.rmi.ServerException;
import java.util.Map;
import java.util.HashMap;

import org.jboss.ejb.Container;
import org.jboss.ejb.InstancePool;
import org.jboss.ejb.EnterpriseContext;
import org.jboss.ejb.StatelessSessionEnterpriseContext;
import org.jboss.ejb.DeploymentException;

import org.jboss.metadata.XmlLoadable;
import org.jboss.metadata.MetaData;
import org.w3c.dom.Element;
import org.jboss.management.j2ee.CountStatistic;


/**
 *  Singleton pool for session beans. This lets you have
 * singletons in EJB!
 *
 *  @see <related>
 *  @author <a href="mailto:rickard.oberg@telkel.com">Rickard Öberg</a>
 *  @version $Revision: 1.13 $
 *
 * <p><b>Revisions:</b>
 * <p><b>20010718 andreas schaefer:</b>
 * <ul>
 * <li>- Added Statistics Gathering
 * </ul>
 */
public class SingletonStatelessSessionInstancePool
   implements InstancePool, XmlLoadable
{
   // Constants -----------------------------------------------------

   // Attributes ----------------------------------------------------
   Container con;

   EnterpriseContext ctx;
   boolean inUse = false;
   boolean isSynchronized = true;

   /** Counter of all the Bean instantiated within the Pool **/
   protected CountStatistic mInstantiate = new CountStatistic( "Instantiation", "", "Beans instantiated in Pool" );
   /** Counter of all the Bean destroyed within the Pool **/
   protected CountStatistic mDestroy = new CountStatistic( "Destroy", "", "Beans destroyed in Pool" );
   /** Counter of all the ready Beans within the Pool (which are not used now) **/
   protected CountStatistic mReadyBean = new CountStatistic( "ReadyBean", "", "Numbers of ready Bean Pool" );

   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------

   // Public --------------------------------------------------------

   /**
    *   Set the callback to the container. This is for initialization.
    *   The pool may extract the configuration from the container.
    *
    * @param   c
    */
   public void setContainer(Container c)
   {
      this.con = c;
   }

   public void init()
      throws Exception
   {
   }

   public void start()
      throws Exception
   {
   }

   public void stop()
   {
   }

   public void destroy()
   {
   }

   /**
    *   Get the singleton instance
    *
    * @return     Context /w instance
    * @exception   RemoteException
    */
   public synchronized EnterpriseContext get()
      throws Exception
   {
      // Wait while someone else is using it
      while(inUse && isSynchronized)
      {
         try { this.wait(); } catch (InterruptedException e) {}
      }

      // Create if not already created (or it has been discarded)
      if (ctx == null)
      {
         try
         {
            mInstantiate.add();
            ctx = create(con.createBeanClassInstance(), con);
         } catch (InstantiationException e)
         {
            throw new ServerException("Could not instantiate bean", e);
         } catch (IllegalAccessException e)
         {
            throw new ServerException("Could not instantiate bean", e);
         }
      }
      else
      {
         mReadyBean.remove();
      }

      // Lock and return instance
      inUse = true;
      return ctx;
   }

   /**
    *   Return an instance after invocation.
    *
    *   Called in 2 cases:
    *   a) Done with finder method
    *   b) Just removed
    *
    * @param   ctx
    */
   public synchronized void free(EnterpriseContext ctx)
   {
      // Notify waiters
      inUse = false;
      this.notifyAll();
      mReadyBean.add();
   }

   public void discard(EnterpriseContext ctx)
   {
      // Throw away
      try
      {
         mDestroy.add();
         mReadyBean.remove();
         ctx.discard();
      } catch (RemoteException e)
      {
         // DEBUG Logger.exception(e);
      }

      // Notify waiters
      inUse = false;
      this.notifyAll();
   }

   public Map retrieveStatistic()
   {
      Map lStatistics = new HashMap();
      lStatistics.put( "InstantiationCount", mInstantiate );
      lStatistics.put( "DestroyCount", mDestroy );
      lStatistics.put( "ReadyBeanCount", mReadyBean );
      return lStatistics;
   }
   public void resetStatistic()
   {
      mInstantiate.reset();
      mDestroy.reset();
      mReadyBean.reset();
   }

   // Z implementation ----------------------------------------------

    // XmlLoadable implementation
    public void importXml(Element element) throws DeploymentException {
        isSynchronized = Boolean.valueOf(MetaData.getElementContent(MetaData.getUniqueChild(element, "Synchronized"))).booleanValue();
    }
    
   // Package protected ---------------------------------------------

   // Protected -----------------------------------------------------
   protected EnterpriseContext create(Object instance, Container con)
      throws Exception
   {
      return new StatelessSessionEnterpriseContext(instance, con);
   }

   // Private -------------------------------------------------------

   // Inner classes -------------------------------------------------

}

