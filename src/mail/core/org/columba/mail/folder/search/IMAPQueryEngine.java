/*
 * Created on 20.11.2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.columba.mail.folder.search;

import java.util.List;

import org.columba.mail.filter.FilterRule;
import org.columba.mail.folder.imap.IMAPFolder;
import org.columba.mail.message.ColumbaMessage;

/**
 * @author Frederik Dietz
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class IMAPQueryEngine implements QueryEngine {

	private final static String[] caps =
		{
			"Body",
			"Subject",
			"From",
			"To",
			"Cc",
			"Bcc",
			"Custom Headerfield",
			"Date",
			"Flags",
			"Priority",
			"Size" };

	IMAPFolder folder;

	/**
	 * 
	 */
	public IMAPQueryEngine(IMAPFolder folder) {

		this.folder = folder;
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see org.columba.mail.folder.search.QueryEngine#getCaps()
	 */
	public String[] getCaps() {
		return caps;
	}

	/* (non-Javadoc)
	 * @see org.columba.mail.folder.search.QueryEngine#sync()
	 */
	public void sync() throws Exception {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.columba.mail.folder.search.QueryEngine#queryEngine(org.columba.mail.filter.FilterRule)
	 */
	public List queryEngine(FilterRule filter) throws Exception {

		return folder.getStore().search(filter, folder.getImapPath());
	}

	/* (non-Javadoc)
	 * @see org.columba.mail.folder.search.QueryEngine#queryEngine(org.columba.mail.filter.FilterRule, java.lang.Object[])
	 */
	public List queryEngine(FilterRule filter, Object[] uids)
		throws Exception {
		return folder.getStore().search(uids, filter, folder.getImapPath());
	}

	/* (non-Javadoc)
	 * @see org.columba.mail.folder.search.QueryEngine#messageAdded(org.columba.mail.message.ColumbaMessage)
	 */
	public void messageAdded(ColumbaMessage message) throws Exception {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.columba.mail.folder.search.QueryEngine#messageRemoved(java.lang.Object)
	 */
	public void messageRemoved(Object uid) throws Exception {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.columba.mail.folder.search.QueryEngine#reset()
	 */
	public void reset() throws Exception {
		// TODO Auto-generated method stub

	}

}
