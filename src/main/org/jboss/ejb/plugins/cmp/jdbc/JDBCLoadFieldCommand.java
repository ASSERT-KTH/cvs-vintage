/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.ejb.plugins.cmp.jdbc;

import java.lang.reflect.Method;

import java.util.ArrayList;
import java.util.Iterator;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.ejb.EJBException;

import org.jboss.ejb.EntityEnterpriseContext;
import org.jboss.ejb.plugins.cmp.CMPStoreManager;
import org.jboss.ejb.plugins.cmp.jdbc.bridge.JDBCCMPFieldBridge;

/**
 * JDBCLoadFieldCommand loads the data for a single field in responce to
 * a lazy load. Lazy load groups can be thought of as the other half of
 * eager loading.  Any field that is not eager loaded must be lazy loaded.
 * In the jbosscmp-jdbc.xml file the bean developer can create groups of 
 * fields to load together.  This command finds all groups of which the
 * field is a member, performs a union of the groups, and loads all the 
 * fields.
 *
 * @author <a href="mailto:dain@daingroup.com">Dain Sundstrom</a>
 * @version $Revision: 1.1 $
 */
public class JDBCLoadFieldCommand
   extends JDBCQueryCommand
{
   // Constructors --------------------------------------------------

	public JDBCLoadFieldCommand(JDBCStoreManager manager) {
		super(manager, "LoadField");
	}
	
	// LoadEntityCommand implementation ---------------------------
	
	public void execute(JDBCCMPFieldBridge field, EntityEnterpriseContext ctx) {
		// start with a set with containing just the field 
		ArrayList fields = new ArrayList(entityMetaData.getCMPFieldCount());
		fields.add(field);
		
		// union all the groups of which field is a member
		Iterator groups = entity.getLazyLoadGroups();
		while(groups.hasNext()) {
			ArrayList group = (ArrayList)groups.next();
			if(group.contains(field)) {
				fields.addAll(group);
			}
		}
		
		// pass this info on 
		ExecutionState es = new ExecutionState();
		es.fields = (JDBCCMPFieldBridge[]) fields.toArray(new JDBCCMPFieldBridge[fields.size()]);
	   es.ctx = ctx;
		
		try {
			jdbcExecute(es);
		} catch(EJBException e) {
			throw e;
		} catch(Exception e) {
			throw new EJBException("Could not load field value: " + field, e);
		}
   }

   // JDBCQueryCommand overrides ------------------------------------

   protected String getSQL(Object argOrArgs) throws Exception {
      ExecutionState es = (ExecutionState)argOrArgs;

		
		StringBuffer sql = new StringBuffer();
		sql.append("SELECT ").append(SQLUtil.getColumnNamesClause(es.fields));
		sql.append(" FROM ").append(entityMetaData.getTableName());
		sql.append(" WHERE ").append(SQLUtil.getWhereClause(entity.getJDBCPrimaryKeyFields()));
		
		return sql.toString();
	}

	protected void setParameters(PreparedStatement ps, Object arg)
		throws Exception
	{
		ExecutionState es = (ExecutionState)arg;		
	   entity.setPrimaryKeyParameters(ps, 1, es.ctx.getId());
	}

   protected Object handleResult(ResultSet rs, Object arg) throws Exception {
		ExecutionState es = (ExecutionState)arg;		

		if(!rs.next()) {
			throw new EJBException("Entity " + es.ctx.getId() + " not found");
		}
		
      // load each field
		int parameterIndex = 1;
		for(int i=0; i<es.fields.length; i++) {
			parameterIndex = es.fields[i].loadInstanceResults(rs, parameterIndex, es.ctx);
			es.fields[i].setClean(es.ctx);
		}
		
		return null;
	}
	
	private class ExecutionState {
		public JDBCCMPFieldBridge[] fields;
		public EntityEnterpriseContext ctx;
	}
}
