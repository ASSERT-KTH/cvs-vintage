/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins.cmp.jdbc.metadata;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import org.jboss.ejb.DeploymentException;
import org.jboss.logging.Logger;

import org.jboss.metadata.ApplicationMetaData;
import org.jboss.metadata.BeanMetaData;
import org.jboss.metadata.EntityMetaData;
import org.jboss.metadata.MetaData;
import org.jboss.metadata.RelationMetaData;
import org.jboss.metadata.XmlLoadable;
import org.w3c.dom.Element;

/**
 *  This immutable class contains information about the application
 *
 * @author <a href="mailto:dain@daingroup.com">Dain Sundstrom</a>
 * @author <a href="sebastien.alborini@m4x.org">Sebastien Alborini</a>
 * @version $Revision: 1.11 $
 */
public final class JDBCApplicationMetaData
{
   public final static String JDBC_PM = "org.jboss.ejb.plugins.cmp.jdbc.JDBCStoreManager";
   static Logger log = Logger.create(JDBCApplicationMetaData.class);
   /**
    * The class loader for this application.  The class loader is used to load all classes
    * used by this application.
    */
   private final ClassLoader classLoader;

   /**
    * Application metadata loaded from the ejb-jar.xml file
    */
   private final ApplicationMetaData applicationMetaData;

   /**
    * Map of the type mappings by name.
    */
   private final Map typeMappings = new HashMap();

   /**
    * Map of the entities managed by jbosscmp-jdbc by bean name.
    */
   private final Map entities = new HashMap();

   /**
    * Map of relations in this application by name.
    * Items are instance of JDBCRelationMetaData.
    */
   private final Map relationships = new HashMap();

   /**
    * Map of the relationship roles for an entity by entity object.
    */
   private final Map entityRoles = new HashMap();

   /**
    * Map of the dependent value classes by java class type.
    */
   private final Map valueClasses = new HashMap();

   /**
    * Map from abstract schema name to entity name
    */
   private final Map entitiesByAbstractSchemaName = new HashMap();

   /**
    * Constructs jdbc application meta data with the data from the applicationMetaData.
    *
    * @param applicationMetaData the application data loaded from the ejb-jar.xml file
    * @param classLoader the ClassLoader used to load the classes of the application
    * @throws DeploymentException if an problem occures while loading the classes or if
    *      data in the ejb-jar.xml is inconsistent with data from jbosscmp-jdbc.xml file
    */
   public JDBCApplicationMetaData(ApplicationMetaData applicationMetaData, ClassLoader classLoader) throws DeploymentException {
      // the classloader is the same for all the beans in the application
      this.classLoader = classLoader;
      this.applicationMetaData = applicationMetaData;

      // create metadata for all jbosscmp-jdbc-managed cmp entities
      // we do that here in case there is no jbosscmp-jdbc.xml
      Iterator beans = applicationMetaData.getEnterpriseBeans();
      while (beans.hasNext()) {
         BeanMetaData bean = (BeanMetaData)beans.next();

         // only take entities
         if(bean.isEntity()) {
            EntityMetaData entity = (EntityMetaData)bean;

            // only take jbosscmp-jdbc-managed CMP entities
            if(entity.isCMP() && entity.getContainerConfiguration().getPersistenceManager().equals(JDBC_PM)) {
               JDBCEntityMetaData jdbcEntity = new JDBCEntityMetaData(this, entity);
               entities.put(entity.getEjbName(), jdbcEntity);
               entitiesByAbstractSchemaName.put(jdbcEntity.getAbstractSchemaName(), jdbcEntity);
               entityRoles.put(entity.getEjbName(), new HashMap());
            }
         }
      }

      // relationships
      Iterator iterator = applicationMetaData.getRelationships();
      while(iterator.hasNext()) {
         RelationMetaData relation = (RelationMetaData) iterator.next();
         JDBCRelationMetaData jdbcRelation = new JDBCRelationMetaData(this, relation);
         relationships.put(jdbcRelation.getRelationName(), jdbcRelation);

         JDBCRelationshipRoleMetaData left = jdbcRelation.getLeftRelationshipRole();
         Map leftEntityRoles = (Map)entityRoles.get(left.getEntity().getName());
         leftEntityRoles.put(left.getRelationshipRoleName(), left);

         JDBCRelationshipRoleMetaData right = jdbcRelation.getRightRelationshipRole();
         Map rightEntityRoles = (Map)entityRoles.get(right.getEntity().getName());
         rightEntityRoles.put(right.getRelationshipRoleName(), right);
      }
   }

   /**
    * Constructs application meta data with the data contained in the jboss-cmp xml
    * element from a jbosscmp-jdbc xml file. Optional values of the xml element that
    * are not present are loaded from the defalutValues parameter.
    *
    * @param element the xml Element which contains the metadata about this application
    * @param defaultValues the JDBCApplicationMetaData which contains the values
    *      for optional elements of the element
    * @throws DeploymentException if the xml element is not semantically correct
    */
   public JDBCApplicationMetaData(Element element, JDBCApplicationMetaData defaultValues) throws DeploymentException {
      // importXml will be called at least once: with standardjbosscmp-jdbc.xml
      // it may be called a second time with user-provided jbosscmp-jdbc.xml
      // we must ensure to set all defaults values in the first call

      classLoader = defaultValues.classLoader;
      applicationMetaData = defaultValues.applicationMetaData;

      // first get the type mappings. (optional, but always set in standardjbosscmp-jdbc.xml)
      typeMappings.putAll(defaultValues.typeMappings);
      Element typeMaps = MetaData.getOptionalChild(element, "type-mappings");
      if(typeMaps != null) {
         for(Iterator i = MetaData.getChildrenByTagName(typeMaps, "type-mapping"); i.hasNext(); ) {
            Element typeMappingElement = (Element)i.next();
            JDBCTypeMappingMetaData typeMapping = new JDBCTypeMappingMetaData(typeMappingElement);
            typeMappings.put(typeMapping.getName(), typeMapping);
         }
      }

      // get default settings for the beans (optional, but always set in standardjbosscmp-jdbc.xml)
      entities.putAll(defaultValues.entities);
      entitiesByAbstractSchemaName.putAll(defaultValues.entitiesByAbstractSchemaName);
      Element defaults = MetaData.getOptionalChild(element, "defaults");
      if(defaults != null) {
         ArrayList values = new ArrayList(entities.values());
         for(Iterator i = values.iterator(); i.hasNext(); ) {
            JDBCEntityMetaData entityMetaData = (JDBCEntityMetaData)i.next();
            entityMetaData = new JDBCEntityMetaData(this, defaults, entityMetaData);
            // replace the old meta data with the new
            entities.put(entityMetaData.getName(), entityMetaData);
            entitiesByAbstractSchemaName.put(entityMetaData.getAbstractSchemaName(), entityMetaData);
         }
      }

      // get the beans data (only in jbosscmp-jdbc.xml)
      Element enterpriseBeans = MetaData.getOptionalChild(element, "enterprise-beans");
      if(enterpriseBeans != null) {
         for(Iterator i = MetaData.getChildrenByTagName(enterpriseBeans, "entity"); i.hasNext(); ) {
            Element beanElement = (Element)i.next();

            // get the bean's data, gaurenteed to work because we create
            // a metadata object for each bean in the constructor.
            String ejbName = MetaData.getUniqueChildContent(beanElement, "ejb-name");
            JDBCEntityMetaData entityMetaData = (JDBCEntityMetaData)entities.get(ejbName);
            if(entityMetaData != null) {
               entityMetaData = new JDBCEntityMetaData(this, beanElement, entityMetaData);
               entities.put(entityMetaData.getName(), entityMetaData);
               entitiesByAbstractSchemaName.put(entityMetaData.getAbstractSchemaName(), entityMetaData);
            } else {
               log.warn("Warning: data found in jbosscmp-jdbc.xml for entity " + ejbName + " but bean is not a jbosscmp-jdbc-managed cmp entity in ejb-jar.xml");
            }
         }
      }

      // get default settings for the relationships (optional, but always set in standardjbosscmp-jdbc.xml)
      relationships.putAll(defaultValues.relationships);
      entityRoles.putAll(defaultValues.entityRoles);
      if(defaults != null) {
         for(Iterator i = relationships.values().iterator(); i.hasNext(); ) {
            JDBCRelationMetaData relationMetaData = (JDBCRelationMetaData)i.next();
            relationMetaData = new JDBCRelationMetaData(this, defaults, relationMetaData);
            relationships.put(relationMetaData.getRelationName(), relationMetaData);

            JDBCRelationshipRoleMetaData left = relationMetaData.getLeftRelationshipRole();
            Map leftEntityRoles = (Map)entityRoles.get(left.getEntity().getName());
            leftEntityRoles.put(left.getRelationshipRoleName(), left);

            JDBCRelationshipRoleMetaData right = relationMetaData.getRightRelationshipRole();
            Map rightEntityRoles = (Map)entityRoles.get(right.getEntity().getName());
            rightEntityRoles.put(right.getRelationshipRoleName(), right);
         }
      }

      // relationships
      // get the beans data (only in jbosscmp-jdbc.xml)
      Element relationshipsElement = MetaData.getOptionalChild(element, "relationships");
      if(relationshipsElement != null) {
         for(Iterator i = MetaData.getChildrenByTagName(relationshipsElement, "ejb-relation"); i.hasNext(); ) {
            Element relationElement = (Element)i.next();

            // get the bean's data, gaurenteed to work because we create
            // a metadata object for each bean in the constructor.
            String relationName = MetaData.getUniqueChildContent(relationElement, "ejb-relation-name");
            JDBCRelationMetaData jdbcRelation = (JDBCRelationMetaData)relationships.get(relationName);
            if(jdbcRelation != null) {
               jdbcRelation = new JDBCRelationMetaData(this, relationElement, jdbcRelation);
               relationships.put(jdbcRelation.getRelationName(), jdbcRelation);

               JDBCRelationshipRoleMetaData left = jdbcRelation.getLeftRelationshipRole();
               Map leftEntityRoles = (Map)entityRoles.get(left.getEntity().getName());
               leftEntityRoles.put(left.getRelationshipRoleName(), left);

               JDBCRelationshipRoleMetaData right = jdbcRelation.getRightRelationshipRole();
               Map rightEntityRoles = (Map)entityRoles.get(right.getEntity().getName());
               rightEntityRoles.put(right.getRelationshipRoleName(), right);
            } else {
               log.warn("Warning: data found in jbosscmp-jdbc.xml for relation " + relationName + " but relation is not a jbosscmp-jdbc-managed relation in ejb-jar.xml");
            }
         }
      }

      // dependent-value-objects
      valueClasses.putAll(defaultValues.valueClasses);
      Element valueClassesElement = MetaData.getOptionalChild(element, "dependent-value-classes");
      if(valueClassesElement != null) {
         for(Iterator i = MetaData.getChildrenByTagName(valueClassesElement, "dependent-value-class"); i.hasNext(); ) {
            Element valueClassElement = (Element)i.next();
            JDBCValueClassMetaData valueClass = new JDBCValueClassMetaData(valueClassElement, classLoader);
            valueClasses.put(valueClass.getJavaType(), valueClass);
         }
      }
   }

   /**
    * Gets the type mapping with the specified name
    * @param name the name for the type mapping
    * @return the matching type mapping or null if not found
    */
   public JDBCTypeMappingMetaData getTypeMappingByName(String name) {
      return (JDBCTypeMappingMetaData)typeMappings.get(name);
   }

   /**
    * Gets the container managed relations in this application.
    * Items are instance of JDBCRelationMetaData.
    * @retun an unmodifiable collection of JDBCRelationMetaData objects
    */
   public Collection getRelationships() {
      return Collections.unmodifiableCollection(relationships.values());
   }

   /**
    * Gets the relationship roles for the entity with the specified name.
    * @param entityName the name of the entity whos roles are returned
    * @return an unmodifiable collection of JDBCRelationshipRoles of the specified entity
    */
   public Collection getRolesForEntity(String entityName) {
      Map rolesMap = (Map)entityRoles.get(entityName);
      return Collections.unmodifiableCollection(rolesMap.values());
   }

   /**
    * Gets dependent value classes that are directly managed by the container.
    * @returns an unmodifiable collection of JDBCValueClassMetaData
    */
   public Collection getValueClasses() {
      return Collections.unmodifiableCollection(valueClasses.values());
   }

   /**
    * Gets the classloader for this application which is used to load all classes.
    * @return the ClassLoader for the application
    */
   public ClassLoader getClassLoader() {
      return classLoader;
   }

   /**
    * Gets the metadata for an entity bean by name.
    * @param name the name of the entity meta data to return
    * @return the entity meta data for the specified name
    */
   public JDBCEntityMetaData getBeanByEjbName(String name) {
      return (JDBCEntityMetaData)entities.get(name);
   }

   /**
    * Gets the metadata for an entity bean by the abstract schema name.
    * @param name the abstract schema name of the entity meta data to return
    * @return the entity meta data for the specified name
    */
   public JDBCEntityMetaData getBeanByAbstractSchemaName(String name) {
      return (JDBCEntityMetaData)entitiesByAbstractSchemaName.get(name);
   }
}
