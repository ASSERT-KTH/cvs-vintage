/*
 * Created on 20.11.2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.columba.mail.folder.search;

import org.columba.mail.filter.FilterRule;
import org.columba.mail.message.ColumbaMessage;

import java.util.List;


/**
 * @author Frederik Dietz
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class DummyQueryEngine implements QueryEngine {
    /* (non-Javadoc)
     * @see org.columba.mail.folder.search.QueryEngine#getCaps()
     */
    public String[] getCaps() {
        return new String[] {  };
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
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.columba.mail.folder.search.QueryEngine#queryEngine(org.columba.mail.filter.FilterRule, java.lang.Object[])
     */
    public List queryEngine(FilterRule filter, Object[] uids)
        throws Exception {
        // TODO Auto-generated method stub
        return null;
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
