/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.tm;


/**
 *  A class for getting a transaction propagation context
 *  at the client-side, suited for same-VM JRMP propagation.
 *  This class is meant to be used with the parts of JBoss
 *  that act as clients to JBoss in the same VM.
 *  In particular, this is <em>not</em> for standalone clients.
 *
 *  @see TransactionPropagationContextFactory
 *  @author <a href="mailto:osh@sparre.dk">Ole Husgaard</a>
 *  @version $Revision: 1.1 $
 */
public class JRMPTransactionPropagationContextFactory
   implements TransactionPropagationContextFactory
{
   /**
    *  Our singleton instance.
    */
   static private JRMPTransactionPropagationContextFactory singleton
                              = new JRMPTransactionPropagationContextFactory();

   /**
    *  Returns the singleton instance of this class.
    */
   static public JRMPTransactionPropagationContextFactory getInstance()
   {
      return singleton;
   }

   /**
    *  A reference to the TxManager singleton instance.
    *  This could be obtained by simply doing
    *  <code>TxManager.getInstance()</code> when needed, but we
    *  do this for <em>every</em> outgoing JRMP call.
    */
   private TxManager tm;

   /**
    *  Private constructor for singleton.
    */
   private JRMPTransactionPropagationContextFactory()
   {
      tm = TxManager.getInstance();
   }

   /**
    *  Return the XidImpl of the current transaction.
    */
   public Object getTransactionPropagationContext()
   {
      return tm.getXid();
   }
}

