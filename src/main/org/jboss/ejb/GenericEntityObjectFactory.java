/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;

/**
 * Entity EJBObject and EJBLocalObject proxy factories implement this generic interface.
 * The getEntityEJBObject method returns either EJBObject or EJBLoadlObject.
 *
 * @author <a href="mailto:alex@jboss.org">Alexey Loubyansky</a>
 * @version <tt>$Revision: 1.2 $</tt>
 */
public interface GenericEntityObjectFactory
{
   Object getEntityEJBObject(Object id);

   class UTIL
   {
      private UTIL()
      {
      }

      public static Collection getEntityCollection(GenericEntityObjectFactory factory, Collection ids)
      {
         List result = new ArrayList();
         if(!ids.isEmpty())
         {
            for(Iterator i = ids.iterator(); i.hasNext();)
            {
               result.add(factory.getEntityEJBObject(i.next()));
            }
         }
         return result;
      }
   }
}
