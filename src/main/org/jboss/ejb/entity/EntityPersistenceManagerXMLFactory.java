/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.entity;

import java.lang.ClassLoader;
import java.util.Iterator;
import javax.ejb.EJBException;
import org.jboss.ejb.Container;
import org.jboss.ejb.Interceptor;
import org.jboss.logging.Logger;
import org.jboss.metadata.MetaData;
import org.w3c.dom.Element;

/**
 * The EntityPersistenceManager is called by other plugins in the
 * container.  
 *
 * see EntityContainer
 * 
 * @author <a href="mailto:dain@daingroup.com">Dain Sundstrom</a>
 * @version $Revision: 1.1 $
 */
public final class EntityPersistenceManagerXMLFactory
{
   public EntityPersistenceManager create(Container container, Element element)
   {
      Logger log = Logger.getLogger(this.getClass());

      // create the persistence manager
      EntityPersistenceManager entityPersistenceManager = 
         new SimplePersistenceManager();
      entityPersistenceManager.setContainer(container);
      
      // get the interceptor elements
      Iterator interceptorElements = MetaData.getChildrenByTagName(
            element, 
            "interceptor");
      if(interceptorElements == null || !interceptorElements.hasNext())
      {
         throw new EJBException("persistence-manager has no " +
               "interceptor elements: ejb-name=" +
               container.getBeanMetaData().getEjbName());
      }

      // interate over the interceptor elements adding the interceptors
      // to the entity persistence manager stack
      ClassLoader cl = container.getClassLoader();
      while(interceptorElements != null && interceptorElements.hasNext() )
      {
         Element interceptorElement = (Element) interceptorElements.next();
         String className = null;
         try
         {
            className = MetaData.getElementContent(interceptorElement);
            Class clazz = cl.loadClass(className);
            Interceptor interceptor = (Interceptor) clazz.newInstance();
            interceptor.setConfiguration(interceptorElement);
            entityPersistenceManager.addInterceptor(interceptor);
         }
         catch(Exception e)
         {
            log.warn("Could not load the " + className + 
                  " interceptor for this container", e);
         }
      }
      return entityPersistenceManager;
   }
} 
