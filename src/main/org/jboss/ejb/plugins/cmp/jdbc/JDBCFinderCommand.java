/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.ejb.plugins.cmp.jdbc;

import java.lang.reflect.Method;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import java.rmi.RemoteException;
import java.rmi.ServerException;

import java.sql.ResultSet;

import javax.ejb.FinderException;

import org.jboss.ejb.EntityEnterpriseContext;
import org.jboss.ejb.plugins.cmp.FindEntitiesCommand;
import org.jboss.ejb.plugins.cmp.jdbc.metadata.JDBCQueryMetaData;
import org.jboss.util.FinderResults;

/**
 * Abstract superclass of finder commands that return collections.
 * Provides the handleResult() implementation that these all need.
 *
 * @author <a href="mailto:dain@daingroup.com">Dain Sundstrom</a>
 * @author <a href="mailto:rickard.oberg@telkel.com">Rickard Öberg</a>
 * @author <a href="mailto:marc.fleury@telkel.com">Marc Fleury</a>
 * @author <a href="mailto:shevlandj@kpi.com.au">Joe Shevland</a>
 * @author <a href="mailto:justin@j-m-f.demon.co.uk">Justin Forder</a>
 * @version $Revision: 1.3 $
 */
public abstract class JDBCFinderCommand
   extends JDBCQueryCommand
   implements FindEntitiesCommand
{
	protected JDBCQueryMetaData queryMetaData;
	
	// Constructors --------------------------------------------------
	
	public JDBCFinderCommand(JDBCStoreManager manager, JDBCQueryMetaData q) {
		super(manager, q.getMethod().getName());
		
		queryMetaData = q;
	}
	
	public JDBCQueryMetaData getQueryMetaData() {
		return queryMetaData;
	}
	
	// FindEntitiesCommand implementation -------------------------
	
	public FinderResults execute(Method finderMethod,
			Object[] args,
			EntityEnterpriseContext ctx)
		throws RemoteException, FinderException
	{
		FinderResults result = null;
		
		try {
			//
			// Execute the find... will return a collection of pks
			Collection keys = (Collection)jdbcExecute(args);

			// creat the finder results
//			if(finderMetaData.hasReadAhead()) {
//				result = new FinderResults(keys, getWhereClause(args), this, args);
//			} else {
				result = new FinderResults(keys, null, null, null);
//			}
		} catch (Exception e) {
			log.debug(e);
			throw new FinderException("Find failed");
		}
		return result;
	}

   // JDBCQueryCommand overrides ------------------------------------

	protected Object handleResult(ResultSet rs, Object argOrArgs) throws Exception {
		
		Collection result = new ArrayList();	
      try {
			Object[] pkRef = new Object[1];
			while(rs.next()) {
				pkRef[0] = null;
				entity.loadPrimaryKeyResults(rs, 1, pkRef);
				result.add(pkRef[0]);
			}
		} catch(Exception e) {
			throw new ServerException("Finder failed: ", e);
		}

		return result;
	}
	
	/** @todo: remove this next bit and add 'getWhereClause' to FinderCommands */
	protected String getWhereClause(Object[] executeArgs) throws Exception {		
		//look for 'where' and ditch everything before it		
		String sql = getSQL(executeArgs);
		int pos = sql.toUpperCase().indexOf("WHERE");
		if(pos >= 0) {
			return sql.substring(pos);
		}
		return "";
	}
}
