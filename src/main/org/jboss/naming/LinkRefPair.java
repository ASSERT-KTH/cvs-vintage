/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.naming;

import javax.naming.MalformedLinkException;
import javax.naming.Name;
import javax.naming.RefAddr;
import javax.naming.Reference;
import javax.naming.StringRefAddr;

import org.jboss.logging.Logger;

/** 
 * A pair of addresses, one to be used in the local machine,
 * the other in remote machines. 
 *   
 * @author Adrian Brock (adrian@jboss.com)
 * @version $Revision: 1.1 $
 */
public class LinkRefPair extends Reference
{
   // Constants -----------------------------------------------------

   /** Serial version UID */
   //private static final long serialVersionUID = -5386290613498931298L;
   
   /** Our class name */
   private static final String linkRefPairClassName = LinkRefPair.class.getName();

   /** The remote jndi object */
   static final String remoteAddress = "remoteAddress"; 

   /** The local jndi object */
   static final String localAddress = "localAddress"; 

   /** The guid used to determine whether we are local to the VM */
   private static final String guidAddress = "guid"; 
   
   // Attributes ----------------------------------------------------

   // Static --------------------------------------------------------
   
   // Constructors --------------------------------------------------

   /**
    * Create a new link ref pair with the give remote and local names.
    * 
    * @param remote the remote name
    * @param local the local name
    */
   public LinkRefPair(String remote, String local)
   {
      super(linkRefPairClassName, LinkRefPairObjectFactory.className, null);
      add(new StringRefAddr(guidAddress, LinkRefPairObjectFactory.guid));
      add(new StringRefAddr(remoteAddress, remote));
      add(new StringRefAddr(localAddress, local));
   }
   
   // Public --------------------------------------------------------

   /**
    * Get the guid link name
    * 
    * @return the guid
    * @throws MalformedLinkException when the reference is malformed
    */
   public String getGUID() throws MalformedLinkException
   {
      if (className != null && className.equals(linkRefPairClassName))
      {
         RefAddr refAddr = get(guidAddress);
         if (refAddr != null && refAddr instanceof StringRefAddr)
         {
            Object content = refAddr.getContent();
            if (content != null && content instanceof String)
               return (String) content;
            else
               throw new MalformedLinkException("Content is not a string: " + content);
         }
         else
            throw new MalformedLinkException("RefAddr is not a string reference: " + refAddr);
      }
      else
         throw new MalformedLinkException("Class is not a LinkRefPair: " + className);
   }

   /**
    * Get the remote link name
    * 
    * @return the remote link
    * @throws MalformedLinkException when the reference is malformed
    */
   public String getRemoteLinkName() throws MalformedLinkException
   {
      if (className != null && className.equals(linkRefPairClassName))
      {
         RefAddr refAddr = get(remoteAddress);
         if (refAddr != null && refAddr instanceof StringRefAddr)
         {
            Object content = refAddr.getContent();
            if (content != null && content instanceof String)
               return (String) content;
            else
               throw new MalformedLinkException("Content is not a string: " + content);
         }
         else
            throw new MalformedLinkException("RefAddr is not a string reference: " + refAddr);
      }
      else
         throw new MalformedLinkException("Class is not a LinkRefPair: " + className);
   }

   /**
    * Get the local link name
    * 
    * @return the remote link
    * @throws MalformedLinkException when the reference is malformed
    */
   public String getLocalLinkName() throws MalformedLinkException
   {
      if (className != null && className.equals(linkRefPairClassName))
      {
         RefAddr refAddr = get(localAddress);
         if (refAddr != null && refAddr instanceof StringRefAddr)
         {
            Object content = refAddr.getContent();
            if (content != null && content instanceof String)
               return (String) content;
            else
               throw new MalformedLinkException("Content is not a string: " + content);
         }
         else
            throw new MalformedLinkException("RefAddr is not a string reference: " + refAddr);
      }
      else
         throw new MalformedLinkException("Class is not a LinkRefPair: " + className);
   }
   
   // Package protected ---------------------------------------------
   
   // Protected -----------------------------------------------------

   // Private -------------------------------------------------------
   
   // Inner classes -------------------------------------------------
}
