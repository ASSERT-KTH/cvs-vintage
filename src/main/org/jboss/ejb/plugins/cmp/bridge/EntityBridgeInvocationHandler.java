/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
 
package org.jboss.ejb.plugins.cmp.bridge;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import java.util.Map;
import java.util.HashMap;

import javax.ejb.EJBException;

import org.jboss.ejb.DeploymentException;
import org.jboss.ejb.EntityContainer;
import org.jboss.ejb.EntityEnterpriseContext;
import org.jboss.proxy.InvocationHandler;

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
 * @version $Revision: 1.6 $
 */                            
public class EntityBridgeInvocationHandler implements InvocationHandler {
   protected EntityContainer container;
   protected EntityBridge entityBridge;
   protected Class beanClass;
   protected EntityEnterpriseContext ctx;
   protected Map cmpFieldMap;
   protected Map cmrFieldMap;
   protected Map selectorMap;
   
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

      Map abstractAccessors = getAbstractAccessors();
      setupCMPFieldMap(abstractAccessors);
      setupCMRFieldMap(abstractAccessors);
      
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
      
      // is this a cmp field accessor
      CMPFieldBridge cmpField = (CMPFieldBridge) cmpFieldMap.get(method);
      if(cmpField != null) {
         if(methodName.startsWith("get")) {
            return cmpField.getInstanceValue(ctx);
         } else if(methodName.startsWith("set")) {
            if(cmpField.isReadOnly()) {
               throw new EJBException("Field is read-only: " +
                     cmpField.getFieldName());
            }
            cmpField.setInstanceValue(ctx, args[0]);
            return null;
         }
         Exception e = new EJBException("Unknown cmp field method: " +
               methodName);

         e.printStackTrace();
         throw e;
      }

      CMRFieldBridge cmrField = (CMRFieldBridge) cmrFieldMap.get(method);
      if(cmrField != null) {
         if(methodName.startsWith("get")) {
            return cmrField.getValue(ctx);
         } else if(methodName.startsWith("set")) {
            cmrField.setValue(ctx, args[0]);
            return null;
         }
         throw new EJBException("Unknown cmr field method: " + methodName);
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
   
   protected Map getAbstractAccessors() {
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
   
   protected void setupCMPFieldMap(Map abstractAccessors)
         throws DeploymentException {

      CMPFieldBridge[] cmpFields = entityBridge.getCMPFields();
      cmpFieldMap = new HashMap(cmpFields.length * 2);
   
      for(int i=0; i<cmpFields.length; i++) {
         setupCMPFieldGetter(abstractAccessors, cmpFields[i]);
         setupCMPFieldSetter(abstractAccessors, cmpFields[i]);
      }
   }

   protected void setupCMPFieldGetter(
         Map abstractAccessors,
         CMPFieldBridge cmpField)  throws DeploymentException {

      String fieldName = cmpField.getFieldName();
      String getterName = "get" + Character.toUpperCase(fieldName.charAt(0)) +
            fieldName.substring(1);
   
      Method getterMethod = (Method)abstractAccessors.get(getterName);
      if(getterMethod != null) {
         cmpFieldMap.put(getterMethod, cmpField);
         abstractAccessors.remove(getterName);
      } else {
         throw new DeploymentException("No getter found for cmp field: " +
               fieldName);
      }
   }
   
   protected void setupCMPFieldSetter(
         Map abstractAccessors,
         CMPFieldBridge cmpField)  throws DeploymentException {

      String fieldName = cmpField.getFieldName();
      String setterName = "set" + Character.toUpperCase(fieldName.charAt(0)) +
            fieldName.substring(1);
   
      Method setterMethod = (Method)abstractAccessors.get(setterName);
      if(setterMethod != null) {
         cmpFieldMap.put(setterMethod, cmpField);
         abstractAccessors.remove(setterName);
      } else {
         throw new DeploymentException("No setter found for cmp field: " +
               fieldName);
      }
   }
   
   protected void setupCMRFieldMap(Map abstractAccessors)
         throws DeploymentException {

      CMRFieldBridge[] cmrFields = entityBridge.getCMRFields();
      cmrFieldMap = new HashMap(cmrFields.length * 2);
   
      for(int i=0; i<cmrFields.length; i++) {
         // in unidirectional relationships only one side has
         // a field name
         if(cmrFields[i].getFieldName() != null) {
            setupCMRFieldGetter(abstractAccessors, cmrFields[i]);
            setupCMRFieldSetter(abstractAccessors, cmrFields[i]);
         }
      }
   }

   protected void setupCMRFieldGetter(
         Map abstractAccessors,
         CMRFieldBridge cmrField) throws DeploymentException {

      String fieldName = cmrField.getFieldName();
      String getterName = "get" + Character.toUpperCase(fieldName.charAt(0)) +
            fieldName.substring(1);
   
      Method getterMethod = (Method)abstractAccessors.get(getterName);
      if(getterMethod != null) {
         cmrFieldMap.put(getterMethod, cmrField);
         abstractAccessors.remove(getterName);
      }
   }
   
   protected void setupCMRFieldSetter(
         Map abstractAccessors,
         CMRFieldBridge cmrField) throws DeploymentException {
      
      String fieldName = cmrField.getFieldName();
      String setterName = "set" + Character.toUpperCase(fieldName.charAt(0)) +
            fieldName.substring(1);
   
      Method setterMethod = (Method)abstractAccessors.get(setterName);
      if(setterMethod != null) {
         cmrFieldMap.put(setterMethod, cmrField);
         abstractAccessors.remove(setterName);
      }
   }
   
   protected void setupSelectorMap() {
      SelectorBridge[] selectors = entityBridge.getSelectors();
      selectorMap = new HashMap(selectors.length);

      for(int i=0; i<selectors.length; i++) {
         selectorMap.put(selectors[i].getMethod(), selectors[i]);
      }
   }
}
