/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */ 
package org.jboss.ejb.plugins.cmp.jdbc.bridge;

import java.lang.reflect.Field;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.ejb.EJBException;

import org.jboss.ejb.DeploymentException;
import org.jboss.ejb.EntityEnterpriseContext;

import org.jboss.ejb.plugins.cmp.jdbc.metadata.JDBCCMPFieldMetaData;

import org.jboss.ejb.plugins.cmp.bridge.CMPFieldBridge;
import org.jboss.ejb.plugins.cmp.jdbc.JDBCStoreManager;
import org.jboss.ejb.plugins.cmp.jdbc.JDBCType;
import org.jboss.ejb.plugins.cmp.jdbc.JDBCUtil;

import org.jboss.logging.Log;

/**
 * Represents the foreign key field in a relationship. This class 
 * wraps the primary key of the related entity.
 *
 * @author <a href="mailto:dain@daingroup.com">Dain Sundstrom</a>
 * @version $Revision: 1.1 $
 */                            
public class JDBCForeignKeyField extends JDBCRelationKeyField {
	protected JDBCStoreManager manager;
	public JDBCForeignKeyField(JDBCCMP2xFieldBridge cmpField, String baseColumnName, JDBCStoreManager manager, Log log) throws DeploymentException {
		super(cmpField, baseColumnName, log);
		this.manager = manager;
	}

	public Object getInstanceValue(EntityEnterpriseContext ctx) {
		if(!cmpField.getFieldState(ctx).isLoaded()) {
			manager.loadField(this, ctx);
			if(!cmpField.getFieldState(ctx).isLoaded()) {
				throw new EJBException("Could not load foreign key field value: " + getFieldName());
			}
		}
		return cmpField.getFieldState(ctx).getValue();
	}
	
   public void setInstanceValue(EntityEnterpriseContext ctx, Object value) {
		cmpField.setInstanceValue(ctx, value);
	}
	
	public boolean isDirty(EntityEnterpriseContext ctx) {
		return cmpField.getFieldState(ctx).isDirty();
	}

	public void setClean(EntityEnterpriseContext ctx) {
		cmpField.setClean(ctx);
	}

	public void resetPersistenceContext(EntityEnterpriseContext ctx) {
		cmpField.resetPersistenceContext(ctx);
	}
	
	public void initInstance(EntityEnterpriseContext ctx) {
		cmpField.initInstance(ctx);
	}		

	public int setInstanceParameters(PreparedStatement ps, int parameterIndex, EntityEnterpriseContext ctx) {
		return cmpField.setInstanceParameters(ps, parameterIndex, ctx);
	}	

	public int loadInstanceResults(ResultSet rs, int parameterIndex, EntityEnterpriseContext ctx) {
		return cmpField.loadInstanceResults(rs, parameterIndex, ctx);
	}		
}
                                         