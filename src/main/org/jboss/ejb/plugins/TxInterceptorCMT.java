/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import org.jboss.ejb.plugins.lock.ApplicationDeadlockException;
import org.jboss.invocation.Invocation;
import org.jboss.invocation.InvocationResponse;
import org.jboss.invocation.InvocationType;
import org.jboss.metadata.BeanMetaData;

/**
 *  This interceptor handles transactions for CMT beans.
 *
 *  @author <a href="mailto:rickard.oberg@telkel.com">Rickard Öberg</a>
 *  @author <a href="mailto:marc.fleury@telkel.com">Marc Fleury</a>
 *  @author <a href="mailto:sebastien.alborini@m4x.org">Sebastien Alborini</a>
 *  @author <a href="mailto:akkerman@cs.nyu.edu">Anatoly Akkerman</a>
 *  @author <a href="mailto:osh@sparre.dk">Ole Husgaard</a>
 *  @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 *  @version $Revision: 1.36 $
 */
public  class TxInterceptorCMT extends AbstractInterceptor
{
   public static int MAX_RETRIES = 5;
   public static Random random = new Random();

   /**
    *  This method does calls internalInvoke up to the specified count of retries.
    *
    * @todo put the retry logic in a separate invoker
    * @todo provide an option to copy the args before calling so the retrys actually use the same arguments.
    * @todo Make this only retry if it has started the tx: it should
    * rollback to the beginning of tx, then retry the whole tx with
    * the new copies of the original arguments.
    */
   public InvocationResponse invoke(Invocation invocation) throws Exception
   {
      Transaction oldTransaction = invocation.getTransaction();
      for (int i = 0; i < MAX_RETRIES; i++)
      {
         try
         {
            return internalInvoke(invocation);
         }
         catch (Exception ex)
         {
            ApplicationDeadlockException deadlock = ApplicationDeadlockException.isADE(ex);
            if (deadlock != null)
            {
               if (!deadlock.retryable() ||
                   oldTransaction != null ||
                   i + 1 >= MAX_RETRIES)
               {
                  throw deadlock;
               }
               log.warn(deadlock.getMessage() + " retrying " + (i + 1));

               Thread.sleep(random.nextInt(1 + i), random.nextInt(1000));
            }
            else
            {
               throw ex;
            }
         }
      }
      throw new RuntimeException("Unreachable");
   }

   /**
    * The <code>internalInvoke</code> method chooses the TxSupport
    * subclass depending on the methods transaction support, then
    * delegates the server side tx handing to it.
    *
    * @param invocation an <code>Invocation</code> value
    * @return an <code>Object</code> value
    * @exception Exception if an error occurs
    */
   protected InvocationResponse internalInvoke(Invocation invocation) throws Exception
   {
      Map methodToTxSupportMap = getContainer().getMethodToTxSupportMap();
      Method m = invocation.getMethod();
      TxSupport txSupport = (TxSupport)methodToTxSupportMap.get(m);
      if (txSupport == null)
      {
         txSupport = TxSupport.DEFAULT;
      } // end of if ()

      printMethod(m, txSupport);

      TransactionManager tm = getContainer().getTransactionManager();
      return txSupport.serverInvoke(invocation, tm, getNext());
   }

   /**
    * The <code>start</code> method sets up the method to TxSupport
    * mapping.  Ideally this logic would be in an ejb specific
    * deployer rather than an interceptor, enabling the use of the
    * interceptor with any AOP object.
    *
    * @exception Exception if an error occurs
    */
   public void start() throws Exception
   {
      log.info("Setting TxSupport map for container: " + getContainer());
      BeanMetaData bmd = getContainer().getBeanMetaData();
      Map methodToTxSupportMap = new HashMap();
      if (getContainer().getHomeClass() != null)
      {
         mapMethods(getContainer().getHomeClass(), bmd, InvocationType.HOME,  methodToTxSupportMap);
      } // end of if ()

      if (getContainer().getRemoteClass() != null)
      {
         mapMethods(getContainer().getRemoteClass(), bmd, InvocationType.REMOTE, methodToTxSupportMap);
      } // end of if ()

      if (getContainer().getLocalHomeClass() != null)
      {
         mapMethods(getContainer().getLocalHomeClass(), bmd, InvocationType.LOCALHOME, methodToTxSupportMap);
      } // end of if ()

      if (getContainer().getLocalClass() != null)
      {
         mapMethods(getContainer().getLocalClass(), bmd, InvocationType.LOCAL, methodToTxSupportMap);
      } // end of if ()

      getContainer().setMethodToTxSupportMap(methodToTxSupportMap);
   }

   /**
    * The <code>mapMethods</code> method maps the methods for one
    * interface to the TxSupport subclass that implements the required
    * behavior.
    *
    * @param clazz a <code>Class</code> value
    * @param bmd a <code>BeanMetaData</code> value
    * @param type an <code>InvocationType</code> value
    * @param methodToTxSupportMap a <code>Map</code> value
    * @exception Exception if an error occurs
    */
   private void mapMethods(Class clazz, BeanMetaData bmd, InvocationType type, Map methodToTxSupportMap)
      throws Exception
   {
      Method[] methods = clazz.getMethods();
      for (int i = 0; i < methods.length; i++)
      {
         Method m = methods[i];
         TxSupport txSupport = bmd.getMethodTransactionType(m.getName(), m.getParameterTypes(),  type);
         methodToTxSupportMap.put(methods[i], txSupport);
      } // end of for ()

   }

   public void stop()
   {
      log.info("Unsetting TxSupport map for container: " + getContainer());
      getContainer().setMethodToTxSupportMap(null);
   }

   private void printMethod(Method m, TxSupport type)
   {

      if(log.isTraceEnabled())
      {
         String methodName;
         if(m != null)
         {
            methodName = m.getName();
         }
         else
         {
            methodName ="<no method>";
         }

         log.trace(type.toString() +" for " + methodName);
      }
   }

}
