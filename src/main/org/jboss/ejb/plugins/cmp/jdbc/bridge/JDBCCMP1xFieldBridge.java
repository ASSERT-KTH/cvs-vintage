/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
 
package org.jboss.ejb.plugins.cmp.jdbc.bridge;

import java.lang.reflect.Field;

import java.util.Map;

import javax.ejb.EJBException;

import org.jboss.ejb.DeploymentException;
import org.jboss.ejb.EntityEnterpriseContext;

import org.jboss.ejb.plugins.cmp.CMPStoreManager;
import org.jboss.ejb.plugins.cmp.jdbc.JDBCStoreManager;
import org.jboss.ejb.plugins.cmp.jdbc.metadata.JDBCCMPFieldMetaData;

import org.jboss.logging.Log;

/**
 * JDBCCMP1xFieldBridge is a concrete implementation of JDBCCMPFieldBridge for 
 * CMP version 1.x. Getting and setting of instance fields set the 
 * corresponding field in bean instance.  Dirty checking is performed by 
 * storing the current value in the entity persistence context when ever
 * setClean is called, and comparing current value to the original value.
 *
 * Life-cycle:
 *		Tied to the EntityBridge.
 *
 * Multiplicity:	
 *		One for each entity bean cmp field. 		
 *
 * @author <a href="mailto:dain@daingroup.com">Dain Sundstrom</a>
 * @version $Revision: 1.4 $
 */                            
public class JDBCCMP1xFieldBridge extends JDBCAbstractCMPFieldBridge {
	protected Field field;
 	
	public JDBCCMP1xFieldBridge(JDBCStoreManager manager, JDBCCMPFieldMetaData metadata) throws DeploymentException {
		super(manager, metadata);

		try {
			field = manager.getMetaData().getEntityClass().getField(getFieldName());
		} catch(NoSuchFieldException e) {
			// Non recoverable internal exception
			throw new DeploymentException("No field named '" + getFieldName() + "' found in entity class.");
		}
	}

	public Object getInstanceValue(EntityEnterpriseContext ctx) {
		try {
			return field.get(ctx.getInstance());
		} catch(Exception e) {
			// Non recoverable internal exception
			throw new EJBException("Internal error getting instance field " + getFieldName() + ": " + e);
		}
	}
	
   public void setInstanceValue(EntityEnterpriseContext ctx, Object value) {
		try {
			field.set(ctx.getInstance(), value);
		} catch(Exception e) {
			// Non recoverable internal exception
			throw new EJBException("Internal error setting instance field " + getFieldName() + ": " + e);
		}
	}
	
  /**
	* Has the value of this field changes since the last time clean was called.
	*/
	public boolean isDirty(EntityEnterpriseContext ctx) {
		// read only and primary key fields are never dirty
		if(isReadOnly() || isPrimaryKeyMember()) {
			return false; 
		}

		// has the value changes since setClean
		return changed(getInstanceValue(ctx), getFieldState(ctx).originalValue);
	}
	
	/**
	* Mark this field as clean.
	* Saves the current state in context, so it can be compared when isDirty is called.
	*/
	public void setClean(EntityEnterpriseContext ctx) {
		FieldState fieldState = getFieldState(ctx);
		fieldState.originalValue = getInstanceValue(ctx);

		// update last read time
		if(isReadOnly()) {
			fieldState.lastRead = System.currentTimeMillis();
		}
	}

	public boolean isReadTimedOut(EntityEnterpriseContext ctx) {
		if(isReadOnly()) {
			long readInterval = System.currentTimeMillis() - getFieldState(ctx).lastRead; 
			return readInterval > metadata.getReadTimeOut();
		}
		
		// if we are read/write then we are always timed out
		return true;
	}
	
	public void resetPersistenceContext(EntityEnterpriseContext ctx) {
		if(isReadTimedOut(ctx)) {
			Map fieldStates = ((CMPStoreManager.PersistenceContext)ctx.getPersistenceContext()).fieldState;
			fieldStates.put(this, new FieldState());
		}
	}

	private FieldState getFieldState(EntityEnterpriseContext ctx) {
		Map fieldStates = ((CMPStoreManager.PersistenceContext)ctx.getPersistenceContext()).fieldState;
      FieldState fieldState = (FieldState)fieldStates.get(this);
		if(fieldState == null) {
			fieldState = new FieldState();
			fieldStates.put(this, fieldState);
		}
		return fieldState;
	}

	private static class FieldState {
		Object originalValue;
		long lastRead = -1;
	}		
}
