/***************************************
 *                                     *
 *  JBoss: The OpenSource J2EE WebOS   *
 *                                     *
 *  Distributable under LGPL license.  *
 *  See terms of license at gnu.org.   *
 *                                     *
 ***************************************/

package org.jboss.cmp.ejb;

import java.util.HashMap;
import java.util.Map;

import org.jboss.persistence.schema.AbstractAssociationEnd;
import org.jboss.persistence.schema.AbstractAttribute;

public class CMPEntity extends Entity
{
   protected String schemaName;
   private Map cmpFields = new HashMap();
   private Map cmrFields = new HashMap();

   public CMPEntity(String ejbName, String schemaName)
   {
      super(ejbName);
      this.schemaName = schemaName;
   }

   public void addCMPField(CMPField type)
   {
      cmpFields.put(type.getName(), type);
   }

   public void addCMRField(CMRField cmrField)
   {
      cmrFields.put(cmrField.getName(), cmrField);
   }

   public AbstractAttribute getAttributeByName(String name)
   {
      return (AbstractAttribute) cmpFields.get(name);
   }

   public AbstractAssociationEnd getAssocationByName(String name)
   {
      return (AbstractAssociationEnd) cmrFields.get(name);
   }

   public String getSchemaName()
   {
      return schemaName;
   }

   public void setPkFields(String[] pkFields)
   {
   }

   public String toString()
   {
      return getName();
   }
}
