/*
 * jBoss, the OpenSource EJB server
 *
 * Distributable under GPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins.jrmp.interfaces;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 *	<description> 
 *      
 *	@see <related>
 *	@author Rickard Öberg (rickard.oberg@telkel.com)
 *	@version $Revision: 1.1 $
 */
public class IteratorImpl
   implements Iterator, java.io.Serializable
{
   // Constants -----------------------------------------------------
    
   // Attributes ----------------------------------------------------
   ArrayList enum;
   int idx = 0;
   
   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------
   public IteratorImpl(ArrayList list)
   {
      this.enum = list;
   }
   
   // Public --------------------------------------------------------

   // Iterator  implementation --------------------------------------
   public boolean hasNext()
   {
      return idx < enum.size();
   }
   
   public Object next()
   {
      if (idx == enum.size())
         throw new NoSuchElementException();
      else
         return enum.get(idx++);
   }
   
   public void remove()
   {
   }
}

