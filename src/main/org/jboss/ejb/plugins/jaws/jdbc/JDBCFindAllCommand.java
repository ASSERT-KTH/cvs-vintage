/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.ejb.plugins.jaws.jdbc;

/**
 * JAWSPersistenceManager JDBCFindAllCommand
 *
 * @see <related>
 * @author <a href="mailto:rickard.oberg@telkel.com">Rickard Öberg</a>
 * @author <a href="mailto:marc.fleury@telkel.com">Marc Fleury</a>
 * @author <a href="mailto:shevlandj@kpi.com.au">Joe Shevland</a>
 * @author <a href="mailto:justin@j-m-f.demon.co.uk">Justin Forder</a>
 * @version $Revision: 1.3 $
 */
public class JDBCFindAllCommand extends JDBCFinderCommand
{
   // Constructors --------------------------------------------------
   
   public JDBCFindAllCommand(JDBCCommandFactory factory)
   {
      super(factory, "FindAll");
      
      String sql = "SELECT " + getPkColumnList() + " FROM " + jawsEntity.getTableName();

      setSQL(sql);
   }
}
