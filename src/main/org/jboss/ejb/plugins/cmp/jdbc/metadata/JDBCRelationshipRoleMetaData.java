/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins.cmp.jdbc.metadata;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.jboss.deployment.DeploymentException;
import org.jboss.metadata.MetaData;
import org.jboss.metadata.RelationshipRoleMetaData;
import org.w3c.dom.Element;

/** 
 * Imutable class which represents one ejb-relationship-role element found in
 * the ejb-jar.xml file's ejb-relation elements.
 *
 * @author <a href="mailto:dain@daingroup.com">Dain Sundstrom</a>
 * @version $Revision: 1.11 $
 */
public final class JDBCRelationshipRoleMetaData {
   /**
    * Relation to which this role belongs.
    */
   private final JDBCRelationMetaData relationMetaData;

   /**
    * Role name
    */
   private final String relationshipRoleName;
   
   /**
    * Is the multiplicity one? If not, multiplicity is many.
    */
   private final boolean multiplicityOne;
   
   /**
    * Should this role have a foreign key constraint?
    */
   private final boolean foreignKeyConstraint;
   
   /**
    * Should this entity be deleted when related entity is deleted.
    */
   private final boolean cascadeDelete;
   
   /**
    * The entity that has this role.
    */
   private final JDBCEntityMetaData entity;
   
   /**
    * Name of the entity's cmr field for this role.
    */
   private final String cmrFieldName;
      
   /**
    * Type of the cmr field (i.e., collection or set)
    */
   private final String cmrFieldType;
   
   private final Map tableKeyFields = new HashMap();
   private final Map foreignKeyFields = new HashMap();
   
   public JDBCRelationshipRoleMetaData(
         JDBCRelationMetaData relationMetaData,
         JDBCApplicationMetaData application,
         RelationshipRoleMetaData relationshipRole) throws DeploymentException {
      
      this.relationMetaData = relationMetaData;
      RelationshipRoleMetaData relatedRole =
               relationshipRole.getRelatedRoleMetaData();
      
      relationshipRoleName = relationshipRole.getRelationshipRoleName();
      multiplicityOne = relationshipRole.isMultiplicityOne();
      cascadeDelete = relationshipRole.isCascadeDelete();
      foreignKeyConstraint = false;
      
      String tempCmrFieldName = relationshipRole.getCMRFieldName();
      if(tempCmrFieldName == null) {
         // no cmr field on this side use relatedEntityName_relatedCMRFieldName
         tempCmrFieldName = relatedRole.getEntityName() + "_" +
               relatedRole.getCMRFieldName();
      }
      cmrFieldName = tempCmrFieldName;
      cmrFieldType = relationshipRole.getCMRFieldType();

      // get the entity for this role
      entity = application.getBeanByEjbName(relationshipRole.getEntityName());
      if(entity == null) {
         throw new DeploymentException("Entity: " + 
              relationshipRole.getEntityName() + 
              " not found for: " + relationshipRoleName);
      }
      
      if(relationMetaData.isTableMappingStyle()) {
         loadTableKeyFields();
      } else if(relatedRole.isMultiplicityOne()){   
         String relatedEntityName = relatedRole.getEntityName();
         loadForeignKeyFields(application.getBeanByEjbName(relatedEntityName));
      }
   }

   public JDBCRelationshipRoleMetaData(
         JDBCRelationMetaData relationMetaData,
         JDBCApplicationMetaData application,
         Element element, 
         JDBCRelationshipRoleMetaData defaultValues)
         throws DeploymentException {
      
      this.relationMetaData = relationMetaData;
      this.entity = application.getBeanByEjbName(
            defaultValues.getEntity().getName());
      
      relationshipRoleName = defaultValues.getRelationshipRoleName();
      multiplicityOne = defaultValues.isMultiplicityOne();
      cascadeDelete = defaultValues.isCascadeDelete();
      
      cmrFieldName = defaultValues.getCMRFieldName();
      cmrFieldType = defaultValues.getCMRFieldType();      
      
      // foreign key constraint?  If not provided, keep default.
      String fkString = MetaData.getOptionalChildContent(
            element, "fk-constraint");
      if(fkString != null) {
         foreignKeyConstraint = Boolean.valueOf(fkString).booleanValue();
      } else {
         foreignKeyConstraint = defaultValues.hasForeignKeyConstraint();
      }

      if(relationMetaData.isTableMappingStyle()) {
         if("defaults".equals(element.getTagName())) {
            loadTableKeyFields();
         } else {
            loadTableKeyFields(element);
         }
      } else if(defaultValues.getRelatedRole().isMultiplicityOne()) {
         String relatedEntityName =
               defaultValues.getRelatedRole().getEntity().getName();
         JDBCEntityMetaData relatedEntity = 
               application.getBeanByEjbName(relatedEntityName);

         if("defaults".equals(element.getTagName())) {
            loadForeignKeyFields(relatedEntity);
         } else {
            loadForeignKeyFields(element, relatedEntity);
         }
      }
   }
   
   /**
    * Gets the relation to which this role belongs.
    */
   public JDBCRelationMetaData getRelationMetaData() {
      return relationMetaData;
   }
   
   /**
    * Gets the name of this role.
    */
   public String getRelationshipRoleName() {
      return relationshipRoleName;
   }
   
   /**
    * Should this role use a foreign key constraint.
    * @return true if the store mananager will execute an ALTER TABLE ADD
    * CONSTRAINT statement to add a foreign key constraint.
    */
   public boolean hasForeignKeyConstraint() {
      return foreignKeyConstraint;
   }
 
   /**
    * Checks if the multiplicity is one.
    */
   public boolean isMultiplicityOne() {
      return multiplicityOne;
   }
   
   /**
    * Checks if the multiplicity is many.
    */
   public boolean isMultiplicityMany() {
      return !multiplicityOne;
   }
   
   /**
    * Is this field single valued, that means it does not return a collection.
    * A relationship role is single valued if the related role has a 
    * multiplicity of one.
    * @return true if this role does not return a collection 
    */
   public boolean isSingleValued() {
      return getRelatedRole().isMultiplicityOne();
   }
   
   /**
    * Is this field collection valued, that means it returns a collection.
    * A relationship role is collection valued if the related role has a
    * multiplicity of many.
    * @return true if this role returns a collection 
    */
   public boolean isCollectionValued() {
      return getRelatedRole().isMultiplicityMany();
   }

   /**
    * Should this entity be deleted when related entity is deleted.
    */
   public boolean isCascadeDelete() {
      return cascadeDelete;
   }
   
   /**
    * Gets the name of the entity that has this role.
    */
   public JDBCEntityMetaData getEntity() {
      return entity;
   }
   
   /**
    * Gets the name of the entity's cmr field for this role.
    */
   public String getCMRFieldName() {
      return cmrFieldName;
   }
   
   /**
    * Gets the type of the cmr field (i.e., collection or set)
    */
   public String getCMRFieldType() {
      return cmrFieldType;
   }   

   /**
    * Gets the related role's jdbc meta data.
    */
   public JDBCRelationshipRoleMetaData getRelatedRole() {
      return relationMetaData.getOtherRelationshipRole(this);
   }
   
   /**
    * Gets the foreign key fields of this role. The foreign key fields hold the
    * primary keys of the related entity. A relationship role has foreign key 
    * fields if the relation mapping style is foreign key and the other side of
    * the relationship has a multiplicity of one.
    * @return an unmodifiable collection of JDBCCMPFieldMetaData objects
    */
   public Collection getForeignKeyFields() {
      return Collections.unmodifiableCollection(foreignKeyFields.values());
   }
   
   /**
    * Gets the key fields of this role in the relation table. The table key
    * fields hold the primary keys of this role's entity. A relationship role
    * has table key  fields if the relation is mapped to a relation table.
    * @return an unmodifiable collection of JDBCCMPFieldMetaData objects
    */
   public Collection getTableKeyFields() {
      return Collections.unmodifiableCollection(tableKeyFields.values());
   }

   /**
    * Loads the foreign key fields for this role based on the primary keys of
    * the specified related entity.
    */
   private void loadForeignKeyFields(JDBCEntityMetaData relatedEntity) 
         throws DeploymentException {

      if(relatedEntity == null) {
         throw new DeploymentException("Entity: Related entity not found " +
               "for: " + relationshipRoleName);
      }

      for(Iterator i = relatedEntity.getCMPFields().iterator(); i.hasNext();) {
         JDBCCMPFieldMetaData cmpField = (JDBCCMPFieldMetaData)i.next();

         if(cmpField.isPrimaryKeyMember()) {
            cmpField = new JDBCCMPFieldMetaData(
                  entity,
                  cmpField,
                  getCMRFieldName() + "_" + cmpField.getFieldName(),
                  false,
                  relationMetaData.isReadOnly(),
                  relationMetaData.getReadTimeOut());
            foreignKeyFields.put(cmpField.getFieldName(), cmpField);
         }
      }
   }
   
   /**
    * Loads the foreign key fields for this role based on the primary keys of
    * the specified related entity and the override data from the xml element.
    */
   private void loadForeignKeyFields(
         Element element,
         JDBCEntityMetaData relatedEntity) throws DeploymentException {

      loadForeignKeyFields(relatedEntity);

      Element foreignKeysElement = MetaData.getOptionalChild(
            element,"foreign-key-fields");
      
      // no field overrides, we're done
      if(foreignKeysElement == null) {
         return;
      }
      
      // load overrides
      Iterator fkIter = MetaData.getChildrenByTagName(
            foreignKeysElement, "foreign-key-field");
      
      // if empty foreign-key-fields element, no fk should be used
      if(!fkIter.hasNext()) {
         foreignKeyFields.clear();
      }
      
      while(fkIter.hasNext()) {
         Element foreignKeyElement = (Element)fkIter.next();
         String foreignKeyName = MetaData.getUniqueChildContent(
               foreignKeyElement, "field-name");
         JDBCCMPFieldMetaData cmpField = 
               (JDBCCMPFieldMetaData)foreignKeyFields.get(foreignKeyName);
         if(cmpField == null) {
            throw new DeploymentException(
                  "CMP field for foreign key not found: field name=" + 
                  foreignKeyName);
         }
         cmpField = new JDBCCMPFieldMetaData(
               entity,
               foreignKeyElement,
               cmpField,
               false,
               relationMetaData.isReadOnly(),
               relationMetaData.getReadTimeOut());
         foreignKeyFields.put(cmpField.getFieldName(), cmpField);
      }
   }

   /**
    * Loads the table key fields for this role based on the primary keys of the
    * this entity.
    */
   private void loadTableKeyFields() {

      for(Iterator i = entity.getCMPFields().iterator(); i.hasNext(); ) {
         JDBCCMPFieldMetaData cmpField = (JDBCCMPFieldMetaData)i.next();

         if(cmpField.isPrimaryKeyMember()) {
            cmpField = new JDBCCMPFieldMetaData(
                  entity,
                  cmpField,
                  getCMRFieldName() + "_" + cmpField.getFieldName(),
                  false,
                  relationMetaData.isReadOnly(),
                  relationMetaData.getReadTimeOut());
            tableKeyFields.put(cmpField.getFieldName(), cmpField);
         }
      }
   }

   /**
    * Loads the table key fields for this role based on the primary keys of the
    * this entity and the override data from the xml element.
    */
   private void loadTableKeyFields(Element element) throws DeploymentException {
      loadTableKeyFields();
      
      Element tableKeysElement = MetaData.getOptionalChild(
            element,"table-key-fields");
      
      // no field overrides, we're done
      if(tableKeysElement == null) {
         return;
      }
      
      // load overrides
      for(Iterator i = MetaData.getChildrenByTagName(
               tableKeysElement, "table-key-field"); i.hasNext(); ) {

         Element tableKeyElement = (Element)i.next();
         String tableKeyName = MetaData.getUniqueChildContent(
               tableKeyElement, "field-name");
         JDBCCMPFieldMetaData cmpField = 
               (JDBCCMPFieldMetaData)tableKeyFields.get(tableKeyName);
         if(cmpField == null) {
            throw new DeploymentException(
                  "CMP field for table key not found: field name=" + 
                  tableKeyName);
         }
         cmpField = new JDBCCMPFieldMetaData(
               entity,
               tableKeyElement,
               cmpField,
               false,
               relationMetaData.isReadOnly(),
               relationMetaData.getReadTimeOut());
         tableKeyFields.put(cmpField.getFieldName(), cmpField);
      }
   }
}
