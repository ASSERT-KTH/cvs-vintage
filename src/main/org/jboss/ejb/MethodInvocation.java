/*
 * jBoss, the OpenSource EJB server
 *
 * Distributable under GPL license.
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

/**
 *	MethodInvocation
 *
 *  This object carries the method to invoke and an identifier for the target ojbect
 *
 *	@see <related>
 *	@author Rickard Öberg (rickard.oberg@telkel.com)
 *  @author <a href="mailto:marc.fleury@telkel.com">Marc Fleury</a>.
 *	@version $Revision: 1.4 $
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
	
	public void setTransaction(Transaction tx)
	{
		
		//Logger.log("Setting a transaction on Method invocation"+hashCode()+" "+m.getName()+" with "+tx);
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
		ctx.setTransaction(tx);
		
		// Set the principal
		ctx.setPrincipal(identity);
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

