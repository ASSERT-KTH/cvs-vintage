/*
 * JBoss, the OpenSource EJB server
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
import org.jboss.ejb.EntityEnterpriseContext;
import org.jboss.proxy.InvocationHandler;

/**
 * EntityBridgeInvocationHandler is the invocation hander used by the CMP 2.x
 * dynamic proxy. This class only interacts with the EntityBridge. The main
 * job of this class is to deligate invocation of abstract methods to the 
 * appropriate EntityBridge method.
 *
 * Life-cycle:
 *		Tied to the life-cycle of an entity bean instance.
 *
 * Multiplicity:	
 *		One per cmp entity bean instance, including beans in pool. 		
 *
 * @author <a href="mailto:dain@daingroup.com">Dain Sundstrom</a>
 * @version $Revision: 1.1 $
 */                            
public class EntityBridgeInvocationHandler implements InvocationHandler {
	protected EntityBridge entityBridge;
	protected Class beanClass;
	protected EntityEnterpriseContext ctx;
	protected Map cmpFieldMap;
	
	public EntityBridgeInvocationHandler(EntityBridge entityBridge, Class beanClass) throws Exception {
		this.entityBridge = entityBridge;
		this.beanClass = beanClass;
		setupCMPFieldMap(getAbstractAccessors());
	}
	
	public void setContext(EntityEnterpriseContext ctx) {
		if(ctx != null && !beanClass.isInstance(ctx.getInstance())) {
			throw new EJBException("Instance must be an instance of beanClass");
		}
		this.ctx = ctx;
	}
	
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		String methodName = method.getName();
		
		// is this a cmp field accessor
		CMPFieldBridge cmpField = (CMPFieldBridge) cmpFieldMap.get(method);
		if(cmpField != null) {
			if(methodName.startsWith("get")) {
				return cmpField.getInstanceValue(ctx);
			} else if(methodName.startsWith("set")) {
				if(cmpField.isReadOnly()) {
					throw new EJBException("Field is read-only: " + cmpField.getFieldName());
				}
				cmpField.setInstanceValue(ctx, args[0]);
				return null;
			}
			throw new IllegalArgumentException("Unknown cmp field method: " + methodName);
		}
		throw new IllegalArgumentException("Unknown method type: " + methodName);
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
	
	protected void setupCMPFieldMap(Map abstractAccessors) {
		CMPFieldBridge[] cmpFields = entityBridge.getCMPFields();
		cmpFieldMap = new HashMap(cmpFields.length * 2);
	
		for(int i=0; i<cmpFields.length; i++) {
			setupCMPFieldGetter(abstractAccessors, cmpFields[i]);
			setupCMPFieldSetter(abstractAccessors, cmpFields[i]);
		}
	}

	protected void setupCMPFieldGetter(Map abstractAccessors, CMPFieldBridge cmpField) {
		String fieldName = cmpField.getFieldName();
		String getterName = "get" + Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
	
		Method getterMethod = (Method)abstractAccessors.get(getterName);
		if(getterMethod != null) {
			verifyGetter(getterMethod, cmpField);
			cmpFieldMap.put(getterMethod, cmpField);
			abstractAccessors.remove(getterName);
		} else {
			// not clear that a getter is required for each cmp field
			// throw new DeploymentException("No getter found for cmp field: " + fieldName);
		}
	}
	
	protected void setupCMPFieldSetter(Map abstractAccessors, CMPFieldBridge cmpField) {
		String fieldName = cmpField.getFieldName();
		String setterName = "set" + Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
	
		Method setterMethod = (Method)abstractAccessors.get(setterName);
		if(setterMethod != null) {
			verifySetter(setterMethod, cmpField);
			cmpFieldMap.put(setterMethod, cmpField);
			abstractAccessors.remove(setterName);
		} else {
			// not clear that a setter is required for each cmp field
			// throw new DeploymentException("No setter found for cmp field: " + fieldName);
		}
	}
	
	protected void verifyGetter(Method getterMethod, CMPFieldBridge cmpField) {
	}

	protected void verifySetter(Method setterMethod, CMPFieldBridge cmpField) {
	}
}
