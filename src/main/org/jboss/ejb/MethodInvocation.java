/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Map;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import java.security.Principal;
import javax.transaction.Transaction;

import org.jboss.tm.TransactionPropagationContextImporter;

import org.jboss.logging.Logger;

/**
 *  MethodInvocation
 *
 *  This object carries the method to invoke and an identifier for the target ojbect
 *
 *  @see <related>
 *  @author <a href="mailto:rickard.oberg@telkel.com">Rickard Öberg</a>
 *  @author <a href="mailto:marc.fleury@telkel.com">Marc Fleury</a>.
 *  @version $Revision: 1.11 $
 */
public class MethodInvocation
{
   // Constants -----------------------------------------------------

   // Attributes ----------------------------------------------------

   Object id;
   Object[] args;
    
   Principal identity;
   Object credential;

   Method m;
   EnterpriseContext ctx;


   // Static --------------------------------------------------------

   private static TransactionPropagationContextImporter tpcImporter;


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
    *  @param tpc
    *    The transaction propagation context of this invocation. 
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
      this.tpc = null;
      this.tx = tx;
      this.identity = identity;
      this.credential = credential;
   }

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
    *  @param identity
    *    The security identity to use in this invocation.
    *  @param credential
    *    The security credentials to use in this invocation.
    *  @param tpc
    *    The transaction propagation context of this invocation. 
    */
   public MethodInvocation(Object id, Method m, Object[] args,
                           Principal identity, Object credential, Object tpc)
   {
      this.id = id;
      this.m = m;
      this.args = args;
      this.tpc = tpc;
      this.tx = null;
      this.identity = identity;
      this.credential = credential;
   }

   // Public --------------------------------------------------------

   public Object getId() { return id; }

   public Method getMethod()
   {
      return m;   
   }

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
        if (tx == null) {
            // See if we have a transaction propagation context
            if (tpc != null) {
                // import the propagation context
                if (tpcImporter == null) {
                    try {
                      tpcImporter = (TransactionPropagationContextImporter)new InitialContext().lookup("java:/TransactionPropagationContextImporter");
                    } catch (NamingException ex) {
                        // No importer: Log exception, and return null.
                        Logger.exception(ex);
                        return null;
                    }
                }
                tx = tpcImporter.importTransactionPropagationContext(tpc);
//DEBUG                Logger.debug("Imported transaction " + tx +
//DEBUG                             " on Method invocation " + hashCode() +
//DEBUG                             " " + m.getName());
            }
        }
        return tx;
    }

    public void setPrincipal(Principal identity)
    {
        this.identity = identity;
    }

    public Principal getPrincipal()
    {
        return identity;
    }

    public void setCredential(Object credential)
    {
        this.credential = credential;
    }

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

    public EnterpriseContext getEnterpriseContext()
    {
        return ctx;
    }

   // Package protected ---------------------------------------------

   // Protected -----------------------------------------------------

   // Private -------------------------------------------------------
 
   /**
    *  The transaction propagation context of this invocation.
    */
   private Object tpc;
 
   /**
    *  The transaction of this invocation.
    */
   private Transaction tx;

   // Inner classes -------------------------------------------------
}

