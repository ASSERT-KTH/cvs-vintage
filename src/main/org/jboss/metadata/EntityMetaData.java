/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.metadata;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.w3c.dom.Element;
import org.jboss.deployment.DeploymentException;

/** The meta data information specific to entity beans.
 *      
 *   @author <a href="mailto:sebastien.alborini@m4x.org">Sebastien Alborini</a>
 *   @author <a href="mailto:Scott_Stark@displayscape.com">Scott Stark</a>.
 *   @author <a href="mailto:dain@daingroup.com">Dain Sundstrom</a>
 *   @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 *   @version $Revision: 1.19 $
 *
 *  <p><b>Revisions:</b><br>
 *  <p><b>2001/10/16: billb</b>
 *  <ol>
 *   <li>Added clustering tags
 *  </ol>
 */
public class EntityMetaData extends BeanMetaData {
   // Constants -----------------------------------------------------
   public final static int CMP_VERSION_1 = 1;
   public final static int CMP_VERSION_2 = 2;
   public static final String DEFAULT_ENTITY_INVOKER_PROXY_BINDING = "entity-rmi-invoker";
   public static final String DEFAULT_CLUSTERED_ENTITY_INVOKER_PROXY_BINDING = "clustered-entity-rmi-invoker";
    
   // Attributes ----------------------------------------------------
   private boolean cmp;
   private String primaryKeyClass;
   private boolean reentrant;
   private int cmpVersion;
   private String abstractSchemaName;
   private ArrayList cmpFields = new ArrayList();
   private String primKeyField;
   private ArrayList queries = new ArrayList();
   private boolean readOnly = false;

   // Static --------------------------------------------------------
    
   // Constructors --------------------------------------------------
   public EntityMetaData(ApplicationMetaData app) {
      super(app, BeanMetaData.ENTITY_TYPE);
   }

   // Public --------------------------------------------------------
   public boolean isCMP() {
      return cmp;
   }

   public boolean isCMP1x() {
      return cmp && (cmpVersion==1);
   }

   public boolean isCMP2x() {
      return cmp && (cmpVersion==2);
   }

   public boolean isBMP() {
      return !cmp;
   }

   public String getPrimaryKeyClass() { 
      return primaryKeyClass;
   }

   public boolean isReentrant() {
      return reentrant;
   }

   public String getAbstractSchemaName() {
      return abstractSchemaName;
   }

   public boolean isReadOnly()
   {
      return readOnly;
   }

   /**
    * Gets the container managed fields.
    * @returns iterator over Strings containing names of the fields
    */
   public Iterator getCMPFields() { 
      return cmpFields.iterator(); 
   }

   public String getPrimKeyField() { 
      return primKeyField; 
   }

   public Iterator getQueries() {
      return queries.iterator();
   }

   public String getDefaultConfigurationName() {
      if (isCMP()) {
         if(getApplicationMetaData().isEJB2x()) {
            if (isClustered())
               return ConfigurationMetaData.CLUSTERED_CMP_2x_13;
            else
               return ConfigurationMetaData.CMP_2x_13;
         } else {
            if (isClustered())
               return ConfigurationMetaData.CLUSTERED_CMP_1x_13;
            else
               return ConfigurationMetaData.CMP_1x_13;
         }
      } else {
         if (isClustered())
            return ConfigurationMetaData.CLUSTERED_BMP_13;
         else
            return ConfigurationMetaData.BMP_13;
      }
   }

   public void importEjbJarXml(Element element) throws DeploymentException {
      super.importEjbJarXml(element);

      // set persistence type
      String persistenceType = getElementContent(getUniqueChild(element, "persistence-type"));
      if (persistenceType.equals("Bean")) {
         cmp = false;
      } else if (persistenceType.equals("Container")) {
         cmp = true;
      } else {
         throw new DeploymentException("persistence-type should be 'Bean' or 'Container'");
      }

      // set primary key class
      primaryKeyClass = getElementContent(getUniqueChild(element, "prim-key-class"));
    
      // set reentrant
      reentrant = Boolean.valueOf(getElementContent(getUniqueChild(element, "reentrant"))).booleanValue();

      if (isCMP()) {
         // cmp-version
         if(getApplicationMetaData().isEJB2x()) {
            String cmpVersionString = getElementContent(getOptionalChild(element, "cmp-version"));

            if(cmpVersionString == null) {
               // default for ejb 2.0 apps is cmp 2.x
               cmpVersion = CMP_VERSION_2;
            } else {
               if("1.x".equals(cmpVersionString)) {
                  cmpVersion = 1;
               } else if("2.x".equals(cmpVersionString)) {
                  cmpVersion = 2;
               } else {
                  throw new DeploymentException("cmp-version must be '1.x' or '2.x', if specified");
               }
            }
         } else {
// default for 1.0 DTDs is version 2
            cmpVersion = CMP_VERSION_1;
         }
 
         // abstract-schema-name
         abstractSchemaName = getOptionalChildContent(element, "abstract-schema-name");

         // cmp-fields
         Iterator iterator = getChildrenByTagName(element, "cmp-field");
         while (iterator.hasNext()) {
            Element field = (Element)iterator.next();
            cmpFields.add(getElementContent(getUniqueChild(field, "field-name")));
         }

         // set the primary key field
         primKeyField = getElementContent(getOptionalChild(element, "primkey-field"));
         if (primKeyField != null && !cmpFields.contains(primKeyField)) {
            throw new DeploymentException("primkey-field " + primKeyField + " is not a cmp-field");
         }

         // queries
         iterator = getChildrenByTagName(element, "query");
         while (iterator.hasNext()) {
            Element queryElement = (Element) iterator.next();

            QueryMetaData queryMetaData = new QueryMetaData();
            queryMetaData.importEjbJarXml(queryElement);

            queries.add(queryMetaData);
         }
      }
   }

   protected void defaultInvokerBindings()
   {
      this.invokerBindings = new HashMap();
      if (isClustered())
      {
         this.invokerBindings.put(DEFAULT_CLUSTERED_ENTITY_INVOKER_PROXY_BINDING, getJndiName());
      }
      else
      {
         this.invokerBindings.put(DEFAULT_ENTITY_INVOKER_PROXY_BINDING, getJndiName());
      }
   }

   public void importJbossXml(Element element) throws DeploymentException
   {
      super.importJbossXml(element);
      // set readonly
      String readOnlyString = getElementContent(getOptionalChild(element, "read-only"));
      if (readOnlyString != null)
      {
	 readOnly = Boolean.valueOf(readOnlyString).booleanValue();
      }
   }

   // Package protected ---------------------------------------------
    
   // Protected -----------------------------------------------------
    
   // Private -------------------------------------------------------
    
   // Inner classes -------------------------------------------------
}
