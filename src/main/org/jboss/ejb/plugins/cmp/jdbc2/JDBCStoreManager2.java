/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins.cmp.jdbc2;

import org.jboss.ejb.EntityEnterpriseContext;
import org.jboss.ejb.Container;
import org.jboss.ejb.EntityContainer;
import org.jboss.ejb.EjbModule;
import org.jboss.ejb.GenericEntityObjectFactory;
import org.jboss.ejb.plugins.cmp.jdbc.JDBCTypeFactory;
import org.jboss.ejb.plugins.cmp.jdbc.JDBCEntityPersistenceStore;
import org.jboss.ejb.plugins.cmp.jdbc.JDBCStartCommand;
import org.jboss.ejb.plugins.cmp.jdbc.JDBCStopCommand;
import org.jboss.ejb.plugins.cmp.jdbc.bridge.JDBCAbstractEntityBridge;
import org.jboss.ejb.plugins.cmp.jdbc.metadata.JDBCEntityMetaData;
import org.jboss.ejb.plugins.cmp.jdbc.metadata.JDBCApplicationMetaData;
import org.jboss.ejb.plugins.cmp.jdbc.metadata.JDBCXmlFileLoader;
import org.jboss.ejb.plugins.cmp.ejbql.Catalog;
import org.jboss.ejb.plugins.cmp.jdbc2.bridge.JDBCEntityBridge2;
import org.jboss.ejb.plugins.cmp.jdbc2.schema.Schema;
import org.jboss.ejb.plugins.cmp.jdbc2.schema.EntityTable;
import org.jboss.logging.Logger;
import org.jboss.deployment.DeploymentException;
import org.jboss.metadata.ApplicationMetaData;

import javax.ejb.DuplicateKeyException;
import javax.ejb.FinderException;
import javax.ejb.EJBException;
import javax.ejb.CreateException;
import javax.ejb.RemoveException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Iterator;
import java.sql.SQLException;


/**
 * @author <a href="mailto:alex@jboss.org">Alexey Loubyansky</a>
 * @version <tt>$Revision: 1.6 $</tt>
 */
public class JDBCStoreManager2
   implements JDBCEntityPersistenceStore
{
   private static final String CATALOG = "CATALOG";
   private static final String SCHEMA = "SCHEMA";
   private static final String CREATED_MANAGERS = "CREATED_JDBCStoreManagers";
   private static final String CMP_JDBC = "CMP-JDBC";

   private EntityContainer container;
   private EjbModule ejbModule;
   private Logger log;
   private JDBCEntityMetaData metaData;
   private JDBCEntityBridge2 entityBridge;
   private JDBCTypeFactory typeFactory;
   private Schema schema;

   private InstanceFactory instanceFactory;
   private QueryFactory queryFactory;
   private JDBCStartCommand startCmd;
   private JDBCStopCommand stop;

   // Public

   public Schema getSchema()
   {
      schema = (Schema) getApplicationData(SCHEMA);
      if(schema == null)
      {
         schema = new Schema();
         putApplicationData(SCHEMA, schema);
      }
      return schema;
   }

   public Catalog getCatalog()
   {
      Catalog catalog = (Catalog) getApplicationData(CATALOG);
      if(catalog == null)
      {
         catalog = new Catalog();
         putApplicationData(CATALOG, catalog);
      }
      return catalog;
   }

   public QueryFactory getQueryFactory()
   {
      return queryFactory;
   }

   // ContainerPlugin implementation

   public void setContainer(Container con)
   {
      this.container = (EntityContainer) con;
      if(container != null)
      {
         ejbModule = container.getEjbModule();
         log = Logger.getLogger(this.getClass().getName() + "." + container.getBeanMetaData().getEjbName());
      }
      else
      {
         // undeploy
         ejbModule = null;
      }
   }

   // Service implementation

   public void create() throws Exception
   {
      HashMap managersMap = (HashMap) getApplicationData(CREATED_MANAGERS);
      if(managersMap == null)
      {
         managersMap = new HashMap();
         putApplicationData(CREATED_MANAGERS, managersMap);
      }
      managersMap.put(container.getBeanMetaData().getEjbName(), this);
   }

   public void start() throws Exception
   {
      initStoreManager();

      HashMap managersMap = (HashMap) getApplicationData(CREATED_MANAGERS);
      Catalog catalog = getCatalog();
      if(catalog.getEntityCount() == managersMap.size() && catalog.getEJBNames().equals(managersMap.keySet()))
      {
         // Make a copy of the managers (for safty)
         List managers = new ArrayList(managersMap.values());

         //
         //
         // Start Phase 2: resolve relationships
         for(int i = 0; i < managers.size(); ++i)
         {
            JDBCStoreManager2 manager = (JDBCStoreManager2) managers.get(i);
            manager.resolveRelationships();
         }

         //
         //
         // Start Phase 3: create tables and compile queries
         for(int i = 0; i < managers.size(); ++i)
         {
            JDBCStoreManager2 manager = (JDBCStoreManager2) managers.get(i);
            manager.startStoreManager();
         }

         // add foreign key constraints
         for(int i = 0; i < managers.size(); ++i)
         {
            JDBCStoreManager2 manager = (JDBCStoreManager2)managers.get(i);
            manager.startCmd.addForeignKeyConstraints();
         }
      }
   }

   public void stop()
   {
      if(stop != null)
      {
         Map managersMap = (HashMap)getApplicationData(CREATED_MANAGERS);
         while(!managersMap.isEmpty())
         {
            int stoppedInIteration = 0;
            for(Iterator i = managersMap.values().iterator(); i.hasNext();)
            {
               JDBCStoreManager2 manager = (JDBCStoreManager2)i.next();
               if(manager.stop.execute())
               {
                  i.remove();
                  try
                  {
                     manager.entityBridge.stop();
                  }
                  catch(Exception e)
                  {
                     log.error("Failed to stop entity bridge.", e);
                  }
                  ++stoppedInIteration;
               }
            }

            if(stoppedInIteration == 0)
            {
               break;
            }
         }
      }
   }

   public void destroy()
   {
      //To change body of implemented methods use File | Settings | File Templates.
   }

   // JDBCEntityPersistenceStore implementation

   public JDBCAbstractEntityBridge getEntityBridge()
   {
      return entityBridge;
   }

   public JDBCEntityMetaData getMetaData()
   {
      return metaData;
   }

   public JDBCTypeFactory getJDBCTypeFactory()
   {
      return typeFactory;
   }

   public EntityContainer getContainer()
   {
      return container;
   }

   public Object getApplicationData(Object key)
   {
      return ejbModule.getModuleData(key);
   }

   public void putApplicationData(Object key, Object value)
   {
      ejbModule.putModuleData(key, value);
   }

   // EntityPersistenceStore implementation

   public Object createBeanClassInstance() throws Exception
   {
      return instanceFactory.newInstance();
   }

   public void initEntity(EntityEnterpriseContext ctx)
   {
      entityBridge.initPersistenceContext(ctx);
      entityBridge.initInstance(ctx);
   }

   public Object createEntity(Method m, Object[] args, EntityEnterpriseContext ctx)
      throws CreateException
   {
      Object pk;
      PersistentContext pctx = (PersistentContext) ctx.getPersistenceContext();
      if(ctx.getId() == null)
      {
         pk = entityBridge.extractPrimaryKeyFromInstance(ctx);

         if(pk == null)
         {
            throw new CreateException("Primary key for created instance is null.");
         }

         pctx.setPk(pk);
      }
      else
      {
         // insert-after-ejb-post-create
         try
         {
            pctx.flush();
         }
         catch(SQLException e)
         {
            if("23000".equals(e.getSQLState()))
            {
               throw new DuplicateKeyException("Unique key violation or invalid foreign key value: pk=" + ctx.getId());
            }
            else
            {
               throw new CreateException("Failed to create instance: pk=" + ctx.getId() +
                  ", state=" + e.getSQLState() +
                  ", msg=" + e.getMessage());
            }
         }
         pk = ctx.getId();
      }
      return pk;
   }

   public Object postCreateEntity(Method m, Object[] args, EntityEnterpriseContext ctx) throws CreateException
   {
      return null;
   }

   public Object findEntity(Method finderMethod, Object[] args, EntityEnterpriseContext instance, GenericEntityObjectFactory factory)
      throws FinderException
   {
      QueryCommand query = queryFactory.getQueryCommand(finderMethod);
      return query.fetchOne(schema, factory, args);
   }

   public Collection findEntities(Method finderMethod, Object[] args, EntityEnterpriseContext instance, GenericEntityObjectFactory factory)
      throws FinderException
   {
      QueryCommand query = queryFactory.getQueryCommand(finderMethod);
      return query.fetchCollection(schema, factory, args);
   }

   public void activateEntity(EntityEnterpriseContext ctx)
   {
      entityBridge.initPersistenceContext(ctx);
   }

   public void loadEntity(EntityEnterpriseContext ctx)
   {
      try
      {
         EntityTable.Row row = entityBridge.getTable().loadRow(ctx.getId());
         PersistentContext pctx = new PersistentContext(entityBridge, row);
         ctx.setPersistenceContext(pctx);
      }
      catch(Exception e)
      {
         throw new EJBException(
            "Failed to load instance of "
            + entityBridge.getEntityName() +
            " with pk=" + ctx.getId(), e
         );
      }
   }

   public boolean isModified(EntityEnterpriseContext instance)
   {
      return entityBridge.isDirty(instance);
   }

   public void storeEntity(EntityEnterpriseContext instance)
   {
      // scary?
   }

   public void passivateEntity(EntityEnterpriseContext ctx)
   {
      JDBCEntityBridge2.destroyPersistenceContext(ctx);
   }

   public void removeEntity(EntityEnterpriseContext ctx) throws RemoveException
   {
      entityBridge.remove(ctx);
      PersistentContext pctx = (PersistentContext) ctx.getPersistenceContext();
      pctx.remove();
   }

   // Private

   protected void initStoreManager() throws Exception
   {
      if(log.isDebugEnabled())
      {
         log.debug("Initializing CMP plugin for " + container.getBeanMetaData().getEjbName());
      }

      metaData = loadJDBCEntityMetaData();

      // setup the type factory, which is used to map java types to sql types.
      typeFactory = new JDBCTypeFactory(
         metaData.getTypeMapping(),
         metaData.getJDBCApplication().getValueClasses(),
         metaData.getJDBCApplication().getUserTypeMappings()
      );

      entityBridge = new JDBCEntityBridge2(this, metaData);
      entityBridge.init();

      Catalog catalog = getCatalog();
      catalog.addEntity(entityBridge);

      stop = new JDBCStopCommand(this);
   }

   private void resolveRelationships() throws Exception
   {
      entityBridge.resolveRelationships();
   }

   protected void startStoreManager() throws Exception
   {
      entityBridge.start();

      queryFactory = new QueryFactory(entityBridge);
      queryFactory.init();

      instanceFactory = new InstanceFactory(this, entityBridge);

      startCmd = new JDBCStartCommand(this);
      startCmd.execute();
   }

   private JDBCEntityMetaData loadJDBCEntityMetaData()
      throws DeploymentException
   {
      ApplicationMetaData amd = container.getBeanMetaData().getApplicationMetaData();

      // Get JDBC MetaData
      JDBCApplicationMetaData jamd = (JDBCApplicationMetaData) amd.getPluginData(CMP_JDBC);

      if(jamd == null)
      {
         // we are the first cmp entity to need jbosscmp-jdbc.
         // Load jbosscmp-jdbc.xml for the whole application
         JDBCXmlFileLoader jfl = new JDBCXmlFileLoader(
            amd,
            container.getClassLoader(),
            container.getLocalClassLoader(),
            log
         );

         jamd = jfl.load();
         amd.addPluginData(CMP_JDBC, jamd);
      }

      // Get JDBC Bean MetaData
      String ejbName = container.getBeanMetaData().getEjbName();
      JDBCEntityMetaData metadata = jamd.getBeanByEjbName(ejbName);
      if(metadata == null)
      {
         throw new DeploymentException("No metadata found for bean " + ejbName);
      }
      return metadata;
   }
}
