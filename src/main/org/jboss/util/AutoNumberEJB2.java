/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.util;

import javax.ejb.EntityBean;
import javax.naming.InitialContext;

/**
 * The CMP 2 compatible version of AutoNumberEJB
 * @author <a href="mailto:michel.anke@wolmail.nl">Michel de Groot</a>
 * @author <a href="mailto:icoloma@iverdino.com">Ignacio Coloma</a>
 * @version $Revision: 1.1 $
 */
public abstract class AutoNumberEJB2 implements EntityBean
{
   
   abstract public Integer getValue();
   abstract public void setValue(Integer value);
   
   abstract public String getName();
   abstract public void setName(String name);
   
   public String ejbCreate(String name)
   {
      return null;
   }
   
   public void ejbPostCreate(String name) {}
   
   public void ejbActivate() {}
   public void ejbPassivate() {}
   public void ejbLoad() {}
   public void ejbStore() {}
   public void ejbRemove() {}
   public void setEntityContext(javax.ejb.EntityContext unused) {}
   public void unsetEntityContext() {}
   
}