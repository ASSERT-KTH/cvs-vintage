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
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.ejb.EJBException;
import javax.transaction.Transaction;

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
 * @version $Revision: 1.14 $
 */                            
public class EntityBridgeInvocationHandler implements InvocationHandler {
   private final EntityContainer container;
   private final EntityBridge entityBridge;
   private final Class beanClass;
   private final Map fieldMap;
   private final Map selectorMap;
   private EntityEnterpriseContext ctx;
   
   /**
    * Creates an invocation handler for the specified entity.
    * @param entityBridge the bridge that will be called in response to 
    *    in invocation
    * @param beanClass the implementation class for the ejb
    * @throws Exception if a problem occures while setting up the method maps
    */
   public EntityBridgeInvocationHandler(
         EntityContainer container,
         EntityBridge entityBridge,
         Class beanClass) throws DeploymentException {

      this.container = container;
      this.entityBridge = entityBridge;
      this.beanClass = beanClass;

      fieldMap = createFieldMap();
      selectorMap = createSelectorMap();
   }
   
   public void setContext(EntityEnterpriseContext ctx) {
      if(ctx != null && !beanClass.isInstance(ctx.getInstance())) {
         throw new EJBException("Instance must be an instance of beanClass");
      }
      this.ctx = ctx;
   }
   
   public Object invoke(Object proxy, Method method, Object[] args) 
         throws FinderException {

      String methodName = method.getName();
   
      try {
         SelectorBridge selector = (SelectorBridge) selectorMap.get(method);
         if(selector != null) {
            Transaction tx;
            if(ctx != null) {
               // it is probably safer to get the tx from the context if we have
               // one (ejbHome methods don't have a context)
               tx = ctx.getTransaction();
            } else {
               tx = container.getTransactionManager().getTransaction();
            }
            EntityContainer.synchronizeEntitiesWithinTransaction(tx);
            return selector.execute(args);
         }
      } catch(RuntimeException e) {
         throw e;
      } catch(FinderException e) {
         throw e;
      } catch(Exception e) {
         throw new EJBException("Internal error", e);
      }
   
      try {
         // get the field object
         FieldBridge field = (FieldBridge) fieldMap.get(method);
   
         if(field == null) { 
            throw new EJBException("Method is not a known CMP field " +
                  "accessor, CMR field accessor, or ejbSelect method: " +
                  "methodName=" + methodName);
         }
   
         // In the case of ejbHome methods there is no context, but ejb home
         // methods are only allowed to call selectors.
         if(ctx == null) {
            throw new EJBException("EJB home methods are not allowed to " +
                  "access CMP or CMR fields: methodName=" + methodName);
         }
   
         if(methodName.startsWith("get")) {
            return field.getValue(ctx);
         } else if(methodName.startsWith("set")) {
            field.setValue(ctx, args[0]);
            return null;
         }
      } catch(RuntimeException e) {
         throw e;
      } catch(Exception e) {
         throw new EJBException("Internal error", e);
      }

      // Should never get here, but it's better to be safe then sorry.
      throw new EJBException("Unknown field accessor method: " +
            "methodName=" + methodName);
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
   
   private Map createFieldMap() throws DeploymentException {

      Map abstractAccessors = getAbstractAccessors();

      Collection fields = entityBridge.getFields();
      Map map = new HashMap(fields.size() * 2);
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
            map.put(getterMethod, field);
            map.put(setterMethod, field);

            // remove the accessors (they have been used)
            abstractAccessors.remove(getterName);
            abstractAccessors.remove(setterName);
         }
      }
      return Collections.unmodifiableMap(map);
   }

   private Map createSelectorMap() {
      Collection selectors = entityBridge.getSelectors();
      Map map = new HashMap(selectors.size());
      for(Iterator iter = selectors.iterator(); iter.hasNext();) {
         SelectorBridge selector = (SelectorBridge)iter.next();
         map.put(selector.getMethod(), selector);
      }
      return Collections.unmodifiableMap(map);
   }
}
