/*
 * jBoss, the OpenSource EJB server
 *
 * Distributable under GPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins.jaws;

import java.lang.reflect.Field;
import java.rmi.RemoteException;
import java.rmi.ServerException;
import java.util.Collection;
import java.util.ArrayList;
import java.util.Iterator;
import java.sql.Types;
import org.jboss.ejb.EntityContainer;
import org.jboss.ejb.plugins.jaws.deployment.JawsEnterpriseBeans;
import org.jboss.ejb.plugins.jaws.deployment.JawsEjbReference;
import org.jboss.ejb.plugins.jaws.deployment.JawsEntity;
import org.jboss.ejb.plugins.jaws.deployment.JawsCMPField;

/**
 * This is a wrapper class that holds all the
 * information JawsPersistenceManager commands
 * need about a CMP field.
 *      
 * @see <related>
 * @author <a href="mailto:rickard.oberg@telkel.com">Rickard Öberg</a>
 * @author <a href="mailto:marc.fleury@telkel.com">Marc Fleury</a>
 * @author <a href="mailto:shevlandj@kpi.com.au">Joe Shevland</a>
 * @author <a href="mailto:justin@j-m-f.demon.co.uk">Justin Forder</a>
 * @version $Revision: 1.1 $
 */
public class CMPFieldInfo 
{
   // Attributes ----------------------------------------------------
    
   private JawsCMPField jawsCMPField;
   private Field field;
   private int jdbcType;
   private String columnName;
   private JawsCMPField[] foreignKeyCMPFields;
   boolean isAPrimaryKeyField = false;

   // Constructors --------------------------------------------------
   
   public CMPFieldInfo(JawsCMPField jawsCMPField,
                       EntityContainer container)
      throws NoSuchFieldException, RemoteException
   {
      this.jawsCMPField = jawsCMPField;

      String name = jawsCMPField.getFieldName();
      this.field = container.getBeanClass().getField(name);

      String jdbcTypeName = jawsCMPField.getJdbcType();
      this.jdbcType = jdbcTypeFromTypeName(jdbcTypeName);

      if (this.jdbcType == Types.REF)
      {
         this.foreignKeyCMPFields = 
            getPkColumns(jawsCMPField, container);
      }
   }
   
   // Public --------------------------------------------------------

   public final String getName()
   {
      return jawsCMPField.getFieldName();
   }

   public final Field getField()
   {
      return field;
   }

   public final int getJDBCType()
   {
      return jdbcType;
   }

   public final String getSQLType()
   {
      return jawsCMPField.getSqlType();
   }

   public final String getColumnName()
   {
      return jawsCMPField.getColumnName();
   }

   public final boolean isEJBReference()
   {
      return (jdbcType == Types.REF);
   }

   public final JawsCMPField[] getForeignKeyCMPFields()
   {
      return foreignKeyCMPFields;
   }

   public final boolean isAPrimaryKeyField()
   {
      return isAPrimaryKeyField;
   }
   
   // Package protected ---------------------------------------------
    
   void setPrimary()
   {
      isAPrimaryKeyField = true;
   }

   // Private -------------------------------------------------------

   private JawsCMPField[] getPkColumns(JawsCMPField field,
                                       EntityContainer container)
      throws RemoteException
   {
      // Find reference
      Iterator enum = ((JawsEntity)field.getBeanContext()).getEjbReferences();
      while (enum.hasNext())
      {
         JawsEjbReference ref = (JawsEjbReference)enum.next();
         if (ref.getName().equals(field.getSqlType()))
         {
            // Find referenced entity
            JawsEnterpriseBeans eb = (JawsEnterpriseBeans)field.getBeanContext().getBeanContext();
            JawsEntity referencedEntity = (JawsEntity)eb.getEjb(ref.getLink());
            // Extract pk
            String pk = referencedEntity.getPrimaryKeyField();
            if (pk.equals(""))
            {
               // Compound key
               try
               {
                  Class pkClass = container.getClassLoader().loadClass(referencedEntity.getPrimaryKeyClass());
                  Field[] pkFields = pkClass.getFields();
                  ArrayList result = new ArrayList();
                  for (int i = 0; i < pkFields.length; i++)
                  {
                     // Find db mapping for pk field
                     Iterator fieldEnum = referencedEntity.getCMPFields();
                     while (fieldEnum.hasNext())
                     {
                        JawsCMPField pkField = (JawsCMPField)fieldEnum.next();
                        if (pkField.getFieldName().equals(pkFields[i].getName()))
                           result.add(pkField);
                     }
                  }
                  return (JawsCMPField[])result.toArray(new JawsCMPField[0]);
               } catch (ClassNotFoundException e)
               {
                  throw new ServerException("Could not load pk class of referenced entity",e);
               }
            } else
            {
               // Find db mapping for pk
               Iterator fieldEnum = referencedEntity.getCMPFields();
               while (fieldEnum.hasNext())
               {
                  JawsCMPField pkField = (JawsCMPField)fieldEnum.next();
                  if (pkField.getFieldName().equals(pk))
                     return new JawsCMPField[] { pkField };
               }
               return new JawsCMPField[0];
            }
         }
      }

      throw new ServerException("Could not find EJB reference. Must be defined in XML-descriptor");
   }
   
   private final int jdbcTypeFromTypeName(String name)
   {
      try
      {
         Integer constant = (Integer)Types.class.getField(name).get(null);
         return constant.intValue();
      } catch (Exception e)
      {
         // Dubious - better to throw a meaningful exception
         e.printStackTrace();
         return Types.OTHER;
      }
   }
}
