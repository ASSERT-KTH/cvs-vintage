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
import java.util.Stack;

import javax.ejb.EJBHome;

import org.jboss.ejb.Container;
import org.jboss.ejb.InstancePool;
import org.jboss.ejb.EnterpriseContext;

import org.w3c.dom.Element;
import org.jboss.ejb.DeploymentException;
import org.jboss.metadata.MetaData;
import org.jboss.metadata.XmlLoadable;
import org.jboss.logging.Logger;

import org.jboss.management.JBossCountStatistic;


/**
*  <review>
*	Abstract Instance Pool class containing the basic logic to create
*  an EJB Instance Pool.
*  </review>
*
*	@see <related>
*	@author <a href="mailto:rickard.oberg@telkel.com">Rickard Öberg</a>
*	@author <a href="mailto:marc.fleury@jboss.org">Marc Fleury</a>
*  @author <a href="mailto:andreas.schaefer@madplanet.com">Andreas Schaefer</a>
*	
*  @version $Revision: 1.14 $
*
*  <p><b>Revisions:</b>
*  <p><b>20010704 marcf:</b>
*  <ul>
*  <li>- Pools if used, do not reuse but restock the pile with fresh instances
*  </ul>
*  <p><b>20010709 andreas schaefer:</b>
*  <ul>
*  <li>- Added statistics gathering
*  </ul>
*/
public abstract class AbstractInstancePool
implements InstancePool, XmlLoadable
{
	// Constants -----------------------------------------------------
	
	// Attributes ----------------------------------------------------
	private Container container;
	
	Stack pool = new Stack();
	int maxSize = 30;
	
   /** Counter of all the Bean instantiated within the Pool **/
   protected JBossCountStatistic mInstantiate = new JBossCountStatistic( "Instantiation", "", "Beans instantiated in Pool" );
   /** Counter of all the Bean destroyed within the Pool **/
   protected JBossCountStatistic mDestroy = new JBossCountStatistic( "Destroy", "", "Beans destroyed in Pool" );
   /** Counter of all the ready Beans within the Pool (which are not used now) **/
   protected JBossCountStatistic mReadyBean = new JBossCountStatistic( "ReadyBean", "", "Numbers of ready Bean Pool" );

	// Static --------------------------------------------------------
	
	// Constructors --------------------------------------------------
	
	// Public --------------------------------------------------------
	
	/**
	*   Set the callback to the container. This is for initialization.
	*   The IM may extract the configuration from the container.
	*
	* @param   c
	*/
	public void setContainer(Container c)
	{
		this.container = c;
	}
	
   /**
   * <review>
   * @return Callback to the container which can be null if not set proviously
   * </review>
   **/
	public Container getContainer()
	{
		return container;
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
	*   Get an instance without identity.
	*   Can be used by finders,create-methods, and activation
	*
	* @return     Context /w instance
	* @exception   RemoteException
	*/
	public synchronized EnterpriseContext get()
	throws Exception
	{
		//DEBUG      Logger.debug("Get instance "+this);
		
		if (!pool.empty())
		{
         mReadyBean.remove();
			return (EnterpriseContext)pool.pop();
		} else
		{
			try
			{
				return create(container.createBeanClassInstance());
			} catch (InstantiationException e)
			{
				throw new ServerException("Could not instantiate bean", e);
			} catch (IllegalAccessException e)
			{
				throw new ServerException("Could not instantiate bean", e);
			}
		}
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
		// Pool it
		//DEBUG      Logger.debug("Free instance:"+ctx.getId()+"#"+ctx.getTransaction());
		ctx.clear();
		
		if (pool.size() < maxSize)
		{
			
			// We do not reuse but create a brand new instance simplifies the design
			try {
				mReadyBean.add();
				pool.push(create(container.createBeanClassInstance()));
			} catch (Exception ignored) {}			
			//pool.push(ctx);
		} else
		{
			discard(ctx);
		}
	}
	
	public void discard(EnterpriseContext ctx)
	{
		// Throw away
		try
		{
         mDestroy.add();
			ctx.discard();
		} catch (RemoteException e)
		{
			// DEBUG Logger.exception(e);
		}
	}
	
	// Z implementation ----------------------------------------------
	
	// XmlLoadable implementation
	public void importXml(Element element) throws DeploymentException {
		String maximumSize = MetaData.getElementContent(MetaData.getUniqueChild(element, "MaximumSize"));
		try {
			maxSize = Integer.parseInt(maximumSize);
		} catch (NumberFormatException e) {
			throw new DeploymentException("Invalid MaximumSize value for instance pool configuration");
		}
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
   
	// Package protected ---------------------------------------------
	
	// Protected -----------------------------------------------------
	protected abstract EnterpriseContext create(Object instance)
	throws Exception;
	
	// Private -------------------------------------------------------
	
	// Inner classes -------------------------------------------------

}

