/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
 
package org.jboss.ejb.plugins.cmp.bridge;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.ejb.EJBException;

import org.jboss.deployment.DeploymentException;
import org.jboss.ejb.EntityContainer;
import org.jboss.ejb.EntityEnterpriseContext;
import org.jboss.proxy.compiler.InvocationHandler;

/**
 * EntityBridgeInvocationHandler is the invocation hander used by the CMP 2.x
 * dynamic proxy. This class only interacts with the EntityBridge. The main
 * job of this class is to deligate invocation of abstract methods to the 
 * appropriate EntityBridge method.
 *
 * Life-cycle:
 *      Tied to the life-cycle of an entity bean instance.
 *
 * Multiplicity:   
 *      One per cmp entity bean instance, including beans in pool.       
 *
 * @author <a href="mailto:dain@daingroup.com">Dain Sundstrom</a>
 * @version $Revision: 1.11 $
 */                            
public class EntityBridgeInvocationHandler implements InvocationHandler {
   private EntityContainer container;
   private EntityBridge entityBridge;
   private Class beanClass;
   private EntityEnterpriseContext ctx;
   private Map fieldMap;
   private Map selectorMap;
   
   /**
    * Creates an invocation handler for the specified entity.
    * @param container the container for this class handles invocations
    * @param entityBridge the bridge that will be called in response to 
    *    in invocation
    * @param beanClass the implementation class for the ejb
    * @throws Exception if a problem occures while setting up the method maps
    */
   public EntityBridgeInvocationHandler(
         EntityContainer container,
         EntityBridge entityBridge,
         Class beanClass) throws Exception {

      this.container = container;
      this.entityBridge = entityBridge;
      this.beanClass = beanClass;

      setupFieldMap();
      setupSelectorMap();
   }
   
   public void setContext(EntityEnterpriseContext ctx) {
      if(ctx != null && !beanClass.isInstance(ctx.getInstance())) {
         throw new EJBException("Instance must be an instance of beanClass");
      }
      this.ctx = ctx;
   }
   
   public Object invoke(Object proxy, Method method, Object[] args)
         throws Throwable {

      String methodName = method.getName();

      // is this a field accessor
      FieldBridge field = (FieldBridge) fieldMap.get(method);
      if(field != null) {
         if(methodName.startsWith("get")) {
            return field.getInstanceValue(ctx);
         } else if(methodName.startsWith("set")) {
            if(field.isReadOnly()) {
               throw new EJBException("Field is read-only: " +
                     field.getFieldName());
            }
            field.setInstanceValue(ctx, args[0]);
            return null;
         }
         Exception e = new EJBException("Unknown field method: " +
               methodName);

         e.printStackTrace();
         throw e;
      }

      SelectorBridge selector = (SelectorBridge) selectorMap.get(method);
      if(selector != null) {
         container.synchronizeEntitiesWithinTransaction(ctx.getTransaction());
         return selector.execute(args);
      }
      
      Exception e = new EJBException("Unknown method type: " + methodName);
      e.printStackTrace();
      throw e;
   }
   
   private Map getAbstractAccessors() {
      Method[] methods = beanClass.getMethods();
      Map abstractAccessors = new HashMap(methods.length);
      
      for(int i=0; i<methods.length; i++) {
          if(Modifier.isAbstract(methods[i].getModifiers())) {
            String methodName = methods[i].getName();
            if(methodName.startsWith("get") || methodName.startsWith("set")) {
               abstractAccessors.put(methodName, methods[i]);
            }               
         }
      }
      return abstractAccessors;
   }
   
   private void setupFieldMap() throws DeploymentException {

      Map abstractAccessors = getAbstractAccessors();

      Collection fields = entityBridge.getFields();
      fieldMap = new HashMap(fields.size() * 2);
      for(Iterator iter = fields.iterator(); iter.hasNext();) {
         FieldBridge field = (FieldBridge)iter.next();

         // get the names
         String fieldName = field.getFieldName();
         String fieldBaseName = Character.toUpperCase(fieldName.charAt(0)) +
            fieldName.substring(1);
         String getterName = "get" + fieldBaseName;
         String setterName = "set" + fieldBaseName;
   
         // get the accessor methods
         Method getterMethod = (Method)abstractAccessors.get(getterName);
         Method setterMethod = (Method)abstractAccessors.get(setterName);

         // getters and setters must come in pairs
         if(getterMethod != null && setterMethod == null) {
            throw new DeploymentException("Getter was found but, no setter " +
                  "was found for field: " + fieldName);
         } else if(getterMethod == null && setterMethod != null) {
            throw new DeploymentException("Setter was found but, no getter " +
                  "was found for field: " + fieldName);
         } else if(getterMethod != null && setterMethod != null) {
            // add methods
            fieldMap.put(getterMethod, field);
            fieldMap.put(setterMethod, field);

            // remove the accessors (they have been used)
            abstractAccessors.remove(getterName);
            abstractAccessors.remove(setterName);
         }
      }
   }

   private void setupSelectorMap() {
      Collection selectors = entityBridge.getSelectors();
      selectorMap = new HashMap(selectors.size());
      for(Iterator iter = selectors.iterator(); iter.hasNext();) {
         SelectorBridge selector = (SelectorBridge)iter.next();
         selectorMap.put(selector.getMethod(), selector);
      }
   }
}
