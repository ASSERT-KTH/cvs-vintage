// This holds all the meta-information

/*
 * jBoss, the OpenSource EJB server
 *
 * Distributable under GPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins.jaws;

import java.beans.Beans;
import java.beans.beancontext.BeanContextServicesSupport;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.ArrayList;
import java.util.Iterator;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;

import org.jboss.ejb.EntityContainer;

import org.jboss.ejb.plugins.jaws.deployment.JawsFileManager;
import org.jboss.ejb.plugins.jaws.deployment.JawsFileManagerFactory;
import org.jboss.ejb.plugins.jaws.deployment.JawsEjbJar;
import org.jboss.ejb.plugins.jaws.deployment.JawsEnterpriseBeans;
import org.jboss.ejb.plugins.jaws.deployment.JawsEntity;
import org.jboss.ejb.plugins.jaws.deployment.JawsCMPField;

/**
 * This is a wrapper for all the meta-information
 * needed by JawsPersistenceManager commands.
 *      
 * @see <related>
 * @author <a href="mailto:rickard.oberg@telkel.com">Rickard Öberg</a>
 * @author <a href="mailto:marc.fleury@telkel.com">Marc Fleury</a>
 * @author <a href="mailto:shevlandj@kpi.com.au">Joe Shevland</a>
 * @author <a href="mailto:justin@j-m-f.demon.co.uk">Justin Forder</a>
 * @version $Revision: 1.2 $
 */
public class MetaInfo
{
   // Attributes ----------------------------------------------------
    
   private String ejbName;
   private JawsEntity entity;
   private String tableName;
   private DataSource dataSource;
   private String dbURL;
   private boolean compositeKey;
   private Class primaryKeyClass = null;
   private Collection cmpFieldInfos = new ArrayList();
   private Collection pkFieldInfos = new ArrayList();
   
   // Constructors --------------------------------------------------
   
   public MetaInfo(EntityContainer container) throws Exception
   {
      ejbName = container.getBeanMetaData().getEjbName();
      JawsEjbJar jar = readJawsEjbJar(container);
      entity = (JawsEntity)jar.getEnterpriseBeans().getEjb(ejbName);

      // we replace the . by _ because some dbs die on it... 
      tableName = entity.getTableName().replace('.','_');
      compositeKey = entity.getPrimaryKeyField().equals("");

      Iterator fieldDescriptors = entity.getCMPFields();
      while (fieldDescriptors.hasNext())
      {
         JawsCMPField fieldDesc = (JawsCMPField)fieldDescriptors.next();
         cmpFieldInfos.add(new CMPFieldInfo(fieldDesc, container));
      }

      if (compositeKey)
      {
         String pkClassName = entity.getPrimaryKeyClass();
         primaryKeyClass = container.getClassLoader().loadClass(pkClassName);
         Field[] pkClassFields = primaryKeyClass.getFields();

         for (int i = 0; i < pkClassFields.length; i++)
         {
            Field pkField = pkClassFields[i];
            pkFieldInfos.add(new PkFieldInfo(pkField, getCMPFieldInfos()));
         }
      } else
      {
         String pkFieldName = entity.getPrimaryKeyField();
         pkFieldInfos.add(new PkFieldInfo(pkFieldName, getCMPFieldInfos()));
      }
      // Find datasource
      dbURL = ((JawsEjbJar)entity.getBeanContext().getBeanContext()).getDataSource();
      if (!dbURL.startsWith("jdbc:"))
      {
         dataSource = 
            (DataSource)new InitialContext().lookup(((JawsEjbJar)entity.getBeanContext().getBeanContext()).getDataSource());
      }
   }

   // Public --------------------------------------------------------

   public final String getName()
   {
      return ejbName;
   }

   public final boolean getCreateTable()
   {
      return entity.getCreateTable();
   }

   public final boolean getRemoveTable()
   {
      return entity.getRemoveTable();
   }

   public final Iterator getFinders()
   {
      return entity.getFinders();
   }

   public final String getTableName()
   {
      return tableName;
   }

   public final String getDbURL()
   {
      return dbURL;
   }

   public final DataSource getDataSource()
   {
      return dataSource;
   }

   public final boolean hasCompositeKey()
   {
      return compositeKey;
   }

   public final Class getPrimaryKeyClass()
   {
      return primaryKeyClass;
   }

   // This is just here for the state handling.
   public final int getNumberOfCMPFields()
   {
      return cmpFieldInfos.size();
   }

   public final Iterator getCMPFieldInfos()
   {
      return cmpFieldInfos.iterator();
   }

   public final Iterator getPkFieldInfos()
   {
      return pkFieldInfos.iterator();
   }

   public final boolean isReadOnly()
   {
      return entity.getReadOnly();
   }

   public final long getReadOnlyTimeOut()
   {
      return entity.getTimeOut();
   }

   public final boolean hasTunedUpdates()
   {
      return entity.getTunedUpdates();
   }

   // Private -------------------------------------------------------

   private JawsEjbJar readJawsEjbJar(EntityContainer container)
      throws Exception
   {
      JawsFileManager jfm = 
         (JawsFileManager)new JawsFileManagerFactory().createFileManager();

      // Setup beancontext to give jfm access to an XML parser
      BeanContextServicesSupport beanCtx = new BeanContextServicesSupport();
      beanCtx.add(Beans.instantiate(getClass().getClassLoader(), 
                                    "com.dreambean.ejx.xml.ProjectX"));
      beanCtx.add(jfm);

      // Load XML
      // If the URL doesn't have default information the filemanager uses defaults
      return jfm.load(container.getApplication().getURL());
   }
}
