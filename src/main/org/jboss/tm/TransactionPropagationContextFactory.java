/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.tm;


/**
 *  Implementations of this interface are used for getting
 *  a transaction propagation context at the client-side.
 *  We need a specific implementation of this interface for
 *  each kind of DTM we are going to interoperate with. (So
 *  we may have 20 new classes if we are going to interoperate
 *  with 20 different kinds of distributed transaction
 *  managers.)
 *
 *  @see TransactionImpl
 *  @author <a href="mailto:osh@sparre.dk">Ole Husgaard</a>
 *  @version $Revision: 1.1 $
 */
public interface TransactionPropagationContextFactory
{
   /**
    *  Return a transaction propagation context for the transaction
    *  currently associated with the invoking thread, or <code>null</code>
    *  if the invoking thread is not associated with a transaction.
    *  The reason for having this method return Object is that we do not
    *  really know what kind of transaction propagation context we get.
    */
   public Object getTransactionPropagationContext();
}

