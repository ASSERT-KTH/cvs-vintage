/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
 
package org.jboss.ejb.plugins.cmp.jdbc.bridge;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.ejb.EJBException;
import javax.sql.DataSource;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.jboss.deployment.DeploymentException;
import org.jboss.ejb.EntityEnterpriseContext;

import org.jboss.ejb.plugins.cmp.jdbc.JDBCContext;
import org.jboss.ejb.plugins.cmp.jdbc.JDBCStoreManager;

import org.jboss.ejb.plugins.cmp.bridge.EntityBridge;
import org.jboss.ejb.plugins.cmp.bridge.EntityBridgeInvocationHandler;
import org.jboss.ejb.plugins.cmp.bridge.CMPFieldBridge;
import org.jboss.ejb.plugins.cmp.bridge.CMRFieldBridge;
import org.jboss.ejb.plugins.cmp.bridge.SelectorBridge;

import org.jboss.ejb.plugins.cmp.jdbc.metadata.JDBCEntityMetaData;
import org.jboss.ejb.plugins.cmp.jdbc.metadata.JDBCCMPFieldMetaData;
import org.jboss.ejb.plugins.cmp.jdbc.metadata.JDBCQueryMetaData;
import org.jboss.ejb.plugins.cmp.jdbc.metadata.JDBCRelationshipRoleMetaData;
import org.jboss.proxy.compiler.Proxies;
import org.jboss.proxy.compiler.InvocationHandler;


/**
 * JDBCEntityBridge follows the Bridge pattern [Gamma et. al, 1995].
 * The main job of this class is to construct the bridge from entity meta data.
 *
 * Life-cycle:
 *      Undefined. Should be tied to CMPStoreManager.
 *
 * Multiplicity:   
 *      One per cmp entity bean type.       
 *
 * @author <a href="mailto:dain@daingroup.com">Dain Sundstrom</a>
 * @version $Revision: 1.18 $
 */                            
public class JDBCEntityBridge implements EntityBridge {
   protected JDBCEntityMetaData metadata;
   protected JDBCStoreManager manager;

   private DataSource dataSource;

   /** is the table assumed to exist */
   protected boolean tableExists;
   
   protected JDBCCMPFieldBridge[] cmpFields;
   protected Map cmpFieldsByName;
   protected JDBCCMPFieldBridge[] primaryKeyFields;   

   protected JDBCCMRFieldBridge[] cmrFields;
   protected Map cmrFieldsByName;
   
   protected JDBCSelectorBridge[] selectors;
   protected Map selectorsByMethod;
   
   protected JDBCCMPFieldBridge[] eagerLoadFields;
   protected ArrayList lazyLoadGroups;
   
   public JDBCEntityBridge(
         JDBCEntityMetaData metadata, 
         JDBCStoreManager manager) throws DeploymentException {

      this.metadata = metadata;                  
      this.manager = manager;
            
      // find the datasource
      try {
         dataSource = (DataSource)new InitialContext().lookup(
               metadata.getDataSourceName());
      } catch(NamingException e) {
         throw new DeploymentException("Error: can't find data source: " + 
               metadata.getDataSourceName());
      }

      // CMP fields
      loadCMPFields(metadata);

      // eager/load groups
      loadEagerLoadFields(metadata);
      loadLazyLoadGroups(metadata);
      
      // CMR fields
      loadCMRFields(metadata);

      // ejbSelect methods
      loadSelectors(metadata);
   }

   protected void loadCMPFields(JDBCEntityMetaData metadata)
         throws DeploymentException {

      // map between field names and field objects
      cmpFieldsByName = new HashMap(metadata.getCMPFields().size());
      // non primary key cmp fields
      ArrayList cmpFieldList = new ArrayList(metadata.getCMPFields().size());
      // primary key cmp fields
      ArrayList pkFieldList = new ArrayList(metadata.getCMPFields().size());
                              
      // create each field    
      Iterator iter = metadata.getCMPFields().iterator();
      while(iter.hasNext()) {
         JDBCCMPFieldMetaData cmpFieldMetaData = 
               (JDBCCMPFieldMetaData)iter.next();
         JDBCCMPFieldBridge cmpField = 
               createCMPField(metadata, cmpFieldMetaData);
         cmpFieldsByName.put(cmpField.getFieldName(), cmpField);
         
         if(cmpField.isPrimaryKeyMember()) {
            pkFieldList.add(cmpField);
         } else {
            cmpFieldList.add(cmpField);
         }
      
      }
      
      // save the pk fields in the pk field array
      primaryKeyFields = new JDBCCMPFieldBridge[pkFieldList.size()];
      primaryKeyFields =
            (JDBCCMPFieldBridge[])pkFieldList.toArray(primaryKeyFields);      
      
      // add the pk fields to the front of the cmp list, per guarantee above
      cmpFieldList.addAll(0, pkFieldList);
      
      // save the cmp fields in the cmp field array
      cmpFields = new JDBCCMPFieldBridge[cmpFieldList.size()];
      cmpFields = (JDBCCMPFieldBridge[])cmpFieldList.toArray(cmpFields);
   }

   protected void loadEagerLoadFields(JDBCEntityMetaData metadata)
         throws DeploymentException {

      ArrayList fields = new ArrayList(metadata.getCMPFields().size());
      Iterator iter = metadata.getEagerLoadFields().iterator();
      while(iter.hasNext()) {
         JDBCCMPFieldMetaData field = (JDBCCMPFieldMetaData)iter.next();
         fields.add(getExistingCMPFieldByName(field.getFieldName()));
      }
      eagerLoadFields = new JDBCCMPFieldBridge[fields.size()];
      eagerLoadFields = (JDBCCMPFieldBridge[])fields.toArray(eagerLoadFields);
   }

   protected void loadLazyLoadGroups(JDBCEntityMetaData metadata)
         throws DeploymentException {

      lazyLoadGroups = new ArrayList();
      
      Iterator groups = metadata.getLazyLoadGroups().iterator();
      while(groups.hasNext()) {
         ArrayList group = new ArrayList();

         Iterator fields = ((ArrayList)groups.next()).iterator();
         while(fields.hasNext()) {
            JDBCCMPFieldMetaData field = (JDBCCMPFieldMetaData)fields.next();
            group.add(getExistingCMPFieldByName(field.getFieldName()));
         }
         lazyLoadGroups.add(group);
      }
   }

   protected JDBCCMPFieldBridge createCMPField(
         JDBCEntityMetaData metadata,
         JDBCCMPFieldMetaData cmpFieldMetaData) throws DeploymentException {

      if(metadata.isCMP1x()) {
         return new JDBCCMP1xFieldBridge(manager, cmpFieldMetaData);
      } else {
         return new JDBCCMP2xFieldBridge(manager, cmpFieldMetaData);
      }
   }
   
   protected void loadCMRFields(JDBCEntityMetaData metadata)
         throws DeploymentException {

      cmrFieldsByName = new HashMap(metadata.getRelationshipRoles().size());
      ArrayList cmrFieldList =
            new ArrayList(metadata.getRelationshipRoles().size());

      // create each field    
      Iterator iter = metadata.getRelationshipRoles().iterator();
      while(iter.hasNext()) {
         JDBCRelationshipRoleMetaData relationshipRole =
               (JDBCRelationshipRoleMetaData)iter.next();
         JDBCCMRFieldBridge cmrField =
               new JDBCCMRFieldBridge(this, manager, relationshipRole); 
         cmrFieldList.add(cmrField);
         cmrFieldsByName.put(cmrField.getFieldName(), cmrField);
      }
      
      // save the cmr fields in the cmr field array
      cmrFields = new JDBCCMRFieldBridge[cmrFieldList.size()];
      cmrFields = (JDBCCMRFieldBridge[])cmrFieldList.toArray(cmrFields);
      
      for(int i=0; i<cmrFields.length; i++) {
         cmrFields[i].initRelatedData();
      }
   }

   protected void loadSelectors(JDBCEntityMetaData metadata)
         throws DeploymentException {
            
      // Don't know if this is the best way to do this.  Another way would be 
      // to deligate seletors to the JDBCFindEntitiesCommand, but this is
      // easier now.
      selectorsByMethod = new HashMap(metadata.getQueries().size());
      Iterator definedFinders = manager.getMetaData().getQueries().iterator();
      while(definedFinders.hasNext()) {
         JDBCQueryMetaData q = (JDBCQueryMetaData)definedFinders.next();

         if(q.getMethod().getName().startsWith("ejbSelect")) {
            selectorsByMethod.put(q.getMethod(), 
                  new JDBCSelectorBridge(manager, q));
         }
      }

      selectors = new JDBCSelectorBridge[selectorsByMethod.values().size()];
      selectors = 
            (JDBCSelectorBridge[])selectorsByMethod.values().toArray(selectors);
   }
   
   public String getEntityName() {
      return metadata.getName();
   }

   public JDBCEntityMetaData getMetaData() {
      return metadata;
   }

   public JDBCStoreManager getManager() {
      return manager;
   }

   /**
    * Returns the datasource for this entity.
    */
   public DataSource getDataSource() {
      return dataSource;
   }
   
   /** 
    * Does the table exists yet? This does not mean that table has been created
    * by the appilcation, or the the database metadata has been checked for the
    * existance of the table, but that at this point the table is assumed to 
    * exist.
    * @return true if the table exists
    */
   public boolean getTableExists() {
      return tableExists;
   }

   /** 
    * Sets table exists flag.
    */
   public void setTableExists(boolean tableExists) {
      this.tableExists = tableExists;
   }

   public String getTableName() {
      return metadata.getTableName();
   }

   public Class getPrimaryKeyClass() {
      return metadata.getPrimaryKeyClass();
   }
   
   public Object createPrimaryKeyInstance() {
      if(metadata.getPrimaryKeyFieldName() ==  null) {
         try {
            return getPrimaryKeyClass().newInstance();
         } catch(Exception e) {
            throw new EJBException("Error creating primary key instance: ", e);
         }
      }
      return null;
   }
   
   public CMPFieldBridge[] getPrimaryKeyFields() {
      return primaryKeyFields;
   }
   
   public JDBCCMPFieldBridge[] getJDBCPrimaryKeyFields() {
      return primaryKeyFields;
   }
   
   public CMPFieldBridge[] getCMPFields() {
      return cmpFields;
   }

   public JDBCCMPFieldBridge[] getEagerLoadFields() {
      return eagerLoadFields;
   }

   public Iterator getLazyLoadGroups() {
      return lazyLoadGroups.iterator();
   }

   public JDBCCMPFieldBridge[] getJDBCCMPFields() {
      return cmpFields;
   }

   public JDBCCMPFieldBridge getCMPFieldByName(String name) {
      return (JDBCCMPFieldBridge)cmpFieldsByName.get(name);
   }
   
   protected JDBCCMPFieldBridge getExistingCMPFieldByName(String name)
         throws DeploymentException {

      JDBCCMPFieldBridge cmpField = getCMPFieldByName(name);
      if(cmpField == null) {
         throw new DeploymentException("cmpField not found: " + name);
      }
      return cmpField;
   }
   
   public CMRFieldBridge[] getCMRFields() {
      return cmrFields;
   }
   
   public JDBCCMRFieldBridge getCMRFieldByName(String name) {
      return (JDBCCMRFieldBridge)cmrFieldsByName.get(name);
   }
   
   public JDBCCMRFieldBridge[] getJDBCCMRFields() {
      return cmrFields;
   }
   
   public SelectorBridge[] getSelectors() {
      return selectors;
   }

   public JDBCSelectorBridge[] getJDBCSelectors() {
      return selectors;
   }

   public void initInstance(EntityEnterpriseContext ctx) {
      for(int i=0; i<cmpFields.length; i++) {
         cmpFields[i].initInstance(ctx);
      }
      for(int i=0; i<cmrFields.length; i++) {
         cmrFields[i].initInstance(ctx);
      }
   }

   public boolean isCreated(EntityEnterpriseContext ctx) {
      return getEntityState(ctx).isCreated();
   }

   public void setCreated(EntityEnterpriseContext ctx) {
      getEntityState(ctx).isCreated = true;
   }

   public void setClean(EntityEnterpriseContext ctx) {
      for(int i=0; i<cmpFields.length; i++) {
         cmpFields[i].setClean(ctx);
      }
   }

   public CMPFieldBridge[] getDirtyFields(EntityEnterpriseContext ctx) {
      ArrayList dirtyFields = new ArrayList(cmpFields.length);
      
      // get dirty cmp fields
      for(int i=0; i<cmpFields.length; i++) {
         if(cmpFields[i].isDirty(ctx)) {
            dirtyFields.add(cmpFields[i]);
         }
      }
      
      // get dirty cmr foreign key fields
      for(int i=0; i<cmrFields.length; i++) {
         if(cmrFields[i].hasForeignKey()) {
            JDBCCMPFieldBridge[] foreignKeyFields =
                  cmrFields[i].getForeignKeyFields();
            for(int j=0; j<foreignKeyFields.length; j++) {
               if(foreignKeyFields[j].isDirty(ctx)) {
                  dirtyFields.add(foreignKeyFields[j]);
               }
            }
         }
      }
      
      JDBCCMPFieldBridge[] dirtyFieldArray =
            new JDBCCMPFieldBridge[dirtyFields.size()];
      return (JDBCCMPFieldBridge[])dirtyFields.toArray(dirtyFieldArray);
   }
   
   public void initPersistenceContext(EntityEnterpriseContext ctx) {
      // If we have an EJB 2.0 dynaymic proxy,
      // notify the handler of the assigned context.
      Object instance = ctx.getInstance();
      if(instance instanceof Proxies.ProxyTarget) {
         InvocationHandler handler = 
               ((Proxies.ProxyTarget)instance).getInvocationHandler();
         if(handler instanceof EntityBridgeInvocationHandler) {
            ((EntityBridgeInvocationHandler)handler).setContext(ctx);
         }
      }

      ctx.setPersistenceContext(new JDBCContext());
   }

   /**
    * This is only called in commit option B
    */
   public void resetPersistenceContext(EntityEnterpriseContext ctx) {
      for(int i=0; i<cmpFields.length; i++) {
         cmpFields[i].resetPersistenceContext(ctx);
      }

      for(int i=0; i<cmrFields.length; i++) {
         cmrFields[i].resetPersistenceContext(ctx);
      }
   }
   

   public void destroyPersistenceContext(EntityEnterpriseContext ctx) {
      // If we have an EJB 2.0 dynaymic proxy,
      // notify the handler of the assigned context.
      Object instance = ctx.getInstance();
      if(instance instanceof Proxies.ProxyTarget) {
         InvocationHandler handler =
               ((Proxies.ProxyTarget)instance).getInvocationHandler();
         if(handler instanceof EntityBridgeInvocationHandler) {
            ((EntityBridgeInvocationHandler)handler).setContext(null);
         }
      }

      ctx.setPersistenceContext(null);
   }

   // JDBC Specific Information
   
   public int setInstanceParameters(
         PreparedStatement ps,
         int parameterIndex,
         EntityEnterpriseContext ctx)  {

      return setInstanceParameters(ps, parameterIndex, ctx, cmpFields);
   }

   public int setInstanceParameters(
         PreparedStatement ps,
         int parameterIndex,
         EntityEnterpriseContext ctx, 
         JDBCCMPFieldBridge[] fields) {

      for(int i=0; i<fields.length; i++) {
         parameterIndex =
               fields[i].setInstanceParameters(ps, parameterIndex, ctx);
      }
      return parameterIndex;
   }

   public int setPrimaryKeyParameters(
         PreparedStatement ps,
         int parameterIndex,
         Object primaryKey) {      

      for(int i=0; i<primaryKeyFields.length; i++) {
         parameterIndex = primaryKeyFields[i].setPrimaryKeyParameters(
               ps,
               parameterIndex,
               primaryKey);
      }
      return parameterIndex;
   }

   public int loadInstanceResults(
         ResultSet rs,
         int parameterIndex,
         EntityEnterpriseContext ctx) {

      for(int i=0; i<cmpFields.length; i++) {
         parameterIndex =
               cmpFields[i].loadInstanceResults(rs, parameterIndex, ctx);
      }
      return parameterIndex;
   }
         
   public int loadNonPrimaryKeyResults(
         ResultSet rs,
         int parameterIndex,
         EntityEnterpriseContext ctx) {

      for(int i=primaryKeyFields.length; i<cmpFields.length; i++) {
         parameterIndex = 
               cmpFields[i].loadInstanceResults(rs, parameterIndex, ctx);
      }
      return parameterIndex;
   }
         
   public int loadPrimaryKeyResults(
         ResultSet rs, 
         int parameterIndex, 
         Object[] pkRef) {

      pkRef[0] = createPrimaryKeyInstance();
      for(int i=0; i<primaryKeyFields.length; i++) {
         parameterIndex = primaryKeyFields[i].loadPrimaryKeyResults(
               rs, parameterIndex, pkRef);
      }
      return parameterIndex;
   }
         
   public Object extractPrimaryKeyFromInstance(EntityEnterpriseContext ctx) {
      try {
         Object pk = null;
         for(int i=0; i<primaryKeyFields.length; i++) {
            Object fieldValue = primaryKeyFields[i].getInstanceValue(ctx);
            
            // updated pk object with return form set primary key value to
            // handle single valued non-composit pks and more complicated
            // behivors.
            pk = primaryKeyFields[i].setPrimaryKeyValue(pk, fieldValue);
         }
         return pk;
      } catch(EJBException e) {
         // to avoid double wrap of EJBExceptions
         throw e;
      } catch(Exception e) {
         // Non recoverable internal exception
         throw new EJBException("Internal error extracting primary key from " +
               "instance: " + e);
      }
   }

   public void injectPrimaryKeyIntoInstance(
         EntityEnterpriseContext ctx,
         Object pk) {

      for(int i=0; i<primaryKeyFields.length; i++) {
         Object fieldValue = primaryKeyFields[i].getPrimaryKeyValue(pk);
         primaryKeyFields[i].setInstanceValue(ctx, fieldValue);
      }
   }

   public EntityState getEntityState(EntityEnterpriseContext ctx) {
      JDBCContext jdbcCtx = (JDBCContext)ctx.getPersistenceContext();
      EntityState entityState = (EntityState)jdbcCtx.get(this);
      if(entityState == null) {
         entityState = new EntityState();
         jdbcCtx.put(this, entityState);
      }
      return entityState;
   }

   public static class EntityState {
      private boolean isCreated = false;
      
      public boolean isCreated() {
         return isCreated;
      }
   }
}
