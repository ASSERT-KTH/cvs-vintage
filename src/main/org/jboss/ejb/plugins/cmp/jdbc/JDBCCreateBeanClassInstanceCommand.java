/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
 
package org.jboss.ejb.plugins.cmp.jdbc;

import java.lang.reflect.Constructor;

import org.jboss.ejb.EntityContainer;
import org.jboss.ejb.plugins.cmp.jdbc.bridge.JDBCEntityBridge;
import org.jboss.ejb.plugins.cmp.bridge.EntityBridgeInvocationHandler;
import org.jboss.proxy.compiler.Proxy;
import org.jboss.proxy.compiler.InvocationHandler;

/**
 * JDBCBeanClassInstanceCommand creates instance of the bean class. For 
 * CMP 2.0 it creates an instance of a subclass of the bean class, as the
 * bean class is abstract.
 *
 * <FIX-ME>should not generat a subclass for ejb 1.1</FIX-ME>
 *    
 * @author <a href="mailto:dain@daingroup.com">Dain Sundstrom</a>
 * @version $Revision: 1.3 $
 */
 
public class JDBCCreateBeanClassInstanceCommand {
   private EntityContainer container;
   private JDBCEntityBridge entityBridge;
   private Class beanClass;
   private Constructor beanProxyConstructor;
   
   public JDBCCreateBeanClassInstanceCommand(JDBCStoreManager manager) 
         throws Exception {

      container = manager.getContainer();
      entityBridge = manager.getEntityBridge();
      beanClass = container.getBeanClass();

      // use proxy generator to create one implementation
      EntityBridgeInvocationHandler handler = new EntityBridgeInvocationHandler(
            container,
            entityBridge,
            beanClass);
      Class[] classes = new Class[] { beanClass };
      ClassLoader classLoader = beanClass.getClassLoader();

      Object o = Proxy.newProxyInstance(classLoader, classes, handler);

      // steal the constructor from the object
      beanProxyConstructor = 
            o.getClass().getConstructor(new Class[]{InvocationHandler.class});
      
      // now create one to make sure everything is cool
      execute();
   }
   
   public Object execute() throws Exception {
      EntityBridgeInvocationHandler handler = new EntityBridgeInvocationHandler(
            container,
            entityBridge,
            beanClass);

      return beanProxyConstructor.newInstance(new Object[]{handler});
   }
}

