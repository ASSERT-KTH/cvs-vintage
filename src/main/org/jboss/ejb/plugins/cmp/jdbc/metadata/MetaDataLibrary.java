/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins.cmp.jdbc.metadata;

import org.jboss.system.ServiceMBeanSupport;
import org.jboss.deployment.DeploymentException;
import org.jboss.metadata.XmlFileLoader;
import org.jboss.metadata.MetaData;
import org.w3c.dom.Element;

import java.net.URL;
import java.util.Iterator;
import java.util.Hashtable;
import java.util.Set;
import java.util.Collections;

/**
 * @author <a href="mailto:alex@jboss.org">Alexey Loubyansky</a>
 * @version <tt>$Revision: 1.3 $</tt>
 * @jmx:mbean name="jboss.jdbc:service=metadata"
 * extends="org.jboss.system.ServiceMBean"
 */
public class MetaDataLibrary
   extends ServiceMBeanSupport
   implements MetaDataLibraryMBean
{
   private final Hashtable typeMappings = new Hashtable();

   /**
    * @jmx.managed-operation
    */
   public JDBCTypeMappingMetaData findTypeMappingMetaData(String name)
   {
      return (JDBCTypeMappingMetaData)typeMappings.get(name);
   }

   /**
    * @jmx.managed-attribute
    */
   public Set getTypeMappingNames()
   {
      return Collections.unmodifiableSet(typeMappings.keySet());
   }

   public void startService() throws Exception
   {
      ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

      URL stdJDBCUrl = classLoader.getResource("standardjbosscmp-jdbc.xml");
      if(stdJDBCUrl == null)
      {
         throw new DeploymentException("No standardjbosscmp-jdbc.xml found");
      }

      log.debug("Loading standardjbosscmp-jdbc.xml : " + stdJDBCUrl.toString());
      
      Element stdJDBCElement = XmlFileLoader.getDocument(stdJDBCUrl, true).getDocumentElement();

      Element typeMaps = MetaData.getOptionalChild(stdJDBCElement, "type-mappings");
      if(typeMaps != null)
      {
         for(Iterator i = MetaData.getChildrenByTagName(typeMaps, "type-mapping"); i.hasNext();)
         {
            Element typeMappingElement = (Element)i.next();
            JDBCTypeMappingMetaData typeMapping = new JDBCTypeMappingMetaData(typeMappingElement);
            typeMappings.put(typeMapping.getName(), typeMapping);

            log.debug("added type-mapping: " + typeMapping.getName());
         }
      }
   }

   public void stopService() throws Exception
   {
      typeMappings.clear();
   }
}
