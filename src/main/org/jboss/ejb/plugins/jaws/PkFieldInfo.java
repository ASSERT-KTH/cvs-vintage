/*
 * jBoss, the OpenSource EJB server
 *
 * Distributable under GPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins.jaws;

import java.lang.reflect.Field;
import java.util.Iterator;

/**
 * This is a wrapper class that holds all the
 * information JawsPersistenceManager commands
 * need about a primary key field.
 *      
 * @see <related>
 * @author <a href="mailto:justin@j-m-f.demon.co.uk">Justin Forder</a>
 * @version $Revision: 1.1 $
 */

public class PkFieldInfo
{
   // Attributes ----------------------------------------------------
    
   private Field pkField = null;
   private CMPFieldInfo cmpFieldInfo;
   
   // Constructors --------------------------------------------------
   
   // This constructor is used when the primary key is composite.
   // The pkField is a field on the primary key class.
   public PkFieldInfo(Field pkField, Iterator cmpFieldInfos)
   {
      this(pkField.getName(), cmpFieldInfos);
      this.pkField = pkField;
   }
   
   // This constructor is used when the primary key is simple.
   // There is no primary key class, and there is no pkField.
   public PkFieldInfo(String pkFieldName, Iterator cmpFieldInfos)
   {
      // Linear search to find a CMPFieldInfo with the same name.
      // Better if the collection supported lookup by name.
      while (cmpFieldInfos.hasNext())
      {
         CMPFieldInfo cmpFieldInfo = (CMPFieldInfo)cmpFieldInfos.next();
         if (cmpFieldInfo.getName().equals(pkFieldName))
         {
            cmpFieldInfo.setPrimary();
            this.cmpFieldInfo = cmpFieldInfo;
            break;
         }
      }
   }
   
   // Public --------------------------------------------------------
   
   public final String getName()
   {
      return cmpFieldInfo.getName();
   }
   
   // N.B. This returns null if the primary key is not composite.
   public final Field getPkField()
   {
      return pkField;
   }
   
   public final Field getCMPField()
   {
      return cmpFieldInfo.getField();
   }
   
   public final int getJDBCType()
   {
      return cmpFieldInfo.getJDBCType();
   }
   
   public final String getSQLType()
   {
      return cmpFieldInfo.getSQLType();
   }
   
   public final String getColumnName()
   {
      return cmpFieldInfo.getColumnName();
   }
}
