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

import java.security.Principal;
import javax.transaction.Transaction;

import org.jboss.logging.Logger;

/**
 *  MethodInvocation
 *
 *  This object carries the method to invoke and an identifier for the target ojbect
 *
 *  @see <related>
 *  @author Rickard Öberg (rickard.oberg@telkel.com)
 *  @author <a href="mailto:marc.fleury@telkel.com">Marc Fleury</a>.
 *  @version $Revision: 1.9 $
 */
public class MethodInvocation
{
   // Constants -----------------------------------------------------

   // Attributes ----------------------------------------------------
   Object id;
   Object[] args;
    
    Transaction tx;
    Principal identity;
  Object credential;

    Method m;
    EnterpriseContext ctx;

   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------
   public MethodInvocation(Object id, Method m, Object[] args, Transaction tx,
    Principal identity, Object credential )
   {
        this.id = id;
        this.m = m;
        this.args = args;
        this.tx = tx;
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
    
    /*
    * setTransaction()
    *
    * This method sets the transaction associated with the method 
    * Note that this doesn't mean that the transaction is associated 
    * with the thread.  In fact this is the only place it exists until 
    * the TxInterceptor logic.  Notably it might be the case that the 
    * tx associated here is different than the one on the target instance.
    */
    public void setTransaction(Transaction tx)
    {
        
//DEBUG     Logger.debug("Setting a transaction on Method invocation"+hashCode()+" "+m.getName()+" with "+tx);
        
        this.tx = tx;
        
    }
    
    public Transaction getTransaction()
    {
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

    /*
    * setEnterpriseContext()
    *
    * Once a context is associated to a Method Invocation the MI can pass it all the relevant information
    * We set Transaction and Principal
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
    
    // Inner classes -------------------------------------------------
}

