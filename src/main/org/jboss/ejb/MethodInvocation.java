/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb;

import java.lang.reflect.Method;

import java.security.Principal;
import javax.transaction.Transaction;


/**
 *  MethodInvocation
 *
 *  This object carries the method to invoke and an identifier for the target ojbect
 *
 *  @see <related>
 *  @author <a href="mailto:rickard.oberg@telkel.com">Rickard Öberg</a>
 *  @author <a href="mailto:marc.fleury@telkel.com">Marc Fleury</a>.
 *  @author <a href="mailto:osh@sparre.dk">Ole Husgaard</a>
 *  @version $Revision: 1.14 $
 */
public class MethodInvocation
{
   // Constants -----------------------------------------------------

   // Attributes ----------------------------------------------------

   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------

   /**
    *  Create a new instance.
    *
    *  @param id
    *    The id of target EJB of this method invocation.
    *  @param m
    *    The method to invoke. This method is declared in the remote or
    *    home interface of the bean.
    *  @param args
    *    The arguments for this invocation.
    *  @param tx
    *    The transaction of this invocation.
    *  @param identity
    *    The security identity to use in this invocation.
    *  @param credential
    *    The security credentials to use in this invocation.
    */
   public MethodInvocation(Object id, Method m, Object[] args, Transaction tx,
                           Principal identity, Object credential)
   {
      this.id = id;
      this.m = m;
      this.args = args;
      this.tx = tx;
      this.identity = identity;
      this.credential = credential;
   }


   // Public --------------------------------------------------------

   /**
    *  Return the invocation target ID.
    *  This is the internal ID of the invoked enterprise bean.
    */
   public Object getId()
   {
      return id;
   }

   /**
    *  Return the invocation method.
    */
   public Method getMethod()
   {
      return m;
   }

   /**
    *  Return the invocation argument list.
    */
   public Object[] getArguments()
   {
      return args;
   }

   /**
    *  This method sets the transaction associated with the method.
    *  Note that this doesn't mean that the transaction is associated
    *  with the thread.  In fact this is the only place it exists until
    *  the TxInterceptor logic.  Notably it might be the case that the
    *  tx associated here is different than the one on the target instance.
    */
   public void setTransaction(Transaction tx)
   {

//DEBUG     Logger.debug("Setting a transaction on Method invocation"+hashCode()+" "+m.getName()+" with "+tx);

      this.tx = tx;
   }

   /**
    *  Return the transaction associated with the method.
    *
    *  If no transaction is associated with this method but we have
    *  a transaction propagation context, import the TPC into the
    *  transaction manager, and associate the resulting transaction
    *  with this method before returning it.
    */
   public Transaction getTransaction()
   {
      return tx;
   }

   /**
    *  Change the security identity of this invocation.
    */
   public void setPrincipal(Principal identity)
   {
      this.identity = identity;
   }

   /**
    *  Return the security identity of this invocation.
    */
   public Principal getPrincipal()
   {
      return identity;
   }

   /**
    *  Change the security credentials of this invocation.
    */
   public void setCredential(Object credential)
   {
      this.credential = credential;
   }

   /**
    *  Return the security credentials of this invocation.
    */
   public Object getCredential()
   {
      return credential;
   }

   /**
    *  Set the enterprise context of this invocation.
    *
    *  Once a context is associated to a Method Invocation,
    *  the MI can pass it all the relevant information.
    *  We set Transaction and Principal.
    */
   public void setEnterpriseContext(EnterpriseContext ctx)
   {
      this.ctx = ctx;

      //Set the transaction
      // MF FIXME: wrong decision. Setting the context is just an assocation of the
      // the Method invocation to the instance.  Decisions on the transaction association
      // should be done elsewhere (new interceptor)
      //ctx.setTransaction(tx);

      // Set the principal
      // MF FIXME: a warning really.  The association of the context variables (tx, principal)
      // to the enterprise Context should not be done here but by the final interceptor in the
      // container, it will signify that the instance is indeed ready for calling
      if (ctx != null) {
         ctx.setPrincipal(identity);
      }
   }

   /**
    *  Return the enterprise context of this invocation.
    */
   public EnterpriseContext getEnterpriseContext()
   {
      return ctx;
   }

   // Package protected ---------------------------------------------

   // Protected -----------------------------------------------------

   // Private -------------------------------------------------------

   /**
    *  The internal ID of the enterprise bean who is the target
    *  of this invocation.
    */
   private Object id;

   /**
    *  The method to invoke.
    *
    *  This method is declared in the remote or home interface
    *  of the target enterprise bean.
    */
   private Method m;

   /**
    *  The argument list of this invocation.
    */
   private Object[] args;

   /**
    *  The transaction of this invocation.
    */
   private Transaction tx;

   /**
    *  The security identity of this invocation.
    */
   private Principal identity;

   /**
    *  The security credentials of this invocation.
    */
   private Object credential;

   /**
    *  The container bean context of the target bean.
    */
   private EnterpriseContext ctx;


   // Inner classes -------------------------------------------------
}

