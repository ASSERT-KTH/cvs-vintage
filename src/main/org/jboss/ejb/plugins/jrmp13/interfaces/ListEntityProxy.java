/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins.jrmp13.interfaces;

import java.util.List;
import org.jboss.ejb.plugins.jrmp.interfaces.ContainerRemote;

/**
 * @author <a href="mailto:on@ibis.odessa.ua">Oleg Nitz</a>
 *	@version $Revision: 1.1 $
 */
public class ListEntityProxy
    extends org.jboss.ejb.plugins.jrmp.interfaces.ListEntityProxy
   implements java.lang.reflect.InvocationHandler
{
   public ListEntityProxy()
   {
      // For externalization to work
   }

   public ListEntityProxy(String name, ContainerRemote container, Object id, boolean optimize, List list, long listId, int index)
   {
       super(name, container, id, optimize, list, listId, index);
   }
}

