/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.naming;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.Name;
import javax.naming.spi.ObjectFactory;

import org.jboss.logging.Logger;
import org.jboss.util.id.GUID;

/** 
 * An object factory that allows different objects to be used
 * in the local virtual machine versus remote virtual machines
 *  
 * @author Adrian Brock (adrian@jboss.com)
 * @version $Revision: 1.1 $
 */
public class LinkRefPairObjectFactory implements ObjectFactory
{
   // Constants -----------------------------------------------------

   /** The logger */
   private static final Logger log = Logger.getLogger(LinkRefPairObjectFactory.class);

   /** Serial version UID */
   //private static final long serialVersionUID = -5386290613498931298L;
   
   /** Our class name */
   static final String className = LinkRefPairObjectFactory.class.getName();
   
   /** The guid used to determine whether we in the same VM */
   static final String guid = new GUID().asString();
   
   // Attributes ----------------------------------------------------

   // Static --------------------------------------------------------
   
   // Constructors --------------------------------------------------
   
   // Public --------------------------------------------------------
   
   // ObjectFactory implementation ----------------------------------

   public Object getObjectInstance(Object obj, Name name, Context nameCtx, Hashtable environment) throws Exception
   {
      LinkRefPair pair = (LinkRefPair) obj;
      String jndiName;
      
      // Local or remote?
      boolean local = false;
      if (guid.equals(pair.getGUID()))
      {
         jndiName = pair.getLocalLinkName();
         local = true;
      }
      else
         jndiName = pair.getRemoteLinkName();
      
      InitialContext ctx;
      if (local || environment == null)
         ctx = new InitialContext();
      else
         ctx = new InitialContext(environment);
      
      return ctx.lookup(jndiName);
   }
   
   // Y overrides ---------------------------------------------------
   
   // Package protected ---------------------------------------------
   
   // Protected -----------------------------------------------------

   // Private -------------------------------------------------------
   
   // Inner classes -------------------------------------------------
}
