/*
 * Created on 20.11.2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.columba.mail.folder.search;

import java.util.List;

import org.columba.mail.filter.FilterRule;
import org.columba.mail.message.ColumbaMessage;

/**
 * @author Frederik Dietz
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public interface QueryEngine {
	
	public abstract String[] getCaps();

	public void sync() throws Exception;
	
	public List queryEngine(FilterRule filter) throws Exception;
	public List queryEngine(FilterRule filter, Object[] uids) throws Exception;

	public void messageAdded(ColumbaMessage message) throws Exception;

	public void messageRemoved(Object uid) throws Exception;

	public void reset() throws Exception;
}
