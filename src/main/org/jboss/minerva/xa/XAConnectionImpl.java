/*
 * jBoss, the OpenSource EJB server
 *
 * Distributable under GPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.minerva.xa;

import java.sql.*;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;
import javax.sql.*;
import javax.transaction.xa.XAResource;
import org.jboss.minerva.jdbc.PreparedStatementInPool;
import org.jboss.minerva.jdbc.PSCacheKey;

/**
 * A transaction wrapper around a java.sql.Connection.  This provides access to
 * an XAResource (there is a one-to-one mapping between XAResource and
 * XAConnection) and a java.sql.Connection (in this implementation, there is
 * also a one-to-one mapping between XAConnection and java.sql.Connection).
 * In order to pool java.sql.Connections in a transactional environment, this
 * is the class that should be pooled - though you could pool the connections,
 * there is no need to create and destroy these wrappers so frequently.
 *
 * <P>Note that there con only be one transaction at a time accessing one of
 * these wrappers, and requests to a pool for multiple connections on behalf of
 * one transaction should use the same wrapper.  This is because there is no
 * distinction between connections and transactions in a java.sql.Connection,
 * and work done by one connection on behalf of a transaction would not be
 * visible to another connection working on behalf of the same transaction - you
 * would have effectively created two transactions.</P>
 *
 * <P>This also implies that an XAConnection should not be released to a
 * connection pool until the work has been committed or rolled back.  However,
 * it must sent the close notification as usual in order to be delisted from
 * the transaction.  So the ConnectionEventListener must not release the
 * XAConnection to a pool when it receives the close event.  Instead, it should
 * also register a TransactionListener that will be notified when the
 * Transaction is finished, and release the XAConnection at that time.</P>
 * @see org.jboss.minerva.xa.TransactionListener
 * @version $Revision: 1.4 $
 * @author Aaron Mulder (ammulder@alumni.princeton.edu)
 */
public class XAConnectionImpl implements XAConnection {
    private final static String CLOSED = "Connection has been closed!";
    private Connection con;
    private XAResourceImpl resource;
    private Vector listeners;
    private TransactionListener transListener;

    /**
     * Creates a new transactional wrapper.
     * @param con The underlying non-transactional Connection.
     * @param resource The transaction resource used to enlist this
     *    connection in a transaction.
     */
    public XAConnectionImpl(Connection con, XAResourceImpl resource) {
        this.con = con;
        this.resource = resource;
        listeners = new Vector();
    }

    /**
     * Sets the transaction listener.
     */
    public void setTransactionListener(TransactionListener tl) {
        transListener = tl;
    }

    /**
     * Clears the transaction listener.
     */
    public void clearTransactionListener() {
        transListener = null;
    }

    /**
     * Shuts down this wrapper (and the underlying Connection) permanently.
     */
    public void close() {
        Map map = (Map)PreparedStatementInPool.preparedStatementCache.clone();
        Iterator it = map.keySet().iterator();
        while(it.hasNext()) {
            PSCacheKey key = (PSCacheKey)it.next();
            if(key.con.equals(con))
                PreparedStatementInPool.preparedStatementCache.remove(key);
        }

        try {
            con.close();
        } catch(SQLException e) {}
        con = null;
        resource = null;
        listeners.clear();
        listeners = null;
    }

    /**
     * Indicates that the connection given to the client has been closed.
     * If there is currently a transaction, this object should not be closed or
     * returned to a pool.  If not, it can be closed or returned immediately.
     */
    public void clientConnectionClosed() {
        boolean trans = resource.isTransaction(); // could be committed directly on notification?  Seems unlikely, but let's not rule it out.
        Vector local = (Vector)listeners.clone();
        for(int i=local.size()-1; i>=0; i--)
            ((ConnectionEventListener)local.elementAt(i)).connectionClosed(new ConnectionEvent(this));
        if(!trans)
            transactionFinished();
    }

    /**
     * Indicates that the outstanding transaction has finished and this object
     * can be closed or returned to a pool.  This dispatches a close event to
     * all listeners.
     * @see #addConnectionEventListener
     */
    public void transactionFinished() {
        if(transListener != null)
            transListener.transactionFinished(this);
    }

    /**
     * Indicates that the outstanding transaction has finished with a fatal
     * error, and this object should be closed or permanently removed from a
     * pool.  This dispatches a close event to all listeners.
     * @see #addConnectionEventListener
     */
    public void transactionFailed() {
        if(transListener != null)
            transListener.transactionFailed(this);
    }

    /**
     * Indicates that the connection given to the client has had an error.
     * If there is currently a transaction, this object should not be closed or
     * returned to a pool.  If not, it can be closed or returned immediately.
     */
    public void setConnectionError(SQLException e) {
        Vector local = (Vector)listeners.clone();
        for(int i=local.size()-1; i>=0; i--)
            ((ConnectionEventListener)local.elementAt(i)).connectionErrorOccurred(new ConnectionEvent(this, e));
    }

    /**
     * Rolls back the underlying connection.  This is used when there is no
     * current transaction and the connection is returned to the pool - since
     * no transaction will be committed or rolled back but this connection
     * will be reused, we must roll it back.
     */
    public void rollback() throws SQLException {
        con.rollback();
    }

    // ---- Implementation of javax.sql.XAConnection ----

    public XAResource getXAResource() {
        return resource;
    }

    public void addConnectionEventListener(ConnectionEventListener listener) {
        listeners.addElement(listener);
    }

    public void removeConnectionEventListener(ConnectionEventListener listener) {
        listeners.remove(listener);
    }

    public Connection getConnection() {
        return new XAClientConnection(this, con);
    }
}
