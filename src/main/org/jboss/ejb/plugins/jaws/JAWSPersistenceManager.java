/*
 * jBoss, the OpenSource EJB server
 *
 * Distributable under GPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins.jaws;

import java.beans.Beans;
import java.beans.beancontext.BeanContextServicesSupport;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.lang.reflect.Method;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.rmi.RemoteException;
import java.rmi.NoSuchObjectException;
import java.rmi.ServerException;
import java.util.Collection;
import java.util.ArrayList;
import java.util.Iterator;
import java.sql.Date;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Types;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.DriverManager;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.Connection;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.MessageFormat;

import javax.ejb.EJBObject;
import javax.ejb.Handle;
import javax.ejb.EntityBean;
import javax.ejb.CreateException;
import javax.ejb.DuplicateKeyException;
import javax.ejb.FinderException;
import javax.ejb.RemoveException;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;

import org.jboss.ejb.Container;
import org.jboss.ejb.EntityContainer;
import org.jboss.ejb.EntityPersistenceManager;
import org.jboss.ejb.EntityEnterpriseContext;

import org.jboss.logging.Log;

import com.dreambean.ejx.ejb.EjbReference;
import org.jboss.ejb.plugins.jaws.deployment.JawsFileManager;
import org.jboss.ejb.plugins.jaws.deployment.JawsFileManagerFactory;
import org.jboss.ejb.plugins.jaws.deployment.JawsEjbJar;
import org.jboss.ejb.plugins.jaws.deployment.JawsEnterpriseBeans;
import org.jboss.ejb.plugins.jaws.deployment.JawsEjbReference;
import org.jboss.ejb.plugins.jaws.deployment.JawsEntity;
import org.jboss.ejb.plugins.jaws.deployment.JawsCMPField;
import org.jboss.ejb.plugins.jaws.deployment.Finder;

/**
 *   Just Another Web Store - an O/R mapper
 *
 *
 *	Note: This is a really long class, but I thought that splitting it up 
 * would only make things more confusing. To compensate for the size
 * I have tried to make many helper methods to keep each method small.
 *      
 *	@see <related>
 *	@author Rickard Öberg (rickard.oberg@telkel.com)
 *	@version $Revision: 1.5 $
 */
public class JAWSPersistenceManager
   implements EntityPersistenceManager
{
   // Constants -----------------------------------------------------
   
   // Attributes ----------------------------------------------------
   EntityContainer container;
   
   Method ejbStore;
   Method ejbLoad;
   Method ejbActivate;
   Method ejbPassivate;
   Method ejbRemove;
   
   // Pre-calculated fields to speed things up
   ArrayList pkFields = new ArrayList(); // Field's in entity class
   ArrayList pkClassFields = new ArrayList(); // Field's in pk class
   ArrayList pkColumns = new ArrayList(); // String's
   String pkColumnList; // Comma-separated list of column names
   String pkColumnWhereList; // Comma-separated list of column names (for WHERE clauses)
   ArrayList cmpFields = new ArrayList(); // Field's
   ArrayList CMPFields = new ArrayList(); // CMPField's
   ArrayList jdbcTypes = new ArrayList(); // Integer's
   ArrayList pkJdbcTypes = new ArrayList(); // Integer's describing pk
   ArrayList ejbRefs = new ArrayList(); // EJB-references
   
   boolean compoundKey;
   Class primaryKeyClass;
   
   JawsEntity entity;
   String dbName;
   
   String createSql;
   String insertSql;
	//   String updateSql; Calculated dynamically (=tuned updates)
   String selectSql;
   String removeSql;
   String dropSql;
   
   Context javaCtx;
   
   Log log = new Log("JAWS");
   
   DataSource ds;
   String url;
	
	boolean readOnly;
	long readOnlyTimeOut;
   
   // Static --------------------------------------------------------
   
   // Constructors --------------------------------------------------
   
   // Public --------------------------------------------------------
   public void setContainer(Container c)
   {
      container = (EntityContainer)c;
   }
   
   public void init()
      throws Exception
   {
      log.debug("Initializing JAWS plugin for "+container.getMetaData().getEjbName());
      
      javaCtx = (Context)new InitialContext().lookup("java:comp/env");
      
      JawsFileManager jfm = (JawsFileManager)new JawsFileManagerFactory().createFileManager();
      
      // Setup beancontext
      BeanContextServicesSupport beanCtx = new BeanContextServicesSupport();
      beanCtx.add(Beans.instantiate(getClass().getClassLoader(), "com.dreambean.ejx.xml.ProjectX"));
      beanCtx.add(jfm);
      
      // Load XML
      JawsEjbJar jar = jfm.load(container.getApplication().getURL());
      
      // Extract meta-info
      entity = (JawsEntity)jar.getEnterpriseBeans().getEjb(container.getMetaData().getEjbName());
      Iterator fields = entity.getCMPFields();
      while (fields.hasNext())
      {
         JawsCMPField field = (JawsCMPField)fields.next();
         CMPFields.add(field);
         cmpFields.add(container.getBeanClass().getField(field.getFieldName()));
         // Identify JDBC-type
         jdbcTypes.add(new Integer(getJDBCType(field.getJdbcType())));
         
         // EJB-reference
         if (field.getJdbcType().equals("REF"))
         {
            ejbRefs.add(getPkColumns(field));
         }
      }
      
		// Read-only?
		readOnly = entity.getReadOnly();
		readOnlyTimeOut = entity.getTimeOut();
		
      // Identify pk
      pkColumnList = "";
      pkColumnWhereList = "";
      compoundKey = entity.getPrimaryKeyField().equals("");
      if (compoundKey)
      {
         // Compound key
         Field[] pkClassFieldList = container.getClassLoader().loadClass(entity.getPrimaryKeyClass()).getFields();
         
         // Build pk field list and SQL-pk string
         for (int i = 0; i < pkClassFieldList.length; i++)
         {
            pkClassFields.add(pkClassFieldList[i]);
            Field field = container.getBeanClass().getField(pkClassFieldList[i].getName());
            pkFields.add(field);
            for (int j = 0; j < CMPFields.size(); j++)
            {
               JawsCMPField cmpField = (JawsCMPField)CMPFields.get(j);
               if (cmpField.getFieldName().equals(field.getName()))
               {
                  pkColumnList += ((i == 0)?"":",") + cmpField.getColumnName();
                  pkColumnWhereList += ((i == 0)?"":" AND ") + cmpField.getColumnName()+"=?";
                  pkJdbcTypes.add(new Integer(getJDBCType(cmpField.getJdbcType())));
                  pkColumns.add(cmpFields.get(j));
                  break;
               }
            }
         }
         
         // Get compound key class
         primaryKeyClass = container.getClassLoader().loadClass(entity.getPrimaryKeyClass());
      } else
      {
         // Primitive key
         pkFields.add(container.getBeanClass().getField(entity.getPrimaryKeyField()));
         for (int j = 0; j < CMPFields.size(); j++)
         {
            JawsCMPField cmpField = (JawsCMPField)CMPFields.get(j);
            if (cmpField.getFieldName().equals(entity.getPrimaryKeyField()))
            {
               pkColumnList = cmpField.getColumnName();
               pkColumnWhereList = cmpField.getColumnName()+"=?";
               pkJdbcTypes.add(new Integer(getJDBCType(cmpField.getJdbcType())));
               pkColumns.add(cmpFields.get(j));
               break;
            }
         }
      }
      
      // Create SQL commands
      makeSql();
      
      // Find EJB-methods
      ejbStore = EntityBean.class.getMethod("ejbStore", new Class[0]);
      ejbLoad = EntityBean.class.getMethod("ejbLoad", new Class[0]);
      ejbActivate = EntityBean.class.getMethod("ejbActivate", new Class[0]);
      ejbPassivate = EntityBean.class.getMethod("ejbPassivate", new Class[0]);
      ejbRemove = EntityBean.class.getMethod("ejbRemove", new Class[0]);
   }
   
   public void start()
      throws Exception
   {
      // Find datasource
      url = ((JawsEnterpriseBeans)entity.getBeanContext()).getDataSource();
      if (!url.startsWith("jdbc:"))
      {
         ds = (DataSource)new InitialContext().lookup(((JawsEnterpriseBeans)entity.getBeanContext()).getDataSource());
      }
      
      // Create table if necessary
      if (entity.getCreateTable())
      {
         // Try to create it
         Connection con = null;
         PreparedStatement stmt = null;
         ResultSet rs = null;
         try
         {
            con = getConnection();
            stmt = con.prepareStatement(createSql);
            stmt.executeQuery();
            log.debug("Table "+entity.getTableName()+" created");
         } catch (SQLException e)
         {
            log.debug("Table "+entity.getTableName()+" exists");
         } finally
         {
            if (rs != null) try { rs.close(); } catch (Exception e) { e.printStackTrace(); }
            if (stmt != null) try { stmt.close(); } catch (Exception e) { e.printStackTrace(); }
            if (con != null) try { con.close(); } catch (Exception e) { e.printStackTrace(); }
         }
      }
   }

   public void stop()
   {
   }
   
   public void destroy()
   {
      if (entity.getRemoveTable())
      {
         // Remove it!
         Connection con = null;
         PreparedStatement stmt = null;
         ResultSet rs = null;
         try
         {
            con = getConnection();
            stmt = con.prepareStatement(dropSql);
            stmt.executeUpdate();
            log.debug("Table "+entity.getTableName()+" removed");
         } catch (SQLException e)
         {
            log.debug("Table "+entity.getTableName()+" could not be removed");
         } finally
         {
            if (rs != null) try { rs.close(); } catch (Exception e) { e.printStackTrace(); }
            if (stmt != null) try { stmt.close(); } catch (Exception e) { e.printStackTrace(); }
            if (con != null) try { con.close(); } catch (Exception e) { e.printStackTrace(); }
         }
      }
   }
   
   public void createEntity(Method m, Object[] args, EntityEnterpriseContext ctx)
      throws RemoteException, CreateException
   {
      log.debug("Create entity");
      // Get methods
      try
      {
         Method createMethod = container.getBeanClass().getMethod("ejbCreate", m.getParameterTypes());
         Method postCreateMethod = container.getBeanClass().getMethod("ejbPostCreate", m.getParameterTypes());
      
         // Call ejbCreate
         createMethod.invoke(ctx.getInstance(), args);
         
         // Extract pk
         Object id = null;
         if (compoundKey)
         {
            try
            {
               id = primaryKeyClass.newInstance();
            } catch (InstantiationException e)
            {
               throw new ServerException("Could not create primary key",e);
            }
            
            for (int i = 0; i < pkFields.size(); i++)
            {
               Field from = (Field)pkFields.get(i);
               Field to = (Field)pkClassFields.get(i);
               to.set(id,from.get(ctx.getInstance()));
            }
         } else
         {
            id = ((Field)pkFields.get(0)).get(ctx.getInstance());
         }
         
         log.debug("Create, id is "+id);
         
         // Check duplicate
         // TODO

         // Set id
         ctx.setId(id);
         
         // Lock instance in cache
         ((EntityContainer)container).getInstanceCache().insert(ctx);
         
         // Create EJBObject
         ctx.setEJBObject(container.getContainerInvoker().getEntityEJBObject(id));

         // Insert in db
         log.debug("Insert");
         Connection con = null;
         PreparedStatement stmt = null;
         ResultSet rs = null;
         try
         {
            con = getConnection();
            stmt = con.prepareStatement(insertSql);
            
            int idx = 1; // Parameter-index
            int refIdx = 0; // EJB-reference count; used to index "ejbRefs"
            for (int i = 0; i < cmpFields.size(); i++)
            {
               Field field = (Field)cmpFields.get(i);
               JawsCMPField cmpField = (JawsCMPField)CMPFields.get(i);
               if (cmpField.getJdbcType().equals("REF"))
               {
                  idx = setParameter(stmt,idx,((Integer)jdbcTypes.get(i)).intValue(), field.get(ctx.getInstance()),refIdx++);
               } else
               {
                  idx = setParameter(stmt,idx,((Integer)jdbcTypes.get(i)).intValue(), field.get(ctx.getInstance()),refIdx);
               }
            }
            
            log.debug("Executing:"+insertSql);
            stmt.executeUpdate();
         } catch (SQLException e)
         {
            throw new CreateException("Could not create entity:"+e);
         } finally
         {
            if (rs != null) try { rs.close(); } catch (Exception e) { e.printStackTrace(); }
            if (stmt != null) try { stmt.close(); } catch (Exception e) { e.printStackTrace(); }
            if (con != null) try { con.close(); } catch (Exception e) { e.printStackTrace(); }
         }
         
         // Store state to be able to do tuned updates
			PersistenceContext pCtx = new PersistenceContext();
			
			// If read-only, set last read to now
			if (readOnly) pCtx.lastRead = System.currentTimeMillis();
			
			// Save initial state for tuned updates
			pCtx.state = getState(ctx);
			
         ctx.setPersistenceContext(pCtx);
         
         // Invoke postCreate
         postCreateMethod.invoke(ctx.getInstance(), args);
      } catch (InvocationTargetException e)
      {
         throw new CreateException("Create failed:"+e);
      } catch (NoSuchMethodException e)
      {
         throw new CreateException("Create methods not found:"+e);
      } catch (IllegalAccessException e)
      {
         throw new CreateException("Could not create entity:"+e);
      } 
   }

   public Object findEntity(Method finderMethod, Object[] args, EntityEnterpriseContext ctx)
      throws RemoteException, FinderException
   {
      if (finderMethod.getName().equals("findByPrimaryKey"))
      {
         // TODO: determine existence
         return args[0];
      }
      else
      {
         ArrayList result = (ArrayList)findEntities(finderMethod, args, ctx);
         if (result.size() == 0)
            throw new FinderException("No such entity!");
         else
            return result.get(0);
      }
   }
     
   public Collection findEntities(Method finderMethod, Object[] args, EntityEnterpriseContext ctx)
      throws RemoteException, FinderException
   {
      Connection con = null;
      PreparedStatement stmt = null;
      ResultSet rs = null;
      
      try
      {
         // Try defined finders
         Iterator finders = entity.getFinders();
         while(finders.hasNext())
         {
            Finder f = (Finder)finders.next();
            if (f.getName().equals(finderMethod.getName()))
            {
               // Replace placeholders with parameters
               String query = MessageFormat.format(f.getQuery(), args);
               
               // Construct SQL
               String sql = "SELECT "+pkColumnList+(f.getOrder().equals("")?"":","+f.getOrder())+" FROM "+entity.getTableName()+" WHERE "+query;
               if (!f.getOrder().equals(""))
               {
                  sql += " ORDER BY "+f.getOrder();
               }
               
               con = getConnection();
               stmt = con.prepareStatement(sql);
               break;
            }
         }
         
         if (con == null)
         {
            if (finderMethod.getName().equals("findAll"))
            {
               // Try findAll
               con = getConnection();
               stmt = con.prepareStatement("SELECT "+pkColumnList+" FROM "+entity.getTableName());
            } else if (finderMethod.getName().startsWith("findBy"))
            {
               // Try findByX
               String cmpFieldName = finderMethod.getName().substring(6).toLowerCase();
					System.out.println("Finder:"+cmpFieldName);
               
               for (int i = 0; i < CMPFields.size(); i++)
               {
                  JawsCMPField cmpField = (JawsCMPField)CMPFields.get(i);
                  
                  // Find field
                  if (cmpFieldName.equals(cmpField.getFieldName().toLowerCase()))
                  {
                     // Is reference?
                     if (cmpField.getJdbcType().equals("REF"))
                     {
                        String sql = "SELECT "+pkColumnList+" FROM "+entity.getTableName()+ " WHERE ";
                        
                        // TODO: Fix this.. I mean it's already been computed once.. 
                        JawsCMPField[] cmpFields = getPkColumns(cmpField);
                        for (int j = 0; j < cmpFields.length; j++)
                        {
                           sql += (j==0?"":" AND ") + cmpField.getColumnName()+"_"+cmpFields[j].getColumnName()+"=?";
                        }
                        
                        con = getConnection();
                        stmt = con.prepareStatement(sql);
                        
                        // Set parameters
                        setParameter(stmt,1,Types.REF, args[0],0);
                     } else
                     {
                        // Find in db
                        String sql = "SELECT "+pkColumnList+" FROM "+entity.getTableName()+ " WHERE "+cmpField.getColumnName()+"=?";
                        
                        con = getConnection();
                        stmt = con.prepareStatement(sql);
                        
                        // Set parameters
                        setParameter(stmt,1,((Integer)jdbcTypes.get(i)).intValue(), args[0],0);
                     }
                  }
               }
            } else
            {
               log.warning("No finder for this method:"+finderMethod.getName());
               return new java.util.ArrayList();
            }
         }
      
         // Compute result
         rs = stmt.executeQuery();
         ArrayList result = new ArrayList();
         if (compoundKey)
         {
            // Compound key
            try
            {
               while (rs.next())
               {
                  Object pk = primaryKeyClass.newInstance();
                  for (int i = 0; i < pkClassFields.size(); i++)
                  {
                     Field field = (Field)pkClassFields.get(i);
                     field.set(pk, rs.getObject(i+1));
                  }
                  result.add(pk);
               }
            } catch (Exception e)
            {
               throw new ServerException("Finder failed",e);
            }
         } else
         {
            // Primitive key
            while (rs.next())
            {
               result.add(rs.getObject(1));
            }
         }
         
         return result;
      } catch (SQLException e)
      {
         log.exception(e);
         throw new FinderException("Find failed");
      } finally
      {
         if (rs != null) try { rs.close(); } catch (Exception e) { e.printStackTrace(); }
         if (stmt != null) try { stmt.close(); } catch (Exception e) { e.printStackTrace(); }
         if (con != null) try { con.close(); } catch (Exception e) { e.printStackTrace(); }
      }
   }

   public void activateEntity(EntityEnterpriseContext ctx)
      throws RemoteException
   {
      // Call bean
      try
      {
         ejbActivate.invoke(ctx.getInstance(), new Object[0]);
      } catch (Exception e)
      {
         throw new ServerException("Activation failed", e);
      }
		
		// Set new persistence context
		ctx.setPersistenceContext(new PersistenceContext());
   }
   
   public void loadEntity(EntityEnterpriseContext ctx)
      throws RemoteException
   {
		// Check read only
		if (readOnly)
		{
			PersistenceContext pCtx = (PersistenceContext)ctx.getPersistenceContext();
			
			// Timeout has expired for this entity?
			if ((pCtx.lastRead + readOnlyTimeOut) > System.currentTimeMillis())
				return; // State is still "up to date"
		}
	
      Connection con = null;
      PreparedStatement stmt = null;
      ResultSet rs = null;
      
      try
      {
         // Load from db
         con = getConnection();
         stmt = con.prepareStatement(selectSql);
         
         log.debug("Load SQL:"+selectSql);
         
         // Primary key in WHERE-clause
         if (compoundKey)
         {
            // Compound key
            for (int i = 0; i < pkClassFields.size(); i++)
            {
               Field field = (Field)pkClassFields.get(i);
               setParameter(stmt,i+1,((Integer)pkJdbcTypes.get(i)).intValue(), field.get(ctx.getId()),0);
               log.debug("Set parameter:"+field.get(ctx.getId()));
            }
         } else
         {
            // Primitive key
            setParameter(stmt,1,((Integer)pkJdbcTypes.get(0)).intValue(), ctx.getId(),0);
         }
         
         rs = stmt.executeQuery();
         
         ResultSetMetaData rmd = rs.getMetaData();
         
         if (!rs.next())
            throw new NoSuchObjectException("Entity "+ctx.getId()+" not found");
         
         // Set values
         int idx = 1;
         int refIdx = 0;
         for (int i = 0; i < CMPFields.size(); i++)
         {
            JawsCMPField cmpField = (JawsCMPField)CMPFields.get(i);
            if (((Integer)jdbcTypes.get(i)).intValue() == Types.REF)
            {
               // Create pk
               JawsCMPField[] pkFields = (JawsCMPField[])ejbRefs.get(refIdx++);
               JawsEntity referencedEntity = (JawsEntity)pkFields[0].getBeanContext();
               Object pk;
               if (referencedEntity.getPrimaryKeyField().equals(""))
               {
                  // Compound key
                  pk = container.getClassLoader().loadClass(referencedEntity.getPrimaryKeyClass()).newInstance();
                  Field[] fields = pk.getClass().getFields();
                  for(int j = 0; j < fields.length; j++)
                  {
                     fields[j].set(pk, rs.getObject(idx++));
                  }
               } else
               {
                  // Primitive key
                  pk = rs.getObject(idx++);
               }
               
               // Find referenced entity
               try
               {
                  Object home = javaCtx.lookup(cmpField.getSqlType());
                  Method[] homeMethods = home.getClass().getMethods();
                  Method finder = null;
                  
                  // We have to locate fBPK iteratively since we don't really know the pk-class
                  for (int j = 0; j < homeMethods.length; j++)
                     if (homeMethods[j].getName().equals("findByPrimaryKey"))
                     {
                        finder = homeMethods[j];
                        break;
                     }
                  
                  if (finder == null)
                     throw new NoSuchMethodException("FindByPrimaryKey method not found in home interface");
                     
                  Object ref = finder.invoke(home, new Object[] { pk });
                  
                  // Set found entity
                  ((Field)cmpFields.get(i)).set(ctx.getInstance(), ref);
               } catch (Exception e)
               {
                  throw new ServerException("Could not restore reference", e);
               }
            } else
            {
               // Load primitive
               
               // TODO: this probably needs to be fixed for BLOB's etc.
               ((Field)cmpFields.get(i)).set(ctx.getInstance(), rs.getObject(idx++));
            }
         }
         
         // Store state to be able to do tuned updates
			PersistenceContext pCtx = (PersistenceContext)ctx.getPersistenceContext();
			if (readOnly) pCtx.lastRead = System.currentTimeMillis();
         
         // Call ejbLoad on bean instance
         ejbLoad.invoke(ctx.getInstance(), new Object[0]);
         
         // Done
      } catch (Exception e)
      {
         throw new ServerException("Load failed", e);
      } finally
      {
         if (rs != null) try { rs.close(); } catch (Exception e) { e.printStackTrace(); }
         if (stmt != null) try { stmt.close(); } catch (Exception e) { e.printStackTrace(); }
         if (con != null) try { con.close(); } catch (Exception e) { e.printStackTrace(); }
      }
   }
      
   public void storeEntity(EntityEnterpriseContext ctx)
      throws RemoteException
   {
		// Check for read-only
		if (readOnly)
			return;
	
      Connection con = null;
      PreparedStatement stmt = null;
      try
      {
         // Call bean
         ejbStore.invoke(ctx.getInstance(), new Object[0]);

         // Create tuned update
         String updateSql = "UPDATE "+entity.getTableName()+" SET ";
         Object[] currentState = getState(ctx);
         boolean[] dirtyField = new boolean[currentState.length];
         Object[] oldState = ((PersistenceContext)ctx.getPersistenceContext()).state;
         boolean dirty = false;
         int refIdx = 0;
         for (int i = 0;i < currentState.length; i++)
         {
            if (((Integer)jdbcTypes.get(i)).intValue() == Types.REF)
            {
					if (((currentState[i] != null) && 
							(oldState[i] == null || !currentState[i].equals(oldState[i]))) ||
						 (oldState[i] != null))
					{
					   JawsCMPField[] pkFields = (JawsCMPField[])ejbRefs.get(refIdx);
					   for (int j = 0; j < pkFields.length; j++)
					   {
					      updateSql += (dirty?",":"") + ((JawsCMPField)CMPFields.get(i)).getColumnName()+"_"+pkFields[j].getColumnName()+"=?";
					      dirty = true;
					   }
					   dirtyField[i] = true;
					}
               refIdx++;
            } else
            {
					if (((currentState[i] != null) &&
						 (oldState[i] == null || !currentState[i].equals(oldState[i]))) ||
						 (oldState[i] != null))
					{
					   updateSql += (dirty?",":"") + ((JawsCMPField)CMPFields.get(i)).getColumnName()+"=?";
					   dirty = true;
					   dirtyField[i] = true;
					}
            }
         }
         
         if (!dirty)
         {
            return;
         } else
         {
            updateSql += " WHERE "+pkColumnWhereList;
         }
         
         // Update database
         con = getConnection();
         stmt = con.prepareStatement(updateSql);
         
         int idx = 1;
         refIdx = 0;
         for (int i = 0;i < dirtyField.length; i++)
         {
            if (((JawsCMPField)CMPFields.get(i)).getJdbcType().equals("REF"))
            {
               if (dirtyField[i])
               {
                  idx = setParameter(stmt,idx,((Integer)jdbcTypes.get(i)).intValue(), currentState[i],refIdx);
               }
               refIdx++;
            } else
            {
               if (dirtyField[i])
               {
                  idx = setParameter(stmt,idx,((Integer)jdbcTypes.get(i)).intValue(), currentState[i],refIdx);
               }
            }
         }
         
         // Primary key in WHERE-clause
         for (int i = 0; i < pkFields.size(); i++)
         {
            Field field = (Field)pkFields.get(i);
            idx = setParameter(stmt,idx,((Integer)pkJdbcTypes.get(i)).intValue(), field.get(ctx.getInstance()),0);
         }
         
         // Execute update
         stmt.execute();
      } catch (Exception e)
      {
         throw new ServerException("Store failed", e);
      } finally
      {
         if (stmt != null) try { stmt.close(); } catch (Exception e) { e.printStackTrace(); }
         if (con != null) try { con.close(); } catch (Exception e) { e.printStackTrace(); }
      }
      
   }

   public void passivateEntity(EntityEnterpriseContext ctx)
      throws RemoteException
   {
      // Call bean
      try
      {
         ejbPassivate.invoke(ctx.getInstance(), new Object[0]);
      } catch (Exception e)
      {
         throw new ServerException("Passivation failed", e);
      }
   }
      
   public void removeEntity(EntityEnterpriseContext ctx)
      throws RemoteException, RemoveException
   {
      Connection con = null;
      PreparedStatement stmt = null;
      
      try
      {
         // Call ejbRemove
         ejbRemove.invoke(ctx.getInstance(), new Object[0]);
      
         // Remove from DB
         con = getConnection();
         stmt = con.prepareStatement(removeSql);
         
         // Primary key in WHERE-clause
         if (compoundKey)
         {
            // Compound key
            for (int i = 0; i < pkClassFields.size(); i++)
            {
               Field field = (Field)pkClassFields.get(i);
               setParameter(stmt,i+1,((Integer)pkJdbcTypes.get(i)).intValue(), field.get(ctx.getId()),0);
            }
         } else
         {
            // Primitive key
            setParameter(stmt,1,((Integer)pkJdbcTypes.get(0)).intValue(), ctx.getId(),0);
         }
         
         int count = stmt.executeUpdate();
         
         if (count == 0)
         {
            throw new RemoveException("Could not remove entity");
         }
   //      System.out.println("Removed file for"+ctx.getId());
      } catch (Exception e)
      {
         throw new RemoveException("Could not remove "+ctx.getId());
      } finally
      {
         if (stmt != null) try { stmt.close(); } catch (Exception e) { e.printStackTrace(); }
         if (con != null) try { con.close(); } catch (Exception e) { e.printStackTrace(); }
      }
   }
   
   // Z implementation ----------------------------------------------
    
   // Package protected ---------------------------------------------
    
   // Protected -----------------------------------------------------
   
   // Private -------------------------------------------------------
   private Connection getConnection()
      throws SQLException
   {
      if (ds != null)
         return ds.getConnection();
      else
         return DriverManager.getConnection(url,"sa","");
   }
   
   private void makeSql()
   {
      // Remove SQL
      removeSql = "DELETE FROM "+entity.getTableName()+" WHERE "+pkColumnWhereList;
      log.debug("Remove:"+removeSql);
      
      // Drop table
      dropSql   = "DROP TABLE "+entity.getTableName();
      log.debug("Drop:"+dropSql);
      
      // Create table
      createSql = "CREATE TABLE "+entity.getTableName()+" (";
      
      int refIdx = 0;
      for (int i = 0;i < CMPFields.size(); i++)
      {
         if (((Integer)jdbcTypes.get(i)).intValue() == Types.REF)
         {
            JawsCMPField[] pkFields = (JawsCMPField[])ejbRefs.get(refIdx);
            for (int j = 0; j < pkFields.length; j++)
            {
               createSql += (i==0 && j==0?"":",") + ((JawsCMPField)CMPFields.get(i)).getColumnName()+"_"+pkFields[j].getColumnName()+" "+pkFields[j].getSqlType();
            }
            refIdx++;
         } else
         {
            JawsCMPField field = (JawsCMPField)CMPFields.get(i);
            createSql += (i==0?"":",") + field.getColumnName()+" "+field.getSqlType();
         }
      }
      
      createSql += ")";
      
      log.debug("Create table:"+createSql);
      
      // Insert SQL fields
      insertSql = "INSERT INTO "+entity.getTableName();
      String fieldSql = "";
      String valueSql = "";
      refIdx = 0;
      for (int i = 0;i < CMPFields.size(); i++)
      {
         if (((Integer)jdbcTypes.get(i)).intValue() == Types.REF)
         {
            JawsCMPField[] pkFields = (JawsCMPField[])ejbRefs.get(refIdx);
            for (int j = 0; j < pkFields.length; j++)
            {
               fieldSql += (fieldSql.equals("") ? "":",") + ((JawsCMPField)CMPFields.get(i)).getColumnName()+"_"+pkFields[j].getColumnName();
               valueSql += (valueSql.equals("") ? "?":",?");
            }
            refIdx++;
         } else
         {
            JawsCMPField field = (JawsCMPField)CMPFields.get(i);
            fieldSql += (fieldSql.equals("") ? "":",") + field.getColumnName();
            valueSql += (valueSql.equals("") ? "?":",?");
         }
      }
      
      insertSql += " ("+fieldSql+") VALUES ("+valueSql+")";
      log.debug("Insert:"+insertSql);
      
      // Select SQL fields
      selectSql = "SELECT ";
      refIdx = 0;
      for (int i = 0;i < CMPFields.size(); i++)
      {
         if (((Integer)jdbcTypes.get(i)).intValue() == Types.REF)
         {
            JawsCMPField[] pkFields = (JawsCMPField[])ejbRefs.get(refIdx);
            for (int j = 0; j < pkFields.length; j++)
            {
               selectSql += (i==0 && j==0?"":",") + ((JawsCMPField)CMPFields.get(i)).getColumnName()+"_"+pkFields[j].getColumnName();
            }
            refIdx++;
         } else
         {
            JawsCMPField field = (JawsCMPField)CMPFields.get(i);
            selectSql += (i==0?"":",") + field.getColumnName();
         }
      }
      
      selectSql += " FROM "+entity.getTableName()+ " WHERE "+pkColumnWhereList;
      log.debug("Select:"+selectSql);
      
/*      // Update SQL fields
      updateSql = "UPDATE "+entity.getTableName()+" SET ";
      fieldSql = "";
      for (int i = 0; i < CMPFields.length; i++)
      {
         CMPField field = CMPFields[i];
         if (field.getJdbcType().equals("REF"))
         {
            String[] pk = getPkColumn(field);
            fieldSql += (fieldSql.equals("") ? "":",")+pk[0]+"=?";
         } else
         {
            fieldSql += (fieldSql.equals("") ? "":",")+field.getColumnName()+"=?";
         }
         
      }
      updateSql += fieldSql +" WHERE "+idColumn+"=?";
      
      log.debug("Update:"+updateSql);
*/      
   }
   
   private Object[] getState(EntityEnterpriseContext ctx)
   {
      Object[] state = new Object[cmpFields.size()];
      for (int i = 0; i < state.length; i++)
      {
         try
         {
            state[i] = ((Field)cmpFields.get(i)).get(ctx.getInstance());
         } catch (Exception e)
         {
            return null;
         }
      }
      
      return state;
   }
   
   private int getJDBCType(String name)
   {
      try
      {
         Integer constant = (Integer)Types.class.getField(name).get(null);
         return constant.intValue();
      } catch (Exception e)
      {
         e.printStackTrace();
         return Types.OTHER;
      }
   }
   
   private JawsCMPField[] getPkColumns(JawsCMPField field)
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
   
   private int setParameter(PreparedStatement stmt,int idx,int jdbcType, Object value, int refIdx)
      throws SQLException
   {
      if (value == null)
      {
         stmt.setNull(idx, jdbcType);
         return idx+1;
      } else
      {
         switch (jdbcType)
         {
            case Types.ARRAY:
            {
               stmt.setArray(idx,(Array)value);
               return idx+1;
            }
            
            case Types.BIGINT:
            {
               stmt.setLong(idx,((Long)value).longValue());
               return idx+1;
            }
            
            case Types.BINARY:
            {
               stmt.setBytes(idx,(byte[])value);
               return idx+1;
            }
            
            case Types.BIT:
            {
               stmt.setBoolean(idx,((Boolean)value).booleanValue());
               return idx+1;
            }
            
            case Types.BLOB:
            {
               stmt.setBlob(idx,(Blob)value);
               return idx+1;
            }
            
   /*         case Types.CHAR:
            {
               stmt.setArray(idx,value);
               return idx+1;
            }
   */         
   /*         case Types.CLOB:
            {
               stmt.setArray(idx,value);
               return idx+1;
            }
   */
   
            case Types.DATE:
            {
               stmt.setDate(idx,(Date)value);
               return idx+1;
            }
            
            case Types.DECIMAL:
            {
               stmt.setBigDecimal(idx,(BigDecimal)value);
               return idx+1;
            }
            
   /*         case Types.DISTINCT:
            {
               stmt.setBlob(idx,(Blob)value);
               return idx+1;
            }
   */         
            case Types.DOUBLE:
            {
               stmt.setDouble(idx,((Double)value).doubleValue());
               return idx+1;
            }
   
            case Types.FLOAT:
            {
               stmt.setFloat(idx,((Float)value).floatValue());
               return idx+1;
            }
            
            case Types.INTEGER:
            {
               stmt.setInt(idx,((Integer)value).intValue());
               return idx+1;
            }
            
            case Types.JAVA_OBJECT:
            {
               // Rickard: Any better solution?
               stmt.setObject(idx,value);
               return idx+1;
            }
            
            case Types.LONGVARBINARY:
            {
               // Rickard: How to do this best?
               stmt.setBytes(idx,(byte[])value);
               return idx+1;
            }
            
            case Types.LONGVARCHAR:
            {
               // Rickard: How to do this best?
               if (value instanceof String)
               {
                  stmt.setString(idx,(String)value);
               } else
               {
                  stmt.setBytes(idx,(byte[])value);
               }
               return idx+1;
            }
            
            case Types.NUMERIC:
            {
               stmt.setBigDecimal(idx,(BigDecimal)value);
               return idx+1;
            }
            
            case Types.OTHER:
            {
               stmt.setObject(idx,value);
               return idx+1;
            }
            
            case Types.REAL:
            {
               stmt.setFloat(idx,((Float)value).floatValue());
               return idx+1;
            }
            
            case Types.REF:
            {
               // EJB-reference
               JawsCMPField[] pkInfo = (JawsCMPField[])ejbRefs.get(refIdx);
               Object pk = null;
               try
               {
                  pk = ((EJBObject)value).getPrimaryKey();
               } catch (RemoteException e)
               {
                  throw new SQLException("Could not extract primary key from EJB reference:"+e);
               }
               
               if (!((JawsEntity)pkInfo[0].getBeanContext()).getPrimaryKeyField().equals(""))
               {
                  // Primitive key
                  setParameter(stmt,idx,getJDBCType(pkInfo[0].getJdbcType()), pk, refIdx);
                  return idx+1;
               } else
               {
                  // Compound key
                  Field[] fields = pk.getClass().getFields();
                  try
                  {
                     for (int i = 0; i < pkInfo.length; i++)
                     {
                        setParameter(stmt,idx+i,getJDBCType(pkInfo[i].getJdbcType()), fields[i].get(pk), refIdx);
                     }
                  } catch (IllegalAccessException e)
                  {
                     throw new SQLException("Could not extract fields from primary key:"+e);
                  }
                  return idx+pkInfo.length;
               }
            }
            
            case Types.SMALLINT:
            {
               stmt.setShort(idx,((Short)value).shortValue());
               return idx+1;
            }
            
            case Types.STRUCT:
            {
               stmt.setObject(idx,value);
               return idx+1;
            }
            
            case Types.TIME:
            {
               stmt.setTime(idx,(Time)value);
               return idx+1;
            }
            
            case Types.TIMESTAMP:
            {
               stmt.setTimestamp(idx,(Timestamp)value);
               return idx+1;
            }
            
            case Types.TINYINT:
            {
               stmt.setByte(idx,((Byte)value).byteValue());
               return idx+1;
            }
            
            case Types.VARBINARY:
            {
               stmt.setBytes(idx,(byte[])value);
               return idx+1;
            }
            
            case Types.VARCHAR:
            {
               stmt.setString(idx,(String)value);
               return idx+1;
            }
            
            default:
            {
               // Not really necessary
               stmt.setObject(idx,value);
               return idx+1;
            }
         }
      }
   }
   
   // Inner classes -------------------------------------------------
   static class CMPObjectOutputStream
      extends ObjectOutputStream
   {
      public CMPObjectOutputStream(OutputStream out)
         throws IOException
      {
         super(out);
         enableReplaceObject(true);
      }
      
      protected Object replaceObject(Object obj)
         throws IOException
      {
         if (obj instanceof EJBObject)
            return ((EJBObject)obj).getHandle();
            
         return obj;
      }
   }
   
   static class CMPObjectInputStream
      extends ObjectInputStream
   {
      public CMPObjectInputStream(InputStream in)
         throws IOException
      {
         super(in);
         enableResolveObject(true);
      }
      
      protected Object resolveObject(Object obj)
         throws IOException
      {
         if (obj instanceof Handle)
            return ((Handle)obj).getEJBObject();
            
         return obj;
      }
   }
	
   static class PersistenceContext
   {
		Object[] state;
		long lastRead = -1;
   }
}
