/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
 
package org.jboss.tm.usertx.client;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.Reference;
import javax.naming.Name;
import javax.naming.spi.ObjectFactory;


/**
 *  This is an object factory for producing client
 *  UserTransactions.
 *  usage for standalone clients.
 *      
 *  @author <a href="mailto:osh@sparre.dk">Ole Husgaard</a>
 *  @version $Revision: 1.3 $
 */
public class ClientUserTransactionObjectFactory
   implements ObjectFactory
{
   public Object getObjectInstance(Object obj, Name name,
                                   Context nameCtx, Hashtable environment)
      throws Exception
   {
      Reference ref = (Reference)obj;
 
      if (ref.getClassName().equals(ClientUserTransaction.class.getName())) {
         return ClientUserTransaction.getSingleton();
      }
      return null;
   }
}

