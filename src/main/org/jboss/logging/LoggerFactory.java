/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.logging;

import org.apache.log4j.Category;
import org.apache.log4j.spi.CategoryFactory;

/** A custom category factory that returns Logger instaneces
 * @author Scott.Stark@jboss.org
 * @version $Revision: 1.1 $
 */
public class LoggerFactory implements CategoryFactory
{
   public Category makeNewCategoryInstance(String name)
   {
      return new Logger(name);
   }
}
