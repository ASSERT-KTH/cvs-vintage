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

import org.jboss.invocation.Invocation;

/**
 *  MethodInvocation
 *
 *  This object carries the method to invoke and an identifier for the target ojbect
 *
 *  @see <related>
 *  @author <a href="mailto:rickard.oberg@telkel.com">Rickard Öberg</a>
 *  @author <a href="mailto:marc.fleury@telkel.com">Marc Fleury</a>.
 *  @author <a href="mailto:osh@sparre.dk">Ole Husgaard</a>
 *  @version $Revision: 1.15 $
 */
public class MethodInvocation extends Invocation
{
   // Constants -----------------------------------------------------

   // Attributes ----------------------------------------------------

   // Private -------------------------------------------------------

   /**
    *  The internal ID of the enterprise bean who is the target
    *  of this invocation.
    */
   private Object id;


   // Static --------------------------------------------------------
   public static final Integer
      //For legacy reasons only
      TARGET_ID = new Integer(new String("TARGET_ID").hashCode()),
      ENTERPRISE_CONTEXT = new Integer(new String("ENTERPRISE_CONTEXT").hashCode());
   
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
   public MethodInvocation(Transaction tx,
                           Principal identity, 
                           Object credential,
                           String[] mbeans, 
                           Object id, 
                           Method m, 
                           Object[] args)
   {

      super(tx, identity, credential, mbeans, m, args);
      
      setId(id);
   }

   public MethodInvocation(Object id, 
                           Method m, 
                           Object[] arguments, 
                           Transaction tx, 
                           Principal identity, 
                           Object credential)
   {
      super(tx, identity, credential, null, m, arguments);
      
      setId(id);
   }                           

   // Public --------------------------------------------------------

   /**
    *  Return the invocation target ID.
    *  This is the internal ID of the invoked enterprise bean.
    */
   public void setId(Object id) { payload.put(TARGET_ID, id);}
   public Object getId() { return payload.get(TARGET_ID);}

   
   /**
    *  Set the enterprise context of this invocation.
    *
    *  Once a context is associated to a Method Invocation,
    *  the MI can pass it all the relevant information.
    *  We set Transaction and Principal.
    */
   public void setEnterpriseContext(EnterpriseContext ctx)
   {
      payload.put(ENTERPRISE_CONTEXT, ctx);

      // marcf: we should remove the association from here, should be done in the interceptor that sets this 
      if (ctx != null) {
         ctx.setPrincipal(identity);
      }
   }
   
   public EnterpriseContext getEnterpriseContext() { return (EnterpriseContext) payload.get(ENTERPRISE_CONTEXT);}

}

