/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins.cmp.jdbc.metadata;

import java.util.Collection;
import java.util.Collections;
import java.util.ArrayList;
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
 * @version $Revision: 1.16 $
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
   
   /**
    * Type of the cmr field (i.e., collection or set)
    */
   private final JDBCReadAheadMetaData readAhead;
   
   /**
    * The other role in this relationship.
    */
   private JDBCRelationshipRoleMetaData relatedRole;

   /**
    * The key fields used by this role by field name.
    */
   private Map keyFields;
   
   public JDBCRelationshipRoleMetaData(
         JDBCRelationMetaData relationMetaData,
         JDBCApplicationMetaData application,
         RelationshipRoleMetaData role) throws DeploymentException {
      
      this.relationMetaData = relationMetaData;
      
      relationshipRoleName = role.getRelationshipRoleName();
      multiplicityOne = role.isMultiplicityOne();
      cascadeDelete = role.isCascadeDelete();
      foreignKeyConstraint = false;
      readAhead = null;
      
      cmrFieldName = loadCMRFieldName(role);
      cmrFieldType = role.getCMRFieldType();

      // get the entity for this role
      entity = application.getBeanByEjbName(role.getEntityName());
      if(entity == null) {
         throw new DeploymentException("Entity: " + role.getEntityName() + 
              " not found for: " + role);
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

      // read-ahead
      Element readAheadElement = 
            MetaData.getOptionalChild(element, "read-ahead");
      if(readAheadElement != null) {
         readAhead = new JDBCReadAheadMetaData(
               readAheadElement, entity.getReadAhead());
      } else {
         readAhead = entity.getReadAhead();
      }
   }

   public void init(JDBCRelationshipRoleMetaData relatedRole) 
         throws DeploymentException {
      init(relatedRole, null);
   }

   public void init(JDBCRelationshipRoleMetaData relatedRole, Element element) 
         throws DeploymentException {
      this.relatedRole = relatedRole;
      if(element == null || "defaults".equals(element.getTagName())) {
         keyFields = loadKeyFields();
      } else {
         keyFields = loadKeyFields(element);
      }
   }

   private String loadCMRFieldName(RelationshipRoleMetaData role) {
      String fieldName = role.getCMRFieldName();
      if(fieldName == null) {
         // no cmr field on this side use relatedEntityName_relatedCMRFieldName
         RelationshipRoleMetaData relatedRole =
               role.getRelatedRoleMetaData();
         fieldName = relatedRole.getEntityName() + "_" +
               relatedRole.getCMRFieldName();
      }
      return fieldName;
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
    * Gets the read ahead meta data
    */
   public JDBCReadAheadMetaData getReadAhead() {
      return readAhead;
   }

   /**
    * Gets the key fields of this role.
    * @return an unmodifiable collection of JDBCCMPFieldMetaData objects
    */
   public Collection getKeyFields() {
      return Collections.unmodifiableCollection(keyFields.values());
   }

   /**
    * Loads the key fields for this role based on the primary keys of the
    * this entity.
    */
   private Map loadKeyFields() {
      // with foreign key mapping, the one side of one-to-many
      // does not have key fields
      if(relationMetaData.isForeignKeyMappingStyle() && isMultiplicityMany()) {
         return Collections.EMPTY_MAP;
      }

      // get all of the pk fields
      ArrayList pkFields = new ArrayList();
      for(Iterator i = entity.getCMPFields().iterator(); i.hasNext(); ) {
         JDBCCMPFieldMetaData cmpField = (JDBCCMPFieldMetaData)i.next();

         if(cmpField.isPrimaryKeyMember()) {
            pkFields.add(cmpField);
         }
      }
      
      // generate a new key field for each pk field
      Map fields = new HashMap(pkFields.size());
      for(Iterator i = pkFields.iterator(); i.hasNext(); ) {
         JDBCCMPFieldMetaData cmpField = (JDBCCMPFieldMetaData)i.next();
      
         String columnName;
         if(relationMetaData.isTableMappingStyle()) {
            if(entity.equals(relatedRole.getEntity())) {
               columnName = getCMRFieldName();
            } else {
               columnName = entity.getName();
            }
         } else {
            columnName = relatedRole.getCMRFieldName();
         }

         if(pkFields.size() > 1) {
            columnName += "_" + cmpField.getFieldName();
         }

         cmpField = new JDBCCMPFieldMetaData(
               entity,
               cmpField,
               columnName,
               false,
               relationMetaData.isTableMappingStyle(),
               relationMetaData.isReadOnly(),
               relationMetaData.getReadTimeOut());
         fields.put(cmpField.getFieldName(), cmpField);
      }
      return Collections.unmodifiableMap(fields);
   }

   /**
    * Loads the key fields for this role based on the primary keys of the
    * this entity and the override data from the xml element.
    */
   private Map loadKeyFields(Element element)
         throws DeploymentException {
      
      Element keysElement = 
            MetaData.getOptionalChild(element,"key-fields");
      
      // no field overrides, we're done
      if(keysElement == null) {
         return loadKeyFields();
      }

      // load overrides
      Iterator iter = MetaData.getChildrenByTagName(keysElement, "key-field");
      
      // if key-fields element empty, no key should be used
      if(!iter.hasNext()) {
         return Collections.EMPTY_MAP;
      } else if(relationMetaData.isForeignKeyMappingStyle() 
            && isMultiplicityMany()) {
         throw new DeploymentException("Role: "+relationshipRoleName+" with multiplicity many using " +
               "foreign-key mapping is not allowed to have key-fields");
      }

      // load the default field values
      Map defaultFields = new HashMap(loadKeyFields());
     
      // load overrides
      Map fields = new HashMap(defaultFields.size());
      while(iter.hasNext()) {
         Element keyElement = (Element)iter.next();
         String fieldName = 
               MetaData.getUniqueChildContent(keyElement, "field-name");

         JDBCCMPFieldMetaData cmpField = 
               (JDBCCMPFieldMetaData)defaultFields.remove(fieldName);
         if(cmpField == null) {
            throw new DeploymentException(
                  "CMP field for key not found: field name=" + fieldName);
         }
         cmpField = new JDBCCMPFieldMetaData(
               entity,
               keyElement,
               cmpField,
               false,
               relationMetaData.isTableMappingStyle(),
               relationMetaData.isReadOnly(),
               relationMetaData.getReadTimeOut());
         fields.put(cmpField.getFieldName(), cmpField);
      }
      
      // all fields must be overriden
      if(!defaultFields.isEmpty()) {
         throw new DeploymentException("Mappings were not provided for all " +
               "fields: unmaped fields=" + defaultFields.keySet());
      }
      return Collections.unmodifiableMap(fields);
   }
}
