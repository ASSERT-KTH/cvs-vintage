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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.math.BigDecimal;

import org.jboss.cmp.schema.AbstractClass;
import org.jboss.cmp.schema.AbstractSchema;
import org.jboss.cmp.schema.AbstractType;
import org.jboss.cmp.schema.DuplicateNameException;

/**
 * An implementation of an AbstractSchema for EJB based systems.
 */
public class EJB20Schema implements AbstractSchema
{
   private Set ejbNames = new HashSet();
   private Map entitiesByEJBName = new HashMap();
   private Map entitiesBySchemaName = new HashMap();
   private Map relationsByName = new HashMap();

   private static final AbstractType[] builtins = {
      null,
      new JavaType(Object.class, AbstractType.OBJECT),
      new JavaType(boolean.class, AbstractType.BOOLEAN),
      new JavaType(String.class, AbstractType.STRING),
      new JavaType(int.class, AbstractType.INTEGER),
      new JavaType(double.class, AbstractType.DOUBLE),
      new JavaType(BigDecimal.class, AbstractType.DECIMAL),
      new JavaType(java.util.Date.class, AbstractType.DATETIME),
      new JavaType(byte[].class, AbstractType.BINARY)
   };

   public AbstractClass getClassByName(String name)
   {
      return getEntityBySchemaName(name);
   }

   public boolean isClassNameInUse(String name)
   {
      return ejbNames.contains(name) || entitiesBySchemaName.keySet().contains(name);
   }

   public AbstractType getBuiltinType(int family)
   {
      return builtins[family];
   }

   /**
    * Add a name to the list of EJBs. This allows ejb-names that are not part
    * of the schema to be reserved (preventing their use as aliases). Typically
    * these would be the ejb-names of Session beans.
    * @param name the ejb-name to reserve
    * @throws DuplicateNameException if the name is already reserved
    */
   public void reserveEJBName(String name) throws DuplicateNameException
   {
      if (ejbNames.add(name) == false)
      {
         throw new DuplicateNameException("EJB already defined with name " + name);
      }
   }

   /**
    * Add a BMP Entity bean to the schema. This allows their use as targets
    * for a ejb-relation association.
    * @param name the ejb-name of the BMP Entity bean
    * @return a BMPEntity representing this entity in the schema
    * @throws DuplicateNameException if a bean with the given name already exists
    */
   public BMPEntity addBMPEntity(String name) throws DuplicateNameException
   {
      reserveEJBName(name);
      BMPEntity entity = new BMPEntity(name);
      entitiesByEJBName.put(name, entity);
      return entity;
   }

   /**
    * Add a CMP Entity bean to the schema.
    * @param name the ejb-name of the entity bean
    * @param schemaName the abstract-schema-name of the entity bean
    * @return a CMPEntity represting this entity in the schema
    * @throws DuplicateNameException if a bean with the given name already exists
    */
   public CMPEntity addCMPEntity(String name, String schemaName) throws DuplicateNameException
   {
      reserveEJBName(name);
      if (entitiesBySchemaName.containsKey(schemaName))
      {
         throw new DuplicateNameException("EJB already defined with abstract schema name " + name);
      }
      CMPEntity entity = new CMPEntity(name, schemaName);
      entitiesByEJBName.put(name, entity);
      entitiesBySchemaName.put(schemaName, entity);
      return entity;
   }

   /**
    * Add a relation between two entity beans
    * @param name the name of this relation; may be null
    * @param left the left hand entity
    * @param leftIsMany whether the left has one or many multiplicity
    * @param leftName the name of the left hand end
    * @param right the right hand entity
    * @param rightIsMany whether the right has one or many multiplicity
    * @param rightName the name of the right hand end
    * @return a EJBRelation representing this association
    */
   public EJBRelation addEJBRelation(String name, Entity left, boolean leftIsMany, String leftName, Entity right, boolean rightIsMany, String rightName)
   {
      CMRField leftEnd = new CMRField(leftName, leftIsMany, left);
      CMRField rightEnd = new CMRField(rightName, rightIsMany, right);
      EJBRelation rel = new EJBRelation(name, leftEnd, rightEnd);
      if (leftEnd.isNavigable())
      {
         ((CMPEntity) left).addCMRField(leftEnd);
      }
      if (rightEnd.isNavigable())
      {
         ((CMPEntity) right).addCMRField(rightEnd);
      }
      relationsByName.put(rel.getName(), rel);
      return rel;
   }

   /**
    * Return the Entity with the given ejb-name
    * @param name the ejb-name of the Entity
    * @return the Entity or null if not found
    */
   public Entity getEntityByName(String name)
   {
      return (Entity) entitiesByEJBName.get(name);
   }

   /**
    * Return the Entity with the given abstract-schema-name
    * @param schemaName the abstract-schema-name of the Entity
    * @return the Entity or null if not found
    */
   public CMPEntity getEntityBySchemaName(String schemaName)
   {
      return (CMPEntity) entitiesBySchemaName.get(schemaName);
   }

   /**
    * Return the EJBRelation with the given name
    * @param name the name of the ejb-relation
    * @return the EJBRelation or null if not found (names are optional)
    */
   public EJBRelation getRelationByName(String name)
   {
      return (EJBRelation) relationsByName.get(name);
   }
}
