/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.ejb;

import java.rmi.RemoteException;
import java.security.Identity;
import java.security.Principal;
import java.util.Properties;
import java.util.HashSet;
import java.util.Iterator;

import javax.ejb.EJBLocalHome;
import javax.ejb.EJBHome;
import javax.ejb.EJBContext;
import javax.ejb.EJBException;
import javax.ejb.TimerService;
import javax.transaction.Status;
import javax.transaction.Transaction;
import javax.transaction.UserTransaction;
import javax.transaction.SystemException;

import org.jboss.logging.Logger;
import org.jboss.metadata.BeanMetaData;
import org.jboss.metadata.ApplicationMetaData;
import org.jboss.metadata.SecurityRoleRefMetaData;
import org.jboss.security.RealmMapping;
import org.jboss.security.SimplePrincipal;


/**
 * The EnterpriseContext is used to associate EJB instances with
 * metadata about it.
 *
 * @see StatefulSessionEnterpriseContext
 * @see StatelessSessionEnterpriseContext
 * @see EntityEnterpriseContext
 *
 * @author <a href="mailto:rickard.oberg@telkel.com">Rickard �berg</a>
 * @author <a href="mailto:marc.fleury@telkel.com">Marc Fleury</a>
 * @author <a href="mailto:sebastien.alborini@m4x.org">Sebastien Alborini</a>
 * @author <a href="mailto:juha@jboss.org">Juha Lindfors</a>
 * @author <a href="mailto:osh@sparre.dk">Ole Husgaard</a>
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @version $Revision: 1.59 $
 */
public abstract class EnterpriseContext
{
   /** Instance logger. */
   protected Logger log = Logger.getLogger(this.getClass());

   /** The EJB instance */
   protected Object instance;

   /** The container using this context */
   protected Container container;

   /** The transaction associated with the instance */
   private Transaction transaction;

   /** The principal associated with the call */
   private Principal principal;

   /** The principal for the bean associated with the call */
   private Principal beanPrincipal;

   /** Only StatelessSession beans have no Id, stateful and entity do */
   protected Object id;

   /** The instance is being used.  This locks it's state */
   private int locked = 0;

   /** The instance is used in a transaction, synchronized methods on the tx */
   private final Object txLock = new Object();

   private final UserTransaction userTransaction;

   public EnterpriseContext(Object instance, Container container)
   {
      this.instance = instance;
      this.container = container;
      if(isContainerManagedTx())
      {
         userTransaction = null;
      }
      else
      {
         userTransaction = container.getUserTransaction();
      }
   }

   public Object getInstance()
   {
      return instance;
   }

   /**
    * Gets the container that manages the wrapped bean.
    */
   public Container getContainer()
   {
      return container;
   }

   public abstract void discard() throws RemoteException;

   /**
    * Get the EJBContext object
    */
   public abstract EJBContext getEJBContext();

   public void setId(Object id)
   {
      this.id = id;
   }

   public Object getId()
   {
      return id;
   }

   public Object getTxLock()
   {
      return txLock;
   }

   public void setTransaction(Transaction transaction)
   {
      // DEBUG log.debug("EnterpriseContext.setTransaction "+((transaction == null) ? "null" : Integer.toString(transaction.hashCode())));
      this.transaction = transaction;
   }

   public Transaction getTransaction()
   {
      return transaction;
   }

   public void setPrincipal(Principal principal)
   {
      this.principal = principal;
      this.beanPrincipal = null;
   }

   public void lock()
   {
      locked ++;
      //DEBUG log.debug("EnterpriseContext.lock() "+hashCode()+" "+locked);
   }

   public void unlock()
   {
      // release a lock
      locked --;

      if(locked <0)
      {
         // new Exception().printStackTrace();
         log.error("locked < 0", new Throwable());
      }

      //DEBUG log.debug("EnterpriseContext.unlock() "+hashCode()+" "+locked);
   }

   public boolean isLocked()
   {
      //DEBUG log.debug("EnterpriseContext.isLocked() "+hashCode()+" at "+locked);
      return locked != 0;
   }

   /**
    * before reusing this context we clear it of previous state called
    * by pool.free()
    */
   public void clear()
   {
      this.id = null;
      this.locked = 0;
      this.principal = null;
      this.beanPrincipal = null;
      this.transaction = null;
   }

   protected boolean isContainerManagedTx()
   {
      BeanMetaData md = container.getBeanMetaData();
      return md.isContainerManagedTx();
   }

   protected class EJBContextImpl implements EJBContext
   {
      /**
       * @deprecated
       */
      public Identity getCallerIdentity()
      {
         throw new EJBException("Deprecated");
      }

      /**
       * Get the Principal for the current caller. This method
       * cannot return null according to the ejb-spec.
       */
      public Principal getCallerPrincipal()
      {
         if(beanPrincipal == null)
         {
            RealmMapping realmMapping = container.getRealmMapping();
            if(principal != null)
            {
               if(realmMapping != null)
               {
                  beanPrincipal = realmMapping.getPrincipal(principal);
               }
               else
               {
                  beanPrincipal = principal;
               }
            }
            else if(realmMapping != null)
            {
               // Let the RealmMapping map the null principal
               beanPrincipal = realmMapping.getPrincipal(principal);
            }
            else
            {
               // Check for a unauthenticated principal value
               ApplicationMetaData appMetaData = container.getBeanMetaData().getApplicationMetaData();
               String name = appMetaData.getUnauthenticatedPrincipal();
               if(name != null)
               {
                  beanPrincipal = new SimplePrincipal(name);
               }
            }
         }
         if(beanPrincipal == null)
         {
            throw new IllegalStateException("No security context set");
         }
         return beanPrincipal;
      }

      public EJBHome getEJBHome()
      {
         EJBProxyFactory proxyFactory;
         if(container instanceof EntityContainer)
         {
            proxyFactory = container.getProxyFactory();
            if(proxyFactory == null)
            {
               throw new IllegalStateException("No remote home defined.");
            }
            return (EJBHome)proxyFactory.getEJBHome();
         }
         else if(container instanceof StatelessSessionContainer)
         {
            proxyFactory = container.getProxyFactory();
            if(proxyFactory == null)
            {
               throw new IllegalStateException("No remote home defined.");
            }
            return (EJBHome) proxyFactory.getEJBHome();
         }
         else if(container instanceof StatefulSessionContainer)
         {
            proxyFactory = container.getProxyFactory();
            if(proxyFactory == null)
            {
               throw new IllegalStateException("No remote home defined.");
            }
            return (EJBHome) proxyFactory.getEJBHome();
         }

         // Should never get here
         throw new EJBException("No EJBHome available (BUG!)");
      }

      public EJBLocalHome getEJBLocalHome()
      {
         if(container instanceof EntityContainer)
         {
            if(container.getLocalHomeClass()==null)
            {
               throw new IllegalStateException("No local home defined.");
            }
            return container.getLocalProxyFactory().getEJBLocalHome();
         }
         else if(container instanceof StatelessSessionContainer)
         {
            if(container.getLocalHomeClass()==null)
            {
               throw new IllegalStateException("No local home defined.");
            }
            return container.getLocalProxyFactory().getEJBLocalHome();
         }
         else if(container instanceof StatefulSessionContainer)
         {
            if(container.getLocalHomeClass()==null)
            {
               throw new IllegalStateException("No local home defined.");
            }
            return container.getLocalProxyFactory().getEJBLocalHome();
         }

         // Should never get here
         throw new EJBException("No EJBLocalHome available (BUG!)");
      }

      /**
       * @deprecated
       */
      public Properties getEnvironment()
      {
         throw new EJBException("Deprecated");
      }

      public boolean getRollbackOnly()
      {
         // EJB1.1 11.6.1: Must throw IllegalStateException if BMT
         if(container.getBeanMetaData().isBeanManagedTx())
         {
            throw new IllegalStateException("ctx.getRollbackOnly() not allowed for BMT beans.");
         }

         try
         {
            return container.getTransactionManager().getStatus() == Status.STATUS_MARKED_ROLLBACK;
         }
         catch (SystemException e)
         {
            log.warn("failed to get tx manager status; ignoring", e);
            return true;
         }
      }

      public void setRollbackOnly()
      {
         // EJB1.1 11.6.1: Must throw IllegalStateException if BMT
         if(container.getBeanMetaData().isBeanManagedTx())
         {
            throw new IllegalStateException("ctx.setRollbackOnly() not allowed for BMT beans.");
         }

         try
         {
            container.getTransactionManager().setRollbackOnly();
         }
         catch (IllegalStateException e)
         {
            // ignore
         }
         catch (SystemException e)
         {
            log.warn("failed to set rollback only; ignoring", e);
         }
      }

      /**
       * @deprecated
       */
      public boolean isCallerInRole(Identity id)
      {
         throw new EJBException("Deprecated");
      }

      // TODO - how to handle this best?
      public boolean isCallerInRole(String id)
      {
         if(principal == null)
         {
            return false;
         }
         RealmMapping realmMapping = container.getRealmMapping();
         if(realmMapping == null)
         {
            throw new IllegalStateException("isCallerInRole() called " +
                  "with no security context. Check that a " +
                  "security-domain has been set for the application.");
         }

         // Map the role name used by Bean Provider to the security role
         // link in the deployment descriptor. The EJB 1.1 spec requires
         // the security role refs in the descriptor but for backward
         // compability we're not enforcing this requirement.
         //
         // TODO (2.3): add a conditional check using jboss.xml <enforce-ejb-restrictions> element
         //             which will throw an exception in case no matching
         //             security ref is found.
         Iterator it = getContainer().getBeanMetaData().getSecurityRoleReferences();
         boolean matchFound = false;

         while(it.hasNext())
         {
            SecurityRoleRefMetaData meta = (SecurityRoleRefMetaData)it.next();
            if(meta.getName().equals(id))
            {
               id = meta.getLink();
               matchFound = true;

               break;
            }
         }

         if(!matchFound)
         {
            log.warn("no match found for security role " + id +
                     " in the deployment descriptor.");
         }

         HashSet set = new HashSet();
         set.add( new SimplePrincipal(id) );

         return realmMapping.doesUserHaveRole( principal, set );
      }

      public UserTransaction getUserTransaction()
      {
         if(isContainerManagedTx())
         {
            throw new IllegalStateException
               ("CMT beans are not allowed to get a UserTransaction");
         }
         return userTransaction;
      }

      public TimerService getTimerService() throws IllegalStateException
      {
         return getContainer().getTimerService( null );
      }
   }
}
